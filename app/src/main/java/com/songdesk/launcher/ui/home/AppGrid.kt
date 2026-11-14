package com.songdesk.launcher.ui.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.songdesk.launcher.data.AppEntry
import com.songdesk.launcher.data.AppRepository
import com.songdesk.launcher.ui.theme.GlassFill
import com.songdesk.launcher.ui.theme.GlassStroke
import com.songdesk.launcher.ui.theme.TextSecondary
import androidx.compose.foundation.BorderStroke

private const val COLS = 6
private const val ROWS = 3
private const val PER_PAGE = COLS * ROWS

/** 分页应用网格：每页 6×3，玻璃底板图标 + 长按菜单（打开/应用信息/卸载/隐藏） */
@Composable
fun AppGrid(
    apps: List<AppEntry>,
    modifier: Modifier = Modifier,
    repo: AppRepository,
    onHide: (String) -> Unit = {},
    onAppClick: (AppEntry) -> Unit,
) {
    val pages = remember(apps) { apps.chunked(PER_PAGE) }
    if (pages.isEmpty()) return
    val pagerState = rememberPagerState(pageCount = { pages.size })

    HorizontalPager(state = pagerState, modifier = modifier) { pageIndex ->
        val pageApps = pages[pageIndex]
        Column(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(28.dp),
        ) {
            pageApps.chunked(COLS).forEach { rowApps ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                    rowApps.forEach { app -> AppIconItem(app, repo, onHide) { onAppClick(app) } }
                    repeat(COLS - rowApps.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun RowScope.AppIconItem(app: AppEntry, repo: AppRepository, onHide: (String) -> Unit, onClick: () -> Unit) {
    val context = LocalContext.current
    var menuOpen by remember { mutableStateOf(false) }

    // Box 作为菜单锚点（HorizontalPager 内 DropdownMenu 直接放 Column 会锚定错位）
    Box(Modifier.weight(1f).aspectRatio(0.8f)) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .combinedClickable(onClick = onClick, onLongClick = { menuOpen = true }),
        ) {
            // 图标玻璃底板
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = GlassFill,
                border = BorderStroke(1.dp, GlassStroke),
            ) {
                Box(Modifier.padding(12.dp), contentAlignment = Alignment.Center) {
                    app.icon?.let { bmp ->
                        Image(
                            bitmap = bmp.asImageBitmap(),
                            contentDescription = app.label,
                            modifier = Modifier
                                .size(52.dp)
                                .clip(RoundedCornerShape(12.dp)),
                        )
                    } ?: Box(Modifier.size(52.dp))
                }
            }
            Text(
                app.label,
                fontSize = 13.sp,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 6.dp),
            )
        }

        if (menuOpen) {
            Popup(
                alignment = Alignment.BottomStart,
                onDismissRequest = { menuOpen = false },
                properties = PopupProperties(focusable = true),
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xF2121727),
                    border = BorderStroke(1.dp, GlassStroke),
                ) {
                    Column(Modifier.width(160.dp)) {
                        MenuRow("打开") { menuOpen = false; onClick() }
                        MenuRow("应用信息") {
                            menuOpen = false
                            runCatching {
                                context.startActivity(
                                    android.content.Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                        .setData(android.net.Uri.fromParts("package", app.packageName, null))
                                        .addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                                )
                            }
                        }
                        MenuRow("卸载") {
                            menuOpen = false
                            runCatching {
                                context.startActivity(
                                    android.content.Intent(android.content.Intent.ACTION_DELETE)
                                        .setData(android.net.Uri.fromParts("package", app.packageName, null))
                                        .addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                                )
                            }
                        }
                        MenuRow("隐藏") {
                            menuOpen = false
                            onHide(app.packageName)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MenuRow(text: String, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Text(text, fontSize = 15.sp, color = com.songdesk.launcher.ui.theme.TextPrimary)
    }
}
