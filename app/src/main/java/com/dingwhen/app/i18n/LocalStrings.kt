package com.dingwhen.app.i18n

import androidx.compose.runtime.staticCompositionLocalOf

/** 当前语言包。由 DingWhenTheme 统一下发，各页面读它取文案。 */
val LocalStrings = staticCompositionLocalOf<Strings> { Zh }
