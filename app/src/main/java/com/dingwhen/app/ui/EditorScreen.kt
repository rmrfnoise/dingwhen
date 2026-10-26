package com.dingwhen.app.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
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
import androidx.core.graphics.drawable.toBitmap
import com.dingwhen.app.i18n.LocalStrings
import com.dingwhen.app.model.Draft
import com.dingwhen.app.model.RepeatType
import com.dingwhen.app.ui.theme.LocalDwColors
import java.time.DayOfWeek
import java.time.LocalDate

@Composable
fun EditorScreen(
    draft: Draft,
    onUpdate: ((Draft) -> Draft) -> Unit,
    onSave: () -> Unit,
    onDelete: (() -> Unit)?,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val extras = LocalDwColors.current
    val s = LocalStrings.current
    val problems = draft.problems()
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showAppPicker by remember { mutableStateOf(false) }
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
                DwCard(onClick = {
                    TimePickerDialog(
                        context,
                        { _, hour, minute -> onUpdate { it.copy(hour = hour, minute = minute) } },
                        draft.hour ?: 8,
                        draft.minute ?: 0,
                        true
                    ).show()
                }) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 26.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val unset = draft.hour == null
                        val size = if (unset) 38.sp else 58.sp
                        Text(
                            text = draft.clockText() ?: s.notSet,
                            style = TextStyle(
                                fontSize = size,
                                lineHeight = (size.value * 1.12f).sp,
                                fontWeight = FontWeight.Light,
                                letterSpacing = if (unset) 0.sp else (-2).sp,
                                fontFeatureSettings = "tnum"
                            ),
                            color = if (unset) {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        )
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = if (draft.hour == null) s.pickTime else s.tapToChange,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }

                // 刚进页面还没动手时不报错，等用户真的填了一部分再提示
                val started = draft.hour != null || draft.content.isNotBlank()
                if (problems.isNotEmpty() && started) {
                    Spacer(Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(16.dp),
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
                Spacer(Modifier.height(22.dp))
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
                    DwCard(onClick = {
                        DatePickerDialog(
                            context,
                            { _, year, month, day ->
                                onUpdate { it.copy(date = LocalDate.of(year, month + 1, day)) }
                            },
                            draft.date.year,
                            draft.date.monthValue - 1,
                            draft.date.dayOfMonth
                        ).show()
                    }) {
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
                                color = MaterialTheme.colorScheme.tertiary
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
                Spacer(Modifier.height(22.dp))
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
                    shape = RoundedCornerShape(18.dp),
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

                // 通知按钮
                Spacer(Modifier.height(22.dp))
                SectionLabel(s.sectionActions, Modifier.padding(bottom = 10.dp))
                DwCard {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showAppPicker = true }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
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
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                s.openApp,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                actionAppLabel ?: s.noAppPicked,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
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

                    ActionDivider()
                    ActionSwitchRow(
                        title = s.snoozeTitle,
                        subtitle = s.snoozeSub,
                        checked = draft.actionSnooze,
                        onCheckedChange = { value -> onUpdate { it.copy(actionSnooze = value) } }
                    )
                    ActionDivider()
                    ActionSwitchRow(
                        title = s.dismissTitle,
                        subtitle = s.dismissSub,
                        checked = draft.actionDismiss,
                        onCheckedChange = { value -> onUpdate { it.copy(actionDismiss = value) } }
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

    if (showAppPicker) {
        AppPickerSheet(
            onDismiss = { showAppPicker = false },
            onPick = { pkg -> onUpdate { it.copy(actionOpenPackage = pkg) } }
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
            shape = RoundedCornerShape(26.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}

/* ------------------------------------------------------------------ 零件 */

@Composable
private fun ActionDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = 62.dp),
        thickness = 1.dp,
        color = LocalDwColors.current.cardBorder
    )
}

@Composable
private fun ActionSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(start = 16.dp, end = 12.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = dwSwitchColors()
        )
    }
}

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
            .clip(CircleShape)
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
            .clip(CircleShape)
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
