package com.songdesk.launcher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.songdesk.launcher.car.CarRepository
import com.songdesk.launcher.car.DiLink3Bridge
import com.songdesk.launcher.car.GuideProvider
import com.songdesk.launcher.data.AppRepository
import com.songdesk.launcher.data.LauncherPrefs
import com.songdesk.launcher.data.WeatherData
import com.songdesk.launcher.data.WeatherRepository
import com.songdesk.launcher.speech.Speech
import com.songdesk.launcher.ui.appcenter.AppCenterScreen
import com.songdesk.launcher.ui.charge.ChargeScreen
import com.songdesk.launcher.ui.home.Dock
import com.songdesk.launcher.ui.home.DockAction
import com.songdesk.launcher.ui.home.HomeScreen
import com.songdesk.launcher.ui.home.launchDockAction
import com.songdesk.launcher.ui.agent.AgentScreen
import com.songdesk.launcher.ui.car.CarControlScreen
import com.songdesk.launcher.ui.settings.SettingsScreen
import com.songdesk.launcher.ui.theme.AppBackdrop
import com.songdesk.launcher.ui.theme.SongDeskTheme
import kotlinx.coroutines.delay

private enum class Dest { Home, Car, Settings, Agent, Charge, AppCenter }

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
        val guideProvider = GuideProvider(applicationContext)
        guideProvider.start()
        Speech.init(applicationContext)
        Speech.voiceOn = prefs.voiceOn

        setContent {
            var voiceOn by remember { mutableStateOf(prefs.voiceOn) }
            var aiKey by remember { mutableStateOf(prefs.aiKey) }
            var sniffing by remember { mutableStateOf(false) }
            var navDemo by remember { mutableStateOf(prefs.navDemo) }
            var mediaDemo by remember { mutableStateOf(false) }
            var weather by remember { mutableStateOf<WeatherData?>(null) }

            // 实时天气（MSN 优先 + Open-Meteo 兜底，失败重试一次）
            LaunchedEffect(Unit) {
                val repo = WeatherRepository()
                weather = repo.load() ?: repo.load()
            }

            // 导航演示：周期合成高德车机版广播（含红绿灯读秒）
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

            SongDeskTheme(accent = com.songdesk.launcher.ui.theme.AccentIce) {
                AppBackdrop(wallpaper = null) {
                    var dest by remember { mutableStateOf(Dest.Home) }
                    // 子页面按返回键回主页；主页不退出（车机返回键交还原车/忽略）
                    BackHandler(enabled = dest != Dest.Home) { dest = Dest.Home }

                    Column(Modifier.fillMaxSize()) {
                        Box(Modifier.weight(1f)) {
                            AnimatedContent(
                                targetState = dest,
                                transitionSpec = {
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
                                        hiddenPkgs = prefs.hiddenPkgs,
                                        onVoiceCommand = { text ->
                                            val res = com.songdesk.launcher.commands.CommandDispatcher.execute(applicationContext, bridge, text)
                                            android.widget.Toast.makeText(
                                                applicationContext, res ?: "未识别到可执行的指令", android.widget.Toast.LENGTH_SHORT,
                                            ).show()
                                        },
                                        onOpenAppCenter = { dest = Dest.AppCenter },
                                        onCar = { dest = Dest.Car },
                                    )
                                    Dest.Car -> CarControlScreen(
                                        bridge,
                                        onBack = { dest = Dest.Home },
                                        onCharge = { dest = Dest.Charge },
                                    )
                                    Dest.Charge -> com.songdesk.launcher.ui.charge.ChargeScreen(
                                        com.songdesk.launcher.data.ChargeStore(applicationContext),
                                        onBack = { dest = Dest.Home },
                                    )
                                    Dest.Agent -> AgentScreen(
                                        bridge, aiKey, onBack = { dest = Dest.Home },
                                    )
                                    Dest.AppCenter -> AppCenterScreen(
                                        repo,
                                        onBack = { dest = Dest.Home },
                                        onAppClick = { pkg -> repo.launchPackage(pkg) },
                                    )
                                    Dest.Settings -> SettingsScreen(
                                        accentIndex = 0,
                                        sniffing = sniffing,
                                        navDemo = navDemo,
                                        voiceOn = voiceOn,
                                        onVoiceChange = { on ->
                                            voiceOn = on
                                            prefs.voiceOn = on
                                            com.songdesk.launcher.speech.Speech.voiceOn = on
                                        },
                                        onAccentChange = { },
                                        aiKey = aiKey,
                                        onAiKeyChange = { v ->
                                            aiKey = v
                                            prefs.aiKey = v
                                        },
                                        onOpenAgent = { dest = Dest.Agent },
                                        onSniffingChange = { on ->
                                            sniffing = on
                                            carRepo.broadcastSource.setSniffing(on)
                                            com.songdesk.launcher.car.telemetry.SniffUploader.setEnabled(on)
                                        },
                                        onNavDemoChange = { on ->
                                            navDemo = on
                                            prefs.navDemo = on
                                        },
                                        onBack = { dest = Dest.Home },
                                        onExit = { finish() },
                                    )
                                }
                            }
                        }
                        Dock(
                            modifier = Modifier.padding(bottom = 8.dp),
                            onAction = { action ->
                                when (action) {
                                    DockAction.CAR -> dest = Dest.Car
                                    DockAction.SETTINGS -> dest = Dest.Settings
                                    DockAction.APP_CENTER -> dest = Dest.AppCenter
                                    else -> launchDockAction(repo, action, this@MainActivity)
                                }
                            },
                        )
                    }
                }
            }
        }
    }
}

private fun dp(dp: Int): androidx.compose.ui.unit.Dp = androidx.compose.ui.unit.Dp(dp.toFloat())
