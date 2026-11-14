package com.songdesk.launcher.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.songdesk.launcher.ui.common.GlassRow
import com.songdesk.launcher.ui.theme.TextPrimary
import com.songdesk.launcher.ui.theme.TextSecondary

enum class DockAction { MAPS, MUSIC, PHONE, APP_CENTER, CAR, SETTINGS }

/** 底部 Dock：地图 / 音乐 / 电话 / 应用中心 / 设置（对齐新设计稿） */
@Composable
fun Dock(
    modifier: Modifier = Modifier,
    onAction: (DockAction) -> Unit,
) {
    Box(modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        GlassRow(Modifier.width(760.dp)) {
            DockItem(Icons.Filled.Map, "地图") { onAction(DockAction.MAPS) }
            DockItem(Icons.Filled.MusicNote, "音乐") { onAction(DockAction.MUSIC) }
            DockItem(Icons.Filled.Phone, "电话") { onAction(DockAction.PHONE) }
            DockItem(Icons.Filled.Apps, "应用中心") { onAction(DockAction.APP_CENTER) }
            DockItem(Icons.Filled.Settings, "设置") { onAction(DockAction.SETTINGS) }
        }
    }
}

@Composable
private fun RowScope.DockItem(icon: ImageVector, label: String, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .weight(1f)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
    ) {
        Icon(icon, contentDescription = label, tint = TextPrimary, modifier = Modifier.size(24.dp))
        Spacer(Modifier.height(4.dp))
        Text(label, fontSize = 12.sp, color = TextSecondary)
    }
}
