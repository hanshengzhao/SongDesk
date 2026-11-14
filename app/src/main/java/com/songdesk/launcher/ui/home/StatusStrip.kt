package com.songdesk.launcher.ui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.SignalCellular4Bar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.songdesk.launcher.data.WeatherData
import com.songdesk.launcher.ui.common.GlassRow
import com.songdesk.launcher.ui.common.rememberClock
import com.songdesk.launcher.ui.theme.TextPrimary
import com.songdesk.launcher.ui.theme.TextSecondary

/** 顶部状态条：时间+日期 ｜ 实时天气 ｜ 语音指令 ｜ 蓝牙/信号 */
@Composable
fun StatusStrip(
    weather: WeatherData?,
    modifier: Modifier = Modifier,
    onVoiceInput: (String) -> Unit = {},
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    GlassRow(modifier = modifier.fillMaxWidth()) {
        Column {
            Text(
                rememberClock("HH:mm"),
                style = TextStyle(fontSize = 22.sp, letterSpacing = 1.sp),
                color = TextPrimary,
            )
            Text(
                rememberClock("MM月dd日 EEE"),
                style = TextStyle(fontSize = 12.sp),
                color = TextSecondary,
            )
        }
        Spacer(Modifier.width(16.dp))
        WeatherChip(weather)
        Spacer(Modifier.weight(1f))
        // 语音指令输入
        Icon(
            Icons.Filled.Mic,
            contentDescription = "语音指令",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .size(22.dp)
                .clickable {
                    runCatching {
                        val sr = android.speech.SpeechRecognizer.createSpeechRecognizer(context)
                        sr.setRecognitionListener(object : android.speech.RecognitionListener {
                            override fun onResults(results: android.os.Bundle) {
                                val text = results
                                    .getStringArrayList(android.speech.SpeechRecognizer.RESULTS_RECOGNITION)
                                    ?.firstOrNull()
                                if (!text.isNullOrBlank()) onVoiceInput(text)
                                sr.destroy()
                            }
                            override fun onError(error: Int) { sr.destroy() }
                            override fun onReadyForSpeech(params: android.os.Bundle?) {}
                            override fun onBeginningOfSpeech() {}
                            override fun onRmsChanged(rmsdB: Float) {}
                            override fun onBufferReceived(buffer: ByteArray?) {}
                            override fun onEndOfSpeech() {}
                            override fun onPartialResults(partialResults: android.os.Bundle?) {}
                            override fun onEvent(eventType: Int, params: android.os.Bundle?) {}
                        })
                        sr.startListening(
                            android.content.Intent(android.speech.RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                                .putExtra(
                                    android.speech.RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                                    android.speech.RecognizerIntent.LANGUAGE_MODEL_FREE_FORM,
                                )
                        )
                    }
                },
        )
        Spacer(Modifier.width(12.dp))
        Icon(
            Icons.Filled.Bluetooth,
            contentDescription = "蓝牙",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp),
        )
        Spacer(Modifier.width(12.dp))
        Icon(
            Icons.Filled.SignalCellular4Bar,
            contentDescription = "信号",
            tint = TextSecondary,
            modifier = Modifier.size(20.dp),
        )
    }
}

/** 实时天气（MSN/_open-meteo）；加载中显示占位，有预警加 ⚠ */
@Composable
private fun WeatherChip(weather: WeatherData?) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(6.dp),
        modifier = Modifier.padding(vertical = 4.dp),
    ) {
        if (weather == null) {
            Text("天气 --", style = TextStyle(fontSize = 14.sp), color = TextSecondary)
        } else {
            Text(
                "${weather.emoji} ${weather.condition} ${weather.minC}~${weather.maxC}° · ${weather.tempC}°",
                style = TextStyle(fontSize = 14.sp),
                color = TextPrimary,
            )
            if (!weather.alertTitle.isNullOrBlank()) {
                Text("⚠ ${weather.alertTitle}", style = TextStyle(fontSize = 13.sp), color = com.songdesk.launcher.ui.theme.WarnAmber)
            }
        }
    }
}
