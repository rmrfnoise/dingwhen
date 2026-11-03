package com.dingwhen.app.speech

import android.util.Base64
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

/**
 * 豆包（火山引擎）语音合成 2.0，新版控制台 API Key 鉴权。
 *
 * 接口：`POST /api/v3/tts/unidirectional`（Chunked，逐块返回纯 JSON）
 * 头部：X-Api-Key / X-Api-Resource-Id / X-Api-Request-Id
 *
 * 只有这一处联网，且只发生在"保存提醒 / 试听"时，触发时刻绝不调用——
 * 断网时提醒照样要能响，这是硬约束。
 */
sealed interface SynthResult {
    data class Ok(val audio: ByteArray) : SynthResult
    data class Err(val reason: String, val detail: String) : SynthResult
}

object DoubaoClient {

    private const val ENDPOINT = "https://openspeech.bytedance.com/api/v3/tts/unidirectional"

    /** 服务端限制较宽，这里按提醒文本的实际长度设一个保守上限 */
    private const val MAX_TEXT_BYTES = 1024

    /** 会话正常结束的状态码 */
    private const val DONE = 20_000_000

    const val ERR_NOT_CONFIGURED = "not_configured"
    const val ERR_EMPTY = "empty_text"
    const val ERR_TOO_LONG = "text_too_long"
    const val ERR_NETWORK = "network"
    const val ERR_AUTH = "auth"
    const val ERR_QUOTA = "quota"
    const val ERR_VOICE = "voice"
    const val ERR_TEXT = "bad_text"
    const val ERR_OTHER = "other"

    fun synthesize(config: SpeechConfig, text: String): SynthResult {
        if (!config.configured) return SynthResult.Err(ERR_NOT_CONFIGURED, "")
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return SynthResult.Err(ERR_EMPTY, "")
        if (trimmed.toByteArray(Charsets.UTF_8).size > MAX_TEXT_BYTES) {
            return SynthResult.Err(ERR_TOO_LONG, "${trimmed.toByteArray(Charsets.UTF_8).size}")
        }

        val payload = buildBody(config, trimmed)
        var conn: HttpURLConnection? = null
        return try {
            conn = (URL(ENDPOINT).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 10_000
                readTimeout = 30_000
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("X-Api-Key", config.apiKey)
                setRequestProperty("X-Api-Resource-Id", VoiceCatalog.RESOURCE_ID)
                // 必填，服务端用它做链路追踪；每次都必须新的
                setRequestProperty("X-Api-Request-Id", UUID.randomUUID().toString())
            }
            conn.outputStream.use { it.write(payload) }

            val status = conn.responseCode
            // 音频很短，整段读完再解析；流到 code=20000000 后服务端会关闭连接
            val body = (if (status in 200..299) conn.inputStream else conn.errorStream)
                ?.use { it.readBytes() }
                ?.toString(Charsets.UTF_8)
                .orEmpty()

            when {
                body.isBlank() -> SynthResult.Err(ERR_NETWORK, "http_$status")
                status !in 200..299 -> parseFailure(body, status)
                else -> parseStream(body)
            }
        } catch (e: Exception) {
            SynthResult.Err(ERR_NETWORK, e.javaClass.simpleName)
        } finally {
            runCatching { conn?.disconnect() }
        }
    }

    private fun buildBody(config: SpeechConfig, text: String): ByteArray {
        val body = JSONObject().apply {
            put("user", JSONObject().put("uid", "dingwhen"))
            put(
                "req_params", JSONObject().apply {
                    put("text", text)
                    put("speaker", config.voiceType)
                    put(
                        "audio_params", JSONObject().apply {
                            put("format", "mp3")
                            put("sample_rate", 24000)
                        }
                    )
                    // 2.0 没有独立的 emotion 字段，语气/情绪只能通过语音指令表达。
                    // 2.0 中文音色官方能力均标注「指令遵循」，所以这里不需要再判音色。
                    if (config.emotionEnabled && config.emotion.isNotBlank()) {
                        put("context_texts", JSONArray().put("请用${config.emotion}的语气朗读"))
                    }
                }
            )
        }
        return body.toString().toByteArray(Charsets.UTF_8)
    }

