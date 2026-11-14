package com.songdesk.launcher.ai

import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/** LLM 平台配置（对齐迪粉桌面的四平台，key 用户自填） */
object LlmPlatforms {
    data class Platform(val name: String, val baseUrl: String, val models: List<String>)

    val all = listOf(
        Platform("DeepSeek", "https://api.deepseek.com/v1/chat/completions", listOf("deepseek-chat", "deepseek-reasoner")),
        Platform("智谱GLM", "https://open.bigmodel.cn/api/paas/v4/chat/completions", listOf("glm-4-flash", "glm-4-plus", "glm-4-air")),
        Platform("通义千问", "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions", listOf("qwen-plus", "qwen-turbo", "qwen-max")),
    )
}

/**
 * 极简 OpenAI 兼容 chat/completions 客户端（同步，调用方在 IO 线程使用）。
 * messages: Pair(role, content)
 */
object LlmClient {

    fun chat(baseUrl: String, apiKey: String, model: String, system: String, history: List<Pair<String, String>>): String? {
        val body = JSONObject().apply {
            put("model", model)
            put("messages", JSONArray().apply {
                put(JSONObject().put("role", "system").put("content", system))
                history.forEach { (role, content) ->
                    put(JSONObject().put("role", role).put("content", content))
                }
            })
            put("temperature", 0.6)
            put("max_tokens", 500)
        }.toString()

        val conn = URL(baseUrl).openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.doOutput = true
        conn.connectTimeout = 10000
        conn.readTimeout = 30000
        conn.setRequestProperty("Content-Type", "application/json")
        conn.setRequestProperty("Authorization", "Bearer $apiKey")
        conn.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }

        val code = conn.responseCode
        val stream = if (code in 200..299) conn.inputStream else conn.errorStream
        val resp = stream?.bufferedReader()?.readText() ?: return null
        if (code !in 200..299) return "接口错误($code)：${resp.take(200)}"

        return runCatching {
            JSONObject(resp)
                .getJSONArray("choices").getJSONObject(0)
                .getJSONObject("message").getString("content")
        }.getOrElse { "响应解析失败：${resp.take(120)}" }
    }
}
