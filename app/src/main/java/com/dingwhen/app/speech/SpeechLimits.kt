package com.dingwhen.app.speech

/**
 * 语音链路的统一边界。UI、客户端、播放器都从这里取值，避免三处各写一个数字。
 *
 * 产品定位是「一句话提醒」，不是听书：300 字已经远超一条提醒的实际长度，
 * 再长应该拆成多条提醒，而不是在客户端做分段合成。
 */
object SpeechLimits {

    /** 单条播报文字最多 300 个 Unicode 码点（中文/英文/标点/emoji 都算 1 个）。 */
    const val MAX_TEXT_CHARS = 300

    /**
     * 服务端单次请求的 UTF-8 字节上限。
     *
     * 300 个汉字约 900 字节，正好在 1024 内；这个检查主要兜住 emoji 等 4 字节字符。
     * 超过时仍按「文字太长」处理，提示用户缩短。
     */
    const val MAX_TEXT_BYTES = 1024

    /** 单条音频的播放保护上限；只防卡死，不截断正常音频。 */
    const val MAX_PLAYBACK_MS = 11 * 60_000L

    /** 一条提醒重复念多遍时的总时长保护，避免前台服务待太久。 */
    const val MAX_TOTAL_PLAYBACK_MS = 15 * 60_000L

    fun charCount(text: String): Int = text.codePointCount(0, text.length)

    fun byteCount(text: String): Int = text.toByteArray(Charsets.UTF_8).size

    fun isWithinLimit(text: String): Boolean =
        charCount(text) <= MAX_TEXT_CHARS && byteCount(text) <= MAX_TEXT_BYTES
}