    /**
     * Chunked 响应是「多个 JSON 对象首尾相连」而不是数组、也没有分隔符，
     * 所以不能按行切，只能数大括号配对。字符串里的花括号要跳过，否则会被提前截断。
     */
    private fun splitJsonObjects(raw: String): List<String> {
        val out = ArrayList<String>()
        val sb = StringBuilder()
        var depth = 0
        var inString = false
        var escaped = false

        for (c in raw) {
            if (inString) {
                sb.append(c)
                when {
                    escaped -> escaped = false
                    c == '\\' -> escaped = true
                    c == '"' -> inString = false
                }
                continue
            }
            when (c) {
                '{' -> {
                    depth++
                    sb.append(c)
                }

                '}' -> {
                    depth--
                    sb.append(c)
                    if (depth == 0) {
                        out.add(sb.toString())
                        sb.setLength(0)
                    }
                }

                '"' -> {
                    inString = true
                    sb.append(c)
                }

                else -> if (depth > 0) sb.append(c)
            }
        }
        return out
    }

    private fun parseStream(raw: String): SynthResult {
        val chunks = splitJsonObjects(raw)
        if (chunks.isEmpty()) return SynthResult.Err(ERR_NETWORK, "bad_stream")

        val audio = ByteArrayOutputStream()
        for (chunk in chunks) {
            val json = runCatching { JSONObject(chunk) }.getOrNull() ?: continue
            val code = json.optInt("code", 0)

            if (code == DONE) break
            if (code != 0) {
                val message = json.optString("message")
                return SynthResult.Err(
                    reasonOf(code, message),
                    if (message.isBlank()) "code_$code" else message
                )
            }

            // 文本/时间戳块（sentence）的 data 是 null，跳过即可
            val data = json.optString("data")
            if (data.isBlank()) continue
            // 必须逐块解码再拼接：每块是独立 base64，拼成字符串再解会破坏块边界
            val bytes = runCatching { Base64.decode(data, Base64.DEFAULT) }.getOrNull()
                ?: continue
            audio.write(bytes)
        }

        if (audio.size() == 0) return SynthResult.Err(ERR_NETWORK, "empty_audio")
        return SynthResult.Ok(audio.toByteArray())
    }

    private fun parseFailure(raw: String, status: Int): SynthResult {
        val json = runCatching { JSONObject(raw) }.getOrNull()
        val code = json?.optInt("code", 0) ?: 0
        val message = json?.optString("message").orEmpty()

        if (status == 401 || status == 403) {
            return SynthResult.Err(ERR_AUTH, message.ifBlank { "http_$status" })
        }
        if (code != 0) {
            return SynthResult.Err(reasonOf(code, message), message.ifBlank { "code_$code" })
        }
        return SynthResult.Err(ERR_NETWORK, message.ifBlank { "http_$status" })
    }

    /**
     * 错误码。注意 45000000 的 message 里带 "permission denied"，
     * 必须排在权限判断之前，否则会被误判成密钥错误——而它的真实含义是"这个音色没开通"。
     */
    private fun reasonOf(code: Int, message: String): String {
        val lower = message.lowercase()
        return when {
            code == 40402003 -> ERR_TOO_LONG
            code == 45000000 -> ERR_VOICE
            code == 401 || code == 403 -> ERR_AUTH
            // 服务端偶发错误与并发限流都属于"过一会儿再试可能就好了"
            code == 55000000 -> ERR_NETWORK
            lower.contains("concurrency") -> ERR_NETWORK
            lower.contains("quota") -> ERR_QUOTA
            lower.contains("authenticate") || lower.contains("api key") -> ERR_AUTH
            lower.contains("permission") -> ERR_VOICE
            else -> ERR_OTHER
        }
    }
}
