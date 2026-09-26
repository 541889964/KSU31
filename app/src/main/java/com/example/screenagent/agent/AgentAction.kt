package com.example.screenagent.agent
import org.json.JSONObject
sealed class AgentAction {
    data class Click(val index: Int) : AgentAction()
    data class Input(val index: Int, val text: String) : AgentAction()
    data class Scroll(val index: Int, val direction: String) : AgentAction()
    data class Back(val dummy: Int = 0) : AgentAction()
    data class Home(val dummy: Int = 0) : AgentAction()
    data class Wait(val ms: Int) : AgentAction()
    data class Finish(val result: String) : AgentAction()
    companion object {
        fun parse(raw: String): AgentAction {
            val cleaned = raw.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
            val s = cleaned.indexOf('{'); val e = cleaned.lastIndexOf('}')
            if (s < 0 || e < s) throw IllegalArgumentException("no json")
            val o = JSONObject(cleaned.substring(s, e + 1))
            return when (o.getString("action")) {
                "click" -> Click(o.getInt("index"))
                "input" -> Input(o.getInt("index"), o.getString("text"))
                "scroll" -> Scroll(o.getInt("index"), o.optString("direction", "down"))
                "back" -> Back(); "home" -> Home()
                "wait" -> Wait(o.optInt("ms", 800))
                "finish" -> Finish(o.optString("result", ""))
                else -> Wait(500)
            }
        }
    }
}
