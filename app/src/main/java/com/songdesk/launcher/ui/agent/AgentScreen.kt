package com.songdesk.launcher.ui.agent

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.songdesk.launcher.car.CarBridge
import com.songdesk.launcher.commands.CommandDispatcher
import com.songdesk.launcher.ui.common.GlassCard
import com.songdesk.launcher.ui.theme.OkGreen
import com.songdesk.launcher.ui.theme.TextPrimary
import com.songdesk.launcher.ui.theme.TextSecondary
import com.songdesk.launcher.ui.theme.WarnAmber
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val SYSTEM_PROMPT = """你是「宋桌面」车机助理，运行在比亚迪宋PLUS DM-i 的车机桌面上。
回答必须简短口语化（行驶场景），一般不超过两句话。

你可以让车机执行以下指令：回复中把要执行的指令放在【指令:名称】标记里，用户端会解析执行。可用指令：
空调开 / 空调关 / 温度:N(18-28) / 风量:N(0-7) / 吹脸 / 吹脚 / 前除霜开 / 前除霜关 / 后除霜开 / 后除霜关 / 一键开窗 / 一键关窗 / 开天窗 / 关天窗 / 主驾加热:N(0-3) / 副驾加热:N(0-3) / 开WIFI / 关WIFI / 开蓝牙 / 关蓝牙
例如用户说"有点热"，你回复"好的，帮你调低一点温度。【指令:温度:22】"。

车辆当前状态（实时）：{CAR_STATE}"""

/** AI 助理聊天页（需要设置页填入 API Key；DeepSeek 免注册可用） */
@Composable
fun AgentScreen(bridge: CarBridge, apiKey: String, onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    val state by bridge.state.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    val history = remember { mutableStateListOf<Pair<String, String>>() } // role/content
    var input by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().padding(24.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(onClick = onBack, shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.surface) {
                Box(Modifier.size(52.dp), contentAlignment = Alignment.Center) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回", tint = TextPrimary)
                }
            }
            Spacer(Modifier.width(16.dp))
            Text("AI 助理", style = TextStyle(fontSize = 24.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Medium), color = TextPrimary)
            Spacer(Modifier.weight(1f))
            Text("DeepSeek", style = TextStyle(fontSize = 13.sp), color = TextSecondary)
        }
        Spacer(Modifier.height(16.dp))

        // 聊天记录
        Column(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (history.isEmpty() && !busy) {
                Text(
                    "试试对我说：「有点热」「帮我关窗」「空调调到 26 度」「附近充电方便吗」",
                    style = TextStyle(fontSize = 14.sp), color = TextSecondary,
                )
            }
            history.forEach { (role, content) ->
                val isUser = role == "user"
                Box(Modifier.fillMaxWidth(), contentAlignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart) {
                    Text(
                        content,
                        style = TextStyle(fontSize = 15.sp),
                        color = if (isUser) TextPrimary else OkGreen,
                        modifier = Modifier
                            .widthIn(max = 1100.dp)
                            .background(
                                color = if (isUser) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                else MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(16.dp),
                            )
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                    )
                }
            }
            if (busy) Text("…思考中", style = TextStyle(fontSize = 13.sp), color = TextSecondary)
        }

        Spacer(Modifier.height(12.dp))
        // 输入行
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                modifier = Modifier.weight(1f).height(56.dp),
                placeholder = { Text("输入指令或问题…", color = TextSecondary) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
            )
            Surface(
                onClick = {
                    val text = input.trim()
                    if (text.isBlank() || busy || apiKey.isBlank()) {
                        if (apiKey.isBlank()) {
                            history.add("assistant" to "请先到 设置 → AI 助理 填入 API Key")
                        }
                        return@Surface
                    }
                    input = ""
                    history.add("user" to text)
                    busy = true
                    scope.launch {
                        val system = SYSTEM_PROMPT.replace("{CAR_STATE}", state.toString())
                        val reply = withContext(Dispatchers.IO) {
                            com.songdesk.launcher.ai.LlmClient.chat(
                                "https://api.deepseek.com/v1/chat/completions",
                                apiKey, "deepseek-chat", system,
                                history.toList(),
                            ) ?: "网络异常，请稍后重试"
                        }
                        history.add("assistant" to reply)
                        Regex("【指令:([^】]+)】").findAll(reply).forEach { m ->
                            val res = CommandDispatcher.execute(context, bridge, m.groupValues[1])
                            if (res != null) history.add("sys" to "已执行：$res")
                        }
                        busy = false
                    }
                },
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, "发送", tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(14.dp))
            }
        }
        if (apiKey.isBlank()) {
            Spacer(Modifier.height(6.dp))
            Text("请先在设置页填入 API Key", style = TextStyle(fontSize = 12.sp), color = WarnAmber)
        }
    }
}
