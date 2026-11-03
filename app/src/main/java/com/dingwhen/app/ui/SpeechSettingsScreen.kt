package com.dingwhen.app.ui

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.dingwhen.app.i18n.AppSettings
import com.dingwhen.app.i18n.Lang
import com.dingwhen.app.i18n.LocalStrings
import com.dingwhen.app.speech.SpeechConfig
import com.dingwhen.app.speech.SpeechPlayer
import com.dingwhen.app.speech.VoiceCatalog
import com.dingwhen.app.ui.theme.LocalDwColors

/**
 * 语音播报设置。
 *
 * 密钥、音色、情感都直接写进 SharedPreferences——和主题、语言一样即时生效，
 * 不需要"保存"按钮。试听是这里最关键的功能：音色能不能用、密钥对不对，
 * 只有真合成一次才知道。没填密钥时试听走系统语音，让用户先听听兜底能不能接受。
 */
@Composable
fun SpeechSettingsScreen(
    vm: AppViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val extras = LocalDwColors.current
    val s = LocalStrings.current

    val stored by AppSettings.speech.collectAsState()
    var config by remember { mutableStateOf(stored) }
    var showVoicePicker by remember { mutableStateOf(false) }

    val previewing by vm.previewing.collectAsState()
    val previewNote by vm.previewNote.collectAsState()
    val regenCount by vm.regenCount.collectAsState()

    val tick = rememberResumeTick()
    val volume = remember(tick) { SpeechPlayer.alarmVolume(context) }

    fun update(next: SpeechConfig) {
        config = next
        AppSettings.setSpeech(next)
    }

    // 试听句留空就用示例句，省得用户每次都得先打字
    val testText = config.testText.ifBlank { s.speechTestSample }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            Surface(color = MaterialTheme.colorScheme.background) {
                Column(
                    Modifier
                        .windowInsetsPadding(WindowInsets.navigationBars.union(WindowInsets.ime))
                        .padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 14.dp)
                ) {
                    // 试听内容可改：用户想听的是自己那句，不是固定的示例。
                    // 输入框和按钮合成一条控件——两者高度差一大截时并排放很别扭。
                    PreviewBar(
                        value = config.testText,
                        placeholder = s.speechTestSample,
                        actionText = if (previewing) s.speechTesting else s.speechTest,
                        enabled = !previewing,
                        onValueChange = { update(config.copy(testText = it)) },
                        onAction = { vm.previewSpeech(testText, config.voiceType) }
                    )
                    previewNote?.let { note ->
                        Spacer(Modifier.height(8.dp))
                        val ok = note == AppViewModel.PREVIEW_OK || note == AppViewModel.PREVIEW_TTS
                        Text(
                            text = when (note) {
                                AppViewModel.PREVIEW_OK -> s.speechTestOk
                                AppViewModel.PREVIEW_TTS -> s.speechTestOkSystem
                                // 系统语音没出声不是"合成失败"，别用错前缀
                                AppViewModel.PREVIEW_TTS_FAIL -> s.speechError(note)
                                else -> "${s.speechTestFail}：${s.speechError(note)}"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = if (ok) extras.success else MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            Column(Modifier.statusBarsPadding()) {
                ScreenHeader(
                    title = s.speechPageTitle,
                    subtitle = when {
                        !config.enabled -> s.speechOffSub
                        config.configured -> s.speechRowSub
                        else -> s.speechNotConfigured
                    },
                    onBack = onBack
                )
            }

            Column(Modifier.padding(horizontal = 20.dp)) {
                Spacer(Modifier.height(12.dp))

                // 总开关
                DwCard {
                    DwToggleRow(
                        title = s.speechRowTitle,
                        subtitle = s.speechToggleSub,
                        checked = config.enabled,
                        onCheckedChange = { update(config.copy(enabled = it)) }
                    )
                }

                // 密钥：只保留新版控制台的 API Key
                Spacer(Modifier.height(SectionGap))
                SectionLabel(s.speechSectionKey, Modifier.padding(bottom = 10.dp))
                DwCard {
                    Column(Modifier.padding(14.dp)) {
                        DwField(
                            label = s.speechApiKey,
                            value = config.apiKey,
                            onValueChange = { update(config.copy(apiKey = it.trim())) },
                            extras = extras,
                            secret = true
                        )
                    }
                }
                Text(
                    s.speechKeyNote,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )

                // 默认音色与默认语气：都是一行入口，顺序与编辑页保持一致
                Spacer(Modifier.height(SectionGap))
                SectionLabel(s.speechSectionVoice, Modifier.padding(bottom = 10.dp))
                DwCard {
                    Column {
                        DwRow(
                            title = s.defaultVoiceTitle,
                            subtitle = VoiceCatalog.labelOf(config.voiceType, s.lang == Lang.EN),
                            onClick = { showVoicePicker = true }
                        ) {
                            ActionPill(s.select) { showVoicePicker = true }
                        }

                        DwDivider()

                        DwToggleRow(
                            title = s.defaultEmotionTitle,
                            subtitle = s.speechEmotionSub,
                            checked = config.emotionEnabled,
                            onCheckedChange = { update(config.copy(emotionEnabled = it)) }
                        )
                        if (config.emotionEnabled) {
                            Box(Modifier.padding(start = 14.dp, end = 14.dp, bottom = 14.dp)) {
                                DwField(
                                    label = s.speechEmotionTitle,
                                    value = config.emotion,
                                    onValueChange = { update(config.copy(emotion = it.trim())) },
                                    extras = extras,
                                    placeholder = s.speechEmotionHint
                                )
                            }
                        }
                    }
                }

                // 改这里只影响以后新建的提醒——音色与语气在合成那一刻就写进音频了，
                // 已有提醒要换得进它自己的编辑页，否则用户会以为改完没生效
                if (config.configured) {
                    Text(
                        s.voiceScopeNote,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                // 没密钥时这些音色一个都用不上，说清楚，别让用户白选半天
                if (!config.configured) {
                    Text(
                        s.speechVoiceNoKey,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                // 其他：只剩系统音量。每轮念几遍、要不要重复响都跟着提醒走——
                // 催打卡和提醒喝水需要的力度不一样，一个全局值只能迁就其中一头
                Spacer(Modifier.height(SectionGap))
                SectionLabel(s.speechSectionAdvanced, Modifier.padding(bottom = 10.dp))
                DwCard {
                    DwRow(
                        title = s.speechVolumeTitle,
                        subtitle = s.speechVolumeValue(volume.first, volume.second),
                        subtitleColor = if (volume.first == 0) {
                            MaterialTheme.colorScheme.error
                        } else {
                            Color.Unspecified
                        }
                    ) {
                        ActionPill(s.goSettings) { openSoundSettings(context) }
                    }
                }

                // 为已有提醒补合成（没有密钥时无从合成，直接不显示）
                if (config.configured) {
                    Spacer(Modifier.height(16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        ActionPill(s.speechRegen) { vm.regenAllSpeech() }
                        regenCount?.let { count ->
                            Spacer(Modifier.width(10.dp))
                            Text(
                                s.speechRegenDone(count),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }

    if (showVoicePicker) {
        VoicePickerSheet(
            selectedId = config.voiceType,
            onDismiss = { showVoicePicker = false },
            onPick = { update(config.copy(voiceType = it)) }
        )
    }
}

/**
 * 试听栏：一个圆角输入条 + 内嵌的主操作按钮。
 * 原来的做法是输入框旁边挂一颗小药丸，两者高度差一倍，并排看总像没对齐。
 */
@Composable
private fun PreviewBar(
    value: String,
    placeholder: String,
    actionText: String,
    enabled: Boolean,
    onValueChange: (String) -> Unit,
    onAction: () -> Unit
) {
    val extras = LocalDwColors.current
    Surface(shape = CircleShape, color = extras.field, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(start = 18.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.weight(1f)) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(Modifier.width(10.dp))
            Surface(
                shape = CircleShape,
                color = if (enabled) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.surfaceContainerHighest
                },
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable(enabled = enabled, onClick = onAction)
            ) {
                Text(
                    text = actionText,
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 1,
                    color = if (enabled) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 9.dp)
                )
            }
        }
    }
}

@Composable
private fun DwField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    extras: com.dingwhen.app.ui.theme.DwColors,
    secret: Boolean = false,
    placeholder: String? = null
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = {
            Text(
                placeholder ?: label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        visualTransformation = if (secret) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
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

private fun openSoundSettings(context: Context) {
    val intent = Intent(Settings.ACTION_SOUND_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    runCatching { context.startActivity(intent) }
}
