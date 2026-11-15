package com.songdesk.launcher.commands

import android.content.Context
import com.songdesk.launcher.car.CarBridge
import com.songdesk.launcher.car.SeatId

/**
 * 小迪风格指令目录 + 调度器。
 * 命名与迪粉桌面小迪指令对齐（温度:24 / 风量:2 / 前除霜开 / 一键关窗 …），
 * 支持同义词归一化（"空调关"→"关闭空调"等前后缀写法等价）。
 *
 * 返回用户可读的执行结果描述；未知指令返回 null。
 */
object CommandDispatcher {

    /** 指令定义：标准名 + 别名 + 执行器（bridge 已绑定） */
    data class Command(
        val name: String,
        val aliases: List<String> = emptyList(),
        val category: String,
        val desc: String,
        val run: (Context, CarBridge, String) -> Unit,
    )

    private fun param(raw: String): Int =
        raw.trim().removePrefix(":").removePrefix("：").filter { it.isDigit() }.toIntOrNull() ?: -1

    /** 全部指令（车控 → 系统 → 媒体） */
    fun catalog(bridge: CarBridge): List<Command> = buildList {
        // ---- 空调 ----
        add(Command("空调开", listOf("打开空调", "开空调"), "空调", "打开空调") { _, b, _ -> b.setAcOn(true) })
        add(Command("空调关", listOf("关闭空调", "关空调"), "空调", "关闭空调") { _, b, _ -> b.setAcOn(false) })
        add(Command("温度:N", listOf("温度"), "空调", "设置空调温度 18~28") { _, b, s ->
            val v = param(s); if (v >= 18 && v <= 28) b.setAcTemp(v)
        })
        add(Command("风量:N", listOf("风量"), "空调", "设置风量 0~7") { _, b, s ->
            val v = param(s); if (v in 0..7) b.setFanLevel(v)
        })
        add(Command("吹脸", listOf("吹脸开"), "空调", "出风模式：吹脸") { _, b, _ -> b.setBlowZone(true, b.state.value.blowFoot) })
        add(Command("吹脚", listOf("吹脚开"), "空调", "出风模式：吹脚") { _, b, _ -> b.setBlowZone(b.state.value.blowFace, true) })
        add(Command("前除霜开", listOf("打开前除霜"), "空调", "前挡除霜") { _, b, _ -> b.setDefrost(true, b.state.value.defrostRear) })
        add(Command("前除霜关", listOf("关闭前除霜"), "空调", "关闭前挡除霜") { _, b, _ -> b.setDefrost(false, b.state.value.defrostRear) })
        add(Command("后除霜开", listOf("打开后除霜"), "空调", "后挡除霜") { _, b, _ -> b.setDefrost(b.state.value.defrostFront, true) })
        add(Command("后除霜关", listOf("关闭后除霜"), "空调", "关闭后挡除霜") { _, b, _ -> b.setDefrost(b.state.value.defrostFront, false) })

        // ---- 车窗/座椅 ----
        add(Command("一键开窗", listOf("打开车窗"), "车窗", "四门车窗全开") { _, b, _ ->
            WindowIds.forEach { b.setWindowOpen(it, true) }
        })
        add(Command("一键关窗", listOf("关闭车窗"), "车窗", "四门车窗全关") { _, b, _ -> b.closeAllWindows() })
        add(Command("开天窗", listOf("打开天窗"), "车窗", "打开天窗") { _, b, _ -> b.setWindowOpen(com.songdesk.launcher.car.WindowId.SUNROOF, true) })
        add(Command("关天窗", listOf("关闭天窗"), "车窗", "关闭天窗") { _, b, _ -> b.setWindowOpen(com.songdesk.launcher.car.WindowId.SUNROOF, false) })
        add(Command("主驾加热:N", listOf("主驾加热"), "座椅", "主驾座椅加热 0~3") { _, b, s ->
            val v = param(s); if (v in 0..3) b.setSeatHeat(SeatId.DRIVER, v)
        })
        add(Command("副驾加热:N", listOf("副驾加热"), "座椅", "副驾座椅加热 0~3") { _, b, s ->
            val v = param(s); if (v in 0..3) b.setSeatHeat(SeatId.PASSENGER, v)
        })
        add(Command("主驾通风:N", listOf("主驾通风"), "座椅", "主驾座椅通风 0~3") { _, b, s ->
            val v = param(s); if (v in 0..3) b.setSeatVent(SeatId.DRIVER, v)
        })
        add(Command("副驾通风:N", listOf("副驾通风"), "座椅", "副驾座椅通风 0~3") { _, b, s ->
            val v = param(s); if (v in 0..3) b.setSeatVent(SeatId.PASSENGER, v)
        })
        add(Command("方向盘加热:N", listOf("方向盘加热"), "座椅", "方向盘加热 0~2") { _, b, s ->
            val v = param(s); if (v in 0..2) b.setSteeringHeat(v)
        })
        add(Command("开后备箱", listOf("打开后备箱"), "车身", "打开后备箱") { _, b, _ -> b.toggleTrunk() })
        add(Command("关后备箱", listOf("关闭后备箱"), "车身", "关闭后备箱") { _, b, _ -> b.toggleTrunk() })
        add(Command("寻车", listOf("我在哪"), "车身", "闪灯鸣笛寻车") { _, b, _ -> b.findCar() })
        add(Command("内循环", listOf("内循环开", "内循环关"), "空调", "切换空调内循环") { _, b, _ -> b.toggleInternalRecirc() })

        // ---- 系统 ----
        add(Command("音量:N", listOf("设置音量"), "系统", "媒体音量 0~100") { c, _, s ->
            val v = param(s); if (v in 0..100) SystemActions.setVolume(c, v)
        })
        add(Command("屏幕亮度:N", listOf("亮度"), "系统", "屏幕亮度 5~100") { c, _, s ->
            val v = param(s); if (v in 5..100) SystemActions.setBrightness(c, v)
        })
        add(Command("开WIFI", listOf("打开WIFI", "开WiFi"), "系统", "打开 WiFi") { c, _, _ -> SystemActions.setWifi(c, true) })
        add(Command("关WIFI", listOf("关闭WIFI", "关WiFi"), "系统", "关闭 WiFi") { c, _, _ -> SystemActions.setWifi(c, false) })
        add(Command("开蓝牙", listOf("打开蓝牙"), "系统", "打开蓝牙") { c, _, _ -> SystemActions.setBluetooth(c, true) })
        add(Command("关蓝牙", listOf("关闭蓝牙"), "系统", "关闭蓝牙") { c, _, _ -> SystemActions.setBluetooth(c, false) })
    }

