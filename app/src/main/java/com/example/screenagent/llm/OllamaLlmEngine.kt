package com.example.screenagent.llm

import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class OllamaLlmEngine(
    private val serverUrl: String = "http://192.168.2.103:11434",
    private val model: String = "qwen2.5:0.5b"
) : LlmEngine {

    override fun load() {}
    override fun release() {}

    override fun generate(prompt: String, maxTokens: Int): String {
        return try {
            val url = URL("$serverUrl/api/generate")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doOutput = true
                connectTimeout = 15_000
                readTimeout = 180_000
                setRequestProperty("Content-Type", "application/json")
            }
            val body = JSONObject().apply {
                put("model", model)
                put("prompt", prompt)
                put("stream", false)
                put("options", JSONObject().apply {
                    put("num_predict", maxTokens)
                    put("temperature", 0.1)
                    put("top_p", 0.9)
                })
            }
            OutputStreamWriter(conn.outputStream).use { it.write(body.toString()) }
            val resp = conn.inputStream.bufferedReader().use { it.readText() }
            conn.disconnect()
            JSONObject(resp).optString("response", "{\"action\":\"wait\",\"ms\":500}")
        } catch (e: Exception) {
            "{\"action\":\"wait\",\"ms\":500}"
        }
    }
}
