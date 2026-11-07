package com.dingwhen.app.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dingwhen.app.i18n.LocalStrings
import com.dingwhen.app.ui.theme.LocalDwColors

/* ------------------------------------------------------------------ 卡片 */

/** 统一卡片：白底 + 1dp 极淡描边 + 22dp 圆角，不用阴影，靠留白分层。 */
@Composable
fun DwCard(
    modifier: Modifier = Modifier,
    muted: Boolean = false,
    onClick: (() -> Unit)? = null,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit
) {
    val extras = LocalDwColors.current
    val shape = RoundedCornerShape(4.dp)
    val base = modifier.fillMaxWidth()
    Surface(
        modifier = if (onClick != null) base.clip(shape).clickable(onClick = onClick) else base,
        shape = shape,
        color = if (muted) extras.cardMuted else MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, extras.cardBorder)
    ) {
        Column(content = content)
    }
}

/* ------------------------------------------------------------------ 标题 */

/**
 * 页面头部。列表页和子页共用同一套大标题：返回键单独占一行，
 * 标题永远从 20dp 起排，和下面的内容网格对齐。
 */
@Composable
fun ScreenHeader(
    title: String,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    trailing: @Composable RowScope.() -> Unit = {}
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        if (onBack != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, end = 12.dp, top = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RoundIconButton(Icons.AutoMirrored.Rounded.ArrowBack, LocalStrings.current.back, onBack)
                Spacer(Modifier.weight(1f))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    content = trailing
                )
            }
            Spacer(Modifier.height(2.dp))
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 12.dp, top = if (onBack == null) 8.dp else 0.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                if (subtitle != null) {
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (onBack == null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    content = trailing
                )
            }
        }
    }
}

/** 38dp 圆形浅底图标按钮，用于头部。 */
@Composable
fun RoundIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    tint: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(RoundedCornerShape(3.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = contentDescription, tint = tint, modifier = Modifier.size(21.dp))
    }
}

/* ------------------------------------------------------------- 小标签 / 分节 */

/** 分节标题：印刷栏目头——短标签加一条通到右边的细线，把内容按栏切开。 */
@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.size(10.dp))
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            thickness = 1.dp,
            color = LocalDwColors.current.cardBorder
        )
        Spacer(Modifier.size(10.dp))
    }
}

/** 分节之间的统一留白：所有页面都隔这么远，滚动时才不会有的松有的紧。 */
val SectionGap = 22.dp

/* --------------------------------------------------------- 标准行 / 分隔线 */

/**
 * 卡片内的一行标准布局：左右内边距、标题与副标题的行距、
 * 以及"文字到尾部控件"的间距都固定在这里。
 *
 * 尾部间距是这里最重要的一个数：文字列必须自己留出 [trailingGap]，
 * 否则副标题一长就会顶到开关或按钮上，看起来像挤在一起。
 */
@Composable
fun DwRow(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    trailingGap: Dp = 14.dp,
    subtitleColor: Color = Color.Unspecified,
    leading: (@Composable () -> Unit)? = null,
    trailing: @Composable RowScope.() -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(start = 16.dp, end = 14.dp, top = 13.dp, bottom = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (leading != null) {
            leading()
            Spacer(Modifier.size(13.dp))
        }
        Column(
            Modifier
                .weight(1f)
                .padding(end = trailingGap)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (subtitle != null) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (subtitleColor == Color.Unspecified) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        subtitleColor
                    }
                )
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            content = trailing
        )
    }
}

/**
 * 带开关的行。开关自带 12dp 的视觉留白，所以这一行的右边只留 10dp，
 * 免得整行看起来偏左；文字和开关之间则强制留 [trailingGap]。
 */
@Composable
fun DwToggleRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    trailingGap: Dp = 14.dp,
    /** 依赖项没开时置灰：整行不可点，开关也点不动，但说明文字仍然读得到 */
    enabled: Boolean = true
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onCheckedChange(!checked) }
            .padding(start = 16.dp, end = 10.dp, top = 13.dp, bottom = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            Modifier
                .weight(1f)
                .padding(end = trailingGap)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (enabled) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
                }
            )
            if (subtitle != null) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            colors = dwSwitchColors()
        )
    }
}

/** 卡片内的分隔线：默认从文字起点（16dp）缩进，和 iOS 设置页同一套逻辑。 */
@Composable
fun DwDivider(indent: Dp = 16.dp) {
    HorizontalDivider(
        modifier = Modifier.padding(start = indent),
        thickness = 1.dp,
        color = LocalDwColors.current.cardBorder
    )
}

/** 可点的操作药丸，例如「去设置」「去开启」。 */
@Composable
fun ActionPill(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val contentColor = if (enabled) {
        MaterialTheme.colorScheme.onSurface
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
    }
    Surface(
        shape = RoundedCornerShape(3.dp),
        color = Color.Transparent,
        border = BorderStroke(1.dp, contentColor.copy(alpha = 0.4f)),
        modifier = modifier
            .clip(RoundedCornerShape(3.dp))
            .clickable(enabled = enabled, onClick = onClick)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = contentColor,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

/** 状态小药丸，例如「已开启」「3 个提醒」。 */
@Composable
fun MicroPill(
    text: String,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    background: Color = LocalDwColors.current.field
) {
    Surface(shape = RoundedCornerShape(2.dp), color = background) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
        )
    }
}

/** 等宽分段选择器：两三档之间的切换，比下拉少一次点击，也更容易扫。 */
@Composable
fun SegmentedRow(
    options: List<String>,
    selectedIndex: Int,
    modifier: Modifier = Modifier,
    onSelect: (Int) -> Unit
) {
    val extras = LocalDwColors.current
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        options.forEachIndexed { index, label ->
            val selected = index == selectedIndex
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(38.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(if (selected) MaterialTheme.colorScheme.onSurface else extras.field)
                    .clickable { onSelect(index) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
                    maxLines = 1,
                    color = if (selected) {
                        MaterialTheme.colorScheme.surface
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
    }
}

/* ------------------------------------------------------------------ 按钮 */

/** 全应用统一的开关配色：开启是墨色印，不是朱砂——红色只留给真正的主行动。 */
@Composable
fun dwSwitchColors() = SwitchDefaults.colors(
    checkedThumbColor = MaterialTheme.colorScheme.surface,
    checkedTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.82f),
    checkedBorderColor = Color.Transparent,
    uncheckedThumbColor = MaterialTheme.colorScheme.surface,
    uncheckedTrackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.28f),
    uncheckedBorderColor = Color.Transparent
)
/** 章末主操作：全宽胶囊按钮。 */
@Composable
fun PrimaryPillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(3.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(19.dp))
            Spacer(Modifier.size(8.dp))
        }
        Text(text, style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Medium))
    }
}

/**
 * 朱砂印。通胜里唯一"盖"出来的形状：方章、双线边、宋体反白字。
 * 只用在两处——列表顶部标出下一条，以及新建按钮。
 */
@Composable
fun SealStamp(
    text: String,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    color: Color = MaterialTheme.colorScheme.primary,
    onColor: Color = MaterialTheme.colorScheme.onPrimary
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(3.dp))
            .background(color),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(3.dp)
                .border(BorderStroke(1.dp, onColor.copy(alpha = 0.55f)), RoundedCornerShape(2.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = onColor,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = com.dingwhen.app.ui.theme.DwSerif,
                    fontWeight = FontWeight.Medium
                ),
                maxLines = 1
            )
        }
    }
}
