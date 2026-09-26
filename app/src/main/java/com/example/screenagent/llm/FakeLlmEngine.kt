package com.example.screenagent.llm
class FakeLlmEngine : LlmEngine {
    override fun load() {}
    override fun release() {}
    override fun generate(prompt: String, maxTokens: Int) = """{"action":"wait","ms":500}"""
}
