package com.example.screenagent
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.screenagent.agent.ScreenInfo
import com.example.screenagent.service.ScreenAgentService
class MainActivity : AppCompatActivity() {
    private lateinit var tvScreen: TextView
    private lateinit var tvState: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val l = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(48, 96, 48, 48) }
        tvScreen = TextView(this).apply { textSize = 14f; setPadding(0, 0, 0, 24) }
        val btnAcc = Button(this).apply { text = "1. 开启无障碍服务" }
        btnAcc.setOnClickListener { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }
        val input = EditText(this).apply { hint = "输入命令" }
        val btnRun = Button(this).apply { text = "普通模式" }
        btnRun.setOnClickListener {
            val svc = ScreenAgentService.instance ?: return@setOnClickListener toast("先开启无障碍")
            val g = input.text.toString().trim()
            if (g.isEmpty()) return@setOnClickListener toast("请输入命令")
            svc.loop.submitCommand(g)
        }
        val btnAuto = Button(this).apply { text = "自动模式" }
        btnAuto.setOnClickListener {
            val svc = ScreenAgentService.instance ?: return@setOnClickListener toast("先开启无障碍")
            svc.loop.startAuto(input.text.toString().trim().ifEmpty { "观察屏幕" })
        }
        val btnStop = Button(this).apply { text = "停止" }
        btnStop.setOnClickListener { ScreenAgentService.instance?.loop?.stop() }
        tvState = TextView(this).apply { textSize = 13f; setPadding(0, 32, 0, 0) }
        listOf(tvScreen, btnAcc, input, btnRun, btnAuto, btnStop, tvState).forEach { l.addView(it) }
        setContentView(l)
    }
    override fun onResume() {
        super.onResume()
        refresh()
        ScreenAgentService.instance?.loop?.onState = { tvState.text = it; refresh() }
    }
    override fun onPause() { super.onPause(); ScreenAgentService.instance?.loop?.onState = null }
    private fun refresh() {
        val i = ScreenInfo.get(this)
        tvScreen.text = "${i.width}x${i.height} rot=${i.rotation} d=${i.density} " + if (i.isLandscape) "[横]" else "[竖]"
    }
    private fun toast(m: String) = Toast.makeText(this, m, Toast.LENGTH_SHORT).show()
}