    private val WindowIds = com.songdesk.launcher.car.WindowId.entries
        .filter { it != com.songdesk.launcher.car.WindowId.SUNROOF }

    /** 归一化：去空格、统一分隔符 */
    private fun normalize(s: String) = s.trim().replace("：", ":").replace(" ", "")

    /**
     * 执行指令文本。匹配规则：精确名/别名 → 参数型模板（"温度24"→"温度:N"）→ null。
     * 返回结果描述；未知指令返回 null。
     */
    fun execute(context: Context, bridge: CarBridge, raw: String): String? {
        val cmd = normalize(raw)
        val catalog = catalog(bridge)
        // 精确匹配
        catalog.firstOrNull { c -> normalize(c.name) == cmd || c.aliases.any { normalize(it) == cmd } }
            ?.let { c -> c.run(context, bridge, cmd); return "${c.name}（${c.desc}）" }
        // 参数模板：带 :N 的指令，数字可拼接（温度24 / 风量2 / 音量30）
        catalog.filter { ":N" in it.name }.forEach { c ->
            val prefix = normalize(c.name).removeSuffix("N").removeSuffix(":")
            if (cmd.startsWith(prefix)) {
                val value = cmd.removePrefix(prefix)
                c.run(context, bridge, ":$value")
                return "${c.name.replace("N", value)}（${c.desc}）"
            }
        }
        return null
    }
}

/** 内置智能场景：一组指令顺序执行（对应迪粉桌面 DiPkg 的预设版） */
object SmartScenes {

    data class Scene(val name: String, val desc: String, val steps: List<String>)

    val all = listOf(
        Scene("雨天模式", "关窗 + 前除霜 + 吹脚", listOf("一键关窗", "前除霜开", "吹脚")),
        Scene("夏日模式", "空调 22° + 吹脸 + 风量 3", listOf("空调开", "温度:22", "吹脸", "风量:3")),
        Scene("冬季模式", "温度 26° + 主驾加热 2 + 吹脚", listOf("空调开", "温度:26", "主驾加热:2", "吹脚")),
        Scene("离车模式", "空调关 + 关窗 + 除霜关", listOf("空调关", "一键关窗", "前除霜关", "后除霜关")),
    )

    /** 执行场景，返回各步骤结果 */
    fun run(context: Context, bridge: CarBridge, scene: Scene): List<String> =
        scene.steps.mapNotNull { CommandDispatcher.execute(context, bridge, it) }
}
