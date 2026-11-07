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
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.dingwhen.app.i18n.Lang
import com.dingwhen.app.i18n.LocalStrings
import com.dingwhen.app.speech.VoiceCatalog
import com.dingwhen.app.ui.theme.LocalDwColors

/**
 * 音色选择器：底部抽屉，一列到底、不按性别分栏。
 *
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
                VoicePickerRow(
                    label = if (en) option.labelEn else option.labelZh,
                    selected = option.id == selectedId,
                    extrasField = extras.field,
                    onClick = { onPick(option.id) }
                )
            }
            item { Spacer(Modifier.height(20.dp)) }
        }
    }
}

@Composable
private fun VoicePickerRow(
    label: String,
    selected: Boolean,
    extrasField: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 11.dp),
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
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
