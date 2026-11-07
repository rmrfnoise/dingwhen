package com.dingwhen.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.dingwhen.app.i18n.AppSettings
import com.dingwhen.app.i18n.Lang
import com.dingwhen.app.i18n.LocalStrings
import com.dingwhen.app.speech.VoiceCatalog
import com.dingwhen.app.speech.VoiceOption
import com.dingwhen.app.ui.theme.LocalDwColors

/**
 * 音色选择器：底部抽屉，一列到底、不按性别分栏。
 *
 * 名称下面是短描述，右侧徽标是语种能力，行尾铅笔用于自定义名称。
 * 之所以单独抽出来，是因为选音色要在两处发生——设置页定默认值，编辑页定单条。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoicePickerSheet(
    selectedId: String,
    onDismiss: () -> Unit,
    onPick: (String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = {
            BottomSheetDefaults.DragHandle(color = MaterialTheme.colorScheme.outlineVariant)
        }
    ) {
        VoicePickerPanel(
            selectedId = selectedId,
            onPick = {
                onPick(it)
                onDismiss()
            }
        )
    }
}

/**
 * 抽屉内容。按内容撑开，只保留一个屏幕高度的上限；
 * 音色多了以后靠滚动，不切栏。
 */
@Composable
fun VoicePickerPanel(
    selectedId: String,
    onPick: (String) -> Unit
) {
    val s = LocalStrings.current
    val extras = LocalDwColors.current
    val en = s.lang == Lang.EN
    val maxSheetHeight = (LocalConfiguration.current.screenHeightDp * 0.86f).dp
    val aliases by AppSettings.voiceAliases.collectAsState()
    var renaming by remember { mutableStateOf<VoiceOption?>(null) }

    Column(
        modifier = Modifier
            .heightIn(max = maxSheetHeight)
            .navigationBarsPadding()
    ) {
        Column(Modifier.padding(horizontal = 20.dp)) {
            Text(s.voicePickerTitle, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(4.dp))
            Text(
                s.voicePickerHint,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(14.dp))
        }

        LazyColumn(Modifier.weight(1f, fill = false)) {
            items(VoiceCatalog.options, key = { it.id }) { option ->
                val defaultName = if (en) option.labelEn else option.labelZh
                val alias = aliases[option.id]?.takeIf { it.isNotBlank() }
                val description = if (en) option.descriptionEn else option.descriptionZh
                VoicePickerRow(
                    label = alias ?: defaultName,
                    description = if (alias != null && alias != defaultName) {
                        "$defaultName · $description"
                    } else {
                        description
                    },
                    language = s.voiceLanguage(option.languages),
                    selected = option.id == selectedId,
                    extrasField = extras.field,
                    onClick = { onPick(option.id) },
                    onRename = { renaming = option }
                )
            }
            item { Spacer(Modifier.height(20.dp)) }
        }
    }

    renaming?.let { option ->
        val defaultName = if (en) option.labelEn else option.labelZh
        VoiceRenameDialog(
            defaultName = defaultName,
            description = if (en) option.descriptionEn else option.descriptionZh,
            initial = aliases[option.id].orEmpty(),
            onDismiss = { renaming = null },
            onSave = { name ->
                AppSettings.setVoiceAlias(option.id, name)
                renaming = null
            }
        )
    }
}

@Composable
private fun VoicePickerRow(
    label: String,
    description: String,
    language: String,
    selected: Boolean,
    extrasField: Color,
    onClick: () -> Unit,
    onRename: () -> Unit
) {
    val s = LocalStrings.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(start = 20.dp, end = 8.dp, top = 9.dp, bottom = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(20.dp)
                .background(
                    if (selected) MaterialTheme.colorScheme.primary else extrasField,
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (selected) {
                Icon(
                    Icons.Rounded.Check,
                    contentDescription = null,
                    modifier = Modifier.size(13.dp),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                if (language.isNotBlank()) {
                    Spacer(Modifier.width(8.dp))
                    MicroPill(language)
                }
            }
            if (description.isNotBlank()) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Spacer(Modifier.width(4.dp))
        IconButton(onClick = onRename) {
            Icon(
                Icons.Rounded.Edit,
                contentDescription = "${s.voiceRename} $label",
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun VoiceRenameDialog(
    defaultName: String,
    description: String,
    initial: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    val s = LocalStrings.current
    val extras = LocalDwColors.current
    var text by remember(initial) { mutableStateOf(initial) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(6.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text(s.voiceRenameTitle, style = MaterialTheme.typography.titleLarge) },
        text = {
            Column {
                Text(
                    text = if (description.isBlank()) defaultName else "$defaultName · $description",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(10.dp))
                TextField(
                    value = text,
                    onValueChange = { value ->
                        text = AppSettings.limitVoiceName(value)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            defaultName,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(4.dp),
                    textStyle = MaterialTheme.typography.bodyLarge,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = extras.field,
                        unfocusedContainerColor = extras.field,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        cursorColor = MaterialTheme.colorScheme.primary
                    )
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    s.voiceRenameHint(AppSettings.MAX_VOICE_NAME_CHARS),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (initial.isNotBlank()) {
                    TextButton(onClick = { onSave("") }) {
                        Text(
                            s.voiceRenameReset,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(text) }) {
                Text(s.voiceRenameSave)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(s.cancel, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}
