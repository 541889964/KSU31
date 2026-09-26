package com.example.screenagent.agent
import android.accessibilityservice.AccessibilityService
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.widget.Toast
import com.example.screenagent.llm.LlmEngine
import com.example.screenagent.llm.PromptBuilder
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong
class AgentLoop(private val service: AccessibilityService, private val llm: LlmEngine) {
    @Volatile var mode: AgentMode = AgentMode.MANUAL; private set
    @Volatile private var autoGoal: String? = null
    @Volatile private var screenDirty = true
    private val queue = LinkedBlockingQueue<String>()
    private val executor = ActionExecutor(service)
    private val history = mutableListOf<String>()
    private val lastAt = AtomicLong(0)
    @Volatile private var running = false
    private val ui = Handler(Looper.getMainLooper())
    @Volatile var onState: ((String) -> Unit)? = null
    fun submitCommand(goal: String) { queue.offer(goal); ensureWorker(); emit("命令: $goal") }
    fun startAuto(goal: String) { autoGoal = goal; mode = AgentMode.AUTO; screenDirty = true; ensureWorker(); emit("自动: $goal") }
    fun stop() { mode = AgentMode.MANUAL; autoGoal = null; queue.clear(); emit("停止") }
    fun onScreenEvent() { screenDirty = true }
    private fun ensureWorker() {
        if (running) return
        running = true
        Thread({ worker() }, "agent").apply { isDaemon = true }.start()
    }
    private fun worker() {
        try {
            while (running) {
                when (mode) {
                    AgentMode.MANUAL -> {
                        val g = queue.poll(300, TimeUnit.MILLISECONDS)
                        if (g != null) runEpisode(g, 15)
                    }
                    AgentMode.AUTO -> {
                        val g = autoGoal ?: run { Thread.sleep(300); null } ?: continue
                        val now = SystemClock.elapsedRealtime()
                        if (screenDirty && now - lastAt.get() >= 2500) {
                            screenDirty = false; runEpisode(g, 4)
                            lastAt.set(SystemClock.elapsedRealtime())
                        } else Thread.sleep(300)
                    }
                }
            }
        } catch (_: InterruptedException) {} finally { running = false }
    }
    private fun runEpisode(goal: String, maxSteps: Int) {
        history.clear(); var step = 0; var lastSig: String? = null; var repeat = 0
        while (running && step < maxSteps) {
            step++
            val root = service.rootInActiveWindow ?: run { Thread.sleep(500); null } ?: continue
            val snap = UiTreeExtractor.extractWithRefs(root)
            executor.updateNodes(snap.refs, snap.nodes)
            if (snap.nodes.isEmpty()) { Thread.sleep(500); continue }
            val screen = ScreenInfo.get(service)
            val prompt = PromptBuilder.build(goal, snap.nodes, history, screen)
            val action = try { AgentAction.parse(llm.generate(prompt)) }
                         catch (_: Exception) { Thread.sleep(300); continue }
            history.add(action.toString())
            if (action is AgentAction.Finish) { emit("完成"); return }
            val sig = action.toString()
            if (sig == lastSig) { repeat++; if (repeat >= 3) { emit("重复中止"); return } }
            else { lastSig = sig; repeat = 0 }
            executor.execute(action)
            Thread.sleep(if (action is AgentAction.Wait) 0 else 800)
        }
    }
    private fun emit(m: String) { ui.post { onState?.invoke(m) } }
}
