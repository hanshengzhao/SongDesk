package com.songdesk.launcher.ui.home

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Workspaces
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.songdesk.launcher.car.CarBridge
import com.songdesk.launcher.data.AppRepository
import com.songdesk.launcher.ui.common.GlassCard
import com.songdesk.launcher.ui.theme.TextPrimary
import com.songdesk.launcher.ui.theme.TextSecondary

private data class QuickTask(
    val icon: ImageVector,
    val title: String,
    val subtitle: String,
    val action: (Context, AppRepository, CarBridge) -> Unit,
)

private fun toast(c: Context, msg: String) = Toast.makeText(c, msg, Toast.LENGTH_SHORT).show()

/**
 * 导航到常用关键字（家/公司）：高德车机版广播 → 高德手机版深链 → 百度深链。
 * 广播协议来自对迪粉桌面 navigate 工具的逆向（KEY_TYPE=10036 关键字搜索）。
 */
private fun navigateTo(c: Context, r: AppRepository, dest: String) {
    if (r.firstInstalled("com.autonavi.amapauto") != null) {
        val i = Intent("AUTONAVI_STANDARD_BROADCAST_SEND").apply {
            setPackage("com.autonavi.amapauto")
            putExtra("KEY_TYPE", 10036)
            putExtra("KEY_KEYWORD", dest)
        }
        if (runCatching { c.sendBroadcast(i); true }.getOrDefault(false)) {
            toast(c, "正在规划去「$dest」的路线")
            return
        }
    }
    if (r.firstInstalled("com.autonavi.minimap") != null) {
        if (runCatching {
                c.startActivity(
                    Intent(Intent.ACTION_VIEW, android.net.Uri.parse("androidamap://route?sourceApplication=songdesk&dname=$dest&dev=0"))
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
                true
            }.getOrDefault(false)) {
            toast(c, "正在规划去「$dest」的路线")
            return
        }
    }
    if (r.firstInstalled("com.baidu.BaiduMap") != null) {
        if (runCatching {
                c.startActivity(
                    Intent(Intent.ACTION_VIEW, android.net.Uri.parse("baidumap://map/direction?destination=$dest"))
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
                true
            }.getOrDefault(false)) {
            toast(c, "正在规划去「$dest」的路线")
            return
        }
    }
    toast(c, "未找到可用的导航应用")
}

private val QUICK_TASKS = listOf(
    QuickTask(Icons.Filled.Home, "回家", "一键导航") { c, r, _ -> navigateTo(c, r, "家") },
    QuickTask(Icons.Filled.Business, "上班", "一键导航") { c, r, _ -> navigateTo(c, r, "公司") },
    QuickTask(Icons.Filled.AcUnit, "一键除霜", "前后除霜") { c, _, b ->
        b.setDefrost(true, true); toast(c, "已开启前后除霜")
    },
    QuickTask(Icons.Filled.Workspaces, "一键关窗", "全车车窗") { c, _, b ->
        b.closeAllWindows(); toast(c, "全车车窗已关闭")
    },
    QuickTask(Icons.Filled.Air, "开空调", "22°C 舒适") { c, _, b ->
        b.setAcOn(true); b.setAcTemp(22); toast(c, "空调已开启 22°C")
    },
)

/** 快捷任务卡片区：Hero 卡与应用网格之间的关键任务入口 */
@Composable
fun QuickTasks(
    bridge: CarBridge,
    repo: AppRepository,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        QUICK_TASKS.forEach { task ->
            GlassCard(
                modifier = Modifier
                    .weight(1f)
                    .clickable { task.action(context, repo, bridge) },
                contentFill = true,
            ) {
                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
                    ) {
                        Icon(
                            task.icon,
                            contentDescription = task.title,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .padding(10.dp)
                                .size(30.dp),
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
                Text(task.title, style = TextStyle(fontSize = 16.sp), color = TextPrimary)
                Text(task.subtitle, style = TextStyle(fontSize = 12.sp), color = TextSecondary)
            }
        }
    }
}
