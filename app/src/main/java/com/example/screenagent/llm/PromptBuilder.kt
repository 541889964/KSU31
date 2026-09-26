package com.example.screenagent.llm
import com.example.screenagent.agent.ScreenInfo
import com.example.screenagent.agent.UiNode
object PromptBuilder {
    private val SYSTEM = """
你是 Android 自动化助手。只输出一个 JSON，不要解释。
动作：click/input/scroll/back/home/wait/finish
规则：index 必须存在，一次一个动作，完成用 finish。
""".trimIndent()
    fun build(goal: String, nodes: List<UiNode>, history: List<String>, screen: ScreenInfo.Info): String {
        val s = nodes.joinToString("\n") { it.toPromptLine() }
        val h = if (history.isEmpty()) "无" else history.takeLast(5).joinToString("\n")
        return "$SYSTEM\n\n【屏幕】${screen.toPromptLine()}\n【目标】$goal\n【历史】\n$h\n【元素】\n$s\n\nJSON："
    }
}
