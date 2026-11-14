package com.songdesk.launcher.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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

enum class DockAction { MAPS, MUSIC, PHONE, CAR, SETTINGS }

/** 底部 Dock：地图/音乐/电话 = 外部应用降级链；车控/设置 = 内部页面 */
@Composable
fun Dock(
    active: DockAction?,
    modifier: Modifier = Modifier,
    onAction: (DockAction) -> Unit,
) {
    Box(modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        GlassRow(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            DockItem(Icons.Filled.Map, "地图", active == DockAction.MAPS) { onAction(DockAction.MAPS) }
            DockItem(Icons.Filled.MusicNote, "音乐", active == DockAction.MUSIC) { onAction(DockAction.MUSIC) }
            DockItem(Icons.Filled.Phone, "电话", active == DockAction.PHONE) { onAction(DockAction.PHONE) }
            DockItem(Icons.Filled.DirectionsCar, "车控", active == DockAction.CAR) { onAction(DockAction.CAR) }
            DockItem(Icons.Filled.Settings, "设置", active == DockAction.SETTINGS) { onAction(DockAction.SETTINGS) }
        }
    }
}

@Composable
private fun RowScope.DockItem(
    icon: ImageVector,
    label: String,
    active: Boolean,
    onClick: () -> Unit,
) {
    val tint = if (active) MaterialTheme.colorScheme.primary else TextPrimary
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .weight(1f)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
    ) {
        Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(26.dp))
        Spacer(Modifier.height(4.dp))
        Text(label, fontSize = 12.sp, color = if (active) tint else TextSecondary)
    }
}
