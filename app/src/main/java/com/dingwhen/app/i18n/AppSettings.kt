package com.dingwhen.app.i18n

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** 浅色 / 深色 / 跟随系统。默认跟随系统，用户选了之后以选择为准。 */
enum class ThemeMode { SYSTEM, LIGHT, DARK }

/**
 * 界面偏好（主题、语言）。用 StateFlow 广播，改了之后 Compose 直接重组，
 * 不需要重建 Activity，通知那边也能同步拿到当前语言。
 */
object AppSettings {

    private const val FILE = "dingwhen"
    private const val KEY_THEME = "theme_mode"
    private const val KEY_LANG = "lang"

    @Volatile
    private var prefs: SharedPreferences? = null

    private val _theme = MutableStateFlow(ThemeMode.SYSTEM)
    val theme: StateFlow<ThemeMode> = _theme.asStateFlow()

    private val _lang = MutableStateFlow(Lang.ZH)
    val lang: StateFlow<Lang> = _lang.asStateFlow()

    fun init(context: Context) {
        if (prefs != null) return
        val store = context.applicationContext.getSharedPreferences(FILE, Context.MODE_PRIVATE)
        prefs = store
        _theme.value = ThemeMode.entries.firstOrNull { it.name == store.getString(KEY_THEME, null) }
            ?: ThemeMode.SYSTEM
        _lang.value = Lang.fromCode(store.getString(KEY_LANG, null)) ?: Lang.system()
    }

    fun setTheme(mode: ThemeMode) {
        _theme.value = mode
        prefs?.edit()?.putString(KEY_THEME, mode.name)?.apply()
    }

    fun setLang(lang: Lang) {
        _lang.value = lang
        prefs?.edit()?.putString(KEY_LANG, lang.code)?.apply()
    }

    /** 通知等非 Compose 场景用它取当前语言。 */
    fun strings(): Strings = strings(_lang.value)

    fun strings(lang: Lang): Strings = if (lang == Lang.EN) En else Zh
}
