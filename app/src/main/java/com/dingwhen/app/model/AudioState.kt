package com.dingwhen.app.model

/**
 * 朗读音频的合成状态。
 * PENDING 表示等待重试（多为网络问题），冷启动时会再试一次；
 * FAILED 表示参数类错误（密钥、欠费、音色），重试没有意义，需要用户处理。
 */
enum class AudioState {
    NONE,
    PENDING,
    READY,
    FAILED;

    companion object {
        fun fromName(name: String?): AudioState = entries.firstOrNull { it.name == name } ?: NONE
    }
}
