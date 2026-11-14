package com.songdesk.launcher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.ui.unit.IntOffset
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.delay
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.songdesk.launcher.data.WeatherData
import com.songdesk.launcher.data.WeatherRepository
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.songdesk.launcher.car.CarRepository
import com.songdesk.launcher.car.DiLink3Bridge
import com.songdesk.launcher.data.AppRepository
import com.songdesk.launcher.data.LauncherPrefs
import com.songdesk.launcher.ui.car.CarControlScreen
import com.songdesk.launcher.ui.home.Dock
import com.songdesk.launcher.ui.home.DockAction
import com.songdesk.launcher.ui.home.HomeScreen
import com.songdesk.launcher.ui.home.launchDockAction
import com.songdesk.launcher.ui.settings.SettingsScreen
import com.songdesk.launcher.ui.theme.AccentIce
import com.songdesk.launcher.ui.theme.AccentOrange
import com.songdesk.launcher.ui.theme.AccentTeal
import com.songdesk.launcher.ui.theme.AppBackdrop
import com.songdesk.launcher.ui.theme.SongDeskTheme
import com.songdesk.launcher.ui.theme.Wallpaper

private enum class Dest { Home, Car, Settings }

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 车机桌面 = 全屏沉浸：隐藏系统栏，从屏幕边缘滑入可临时唤出
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        val repo = AppRepository(this)
        val prefs = LauncherPrefs(this)
        val carRepo = CarRepository(applicationContext)
        carRepo.start()
        val bridge = DiLink3Bridge(this, carRepo)
        val guideProvider = com.songdesk.launcher.car.GuideProvider(applicationContext)
        guideProvider.start()

        setContent {
            var accentIndex by remember { mutableIntStateOf(prefs.accentIndex) }
            var wallpaperIndex by remember { mutableIntStateOf(prefs.wallpaperIndex) }
            var sniffing by remember { mutableStateOf(false) }
            var navDemo by remember { mutableStateOf(prefs.navDemo) }
            val accents = remember { listOf(AccentIce, AccentTeal, AccentOrange) }
            val wallpapers = remember { Wallpaper.entries.toList() }
            var weather by remember { mutableStateOf<WeatherData?>(null) }

            // 实时天气（MSN 优先 + Open-Meteo 兜底，失败重试一次）
            LaunchedEffect(Unit) {
                val repo = WeatherRepository()
                weather = repo.load() ?: repo.load()
            }

            // 导航演示：周期合成高德车机版广播（含红绿灯读秒），验证导航卡全链路
            LaunchedEffect(navDemo) {
                if (!navDemo) return@LaunchedEffect
                val roads = listOf("未来大道", "滨河东路", "科创路", "郑州绕城高速")
                var cd = 15
                var dis = 850
                while (true) {
                    guideProvider.sendDemoBroadcast(cd, roads[(cd / 4).mod(roads.size)], dis)
                    cd -= 1
                    dis = (dis - 35).coerceAtLeast(60)
                    if (cd < 0) { cd = 15; dis = 850 }
                    delay(1000)
                }
            }

            SongDeskTheme(accents[accentIndex.coerceIn(0, accents.lastIndex)]) {
                AppBackdrop(wallpapers[wallpaperIndex.coerceIn(0, wallpapers.lastIndex)]) {
                    var dest by remember { mutableStateOf(Dest.Home) }

                    Column(Modifier.fillMaxSize()) {
                        Box(Modifier.weight(1f)) {
                            AnimatedContent(
                                targetState = dest,
                                transitionSpec = {
                                    // 按 Dest 枚举顺序做方向性滑动
                                    val forward = targetState.ordinal >= initialState.ordinal
                                    val fadeSpec = tween<Float>(260, easing = FastOutSlowInEasing)
                                    val slideSpec = tween<IntOffset>(260, easing = FastOutSlowInEasing)
                                    if (forward) {
                                        (slideInHorizontally(slideSpec) { it / 6 } + fadeIn(fadeSpec)) togetherWith
                                            (slideOutHorizontally(slideSpec) { -it / 6 } + fadeOut(fadeSpec))
                                    } else {
                                        (slideInHorizontally(slideSpec) { -it / 6 } + fadeIn(fadeSpec)) togetherWith
                                            (slideOutHorizontally(slideSpec) { it / 6 } + fadeOut(fadeSpec))
                                    }
                                },
                                label = "dest",
                            ) { d ->
                                when (d) {
                                    Dest.Home -> HomeScreen(
                                        repo, bridge, weather,
                                        guideState = guideProvider.state.collectAsState().value,
                                        onCar = { dest = Dest.Car },
                                    )
                                    Dest.Car -> CarControlScreen(bridge, onBack = { dest = Dest.Home })
                                    Dest.Settings -> SettingsScreen(
                                        accentIndex = accentIndex,
                                        wallpaperIndex = wallpaperIndex,
                                        sniffing = sniffing,
                                        navDemo = navDemo,
                                        onNavDemoChange = { on ->
                                            navDemo = on
                                            prefs.navDemo = on
                                        },
                                        onSniffingChange = { on ->
                                            sniffing = on
                                            carRepo.broadcastSource.setSniffing(on)
                                            com.songdesk.launcher.car.telemetry.SniffUploader.setEnabled(on)
                                        },
                                        onAccentChange = { i ->
                                            accentIndex = i
                                            prefs.accentIndex = i
                                        },
                                        onWallpaperChange = { i ->
                                            wallpaperIndex = i
                                            prefs.wallpaperIndex = i
                                            // 壁纸切换时自动配对强调色
                                            val paired = Wallpaper.at(i).pairedAccent
                                            val newAccent = accents.indexOfFirst { it == paired }.takeIf { it >= 0 } ?: accentIndex
                                            accentIndex = newAccent
                                            prefs.accentIndex = newAccent
                                        },
                                        onBack = { dest = Dest.Home },
                                        onExit = { finish() },
                                    )
                                }
                            }
                        }
                        Dock(
                            active = dest.toDockAction(),
                            modifier = Modifier.padding(bottom = 16.dp),
                            onAction = { action ->
                                when (action) {
                                    DockAction.CAR -> dest = Dest.Car
                                    DockAction.SETTINGS -> dest = Dest.Settings
                                    else -> launchDockAction(repo, action, this@MainActivity)
                                }
                            },
                        )
                    }
                }
            }
        }
    }

    private fun Dest.toDockAction(): DockAction? = when (this) {
        Dest.Car -> DockAction.CAR
        Dest.Settings -> DockAction.SETTINGS
        Dest.Home -> null
    }
}
