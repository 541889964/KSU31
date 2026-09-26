package com.example.screenagent.agent
import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.os.Bundle
import android.view.accessibility.AccessibilityNodeInfo
class ActionExecutor(private val service: AccessibilityService) {
    private var lastNodes: List<UiNode> = emptyList()
    private var lastRefs: List<AccessibilityNodeInfo> = emptyList()
    fun updateNodes(refs: List<AccessibilityNodeInfo>, nodes: List<UiNode>) {
        lastRefs = refs; lastNodes = nodes
    }
    fun execute(action: AgentAction): Boolean = when (action) {
        is AgentAction.Click -> click(action.index)
        is AgentAction.Input -> input(action.index, action.text)
        is AgentAction.Scroll -> scroll(action.index, action.direction)
        is AgentAction.Back -> service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK)
        is AgentAction.Home -> service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME)
        is AgentAction.Wait -> { Thread.sleep(action.ms.toLong()); true }
        is AgentAction.Finish -> true
    }
    private fun click(index: Int): Boolean {
        val node = lastNodes.getOrNull(index) ?: return false
        val ref = lastRefs.getOrNull(index)
        if (ref != null && ref.isClickable) {
            if (ref.performAction(AccessibilityNodeInfo.ACTION_CLICK)) return true
            var p = ref.parent
            while (p != null) {
                if (p.isClickable && p.performAction(AccessibilityNodeInfo.ACTION_CLICK)) return true
                p = p.parent
            }
        }
        return gestureClick(node.bounds.centerX().toFloat(), node.bounds.centerY().toFloat())
    }
    private fun gestureClick(x: Float, y: Float): Boolean {
        val path = Path().apply { moveTo(x, y) }
        val g = GestureDescription.Builder().addStroke(GestureDescription.StrokeDescription(path, 0, 60)).build()
        return service.dispatchGesture(g, null, null)
    }
    private fun input(index: Int, text: String): Boolean {
        val ref = lastRefs.getOrNull(index) ?: return false
        ref.performAction(AccessibilityNodeInfo.ACTION_FOCUS)
        val args = Bundle().apply { putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text) }
        return ref.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)
    }
    private fun scroll(index: Int, direction: String): Boolean {
        val ref = lastRefs.getOrNull(index) ?: return false
        val d = if (direction == "down") AccessibilityNodeInfo.ACTION_SCROLL_FORWARD
                else AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD
        return ref.performAction(d)
    }
}
