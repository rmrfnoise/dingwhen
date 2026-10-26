package com.dingwhen.app.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.NotificationsNone
import androidx.compose.material.icons.rounded.NotificationsOff
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dingwhen.app.data.Reminder
import com.dingwhen.app.i18n.LocalStrings
import com.dingwhen.app.i18n.Strings
import com.dingwhen.app.ui.theme.LocalDwColors
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListScreen(
    reminders: List<Reminder>,
    notificationGranted: Boolean,
    exactAlarmGranted: Boolean,
    onAddManual: () -> Unit,
    onToggle: (Reminder, Boolean) -> Unit,
    onDelete: (Reminder) -> Unit,
    onEdit: (Reminder) -> Unit,
    onOpenPermissions: () -> Unit
) {
    val s = LocalStrings.current
    val active = reminders.filter { it.enabled }
    val paused = reminders.filterNot { it.enabled }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var pendingDelete by remember { mutableStateOf<Reminder?>(null) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = {
            SnackbarHost(snackbar, modifier = Modifier.navigationBarsPadding().padding(16.dp))
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddManual,
                modifier = Modifier.navigationBarsPadding(),
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 6.dp,
                    pressedElevation = 2.dp
                ),
                icon = { Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(20.dp)) },
                text = {
                    Text(
                        s.newReminder,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item(key = "header") {
                Column(Modifier.statusBarsPadding()) {
                    ScreenHeader(
                        title = s.listTitle,
                        subtitle = listSubtitle(active, reminders, s),
                        trailing = {
                            RoundIconButton(Icons.Rounded.Tune, s.settingsDesc, onOpenPermissions)
                        }
                    )
                    if (!notificationGranted || !exactAlarmGranted) {
                        PermissionBanner(
                            notificationGranted = notificationGranted,
                            exactAlarmGranted = exactAlarmGranted,
                            onFix = onOpenPermissions
                        )
                    }
                }
            }

            if (reminders.isEmpty()) {
                item(key = "empty") { EmptyState(s) }
            }

            if (active.isNotEmpty()) {
                if (paused.isNotEmpty()) {
                    item(key = "label-active") {
                        SectionLabel(s.sectionActive, Modifier.padding(top = 14.dp, start = 20.dp, bottom = 8.dp))
                    }
                }
                items(active, key = { "a${it.id}" }) { reminder ->
                    ReminderCard(
                        modifier = Modifier.padding(horizontal = 20.dp),
                        reminder = reminder,
                        onToggle = { onToggle(reminder, it) },
                        onClick = { onEdit(reminder) },
                        onDeleteRequest = { pendingDelete = reminder }
                    )
                }
            }

            if (paused.isNotEmpty()) {
                item(key = "label-paused") {
                    SectionLabel(s.sectionPaused, Modifier.padding(top = 14.dp, start = 20.dp, bottom = 8.dp))
                }
                items(paused, key = { "p${it.id}" }) { reminder ->
                    ReminderCard(
                        modifier = Modifier.padding(horizontal = 20.dp),
                        reminder = reminder,
                        onToggle = { onToggle(reminder, it) },
                        onClick = { onEdit(reminder) },
                        onDeleteRequest = { pendingDelete = reminder }
                    )
                }
            }
        }
    }

    pendingDelete?.let { target ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text(s.deleteTitle) },
            text = {
                Text(
                    s.deleteBody(target.content),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    pendingDelete = null
                    onDelete(target)
                    scope.launch {
                        snackbar.showSnackbar(s.deletedSnack(target.content))
                    }
                }) {
                    Text(
                        s.delete,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) {
                    Text(
                        s.cancel,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            shape = RoundedCornerShape(26.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}

private fun listSubtitle(active: List<Reminder>, all: List<Reminder>, s: Strings): String = when {
    all.isEmpty() -> s.listTagline
    active.isEmpty() -> s.allPaused
    else -> {
        val next = active.minByOrNull { it.nextTriggerAt }?.nextTriggerAt ?: 0L
        s.activeSummary(active.size, nextTriggerText(next, s))
    }
}

/* ------------------------------------------------------------------ 卡片 */

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ReminderCard(
    modifier: Modifier = Modifier,
    reminder: Reminder,
    onToggle: (Boolean) -> Unit,
    onClick: () -> Unit,
    onDeleteRequest: () -> Unit
) {
    val s = LocalStrings.current
    val dimmed = !reminder.enabled

    DwCard(modifier = modifier, muted = dimmed) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(onClick = onClick, onLongClick = onDeleteRequest)
                .padding(start = 18.dp, end = 14.dp, top = 16.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 左：时间徽章（固定宽度，让右边的文字左边缘对齐）
            TimeBadge(reminder = reminder, dimmed = dimmed, s = s)

            Spacer(Modifier.width(16.dp))

            // 右：内容 + 周期
            Column(Modifier.weight(1f)) {
                Text(
                    text = reminder.content,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = if (dimmed) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    text = s.repeatLabel(reminder.repeatType, reminder.days, reminder.dayOfMonth),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.width(8.dp))
            Switch(
                checked = reminder.enabled,
                onCheckedChange = onToggle,
                modifier = Modifier.scale(0.86f),
                colors = dwSwitchColors()
            )
        }
    }
}

@Composable
private fun TimeBadge(reminder: Reminder, dimmed: Boolean, s: Strings) {
    Column(modifier = Modifier.width(60.dp), horizontalAlignment = Alignment.Start) {
        Text(
            text = timeText(reminder.hour, reminder.minute),
            style = TextStyle(
                fontSize = 20.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = (-0.5).sp,
                fontFeatureSettings = "tnum"
            ),
            color = if (dimmed) {
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
            } else {
                MaterialTheme.colorScheme.onSurface
            }
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = if (reminder.enabled) dayTag(reminder.nextTriggerAt, s) else s.pausedTag,
            fontSize = 11.sp,
            lineHeight = 14.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.2.sp,
            maxLines = 1,
            color = if (reminder.enabled) {
                MaterialTheme.colorScheme.tertiary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            }
        )
    }
}

/* ------------------------------------------------------------------ 附属 */

@Composable
private fun PermissionBanner(
    notificationGranted: Boolean,
    exactAlarmGranted: Boolean,
    onFix: () -> Unit
) {
    val s = LocalStrings.current
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.errorContainer
    ) {
        Row(
            modifier = Modifier.padding(start = 14.dp, end = 6.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Rounded.NotificationsOff,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(19.dp)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = when {
                    !notificationGranted && !exactAlarmGranted -> s.bannerBoth
                    !notificationGranted -> s.bannerNotification
                    else -> s.bannerExact
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = onFix) {
                Text(
                    s.bannerFix,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun EmptyState(s: Strings) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 118.dp, start = 40.dp, end = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(92.dp)
                .clip(CircleShape)
                .background(LocalDwColors.current.field),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Rounded.NotificationsNone,
                contentDescription = null,
                modifier = Modifier.size(38.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
        }
        Spacer(Modifier.height(22.dp))
        Text(s.emptyTitle, style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        Text(
            s.emptyBody,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
