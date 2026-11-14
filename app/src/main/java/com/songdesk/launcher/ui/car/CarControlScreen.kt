package com.songdesk.launcher.ui.car

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.songdesk.launcher.car.CarBridge
import com.songdesk.launcher.car.SeatId
import com.songdesk.launcher.car.WindowId
import com.songdesk.launcher.commands.CommandDispatcher
import com.songdesk.launcher.commands.SmartScenes
import com.songdesk.launcher.ui.common.GlassCard
import com.songdesk.launcher.ui.common.GlassChip
import com.songdesk.launcher.ui.common.SectionLabel
import com.songdesk.launcher.ui.theme.BigValue
import com.songdesk.launcher.ui.theme.TextPrimary
import com.songdesk.launcher.ui.theme.TextSecondary

/** 车控面板：空调（温度/风量/模式/除霜）+ 车窗天窗 + 座椅加热 + 智能场景 + 指令面板 */
@Composable
fun CarControlScreen(bridge: CarBridge, onBack: () -> Unit) {
    val state by bridge.state.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    var panelOpen by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }

    Column(Modifier.fillMaxSize().padding(24.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BackButton(onBack)
            Spacer(Modifier.size(16.dp))
            Text("车控", style = TextStyle(fontSize = 24.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Medium), color = TextPrimary)
            if (state.charging) {
                Spacer(Modifier.size(10.dp))
                Text("⚡充电中", style = TextStyle(fontSize = 13.sp), color = com.songdesk.launcher.ui.theme.OkGreen)
            }
            Spacer(Modifier.weight(1f))
            GlassChip(
                text = "指令面板",
                selected = false,
            ) { panelOpen = true }
            Spacer(Modifier.size(10.dp))
            GlassChip(
                text = if (state.acOn) "空调 开" else "空调 关",
                selected = state.acOn,
            ) { bridge.setAcOn(!state.acOn) }
        }
        Spacer(Modifier.height(16.dp))

        // ---- 智能场景行 ----
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SectionLabel("智能场景", Modifier.align(Alignment.CenterVertically))
            SmartScenes.all.forEach { scene ->
                GlassChip(scene.name, selected = false) {
                    val results = SmartScenes.run(context, bridge, scene)
                    android.widget.Toast.makeText(
                        context, "${scene.name}：${results.size} 步已执行", android.widget.Toast.LENGTH_SHORT,
                    ).show()
                }
            }
        }
        Spacer(Modifier.height(14.dp))

        if (panelOpen) {
            CommandPanelDialog(bridge, onDismiss = { panelOpen = false })
        }

        Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            // ---- 左：空调 ----
            GlassCard(Modifier.weight(1.1f), contentFill = true) {
                SectionLabel("空调")
                Spacer(Modifier.weight(0.8f))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RoundButton(Icons.Filled.Remove, "降温") { bridge.setAcTemp(state.tempC - 1) }
                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text("${state.tempC}", style = BigValue, color = MaterialTheme.colorScheme.primary)
                            Text("°C", style = TextStyle(fontSize = 24.sp), color = TextSecondary)
                        }
                    }
                    RoundButton(Icons.Filled.Add, "升温") { bridge.setAcTemp(state.tempC + 1) }
                }
                Spacer(Modifier.weight(0.8f))
                SectionLabel("风量  ${state.fanLevel}/7")
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    (0..7).forEach { lv ->
                        FanLevelDot(
                            level = lv,
                            selected = lv == state.fanLevel,
                            enabled = state.acOn,
                            onClick = { bridge.setFanLevel(lv) },
                        )
                    }
                }
                Spacer(Modifier.weight(0.8f))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    GlassChip("吹脸", state.blowFace) { bridge.setBlowZone(!state.blowFace, state.blowFoot) }
                    GlassChip("吹脚", state.blowFoot) { bridge.setBlowZone(state.blowFace, !state.blowFoot) }
                    GlassChip("前除霜", state.defrostFront) { bridge.setDefrost(!state.defrostFront, state.defrostRear) }
                    GlassChip("后除霜", state.defrostRear) { bridge.setDefrost(state.defrostFront, !state.defrostRear) }
                }
            }

            // ---- 右列：车窗 / 座椅 ----
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                GlassCard(Modifier.weight(1f), contentFill = true) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SectionLabel("车窗 / 天窗")
                        Spacer(Modifier.weight(1f))
                        GlassChip("一键关窗", selected = false) { bridge.closeAllWindows() }
                    }
                    Spacer(Modifier.weight(0.5f))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        GlassChip(
                            "左前 ${if (state.windowOpen[WindowId.FL] == true) "开" else "关"}",
                            state.windowOpen[WindowId.FL] == true,
                        ) { bridge.setWindowOpen(WindowId.FL, state.windowOpen[WindowId.FL] != true) }
                        GlassChip(
                            "右前 ${if (state.windowOpen[WindowId.FR] == true) "开" else "关"}",
                            state.windowOpen[WindowId.FR] == true,
                        ) { bridge.setWindowOpen(WindowId.FR, state.windowOpen[WindowId.FR] != true) }
                        GlassChip(
                            "左后 ${if (state.windowOpen[WindowId.RL] == true) "开" else "关"}",
                            state.windowOpen[WindowId.RL] == true,
                        ) { bridge.setWindowOpen(WindowId.RL, state.windowOpen[WindowId.RL] != true) }
                    }
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        GlassChip(
                            "右后 ${if (state.windowOpen[WindowId.RR] == true) "开" else "关"}",
                            state.windowOpen[WindowId.RR] == true,
                        ) { bridge.setWindowOpen(WindowId.RR, state.windowOpen[WindowId.RR] != true) }
                        GlassChip(
                            "天窗 ${if (state.windowOpen[WindowId.SUNROOF] == true) "开" else "关"}",
                            state.windowOpen[WindowId.SUNROOF] == true,
                        ) { bridge.setWindowOpen(WindowId.SUNROOF, state.windowOpen[WindowId.SUNROOF] != true) }
                    }
                    Spacer(Modifier.weight(0.5f))
                }

                GlassCard(Modifier.weight(0.55f), contentFill = true) {
                    SectionLabel("座椅加热")
                    Spacer(Modifier.weight(1f))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("主驾", style = TextStyle(fontSize = 14.sp), color = TextSecondary)
                        SeatHeatPicker(state.driverSeatHeat) { bridge.setSeatHeat(SeatId.DRIVER, it) }
                        Spacer(Modifier.weight(0.5f))
                        Text("副驾", style = TextStyle(fontSize = 14.sp), color = TextSecondary)
                        SeatHeatPicker(state.passengerSeatHeat) { bridge.setSeatHeat(SeatId.PASSENGER, it) }
                    }
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun BackButton(onBack: () -> Unit) {
    Surface(
        onClick = onBack,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface,
    ) {
        Box(Modifier.size(52.dp), contentAlignment = Alignment.Center) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "返回",
                tint = TextPrimary,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

@Composable
private fun RoundButton(icon: ImageVector, desc: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
    ) {
        Box(Modifier.size(76.dp), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = desc, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(30.dp))
        }
    }
}

