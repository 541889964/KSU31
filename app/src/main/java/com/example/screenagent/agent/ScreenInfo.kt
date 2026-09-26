package com.example.screenagent.agent
import android.content.Context
import android.graphics.Point
import android.os.Build
import android.view.WindowManager
object ScreenInfo {
    data class Info(val width: Int, val height: Int, val rotation: Int, val density: Float) {
        val isLandscape: Boolean get() = width > height
        fun toPromptLine() = "屏幕: ${width}x${height}, 旋转=$rotation, density=$density, " + (if (isLandscape) "横屏" else "竖屏")
    }
    fun get(ctx: Context): Info {
        val wm = ctx.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val dm = ctx.resources.displayMetrics
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val b = wm.currentWindowMetrics.bounds
            Info(b.width(), b.height(), ctx.display?.rotation ?: 0, dm.density)
        } else {
            val p = Point()
            @Suppress("DEPRECATION") wm.defaultDisplay.getRealSize(p)
            @Suppress("DEPRECATION") Info(p.x, p.y, wm.defaultDisplay.rotation, dm.density)
        }
    }
}
