package com.dingwhen.app.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.graphics.drawable.toBitmap
import com.dingwhen.app.i18n.AppSettings
import com.dingwhen.app.i18n.Lang
import com.dingwhen.app.i18n.LocalStrings
import com.dingwhen.app.model.AudioState
import com.dingwhen.app.model.Draft
import com.dingwhen.app.model.RepeatType
import com.dingwhen.app.speech.SpeechLimits
import com.dingwhen.app.speech.TextLanguageDetector
import com.dingwhen.app.speech.VoiceCatalog
import com.dingwhen.app.speech.supports
import com.dingwhen.app.ui.theme.LocalDwColors
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    draft: Draft,
    audioState: AudioState,
    audioError: String?,
    previewing: Boolean,
    previewNote: String?,
    onUpdate: ((Draft) -> Draft) -> Unit,
    onSave: () -> Unit,
    onPreview: (String, String, String?, Boolean) -> Unit,
    onDelete: (() -> Unit)?,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val extras = LocalDwColors.current
    val s = LocalStrings.current
    val speechConfig by AppSettings.speech.collectAsState()
    val aliases by AppSettings.voiceAliases.collectAsState()
    val problems = draft.problems()
    val en = s.lang == Lang.EN
    val voice = VoiceCatalog.find(draft.voiceType)
    val voiceLabel = AppSettings.voiceLabel(draft.voiceType, en, aliases)
    val voiceSummary = listOfNotNull(
        voiceLabel,
        voice?.let { s.voiceLanguage(it.languages) },
        voice?.let { if (en) it.descriptionEn else it.descriptionZh }
    ).joinToString(" · ")
    val spoken = draft.spokenText()
    val charCount = SpeechLimits.charCount(spoken)
    val textWithinLimit = SpeechLimits.isWithinLimit(spoken)
    val textLanguage = remember(spoken) { TextLanguageDetector.detect(spoken) }
    val voiceMismatch = speechConfig.enabled && speechConfig.configured &&
        voice != null && !voice.supports(textLanguage)
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showAppPicker by remember { mutableStateOf(false) }
    var showVoicePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    val vertical = rememberScrollState()

    val actionAppLabel = remember(draft.actionOpenPackage) {
        appLabelOf(context, draft.actionOpenPackage)
    }
    val actionAppIcon = remember(draft.actionOpenPackage) {
        appIconOf(context, draft.actionOpenPackage)?.let { drawable ->
            runCatching { drawable.toBitmap(72, 72).asImageBitmap() }.getOrNull()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            Surface(color = MaterialTheme.colorScheme.background) {
                Column(
                    Modifier
                        .windowInsetsPadding(WindowInsets.navigationBars.union(WindowInsets.ime))
                        .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 14.dp)
                ) {
                    PrimaryPillButton(
                        text = if (draft.id == null) s.saveReminder else s.saveChanges,
                        onClick = onSave,
                        enabled = problems.isEmpty(),
                        icon = Icons.Rounded.Check
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(vertical)
        ) {
            Column(Modifier.statusBarsPadding()) {
                ScreenHeader(
                    title = if (draft.id == null) s.editorNew else s.editorEdit,
                    onBack = onBack,
                    trailing = {
                        if (onDelete != null) {
                            RoundIconButton(Icons.Rounded.DeleteOutline, s.delete, {
                                showDeleteConfirm = true
                            })
                        }
                    }
                )
            }

            Column(Modifier.padding(horizontal = 20.dp)) {

                // 时间：整页的视觉重心
                Spacer(Modifier.height(16.dp))
                DwCard(onClick = { showTimePicker = true }) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 26.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val unset = draft.hour == null
                        val size = if (unset) 32.sp else 56.sp
                        Text(
                            text = draft.clockText() ?: s.notSet,
                            style = TextStyle(
                                fontFamily = com.dingwhen.app.ui.theme.DwSerif,
                                fontSize = size,
                                lineHeight = (size.value * 1.1f).sp,
                                fontWeight = FontWeight.Normal,
                                letterSpacing = if (unset) 0.sp else (-1.5).sp,
                                fontFeatureSettings = "tnum"
                            ),
                            color = if (unset) {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        )
                        if (unset) {
                            Spacer(Modifier.height(10.dp))
                            Text(
                                text = s.pickTime,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // 刚进页面还没动手时不报错，等用户真的填了一部分再提示
                val started = draft.hour != null || draft.content.isNotBlank() ||
                    draft.speechText.isNotBlank()
                if (problems.isNotEmpty() && started) {
                    Spacer(Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.errorContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Rounded.ErrorOutline,
                                contentDescription = null,
                                modifier = Modifier.size(15.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                s.joinProblems(problems.map { s.problem(it) }),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }

                // 重复
                Spacer(Modifier.height(SectionGap))
                SectionLabel(s.sectionRepeat, Modifier.padding(bottom = 10.dp))
                SegmentedRow(
                    options = RepeatType.entries.map { s.repeatChip(it) },
                    selectedIndex = draft.repeat.ordinal,
                    onSelect = { index ->
                        val type = RepeatType.entries[index]
                        onUpdate { it.copy(repeat = type) }
                    }
                )

                if (draft.repeat == RepeatType.ONCE) {
                    Spacer(Modifier.height(10.dp))
                    DwCard(onClick = { showDatePicker = true }) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                s.monthDay(draft.date.monthValue, draft.date.dayOfMonth),
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                s.weekday(draft.date.dayOfWeek),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.weight(1f))
                            Text(
                                s.change,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                if (draft.repeat == RepeatType.WEEKLY) {
                    Spacer(Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        DayOfWeek.values().forEach { day ->
                            WeekdayChip(
                                label = s.weekday(day),
                                selected = draft.days.contains(day),
                                onClick = {
                                    onUpdate { current ->
                                        val next = if (current.days.contains(day)) {
                                            current.days - day
                                        } else {
                                            current.days + day
                                        }
                                        current.copy(days = next)
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                if (draft.repeat == RepeatType.MONTHLY) {
                    Spacer(Modifier.height(10.dp))
                    DwCard {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                s.monthlyWord,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.width(14.dp))
                            Stepper("−") {
                                onUpdate { it.copy(dayOfMonth = ((it.dayOfMonth ?: 1) - 1).coerceIn(1, 31)) }
                            }
                            Text(
                                text = "${draft.dayOfMonth ?: "-"}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 18.dp)
                            )
                            Stepper("+") {
                                onUpdate { it.copy(dayOfMonth = ((it.dayOfMonth ?: 0) + 1).coerceIn(1, 31)) }
                            }
                            Spacer(Modifier.weight(1f))
                            Text(
                                s.dayWord,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // 内容
                Spacer(Modifier.height(SectionGap))
                SectionLabel(s.sectionContent, Modifier.padding(bottom = 10.dp))
                TextField(
                    value = draft.content,
                    onValueChange = { text -> onUpdate { it.copy(content = text) } },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            s.contentHint,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    },
                    shape = RoundedCornerShape(4.dp),
                    textStyle = MaterialTheme.typography.bodyLarge,
                    keyboardOptions = KeyboardOptions.Default,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = extras.field,
                        unfocusedContainerColor = extras.field,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        cursorColor = MaterialTheme.colorScheme.primary
                    ),
                    minLines = 1,
                    maxLines = 4
                )

                // 播报内容：通知要短，朗读要完整，所以单独一段，留空就复用上面的文字
                Spacer(Modifier.height(SectionGap))
                SectionLabel(s.speechEditorSection, Modifier.padding(bottom = 10.dp))
                DwCard {
                    Column(Modifier.padding(14.dp)) {
                        TextField(
                            value = draft.speechText,
                            onValueChange = { text -> onUpdate { it.copy(speechText = text) } },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = {
                                Text(
                                    s.speechEditorHint,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            },
                            shape = RoundedCornerShape(4.dp),
                            textStyle = MaterialTheme.typography.bodyLarge,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = extras.field,
                                unfocusedContainerColor = extras.field,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent,
                                cursorColor = MaterialTheme.colorScheme.primary
                            ),
                            minLines = 1,
                            maxLines = 6
                        )
                        Spacer(Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Text(
                                text = s.speechTextCount(charCount, SpeechLimits.MAX_TEXT_CHARS),
                                style = MaterialTheme.typography.bodySmall,
                                color = if (textWithinLimit) {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                } else {
                                    MaterialTheme.colorScheme.error
                                }
                            )
                        }
                        if (!textWithinLimit) {
                            Text(
                                text = s.speechTextLimitHint(SpeechLimits.MAX_TEXT_CHARS),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // 没填密钥时不是"没语音"，而是交给系统语音念——别让用户以为不会出声
                            val systemVoice = speechConfig.enabled && !speechConfig.configured
                            Text(
                                text = if (audioState == AudioState.NONE && systemVoice) {
                                    s.speechStateSystem
                                } else when (audioState) {
                                    AudioState.READY -> s.speechStateReady
                                    AudioState.PENDING -> s.speechStatePending
                                    AudioState.FAILED -> audioError?.let {
                                        "${s.speechStateFailed}：${s.speechError(it)}"
                                    } ?: s.speechStateFailed
                                    AudioState.NONE -> s.speechStateNone
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = when (audioState) {
                                    AudioState.READY -> extras.success
                                    AudioState.FAILED -> MaterialTheme.colorScheme.error
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 14.dp)
                            )
                            ActionPill(
                                text = if (previewing) s.speechTesting else s.speechPreview,
                                enabled = !previewing && textWithinLimit
                            ) {
                                onPreview(
                                    draft.speechText.ifBlank { draft.content },
                                    draft.voiceType,
                                    draft.emotion,
                                    draft.emotionEnabled
                                )
                            }
                        }
                        previewNote?.let { note ->
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = when (note) {
                                    AppViewModel.PREVIEW_OK -> s.speechTestOk
                                    AppViewModel.PREVIEW_TTS -> s.speechTestOkSystem
                                    AppViewModel.PREVIEW_TTS_FAIL -> s.speechError(note)
                                    else -> "${s.speechTestFail}：${s.speechError(note)}"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = if (note == AppViewModel.PREVIEW_OK ||
                                    note == AppViewModel.PREVIEW_TTS
                                ) {
                                    extras.success
                                } else {
                                    MaterialTheme.colorScheme.error
                                }
                            )
                        }
                    }

                    // 音色属于这条提醒：同一条文案用不同音色会合成出不同的文件，
                    // 所以「打卡用男声、喝水用女声」不需要任何播放侧的判断
                    DwDivider()
                    DwRow(
                        title = s.voiceTitle,
                        subtitle = voiceSummary.takeIf { it.isNotBlank() },
                        onClick = { showVoicePicker = true }
                    ) {
                        ActionPill(s.select) { showVoicePicker = true }
                    }
                    if (voiceMismatch) {
                        Text(
                            text = s.voiceLanguageMismatch,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
                        )
                    }

                    // 语气也跟这条提醒走：上班打卡要催着点，下班打卡轻一点更好
                    DwDivider()
                    DwToggleRow(
                        title = s.speechEmotionTitle,
                        subtitle = s.speechEmotionSub,
                        checked = draft.emotionEnabled,
                        onCheckedChange = { value ->
                            onUpdate { it.copy(emotionEnabled = value) }
                        }
                    )
                    if (draft.emotionEnabled) {
                        Box(Modifier.padding(start = 14.dp, end = 14.dp, bottom = 14.dp)) {
                            TextField(
                                value = draft.emotion,
                                onValueChange = { text -> onUpdate { it.copy(emotion = text) } },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = {
                                    Text(
                                        s.speechEmotionHint,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(3.dp),
                                textStyle = MaterialTheme.typography.bodyMedium,
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = extras.field,
                                    unfocusedContainerColor = extras.field,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    disabledIndicatorColor = Color.Transparent,
                                    cursorColor = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                    }

                    // 每轮念几遍：催打卡要念两遍，提醒喝水一遍就够，所以跟着提醒走
                    DwDivider()
                    Column(
                        Modifier.padding(start = 16.dp, end = 16.dp, top = 13.dp, bottom = 14.dp)
                    ) {
                        Text(
                            s.speechTimesTitle,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            s.speechTimesSub,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        SegmentedRow(
                            options = listOf(1, 2, 3).map { s.speechTimesValue(it) },
                            selectedIndex = (draft.playTimes - 1).coerceIn(0, 2),
                            modifier = Modifier.padding(top = 10.dp)
                        ) { index -> onUpdate { it.copy(playTimes = index + 1) } }
                    }
                }

                // 通知按钮
                Spacer(Modifier.height(SectionGap))
                SectionLabel(s.sectionActions, Modifier.padding(bottom = 10.dp))
                DwCard {
                    DwRow(
                        title = s.openApp,
                        subtitle = actionAppLabel ?: s.noAppPicked,
                        onClick = { showAppPicker = true },
                        leading = {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(extras.field),
                                contentAlignment = Alignment.Center
                            ) {
                                if (actionAppIcon != null) {
                                    Image(
                                        bitmap = actionAppIcon,
                                        contentDescription = null,
                                        modifier = Modifier.size(34.dp)
                                    )
                                } else {
                                    Icon(
                                        Icons.Rounded.Apps,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    ) {
                        if (actionAppLabel != null) {
                            IconButton(onClick = { onUpdate { it.copy(actionOpenPackage = null) } }) {
                                Icon(
                                    Icons.Rounded.Close,
                                    contentDescription = s.clearAction,
                                    modifier = Modifier.size(17.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            ActionPill(s.select) { showAppPicker = true }
                        }
                    }

                    DwDivider()
                    DwToggleRow(
                        title = s.dismissTitle,
                        subtitle = s.dismissSub,
                        checked = draft.actionDismiss,
                        onCheckedChange = { value -> onUpdate { it.copy(actionDismiss = value) } }
                    )
                    // 按钮文字可自定义：内容是"打卡了吗？"时，按钮叫"知道了"很别扭
                    if (draft.actionDismiss) {
                        Box(Modifier.padding(start = 14.dp, end = 14.dp, bottom = 14.dp)) {
                            TextField(
                                value = draft.dismissLabel,
                                onValueChange = { text -> onUpdate { it.copy(dismissLabel = text) } },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = {
                                    Text(
                                        s.dismissLabelHint,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(3.dp),
                                textStyle = MaterialTheme.typography.bodyMedium,
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = extras.field,
                                    unfocusedContainerColor = extras.field,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    disabledIndicatorColor = Color.Transparent,
                                    cursorColor = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                    }

                    // 没确认就再响：关掉确认按钮后就没有停下来的入口，
                    // 持续重响只会逼人去关掉整条提醒，所以这里跟着一起失效
                    DwDivider()
                    DwToggleRow(
                        title = s.speechRepeatTitle,
                        subtitle = if (draft.actionDismiss) {
                            s.speechRepeatSub
                        } else {
                            s.speechRepeatOffSub
                        },
                        checked = draft.actionDismiss && draft.repeatOn,
                        enabled = draft.actionDismiss,
                        onCheckedChange = { value -> onUpdate { it.copy(repeatOn = value) } }
                    )
                }
                Text(
                    s.actionsNote,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )

                Spacer(Modifier.height(28.dp))
            }
        }
    }

    if (showTimePicker) {
        val state = rememberTimePickerState(
            initialHour = draft.hour ?: 8,
            initialMinute = draft.minute ?: 0,
            is24Hour = true
        )
        Dialog(onDismissRequest = { showTimePicker = false }) {
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TimePicker(state = state)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showTimePicker = false }) {
                            Text(s.cancel, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        TextButton(onClick = {
                            onUpdate { it.copy(hour = state.hour, minute = state.minute) }
                            showTimePicker = false
                        }) {
                            Text(s.done)
                        }
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        val dateState = rememberDatePickerState(
            initialSelectedDateMillis = draft.date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    dateState.selectedDateMillis?.let { millis ->
                        onUpdate {
                            it.copy(date = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate())
                        }
                    }
                    showDatePicker = false
                }) { Text(s.done) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(s.cancel, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            shape = RoundedCornerShape(6.dp)
        ) {
            DatePicker(state = dateState)
        }
    }

    if (showAppPicker) {
        AppPickerSheet(
            onDismiss = { showAppPicker = false },
            onPick = { pkg -> onUpdate { it.copy(actionOpenPackage = pkg) } }
        )
    }

    if (showVoicePicker) {
        VoicePickerSheet(
            selectedId = draft.voiceType,
            onDismiss = { showVoicePicker = false },
            onPick = { voice -> onUpdate { it.copy(voiceType = voice) } }
        )
    }

    if (showDeleteConfirm && onDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(s.deleteTitle) },
            text = {
                Text(
                    s.deleteOnlyBody,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirm = false
                    onDelete()
                }) {
                    Text(
                        s.delete,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(
                        s.cancel,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            shape = RoundedCornerShape(6.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}

/* ------------------------------------------------------------------ 零件 */

@Composable
private fun WeekdayChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val extras = LocalDwColors.current
    Box(
        modifier = modifier
            .height(42.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(if (selected) MaterialTheme.colorScheme.primary else extras.field)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
            maxLines = 1,
            color = if (selected) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    }
}

@Composable
private fun Stepper(text: String, onClick: () -> Unit) {
    val extras = LocalDwColors.current
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(extras.field)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
