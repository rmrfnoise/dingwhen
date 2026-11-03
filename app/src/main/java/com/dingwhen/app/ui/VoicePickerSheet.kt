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
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.dingwhen.app.i18n.Lang
import com.dingwhen.app.i18n.LocalStrings
import com.dingwhen.app.speech.VoiceCatalog
import com.dingwhen.app.speech.VoiceGender
import com.dingwhen.app.speech.VoiceOption
import com.dingwhen.app.ui.theme.LocalDwColors

/**
 * 音色选择器：底部抽屉，按女声 / 男声分栏。
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
        dragHandle = { BottomSheetDefaults.DragHandle(color = MaterialTheme.colorScheme.outlineVariant) }
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
 * 抽屉内容。抽出来是为了能在预览里量高度：
 * 音色从 48 个收到 12 个后，原来固定 86% 的抽屉会在下面留一大片空白，
 * 现在按内容撑开，只保留一个屏幕高度的上限。
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

    // 打开时停在当前音色所在那一栏，省得先去另一边找自己
    var tab by remember {
        mutableStateOf(VoiceCatalog.find(selectedId)?.gender ?: VoiceCatalog.genderOrder.first())
    }
    val voices = remember(tab) { VoiceCatalog.byGender(tab) }
    val listState = rememberLazyListState()
    // 换栏回到顶部：否则会停在上一栏滚动到的位置，看起来像列表跳了
    LaunchedEffect(tab) { listState.scrollToItem(0) }

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
            // 两栏切换而不是一长条分节：女声男声各占一栏，找起来更快
            SegmentedRow(
                options = VoiceCatalog.genderOrder.map { gender ->
                    when (gender) {
                        VoiceGender.FEMALE -> s.voiceGroupFemale
                        VoiceGender.MALE -> s.voiceGroupMale
                    }
                },
                selectedIndex = VoiceCatalog.genderOrder.indexOf(tab).coerceAtLeast(0)
            ) { index ->
                tab = VoiceCatalog.genderOrder[index]
            }
            Spacer(Modifier.height(8.dp))
        }

        LazyColumn(Modifier.weight(1f, fill = false), state = listState) {
            items(voices, key = { it.id }) { option ->
                VoicePickerRow(
                    option = option,
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
    option: VoiceOption,
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
