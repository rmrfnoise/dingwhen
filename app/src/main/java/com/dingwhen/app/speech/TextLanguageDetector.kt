package com.dingwhen.app.speech

/** 一段文字对语言能力的要求。NEUTRAL 表示只有数字/标点/符号，任何音色都能试。 */
enum class TextLanguage { ZH, EN, MIXED, NEUTRAL }

/**
 * 轻量文字语种判断：只区分中文、英文、中英混合和中性。
 *
 * 中文按 Unicode HAN script 判断，英文只认 ASCII 字母；这样中文里的数字、
 * 标点、emoji 不会被误判成英文。只服务中文/英文界面，不处理日语等其他语种。
 */
object TextLanguageDetector {

    fun detect(text: String): TextLanguage {
        var zh = false
        var en = false
        var i = 0
        while (i < text.length) {
            val cp = text.codePointAt(i)
            if (Character.UnicodeScript.of(cp) == Character.UnicodeScript.HAN) zh = true
            if (cp in 'a'.code..'z'.code || cp in 'A'.code..'Z'.code) en = true
            if (zh && en) return TextLanguage.MIXED
            i += Character.charCount(cp)
        }
        return when {
            zh -> TextLanguage.ZH
            en -> TextLanguage.EN
            else -> TextLanguage.NEUTRAL
        }
    }
}

/** 音色是否具备这段文字要求的语言能力。混合文字要求同时支持中文和英文。 */
fun VoiceOption.supports(language: TextLanguage): Boolean = when (language) {
    TextLanguage.ZH -> supportsChinese
    TextLanguage.EN -> supportsEnglish
    TextLanguage.MIXED -> supportsChinese && supportsEnglish
    TextLanguage.NEUTRAL -> true
}
