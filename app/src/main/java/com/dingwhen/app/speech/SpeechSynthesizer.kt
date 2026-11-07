package com.dingwhen.app.speech

import com.dingwhen.app.data.Reminder
import com.dingwhen.app.data.ReminderDao
import com.dingwhen.app.i18n.AppSettings
import com.dingwhen.app.model.AudioState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

data class PreviewResult(
    val file: File?,
    val reason: String?,
    val detail: String?,
    /** 没有密钥时的兜底：不合成，直接交给系统语音朗读 */
    val systemVoice: Boolean = false
)

/**
 * 合成编排：决定什么该合成、合成结果怎么落库。
 * 全程不抛异常——失败也是一种状态，必须写回数据库让界面能看见。
 */
class SpeechSynthesizer(
    private val dao: ReminderDao,
    private val store: AudioStore
) {

    fun speechTextOf(reminder: Reminder): String =
        reminder.speechText?.takeIf { it.isNotBlank() } ?: reminder.content

    /**
     * 这条提醒实际该用的配置。
     *
     * 音色和语气都是提醒的属性：新建时从设置里预填，之后各条互不影响。
     * 当前不回落全局——落库时已经把值定死了，回落到全局会让"改了设置没生效"
     * 和"改了设置偷偷生效"两种行为交替出现，反而更难解释。
     *
     * 密钥、播报开关仍是全局的。
     *
     * 缓存键包含 voiceType 与 emotion，所以这里返回的配置必须被所有合成路径使用；
     * 漏掉一处就会命中别的音色或语气的音频，表现为"换了设置但播出来没变"。
     */
    private fun configFor(reminder: Reminder, base: SpeechConfig): SpeechConfig {
        val voice = reminder.voiceType
        val withVoice =
            if (voice.isBlank() || voice == base.voiceType) base else base.copy(voiceType = voice)
        return withVoice.copy(
            emotion = reminder.emotion,
            emotionEnabled = reminder.emotionEnabled
        )
    }

    suspend fun synthesize(reminderId: Long): Unit = withContext(Dispatchers.IO) {
        val reminder = dao.getById(reminderId) ?: return@withContext
        val config = configFor(reminder, AppSettings.speechConfig())
        val text = speechTextOf(reminder)

        if (!config.enabled || !config.configured || text.isBlank()) {
            persist(reminder.id, AudioState.NONE, null, null)
            return@withContext
        }

        // 命中文档缓存就不必再花钱合成一次
        val name = "${store.keyOf(text, config)}.mp3"
        val cached = store.file(name)
        if (cached != null) {
            if (stillCurrent(reminder, text)) {
                persist(reminder.id, AudioState.READY, cached.name, null)
            } else {
                persist(reminder.id, AudioState.PENDING, null, null)
            }
            return@withContext
        }

        // 超限直接给明确错误，不发一次注定失败的请求
        if (!SpeechLimits.isWithinLimit(text)) {
            persist(reminder.id, AudioState.FAILED, null, DoubaoClient.ERR_TOO_LONG)
            return@withContext
        }

        // 纯语种不匹配直接给明确错误，不发一次注定失败的请求
        if (hardLanguageMismatch(text, config.voiceType)) {
            persist(reminder.id, AudioState.FAILED, null, DoubaoClient.ERR_LANGUAGE)
            return@withContext
        }

        when (val result = DoubaoClient.synthesize(config, text)) {
            is SynthResult.Ok -> {
                // 合成期间用户可能改了文字/音色/语气；旧快照写回去会覆盖新编辑
                if (!stillCurrent(reminder, text)) {
                    persist(reminder.id, AudioState.PENDING, null, null)
                    return@withContext
                }
                val saved = store.write(name, result.audio)
                if (saved != null) {
                    persist(reminder.id, AudioState.READY, saved.name, null)
                } else {
                    persist(reminder.id, AudioState.PENDING, null, DoubaoClient.ERR_NETWORK)
                }
            }

            is SynthResult.Err -> {
                if (!stillCurrent(reminder, text)) {
                    persist(reminder.id, AudioState.PENDING, null, null)
                    return@withContext
                }
                // 网络类失败留 PENDING 等下次冷启动重试；配置类失败标 FAILED，重试没意义
                val state = if (result.reason == DoubaoClient.ERR_NETWORK) {
                    AudioState.PENDING
                } else {
                    AudioState.FAILED
                }
                persist(reminder.id, state, null, result.reason)
            }
        }
    }

    private fun hardLanguageMismatch(text: String, voiceId: String): Boolean {
        val voice = VoiceCatalog.find(voiceId) ?: return false
        return when (TextLanguageDetector.detect(text)) {
            TextLanguage.ZH -> !voice.supportsChinese
            TextLanguage.EN -> !voice.supportsEnglish
            // 中英混合只提示、不拦：中文里夹一个 OK 不应该被一刀切
            else -> false
        }
    }

    private suspend fun stillCurrent(reminder: Reminder, text: String): Boolean {
        val latest = dao.getById(reminder.id) ?: return false
        return latest.voiceType == reminder.voiceType &&
            latest.emotion == reminder.emotion &&
            latest.emotionEnabled == reminder.emotionEnabled &&
            speechTextOf(latest) == text
    }

    /** 冷启动时补做上次没合成的（多为离线时创建的提醒） */
    suspend fun reconcile(): Unit = withContext(Dispatchers.IO) {
        val config = AppSettings.speechConfig()
        if (!config.enabled || !config.configured) return@withContext
        dao.getAll()
            .filter { it.audioState == AudioState.PENDING }
            .forEach { synthesize(it.id) }
        sweep()
    }

    /** 刚配好密钥时，把所有已有提醒排进队列 */
    suspend fun queueAll(): Int = withContext(Dispatchers.IO) {
        val base = AppSettings.speechConfig()
        if (!base.configured) return@withContext 0

        var queued = 0
        dao.getAll().forEach { reminder ->
            val text = speechTextOf(reminder)
            if (text.isBlank()) return@forEach

            // 每条按自己的音色算缓存键，否则改名后一律回落到默认音色的那份
            val config = configFor(reminder, base)
            val cached = store.file("${store.keyOf(text, config)}.mp3")
            val tooLong = !SpeechLimits.isWithinLimit(text)
            val wrongLanguage = hardLanguageMismatch(text, config.voiceType)

            val state = when {
                cached != null -> AudioState.READY
                tooLong || wrongLanguage -> AudioState.FAILED
                else -> AudioState.PENDING
            }
            val error = when {
                cached != null -> null
                tooLong -> DoubaoClient.ERR_TOO_LONG
                wrongLanguage -> DoubaoClient.ERR_LANGUAGE
                else -> null
            }
            val file = cached?.name
            if (reminder.audioState != state || reminder.audioFile != file ||
                reminder.audioError != error
            ) {
                dao.updateAudio(reminder.id, state, file, error)
                queued++
            }
        }
        queued
    }

    /**
     * 试听。音色和语气由调用方指定：在设置页试的是默认值，
     * 在提醒编辑页试的是这条提醒自己的——否则试听和到点播出来不是一个声音。
     * emotion 传 null 表示没指定，沿用设置里的语气。
     */
    suspend fun preview(
        text: String,
        voiceType: String = "",
        emotion: String? = null,
        emotionEnabled: Boolean = false
    ): PreviewResult = withContext(Dispatchers.IO) {
        if (text.isBlank()) {
            return@withContext PreviewResult(null, DoubaoClient.ERR_EMPTY, null)
        }
        if (!SpeechLimits.isWithinLimit(text)) {
            return@withContext PreviewResult(
                null,
                DoubaoClient.ERR_TOO_LONG,
                "${SpeechLimits.charCount(text)}/${SpeechLimits.MAX_TEXT_CHARS} chars"
            )
        }
        val base = AppSettings.speechConfig()
        val withVoice = if (voiceType.isBlank() || voiceType == base.voiceType) {
            base
        } else {
            base.copy(voiceType = voiceType)
        }
        val config = if (emotion == null) {
            withVoice
        } else {
            withVoice.copy(emotion = emotion, emotionEnabled = emotionEnabled)
        }
        // 没密钥不是错误：退回系统自带语音，用户照样能听到这句话
        if (!config.configured) {
            return@withContext PreviewResult(null, null, null, systemVoice = true)
        }
        val name = "${store.keyOf(text, config)}.mp3"
        store.file(name)?.let { return@withContext PreviewResult(it, null, null) }

        if (hardLanguageMismatch(text, config.voiceType)) {
            return@withContext PreviewResult(null, DoubaoClient.ERR_LANGUAGE, null)
        }

        when (val result = DoubaoClient.synthesize(config, text)) {
            is SynthResult.Ok -> PreviewResult(store.write(name, result.audio), null, null)
            is SynthResult.Err -> PreviewResult(null, result.reason, result.detail)
        }
    }

    /** 清掉没人引用的音频。改文案会不断产生孤儿文件。 */
    suspend fun sweep(): Unit = withContext(Dispatchers.IO) {
        store.sweep(dao.getAll().mapNotNull { it.audioFile }.toSet())
    }

    /**
     * 只更新音频字段。合成可能持续数秒，期间用户改时间/内容/音色都不该被旧快照覆盖。
     * 表结构不变，不需要升 Room 版本。
     */
    private suspend fun persist(
        reminderId: Long,
        state: AudioState,
        file: String?,
        error: String?
    ) {
        dao.updateAudio(reminderId, state, file, error)
    }
}
