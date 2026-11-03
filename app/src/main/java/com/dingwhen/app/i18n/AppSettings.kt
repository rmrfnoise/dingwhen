package com.dingwhen.app.i18n

import android.content.Context
import android.content.SharedPreferences
import com.dingwhen.app.speech.SpeechConfig
import com.dingwhen.app.speech.VoiceCatalog
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

    private const val KEY_SP_ENABLED = "sp_enabled"
    private const val KEY_SP_API_KEY = "sp_api_key"
    private const val KEY_SP_VOICE = "sp_voice"
    private const val KEY_SP_EMOTION = "sp_emotion"
    private const val KEY_SP_EMOTION_ON = "sp_emotion_on"
    private const val KEY_SP_TEST = "sp_test"

    @Volatile
    private var prefs: SharedPreferences? = null

    private val _theme = MutableStateFlow(ThemeMode.SYSTEM)
    val theme: StateFlow<ThemeMode> = _theme.asStateFlow()

    private val _lang = MutableStateFlow(Lang.ZH)
    val lang: StateFlow<Lang> = _lang.asStateFlow()

    /** 语音播报配置。密钥只落本地，公开仓库里不能出现。 */
    private val _speech = MutableStateFlow(SpeechConfig())
    val speech: StateFlow<SpeechConfig> = _speech.asStateFlow()

    fun init(context: Context) {
        if (prefs != null) return
        val store = context.applicationContext.getSharedPreferences(FILE, Context.MODE_PRIVATE)
        prefs = store
        _theme.value = ThemeMode.entries.firstOrNull { it.name == store.getString(KEY_THEME, null) }
            ?: ThemeMode.SYSTEM
        _lang.value = Lang.fromCode(store.getString(KEY_LANG, null)) ?: Lang.system()
        _speech.value = SpeechConfig(
            apiKey = store.getString(KEY_SP_API_KEY, "").orEmpty(),
            // 老配置里可能存着 1.0 音色，新版本会直接报 45000000，这里迁回默认值
            voiceType = VoiceCatalog.migrate(store.getString(KEY_SP_VOICE, null)),
            emotion = store.getString(KEY_SP_EMOTION, "").orEmpty(),
            emotionEnabled = store.getBoolean(KEY_SP_EMOTION_ON, false),
            testText = store.getString(KEY_SP_TEST, "").orEmpty(),
            enabled = store.getBoolean(KEY_SP_ENABLED, true)
        )
    }

    fun setTheme(mode: ThemeMode) {
        _theme.value = mode
        prefs?.edit()?.putString(KEY_THEME, mode.name)?.apply()
    }

    fun setLang(lang: Lang) {
        _lang.value = lang
        prefs?.edit()?.putString(KEY_LANG, lang.code)?.apply()
    }

    fun speechConfig(): SpeechConfig = _speech.value

    fun setSpeech(config: SpeechConfig) {
        _speech.value = config
        prefs?.edit()
            ?.putBoolean(KEY_SP_ENABLED, config.enabled)
            ?.putString(KEY_SP_API_KEY, config.apiKey)
            ?.putString(KEY_SP_VOICE, config.voiceType)
            ?.putString(KEY_SP_EMOTION, config.emotion)
            ?.putBoolean(KEY_SP_EMOTION_ON, config.emotionEnabled)
            ?.putString(KEY_SP_TEST, config.testText)
            ?.apply()
    }

    /** 通知等非 Compose 场景用它取当前语言。 */
    fun strings(): Strings = strings(_lang.value)

    fun strings(lang: Lang): Strings = if (lang == Lang.EN) En else Zh
}
