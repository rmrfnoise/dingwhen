package com.dingwhen.app.speech

import android.content.Context
import java.io.File
import java.security.MessageDigest

/**
 * 朗读音频的本地存放。
 *
 * 必须用 filesDir 而不是 cacheDir：cacheDir 会被系统在存储紧张时清空，
 * 表现为"用了一阵子突然不响了"，而且完全没有提示。
 */
class AudioStore(private val context: Context) {

    private val dir: File
        get() = File(context.filesDir, "tts").apply { if (!exists()) mkdirs() }

    /** 内容 + 音色决定文件名，内容没变就复用，改一个字就换新文件 */
    fun keyOf(text: String, config: SpeechConfig): String {
        val raw = "$text\u0000${config.voiceKey()}"
        val digest = MessageDigest.getInstance("SHA-256").digest(raw.toByteArray(Charsets.UTF_8))
        // 必须显式取低 8 位：Byte 直接参与 %02x 在负数上会得到 ffffff 之类的长度不一致结果
        return digest.joinToString("") { "%02x".format(it.toInt() and 0xFF) }.take(24)
    }

    fun file(name: String?): File? {
        if (name.isNullOrBlank()) return null
        val target = File(dir, name)
        return if (target.exists() && target.length() > 0) target else null
    }

    fun write(name: String, bytes: ByteArray): File? = runCatching {
        val target = File(dir, name)
        val temp = File(dir, "$name.tmp")
        temp.writeBytes(bytes)
        if (target.exists()) target.delete()
        if (!temp.renameTo(target)) {
            target.writeBytes(bytes)
            temp.delete()
        }
        target
    }.getOrNull()

    fun delete(name: String?) {
        if (name.isNullOrBlank()) return
        runCatching { File(dir, name).delete() }
    }

    /** 清掉没有任何提醒引用的旧文件。改文案会不断产生孤儿文件。 */
    fun sweep(keep: Set<String>) {
        runCatching {
            dir.listFiles()?.forEach { file ->
                if (file.name !in keep) file.delete()
            }
        }
    }
}
