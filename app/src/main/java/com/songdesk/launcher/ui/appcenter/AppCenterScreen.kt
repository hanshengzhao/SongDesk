package com.songdesk.launcher.ui.appcenter

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.songdesk.launcher.data.AppRepository
import com.songdesk.launcher.ui.common.GlassCard
import com.songdesk.launcher.ui.home.AppGrid
import com.songdesk.launcher.ui.theme.TextPrimary
import com.songdesk.launcher.ui.theme.TextSecondary

/** 应用中心：搜索 + 全部应用网格（从主页拆出，主页纯车功能） */
@Composable
fun AppCenterScreen(
    repo: AppRepository,
    onBack: () -> Unit,
    onAppClick: (String) -> Unit,
) {
    var query by remember { mutableStateOf("") }
    val apps = remember { repo.loadLaunchableApps() }
    val filtered = remember(query) {
        if (query.isBlank()) apps
        else apps.filter { it.label.contains(query, ignoreCase = true) || it.packageName.contains(query, ignoreCase = true) }
    }

    Column(Modifier.fillMaxSize().padding(24.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(onClick = onBack, shape = CircleShape, color = MaterialTheme.colorScheme.surface) {
                Box(Modifier.size(52.dp), contentAlignment = Alignment.Center) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回", tint = TextPrimary)
                }
            }
            Spacer(Modifier.width(16.dp))
            Text("应用中心", style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Medium), color = TextPrimary)
            Spacer(Modifier.weight(1f))
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.width(360.dp).height(56.dp),
                placeholder = { Text("搜索应用…", color = TextSecondary) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = TextSecondary) },
                singleLine = true,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
            )
        }
        Spacer(Modifier.height(16.dp))
        GlassCard(Modifier.fillMaxSize(), contentFill = true) {
            AppGrid(filtered, Modifier.fillMaxSize(), repo) { app ->
                onAppClick(app.packageName)
            }
        }
    }
}
