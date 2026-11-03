package com.dingwhen.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dingwhen.app.Graph
import com.dingwhen.app.data.Reminder
import com.dingwhen.app.i18n.AppSettings
import com.dingwhen.app.model.AudioState
import com.dingwhen.app.model.Draft
import com.dingwhen.app.model.NextTrigger
import com.dingwhen.app.model.RepeatType
import com.dingwhen.app.speech.DoubaoClient
import com.dingwhen.app.speech.SpeechPlayer
import com.dingwhen.app.speech.VoiceCatalog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZonedDateTime

enum class Route { LIST, EDITOR, PERMISSIONS, SPEECH }

class AppViewModel : ViewModel() {

    private val repository = Graph.repository
    private val synthesizer = Graph.synthesizer

    // 初始值是 null 而不是空表：冷启动还没读到数据库时不要先闪一下"还没有提醒"
    val reminders: StateFlow<List<Reminder>?> = repository.observe()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _route = MutableStateFlow(Route.LIST)
    val route: StateFlow<Route> = _route.asStateFlow()

    private val _draft = MutableStateFlow(Draft())
    val draft: StateFlow<Draft> = _draft.asStateFlow()

    /** 试听状态：true 表示正在合成，note 为 null 表示没有反馈，PREVIEW_OK 表示成功 */
    private val _previewing = MutableStateFlow(false)
    val previewing: StateFlow<Boolean> = _previewing.asStateFlow()

    private val _previewNote = MutableStateFlow<String?>(null)
    val previewNote: StateFlow<String?> = _previewNote.asStateFlow()

    private val _regenCount = MutableStateFlow<Int?>(null)
    val regenCount: StateFlow<Int?> = _regenCount.asStateFlow()

    fun openManualCreate() {
        // 新提醒默认继承设置里的音色与语气；想换就在编辑页改，只影响这一条
        val config = AppSettings.speechConfig()
        _draft.value = Draft(
            voiceType = config.voiceType,
            emotion = config.emotion,
            emotionEnabled = config.emotionEnabled
        )
        _previewNote.value = null
        _route.value = Route.EDITOR
    }

    fun openPermissions() {
        _route.value = Route.PERMISSIONS
    }

    fun openSpeech() {
        _route.value = Route.SPEECH
    }

    fun backToList() {
        _route.value = Route.LIST
    }

    fun edit(reminder: Reminder) {
        _draft.value = Draft(
            id = reminder.id,
            content = reminder.content,
            hour = reminder.hour,
            minute = reminder.minute,
            repeat = reminder.repeatType,
            date = reminder.date ?: LocalDate.now(),
            days = reminder.days,
            dayOfMonth = reminder.dayOfMonth ?: LocalDate.now().dayOfMonth,
            speechText = reminder.speechText.orEmpty(),
            voiceType = VoiceCatalog.migrate(
                reminder.voiceType.ifBlank { AppSettings.speechConfig().voiceType }
            ),
            emotion = reminder.emotion,
            emotionEnabled = reminder.emotionEnabled,
            playTimes = reminder.playTimes.coerceIn(1, 3),
            repeatOn = reminder.repeatOn,
            actionOpenPackage = reminder.actionOpenPackage,
            actionDismiss = reminder.actionDismiss,
            dismissLabel = reminder.actionDismissLabel.orEmpty()
        )
        _previewNote.value = null
        _route.value = Route.EDITOR
    }

    fun updateDraft(block: (Draft) -> Draft) {
        _draft.value = block(_draft.value)
    }

