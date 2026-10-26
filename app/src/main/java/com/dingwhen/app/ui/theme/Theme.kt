package com.dingwhen.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dingwhen.app.i18n.AppSettings
import com.dingwhen.app.i18n.Lang
import com.dingwhen.app.i18n.LocalStrings

/* ---------------------------------------------------------------- 颜色 */

private val LightColors = lightColorScheme(
    // 主色是墨，不是彩：按钮、开关、选中态都用它，靠明度而不是色相说话
    primary = Color(0xFF1A1B1F),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFEFEBE4),
    onPrimaryContainer = Color(0xFF2A2723),
    secondary = Color(0xFF5B554C),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFEFEBE4),
    onSecondaryContainer = Color(0xFF2A2723),
    // 唯一的彩色：赤陶，只用在「下一个」「点一下修改」这类引导上
    tertiary = Color(0xFFB4572E),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFF7E7DC),
    onTertiaryContainer = Color(0xFF6B3018),
    background = Color(0xFFF6F4F0),
    onBackground = Color(0xFF17181B),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF17181B),
    surfaceVariant = Color(0xFFEFECE6),
    onSurfaceVariant = Color(0xFF6E6A62),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFFBFAF7),
    surfaceContainer = Color(0xFFF6F4F0),
    surfaceContainerHigh = Color(0xFFEFECE6),
    surfaceContainerHighest = Color(0xFFE7E3DB),
    outline = Color(0xFFA8A29A),
    outlineVariant = Color(0xFFE7E2D9),
    error = Color(0xFFB3261E),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFF9E3DF),
    onErrorContainer = Color(0xFF7A211A),
    inverseSurface = Color(0xFF232428),
    inverseOnSurface = Color(0xFFF4F1EC),
    scrim = Color(0x66000000)
)

private val DarkColors = darkColorScheme(
    // 深色下反相：主色变近白，按钮就成了黑底上的白按钮
    primary = Color(0xFFF1EFEA),
    onPrimary = Color(0xFF141519),
    primaryContainer = Color(0xFF2C2D33),
    onPrimaryContainer = Color(0xFFE9E5DE),
    secondary = Color(0xFFC9C3B8),
    onSecondary = Color(0xFF1B1C20),
    secondaryContainer = Color(0xFF2C2D33),
    onSecondaryContainer = Color(0xFFE9E5DE),
    tertiary = Color(0xFFE09A76),
    onTertiary = Color(0xFF3C1A0B),
    tertiaryContainer = Color(0xFF3D2518),
    onTertiaryContainer = Color(0xFFF6D8C5),
    background = Color(0xFF0E0E10),
    onBackground = Color(0xFFEDEAE3),
    surface = Color(0xFF17181B),
    onSurface = Color(0xFFEDEAE3),
    surfaceVariant = Color(0xFF212228),
    onSurfaceVariant = Color(0xFFA29C92),
    surfaceContainerLowest = Color(0xFF0A0A0C),
    surfaceContainerLow = Color(0xFF131417),
    surfaceContainer = Color(0xFF17181B),
    surfaceContainerHigh = Color(0xFF212228),
    surfaceContainerHighest = Color(0xFF2B2C32),
    outline = Color(0xFF76716A),
    outlineVariant = Color(0xFF26272C),
    error = Color(0xFFF2B8B5),
    onError = Color(0xFF5C120E),
    errorContainer = Color(0xFF3B1714),
    onErrorContainer = Color(0xFFF9DAD6),
    inverseSurface = Color(0xFFEDEAE3),
    inverseOnSurface = Color(0xFF17181B),
    scrim = Color(0x99000000)
)

/**
 * Material 配色覆盖不到、但版式上反复要用的几个中性色。
 * 统一放这里，避免各页面各写一个 alpha。
 */
@Immutable
data class DwColors(
    /** 卡片 1dp 描边，比 outlineVariant 更淡，只用来托住白色块 */
    val cardBorder: Color,
    /** 停用状态的卡片底色 */
    val cardMuted: Color,
    /** 未选中的 chip / 输入框底色 */
    val field: Color,
    /** 状态"已完成"用的绿 */
    val success: Color,
    val onSuccess: Color,
    val successContainer: Color
)

private val LightExtras = DwColors(
    cardBorder = Color(0xFFEDE9E1),
    cardMuted = Color(0xFFF1EEE8),
    field = Color(0xFFEFECE6),
    success = Color(0xFF256B4E),
    onSuccess = Color(0xFFFFFFFF),
    successContainer = Color(0xFFE5F0E9)
)

private val DarkExtras = DwColors(
    cardBorder = Color(0xFF24252A),
    cardMuted = Color(0xFF1A1B1F),
    field = Color(0xFF202127),
    success = Color(0xFF7AC7A1),
    onSuccess = Color(0xFF0B2E1F),
    successContainer = Color(0xFF17332A)
)

val LocalDwColors = staticCompositionLocalOf { LightExtras }

/* ------------------------------------------------------------- 文字与圆角 */

/**
 * 只调必要的几档：正文用中性灰阶，标题收紧字距才有"设计过"的观感。
 * 中文没有大小写，靠字重和字距拉开层级。
 */
private val DwTypography = Typography().run {
    copy(
        headlineLarge = TextStyle(
            fontSize = 34.sp,
            lineHeight = 40.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = (-0.8).sp
        ),
        headlineMedium = TextStyle(
            fontSize = 28.sp,
            lineHeight = 34.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = (-0.6).sp
        ),
        titleLarge = TextStyle(
            fontSize = 20.sp,
            lineHeight = 26.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = (-0.2).sp
        ),
        titleMedium = TextStyle(
            fontSize = 16.sp,
            lineHeight = 22.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.sp
        ),
        titleSmall = TextStyle(
            fontSize = 14.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.sp
        ),
        bodyLarge = TextStyle(
            fontSize = 16.sp,
            lineHeight = 24.sp,
            fontWeight = FontWeight.Normal,
            letterSpacing = 0.sp
        ),
        bodyMedium = TextStyle(
            fontSize = 14.sp,
            lineHeight = 21.sp,
            fontWeight = FontWeight.Normal,
            letterSpacing = 0.sp
        ),
        bodySmall = TextStyle(
            fontSize = 12.5.sp,
            lineHeight = 18.sp,
            fontWeight = FontWeight.Normal,
            letterSpacing = 0.1.sp
        ),
        labelLarge = TextStyle(
            fontSize = 14.sp,
            lineHeight = 18.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.1.sp
        ),
        labelMedium = TextStyle(
            fontSize = 12.sp,
            lineHeight = 16.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.3.sp
        ),
        labelSmall = TextStyle(
            fontSize = 11.sp,
            lineHeight = 14.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.4.sp
        )
    )
}

private val DwShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun DingWhenTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    lang: Lang = Lang.ZH,
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors
    CompositionLocalProvider(
        LocalDwColors provides if (darkTheme) DarkExtras else LightExtras,
        LocalStrings provides AppSettings.strings(lang)
    ) {
        MaterialTheme(
            colorScheme = colors,
            typography = DwTypography,
            shapes = DwShapes,
            content = content
        )
    }
}
