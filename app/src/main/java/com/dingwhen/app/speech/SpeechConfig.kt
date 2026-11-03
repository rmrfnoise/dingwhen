package com.dingwhen.app.speech

/**
 * 语音播报配置。密钥只在本地保存，不进仓库——项目是公开仓库，写进代码等于公开泄露。
 *
 * 只支持豆包语音合成 2.0（seed-tts-2.0）。1.0 音色与旧版 V1 接口已移除：
 * V1 接口本身不支持 2.0 音色，留着只会构成一条永远走不通的分支。
 */
data class SpeechConfig(
    val apiKey: String = "",
    val voiceType: String = VoiceCatalog.DEFAULT_VOICE_ID,
    val emotion: String = "",
    val emotionEnabled: Boolean = false,
    /** 试听用的句子。留空则用示例句，让用户能听到自己每天会听到的那句话 */
    val testText: String = "",
    /** 关闭后不合成也不播报，退回"通知 + 振动" */
    val enabled: Boolean = true
) {
    /** 有密钥才走云端音色；没有就退到系统自带语音，保证"不填也能出声" */
    val configured: Boolean get() = apiKey.isNotBlank()

    /** 参与缓存键的音色指纹：任一变化都要重新合成 */
    fun voiceKey(): String =
        listOf(voiceType, if (emotionEnabled) emotion else "").joinToString("|")
}
