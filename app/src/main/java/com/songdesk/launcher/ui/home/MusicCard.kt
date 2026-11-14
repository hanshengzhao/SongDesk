package com.songdesk.launcher.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.songdesk.launcher.media.NowPlayingStore
import com.songdesk.launcher.ui.common.GlassCard
import com.songdesk.launcher.ui.theme.TextPrimary
import com.songdesk.launcher.ui.theme.TextSecondary

/** 音乐卡：正在播放信息 + 播控；无媒体会话时显示快捷启动音乐 App */
@Composable
fun MusicCard(
    modifier: Modifier = Modifier,
    onLaunchMusic: () -> Unit,
) {
    val np by NowPlayingStore.state.collectAsState()
    val context = LocalContext.current

    GlassCard(modifier, contentFill = true) {
        if (np.title.isBlank()) {
            Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.MusicNote,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(34.dp),
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("音乐", style = TextStyle(fontSize = 17.sp), color = TextPrimary)
                    Text(
                        "点击启动音乐应用 · 播放时这里显示歌曲",
                        style = TextStyle(fontSize = 12.sp),
                        color = TextSecondary,
                    )
                }
                Spacer(Modifier.weight(1f))
                CtrlButton(Icons.Filled.PlayArrow, "播放音乐") {
                    if (!NowPlayingStore.transport(
                            { NowPlayingStore.activeController?.transportControls?.pause() },
                            { NowPlayingStore.activeController?.transportControls?.play() },
                        )
                    ) onLaunchMusic()
                }
            }
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        np.title,
                        style = TextStyle(fontSize = 18.sp),
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        np.artist,
                        style = TextStyle(fontSize = 13.sp),
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Spacer(Modifier.width(6.dp))
                CtrlButton(Icons.Filled.SkipPrevious, "上一首") {
                    NowPlayingStore.activeController?.transportControls?.skipToPrevious()
                }
                Spacer(Modifier.width(6.dp))
                CtrlButton(
                    if (np.playing) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    if (np.playing) "暂停" else "播放",
                ) {
                    if (np.playing) NowPlayingStore.activeController?.transportControls?.pause()
                    else NowPlayingStore.activeController?.transportControls?.play()
                }
                Spacer(Modifier.width(6.dp))
                CtrlButton(Icons.Filled.SkipNext, "下一首") {
                    NowPlayingStore.activeController?.transportControls?.skipToNext()
                }
            }
        }
    }
}

@Composable
private fun CtrlButton(icon: ImageVector, desc: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
    ) {
        Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = desc, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
        }
    }
}
