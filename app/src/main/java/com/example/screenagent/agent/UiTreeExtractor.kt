package com.example.screenagent.agent
import android.graphics.Rect
import android.view.accessibility.AccessibilityNodeInfo
data class UiSnapshot(val nodes: List<UiNode>, val refs: List<AccessibilityNodeInfo>)
object UiTreeExtractor {
    private const val MAX_NODES = 80
    fun extractWithRefs(root: AccessibilityNodeInfo?): UiSnapshot {
        val nodes = ArrayList<UiNode>(MAX_NODES)
        val refs = ArrayList<AccessibilityNodeInfo>(MAX_NODES)
        if (root == null) return UiSnapshot(nodes, refs)
        walk(root, nodes, refs, intArrayOf(0))
        return UiSnapshot(nodes, refs)
    }
    private fun walk(n: AccessibilityNodeInfo?, nodes: MutableList<UiNode>, refs: MutableList<AccessibilityNodeInfo>, c: IntArray) {
        if (n == null || c[0] >= MAX_NODES) return
        if (isMeaningful(n)) {
            val r = Rect(); n.getBoundsInScreen(r)
            if (r.width() > 0 && r.height() > 0 && r.top < 5000) {
                val idx = c[0]++
                nodes.add(UiNode(idx, n.className?.toString() ?: "", n.text?.toString(),
                    n.contentDescription?.toString(), n.viewIdResourceName, r,
                    n.isClickable, n.isEditable, n.isScrollable, n.isCheckable, n.isChecked))
                refs.add(n)
            }
        }
        for (i in 0 until n.childCount) walk(n.getChild(i), nodes, refs, c)
    }
    private fun isMeaningful(n: AccessibilityNodeInfo): Boolean {
        if (n.isClickable || n.isEditable || n.isScrollable) return true
        return !n.text?.toString().isNullOrBlank() || !n.contentDescription?.toString().isNullOrBlank()
    }
}