@Composable
private fun FanLevelDot(level: Int, selected: Boolean, enabled: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        color = when {
            selected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.30f)
            else -> MaterialTheme.colorScheme.surface
        },
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
        ),
        modifier = Modifier.size(44.dp),
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Icon(
                Icons.Filled.Air,
                contentDescription = "风量$level",
                tint = if (selected) MaterialTheme.colorScheme.primary else TextSecondary,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun SeatHeatPicker(level: Int, onPick: (Int) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        (0..3).forEach { lv ->
            GlassChip(
                text = if (lv == 0) "关" else "$lv",
                selected = level == lv,
            ) { onPick(lv) }
        }
    }
}

/** 指令面板：全部小迪指令按分类列出，点击即执行（参数指令用默认值） */
@Composable
private fun CommandPanelDialog(bridge: CarBridge, onDismiss: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var lastResult by androidx.compose.runtime.remember {
        androidx.compose.runtime.mutableStateOf<String?>(null)
    }
    val catalog = androidx.compose.runtime.remember { CommandDispatcher.catalog(bridge) }

    // 参数指令的演示默认值（面板点击用；文本输入执行走 Dispatcher）
    val defaultParam = mapOf("温度" to 24, "风量" to 3, "音量" to 40, "亮度" to 70, "主驾加热" to 2, "副驾加热" to 2)

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color(0xF60E1420),
            border = androidx.compose.foundation.BorderStroke(1.dp, com.songdesk.launcher.ui.theme.GlassStroke),
        ) {
            Column(
                Modifier
                    .width(720.dp)
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "指令面板",
                        style = TextStyle(fontSize = 22.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Medium),
                        color = TextPrimary,
                    )
                    Spacer(Modifier.weight(1f))
                    GlassChip("关闭", selected = false) { onDismiss() }
                }
                if (lastResult != null) {
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "→ $lastResult",
                        style = TextStyle(fontSize = 14.sp),
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                Spacer(Modifier.height(14.dp))
                catalog.groupBy { it.category }.forEach { (category, cmds) ->
                    SectionLabel(category)
                    Spacer(Modifier.height(8.dp))
                    cmds.chunked(4).forEach { rowCmds ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            rowCmds.forEach { c ->
                                GlassChip(
                                    text = c.name.removeSuffix(":N"),
                                    selected = false,
                                    modifier = Modifier.weight(1f),
                                ) {
                                    val paramCmd = if (":N" in c.name) {
                                        val base = c.name.removeSuffix(":N")
                                        "${base}:${defaultParam[base] ?: 1}"
                                    } else c.name
                                    lastResult = CommandDispatcher.execute(context, bridge, paramCmd)
                                        ?: "已发送：$paramCmd"
                                }
                            }
                            repeat(4 - rowCmds.size) { Spacer(Modifier.weight(1f)) }
                        }
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}