    fun saveDraft() {
        val draft = _draft.value
        if (!draft.isValid()) return
        val hour = draft.hour ?: return
        val minute = draft.minute ?: return

        viewModelScope.launch {
            val next = NextTrigger.next(
                now = ZonedDateTime.now(),
                hour = hour,
                minute = minute,
                repeat = draft.repeat,
                days = draft.days,
                dayOfMonth = draft.dayOfMonth,
                date = if (draft.repeat == RepeatType.ONCE) draft.date else null
            )
            val content = draft.content.trim()
            val speech = draft.speechText.trim().takeIf { it.isNotBlank() }
            val config = AppSettings.speechConfig()
            val text = speech ?: content
            val needsVoice = config.enabled && config.configured && text.isNotBlank()

            val entity = Reminder(
                id = draft.id ?: 0L,
                content = content,
                hour = hour,
                minute = minute,
                repeatType = draft.repeat,
                daysOfWeek = draft.days.joinToString(",") { it.value.toString() },
                dayOfMonth = if (draft.repeat == RepeatType.MONTHLY) draft.dayOfMonth else null,
                dateEpochDay = if (draft.repeat == RepeatType.ONCE) draft.date.toEpochDay() else null,
                enabled = true,
                nextTriggerAt = next.toInstant().toEpochMilli(),
                speechText = speech,
                // 落库时把音色定下来：之后改设置里的默认音色，不会偷偷换掉这条的声音
                voiceType = draft.voiceType.ifBlank { config.voiceType },
                emotion = draft.emotion.trim(),
                emotionEnabled = draft.emotionEnabled,
                playTimes = draft.playTimes.coerceIn(1, 3),
                repeatOn = draft.repeatOn,
                // 先落 PENDING 立刻返回列表，合成在后台做——用户不该为一次网络请求等在保存按钮上
                audioState = if (needsVoice) AudioState.PENDING else AudioState.NONE,
                actionOpenPackage = draft.actionOpenPackage,
                actionDismiss = draft.actionDismiss,
                actionDismissLabel = draft.dismissLabel.trim().takeIf { it.isNotBlank() }
            )
            val id = repository.save(entity)
            _route.value = Route.LIST
            // 合成失败不能把整个保存流程带崩：状态已经落库，界面能看到"合成失败"
            if (needsVoice) runCatching { synthesizer.synthesize(id) }
        }
    }

    /**
     * 试听：合成一句并立刻播放，让用户当场判断音色与语气。
     * voiceType 传空则用设置里的默认音色；emotion 传 null 则用设置里的语气。
     * 编辑页会把这条提醒自己的三个值都传进来。
     */
    fun previewSpeech(
        text: String,
        voiceType: String = "",
        emotion: String? = null,
        emotionEnabled: Boolean = false
    ) {
        val target = text.ifBlank { _draft.value.content }.trim()
        if (target.isBlank() || _previewing.value) return

        viewModelScope.launch {
            _previewing.value = true
            try {
                _previewNote.value = null
                val result = synthesizer.preview(target, voiceType, emotion, emotionEnabled)
                val file = result.file
                when {
                    file != null -> {
                        _previewNote.value = PREVIEW_OK
                        // 试听发生在应用前台，不必抢音频焦点
                        SpeechPlayer.playFile(file, times = 1)
                    }
                    // 没填密钥：系统语音当场念，用户能判断"兜底到底能不能听"
                    result.systemVoice -> {
                        val spoken = SpeechPlayer.speakText(Graph.context, target)
                        _previewNote.value = if (spoken) PREVIEW_TTS else PREVIEW_TTS_FAIL
                    }
                    else -> _previewNote.value = result.reason ?: DoubaoClient.ERR_OTHER
                }
            } catch (e: Exception) {
                _previewNote.value = "other"
            } finally {
                _previewing.value = false
            }
        }
    }

    fun clearPreviewNote() {
        _previewNote.value = null
    }

    fun regenAllSpeech() {
        viewModelScope.launch {
            val queued = runCatching { synthesizer.queueAll() }.getOrDefault(0)
            _regenCount.value = queued
            runCatching { synthesizer.reconcile() }
        }
    }

    fun clearRegenNote() {
        _regenCount.value = null
    }

    fun toggle(reminder: Reminder, enabled: Boolean) {
        viewModelScope.launch { repository.setEnabled(reminder, enabled) }
    }

    fun delete(reminder: Reminder) {
        viewModelScope.launch {
            repository.delete(reminder)
            _route.value = Route.LIST
        }
    }

    fun deleteCurrentDraft() {
        val id = _draft.value.id ?: return
        viewModelScope.launch {
            repository.get(id)?.let { repository.delete(it) }
            _route.value = Route.LIST
        }
    }

    /** 权限变化后重新对齐所有闹钟。 */
    fun syncSchedule() {
        viewModelScope.launch { repository.rescheduleAll() }
    }

    companion object {
        /** 试听成功的哨兵值，界面据此显示"合成成功"而不是错误文案 */
        const val PREVIEW_OK = "ok"

        /** 没密钥、用系统语音念的哨兵值 */
        const val PREVIEW_TTS = "system_tts"

        /** 系统语音也没能出声（多半是手机没装语音引擎） */
        const val PREVIEW_TTS_FAIL = "tts_failed"
    }
}
