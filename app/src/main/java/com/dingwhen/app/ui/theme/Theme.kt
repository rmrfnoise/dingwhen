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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dingwhen.app.i18n.AppSettings
import com.dingwhen.app.i18n.Lang
import com.dingwhen.app.i18n.LocalStrings

/* ---------------------------------------------------------------- 颜色
 *
 * 通胜：米纸为地，墨为字，朱砂只负责"要你动手"的那一处。
 * 主色是朱砂，不是 Material 默认的紫；开关、选中、主按钮都用它。
 */

private val LightColors = lightColorScheme(
    primary = Color(0xFFA63A2B),
    onPrimary = Color(0xFFF8F0DF),
    primaryContainer = Color(0xFFF0DFD3),
    onPrimaryContainer = Color(0xFF5A1D13),
    secondary = Color(0xFF6B6152),
    onSecondary = Color(0xFFF8F0DF),
    secondaryContainer = Color(0xFFEDE2CB),
    onSecondaryContainer = Color(0xFF332C22),
    tertiary = Color(0xFF3F5B43),
    onTertiary = Color(0xFFF8F0DF),
    tertiaryContainer = Color(0xFFE3E9DC),
    onTertiaryContainer = Color(0xFF243024),
    background = Color(0xFFF3E9D6),
    onBackground = Color(0xFF1E1A15),
    surface = Color(0xFFFBF4E4),
    onSurface = Color(0xFF1E1A15),
    surfaceVariant = Color(0xFFEDE2CB),
    onSurfaceVariant = Color(0xFF6B6152),
    surfaceContainerLowest = Color(0xFFFFFBF1),
    surfaceContainerLow = Color(0xFFFBF4E4),
    surfaceContainer = Color(0xFFF3E9D6),
    surfaceContainerHigh = Color(0xFFEDE2CB),
    surfaceContainerHighest = Color(0xFFE7DABF),
    outline = Color(0xFFA79878),
    outlineVariant = Color(0xFFDFD2B8),
    error = Color(0xFF9B2C1C),
    onError = Color(0xFFF8F0DF),
    errorContainer = Color(0xFFF2DDD5),
    onErrorContainer = Color(0xFF5A1D13),
    inverseSurface = Color(0xFF1E1A15),
    inverseOnSurface = Color(0xFFF3E9D6),
    scrim = Color(0x66000000)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFD2684F),
    onPrimary = Color(0xFF2A1109),
    primaryContainer = Color(0xFF4A2117),
    onPrimaryContainer = Color(0xFFF3D9CC),
    secondary = Color(0xFFC4B79E),
    onSecondary = Color(0xFF1E1A15),
    secondaryContainer = Color(0xFF332C22),
    onSecondaryContainer = Color(0xFFEDE2CB),
    tertiary = Color(0xFF9DB79A),
    onTertiary = Color(0xFF12200F),
    tertiaryContainer = Color(0xFF2A3A28),
    onTertiaryContainer = Color(0xFFDCE7D6),
    background = Color(0xFF14110D),
    onBackground = Color(0xFFEFE6D3),
    surface = Color(0xFF1C1812),
    onSurface = Color(0xFFEFE6D3),
    surfaceVariant = Color(0xFF2A241B),
    onSurfaceVariant = Color(0xFFB4A88F),
    surfaceContainerLowest = Color(0xFF100D0A),
    surfaceContainerLow = Color(0xFF1C1812),
    surfaceContainer = Color(0xFF221D16),
    surfaceContainerHigh = Color(0xFF2A241B),
    surfaceContainerHighest = Color(0xFF352E24),
    outline = Color(0xFF7E7159),
    outlineVariant = Color(0xFF352E24),
    error = Color(0xFFE59B8B),
    onError = Color(0xFF3A0F07),
    errorContainer = Color(0xFF4A2117),
    onErrorContainer = Color(0xFFF3D9CC),
    inverseSurface = Color(0xFFEFE6D3),
    inverseOnSurface = Color(0xFF1E1A15),
    scrim = Color(0x99000000)
)

/**
 * Material 配色覆盖不到、但版式上反复要用的几个颜色。
 * 统一放这里，避免各页面各写一个 alpha。
 */
@Immutable
data class DwColors(
    /** 栏线：卡片与行之间那条 1dp 的印刷线 */
    val cardBorder: Color,
    /** 停用状态的底色 */
    val cardMuted: Color,
    /** 输入框 / 未选中分段器的底色 */
    val field: Color,
    /** 「下一条」印块与新建印：浅色是朱砂，深色是压暗的朱砂，夜里不刺眼 */
    val seal: Color,
    val onSeal: Color,
    /** 状态"已完成"用的青绿 */
    val success: Color,
    val onSuccess: Color,
    val successContainer: Color
)

private val LightExtras = DwColors(
    cardBorder = Color(0xFFDFD2B8),
    cardMuted = Color(0xFFEDE2CB),
    field = Color(0xFFEDE2CB),
    seal = Color(0xFFA63A2B),
    onSeal = Color(0xFFF8F0DF),
    success = Color(0xFF3F5B43),
    onSuccess = Color(0xFFFFFFFF),
    successContainer = Color(0xFFE3E9DC)
)

private val DarkExtras = DwColors(
    cardBorder = Color(0xFF352E24),
    cardMuted = Color(0xFF221D16),
    field = Color(0xFF2A241B),
    seal = Color(0xFF48200F),
    onSeal = Color(0xFFF3D9CC),
    success = Color(0xFF9DB79A),
    onSuccess = Color(0xFF12200F),
    successContainer = Color(0xFF2A3A28)
)

val LocalDwColors = staticCompositionLocalOf { LightExtras }

/* ------------------------------------------------------------- 文字与圆角
 *
 * 通胜的字：标题、时间、数字用系统的宋体（serif），正文用无衬线。
 * 中文没有大小写，层级靠字重、字号与字距。
 */

/** 宋体承担"印刷"的部分：标题、时间、印章。 */
val DwSerif = FontFamily.Serif

private val DwTypography = Typography().run {
    copy(
        displayLarge = TextStyle(
            fontFamily = DwSerif,
            fontSize = 64.sp,
            lineHeight = 68.sp,
            fontWeight = FontWeight.Normal,
            letterSpacing = (-2).sp
        ),
        displayMedium = TextStyle(
            fontFamily = DwSerif,
            fontSize = 40.sp,
            lineHeight = 44.sp,
            fontWeight = FontWeight.Normal,
            letterSpacing = (-1).sp
        ),
        headlineLarge = TextStyle(
            fontFamily = DwSerif,
            fontSize = 32.sp,
            lineHeight = 38.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.sp
        ),
        headlineMedium = TextStyle(
            fontFamily = DwSerif,
            fontSize = 26.sp,
            lineHeight = 32.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.sp
        ),
        titleLarge = TextStyle(
            fontFamily = DwSerif,
            fontSize = 20.sp,
            lineHeight = 26.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.sp
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
            letterSpacing = 0.2.sp
        ),
        labelMedium = TextStyle(
            fontSize = 12.sp,
            lineHeight = 16.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.4.sp
        ),
        labelSmall = TextStyle(
            fontSize = 11.sp,
            lineHeight = 14.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.6.sp
        )
    )
}

/** 印刷品不是塑料：圆角小，边框细，靠栏线分层。 */
private val DwShapes = Shapes(
    extraSmall = RoundedCornerShape(2.dp),
    small = RoundedCornerShape(3.dp),
    medium = RoundedCornerShape(4.dp),
    large = RoundedCornerShape(6.dp),
    extraLarge = RoundedCornerShape(8.dp)
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
