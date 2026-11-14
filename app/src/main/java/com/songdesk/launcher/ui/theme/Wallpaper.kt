package com.songdesk.launcher.ui.theme

import androidx.compose.ui.graphics.Color

/** 内置壁纸（res/drawable-nodpi 程序生成图）+ 建议搭配的强调色 */
enum class Wallpaper(val resName: String, val label: String, val pairedAccent: Color) {
    NEBULA("wp_nebula", "深空星云", AccentIce),
    AURORA("wp_aurora", "极光山脉", AccentTeal),
    SUNSET("wp_sunset", "落日余晖", AccentOrange),
    OCEAN("wp_ocean", "深海浪潮", AccentTeal);

    companion object {
        fun at(i: Int) = entries[i.coerceIn(0, entries.lastIndex)]
    }
}
