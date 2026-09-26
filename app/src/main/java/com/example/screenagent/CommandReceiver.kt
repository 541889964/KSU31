package com.example.screenagent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.screenagent.service.ScreenAgentService
class CommandReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val svc = ScreenAgentService.instance ?: return
        when (intent.action) {
            ACTION_COMMAND -> {
                val g = intent.getStringExtra(EXTRA_GOAL) ?: return
                svc.loop.stop(); svc.loop.submitCommand(g)
            }
            ACTION_AUTO_START -> svc.loop.startAuto(intent.getStringExtra(EXTRA_GOAL) ?: "观察屏幕")
            ACTION_STOP -> svc.loop.stop()
        }
    }
    companion object {
        const val ACTION_COMMAND = "com.example.screenagent.COMMAND"
        const val ACTION_AUTO_START = "com.example.screenagent.AUTO_START"
        const val ACTION_STOP = "com.example.screenagent.STOP"
        const val EXTRA_GOAL = "goal"
    }
}
