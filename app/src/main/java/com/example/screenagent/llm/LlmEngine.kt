package com.example.screenagent.llm
interface LlmEngine {
    fun load()
    fun generate(prompt: String, maxTokens: Int = 256): String
    fun release()
}
