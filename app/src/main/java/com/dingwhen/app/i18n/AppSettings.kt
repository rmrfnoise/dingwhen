package com.dingwhen.app.i18n

import android.content.Context
import android.content.SharedPreferences
import com.dingwhen.app.speech.SpeechConfig
import com.dingwhen.app.speech.VoiceCatalog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject

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

    private const val KEY_VOICE_ALIASES = "voice_aliases"

    /** 音色自定义名称的最大长度；按 Unicode 码点算，避免 emoji 被截成半个。 */
    const val MAX_VOICE_NAME_CHARS = 24

    @Volatile
    private var prefs: SharedPreferences? = null

    private val _theme = MutableStateFlow(ThemeMode.SYSTEM)
    val theme: StateFlow<ThemeMode> = _theme.asStateFlow()

    private val _lang = MutableStateFlow(Lang.ZH)
    val lang: StateFlow<Lang> = _lang.asStateFlow()

    /** 语音播报配置。密钥只落本地，公开仓库里不能出现。 */
    private val _speech = MutableStateFlow(SpeechConfig())
    val speech: StateFlow<SpeechConfig> = _speech.asStateFlow()

    /**
     * 音色别名：键是音色 ID，值是用户起的名字。
     *
     * 只影响显示，不进入 voiceType、不进缓存键；改名不会触发重新合成。
     * 存在 SharedPreferences 而不是 Room：它是设备级显示偏好，不是提醒数据，
     * 这样也不用为它升数据库版本、冒 fallbackToDestructiveMigration 清空提醒的风险。
     */
    private val _voiceAliases = MutableStateFlow<Map<String, String>>(emptyMap())
    val voiceAliases: StateFlow<Map<String, String>> = _voiceAliases.asStateFlow()

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
        _voiceAliases.value = parseVoiceAliases(store.getString(KEY_VOICE_ALIASES, null))
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

    /**
     * 音色的显示名：别名优先，没有别名回落到当前语言的官方名称。
     *
     * Compose 里请把 [voiceAliases] collect 成 State 再传 aliases；
     * 只调用本方法而不订阅 StateFlow 的话，改名不会触发重组。
     */
    fun voiceLabel(
        id: String?,
        en: Boolean,
        aliases: Map<String, String> = _voiceAliases.value
    ): String? {
        if (id == null) return null
        aliases[id]?.takeIf { it.isNotBlank() }?.let { return it }
        return VoiceCatalog.labelOf(id, en)
    }

    /** 输入框用：按码点截断，避免粘贴超长名称时整个输入被拒绝。 */
    fun limitVoiceName(name: String): String = name.takeCodePoints(MAX_VOICE_NAME_CHARS)

    /** 空字符串 / 纯空白表示恢复默认名称。 */
    fun setVoiceAlias(id: String, name: String?) {
        val clean = name
            ?.trim()
            ?.replace(Regex("\\s+"), " ")
            ?.takeCodePoints(MAX_VOICE_NAME_CHARS)
            ?.takeIf { it.isNotBlank() }

        val next = _voiceAliases.value.toMutableMap()
        if (clean == null) next.remove(id) else next[id] = clean
        if (next == _voiceAliases.value) return

        _voiceAliases.value = next
        val json = JSONObject()
        next.forEach { (key, value) -> json.put(key, value) }
        prefs?.edit()?.putString(KEY_VOICE_ALIASES, json.toString())?.apply()
    }

    private fun parseVoiceAliases(raw: String?): Map<String, String> {
        if (raw.isNullOrBlank()) return emptyMap()
        return runCatching {
            val json = JSONObject(raw)
            buildMap {
                json.keys().forEach { key ->
                    val value = json.optString(key).trim()
                    if (value.isNotEmpty()) {
                        put(key, value.takeCodePoints(MAX_VOICE_NAME_CHARS))
                    }
                }
            }
        }.getOrDefault(emptyMap())
    }

    private fun String.takeCodePoints(max: Int): String {
        if (max <= 0) return ""
        if (codePointCount(0, length) <= max) return this
        return substring(0, offsetByCodePoints(0, max))
    }

    /** 通知等非 Compose 场景用它取当前语言。 */
    fun strings(): Strings = strings(_lang.value)

    fun strings(lang: Lang): Strings = if (lang == Lang.EN) En else Zh
}
