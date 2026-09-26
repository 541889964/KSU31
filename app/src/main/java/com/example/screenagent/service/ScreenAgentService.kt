package com.example.screenagent.service
import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import com.example.screenagent.agent.AgentLoop
import com.example.screenagent.llm.FakeLlmEngine
class ScreenAgentService : AccessibilityService() {
    lateinit var loop: AgentLoop; private set
    override fun onServiceConnected() {
        super.onServiceConnected()
        loop = AgentLoop(this, FakeLlmEngine().also { it.load() })
        instance = this
    }
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val e = event ?: return
        when (e.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED,
            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED,
            AccessibilityEvent.TYPE_VIEW_SCROLLED -> instance?.loop?.onScreenEvent()
        }
    }
    override fun onInterrupt() {}
    override fun onDestroy() { loop.stop(); instance = null; super.onDestroy() }
    companion object { @Volatile var instance: ScreenAgentService? = null }
}
