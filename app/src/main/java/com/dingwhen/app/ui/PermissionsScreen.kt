package com.dingwhen.app.ui

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material.icons.rounded.BatterySaver
import androidx.compose.material.icons.rounded.NotificationsNone
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dingwhen.app.i18n.AppSettings
import com.dingwhen.app.i18n.Lang
import com.dingwhen.app.i18n.LocalStrings
import com.dingwhen.app.i18n.Strings
import com.dingwhen.app.i18n.ThemeMode
import com.dingwhen.app.ui.theme.LocalDwColors

@Composable
fun PermissionsScreen(
    onBack: () -> Unit,
    onFinished: () -> Unit
) {
    val context = LocalContext.current
    val extras = LocalDwColors.current
    val s = LocalStrings.current
    val themeMode by AppSettings.theme.collectAsState()
    val lang by AppSettings.lang.collectAsState()
    var refresh by remember { mutableIntStateOf(0) }
    // 从系统设置返回时 tick 自增，顺带刷新状态
    val tick = rememberResumeTick()
    val entries = remember(refresh, tick) { Permissions.build(context) }
    val pending = entries.count { !it.granted && !it.manual }

    val notificationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        refresh++
        if (!granted) Permissions.openNotificationSettings(context)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            Surface(color = MaterialTheme.colorScheme.background) {
                Box(
                    Modifier
                        .navigationBarsPadding()
                        .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 14.dp)
                ) {
                    PrimaryPillButton(text = s.done, onClick = onFinished)
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
                    title = s.permTitle,
                    subtitle = if (pending > 0) s.permSubtitlePending(pending) else s.permSubtitleOk,
                    onBack = onBack
                )
            }

            Column(Modifier.padding(horizontal = 20.dp)) {
                Spacer(Modifier.height(12.dp))
                SectionLabel(s.sectionPermissions, Modifier.padding(bottom = 10.dp))

                entries.forEach { entry ->
                    DwCard(modifier = Modifier.padding(bottom = 10.dp)) {
                        Row(
                            modifier = Modifier.padding(start = 16.dp, end = 14.dp, top = 14.dp, bottom = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (entry.granted) extras.successContainer else extras.field
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = iconOf(entry.key),
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = if (entry.granted) {
                                        extras.success
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                            }
                            Spacer(Modifier.width(13.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    titleOf(entry.key, s),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    descOf(entry.key, s),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(Modifier.width(6.dp))

                            when {
                                entry.granted -> MicroPill(
                                    text = s.grantedOn,
                                    color = extras.success,
                                    background = extras.successContainer
                                )

                                entry.manual -> ActionPill(s.goSettings) { entry.action() }

                                else -> ActionPill(actionLabelOf(entry, s)) {
                                    if (entry.key == "notification" &&
                                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                                    ) {
                                        notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    } else {
                                        entry.action()
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(2.dp))
                Text(
                    s.romNote,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // 偏好：外观和语言放同一张卡里，少一层分隔，整页也刚好一屏放得下
                Spacer(Modifier.height(18.dp))
                DwCard {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                        SectionLabel(s.sectionAppearance, Modifier.padding(bottom = 8.dp))
                        SegmentedRow(
                            options = listOf(s.themeSystem, s.themeLight, s.themeDark),
                            selectedIndex = themeMode.ordinal,
                            onSelect = { index -> AppSettings.setTheme(ThemeMode.entries[index]) }
                        )
                        Spacer(Modifier.height(16.dp))
                        SectionLabel(s.sectionLanguage, Modifier.padding(bottom = 8.dp))
                        SegmentedRow(
                            options = listOf("中文", "English"),
                            selectedIndex = lang.ordinal,
                            onSelect = { index -> AppSettings.setLang(Lang.entries[index]) }
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))
            }
        }
    }
}

private fun titleOf(key: String, s: Strings): String = when (key) {
    "notification" -> s.permNotificationTitle
    "exact_alarm" -> s.permExactTitle
    "battery" -> s.permBatteryTitle
    else -> s.permAutostartTitle
}

private fun descOf(key: String, s: Strings): String = when (key) {
    "notification" -> s.permNotificationDesc
    "exact_alarm" -> s.permExactDesc
    "battery" -> s.permBatteryDesc
    else -> s.permAutostartDesc
}

private fun actionLabelOf(entry: PermissionEntry, s: Strings): String = when {
    entry.granted -> s.grantedOn
    entry.key == "exact_alarm" -> s.goAllow
    else -> s.goEnable
}

private fun iconOf(key: String): ImageVector = when (key) {
    "notification" -> Icons.Rounded.NotificationsNone
    "exact_alarm" -> Icons.Rounded.Alarm
    "battery" -> Icons.Rounded.BatterySaver
    else -> Icons.Rounded.RestartAlt
}
