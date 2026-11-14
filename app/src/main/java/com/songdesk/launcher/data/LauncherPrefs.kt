package com.songdesk.launcher.data

import android.content.Context

/** 轻量偏好：SharedPreferences 足够 v1（强调色/壁纸） */
class LauncherPrefs(context: Context) {
    private val sp = context.getSharedPreferences("songdesk", Context.MODE_PRIVATE)

    var accentIndex: Int
        get() = sp.getInt("accent", 0)
        set(v) = sp.edit().putInt("accent", v).apply()

    var wallpaperIndex: Int
        get() = sp.getInt("wallpaper", 0)
        set(v) = sp.edit().putInt("wallpaper", v).apply()

    /** 导航演示（合成高德车机版广播，验证导航卡/红绿灯读秒） */
    var navDemo: Boolean
        get() = sp.getBoolean("navDemo", false)
        set(v) = sp.edit().putBoolean("navDemo", v).apply()

    /** 语音播报开关 */
    var voiceOn: Boolean
        get() = sp.getBoolean("voice", true)
        set(v) = sp.edit().putBoolean("voice", v).apply()

    /** 隐藏的应用包名集合 */
    var hiddenPkgs: Set<String>
        get() = sp.getStringSet("hidden", emptySet()) ?: emptySet()
        set(v) = sp.edit().putStringSet("hidden", v).apply()

    /** Dock 前三槽自定义包名（逗号分隔；空 = 默认 地图/音乐/电话） */
    var dockApps: String
        get() = sp.getString("dockApps", "") ?: ""
        set(v) = sp.edit().putString("dockApps", v).apply()

    /** AI 助理 API Key */
    var aiKey: String
        get() = sp.getString("aiKey", "") ?: ""
        set(v) = sp.edit().putString("aiKey", v).apply()
}
