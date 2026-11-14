package com.songdesk.launcher.data

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.graphics.drawable.toBitmap

data class AppEntry(
    val packageName: String,
    val label: String,
    val icon: Bitmap?,
)

/** 应用枚举 + 外部应用降级启动链 */
class AppRepository(private val context: Context) {

    fun loadLaunchableApps(): List<AppEntry> {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        return pm.queryIntentActivities(intent, 0).mapNotNull { ri ->
            val pkg = ri.activityInfo?.packageName ?: return@mapNotNull null
            if (pkg == context.packageName) return@mapNotNull null
            val label = runCatching { ri.loadLabel(pm).toString() }.getOrDefault(pkg)
            val icon = runCatching { pm.getApplicationIcon(pkg).toBitmap() }.getOrNull()
            AppEntry(pkg, label, icon)
        }.distinctBy { it.packageName }.sortedBy { it.label.lowercase() }
    }

    /** 候选包名里找第一个已安装的（仿迪粉桌面 LauncherController 的降级链） */
    fun firstInstalled(vararg candidates: String): String? =
        candidates.firstOrNull { pkg ->
            runCatching { context.packageManager.getPackageInfo(pkg, 0) }.isSuccess
        }

    fun launchPackage(packageName: String): Boolean {
        val pm = context.packageManager
        val intent = pm.getLaunchIntentForPackage(packageName)
            ?: Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
        return runCatching {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            true
        }.getOrDefault(false)
    }

    fun launchFirst(vararg candidates: String): Boolean {
        val pkg = firstInstalled(*candidates) ?: return false
        return launchPackage(pkg)
    }
}
