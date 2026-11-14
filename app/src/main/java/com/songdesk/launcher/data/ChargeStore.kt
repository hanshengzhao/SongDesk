package com.songdesk.launcher.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/** 一条充电记录 */
data class ChargeRecord(
    val ts: Long,        // 时间戳
    val socFrom: Int,    // 开始 SOC
    val socTo: Int,      // 结束 SOC
    val costYuan: Double,// 花费（元）
)

/** 充电记录仓库：JSON 文件持久化（轻量，无需数据库） */
class ChargeStore(context: Context) {

    private val file: File = File(context.filesDir, "charge_records.json")

    fun load(): List<ChargeRecord> = runCatching {
        val arr = JSONArray(file.readText())
        (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            ChargeRecord(
                ts = o.getLong("ts"),
                socFrom = o.getInt("from"),
                socTo = o.getInt("to"),
                costYuan = o.getDouble("cost"),
            )
        }
    }.getOrDefault(emptyList())

    fun add(rec: ChargeRecord): List<ChargeRecord> {
        val list = load().toMutableList().also { it.add(0, rec) }
        save(list)
        return list
    }

    fun remove(ts: Long): List<ChargeRecord> {
        val list = load().filterNot { it.ts == ts }
        save(list)
        return list
    }

    private fun save(list: List<ChargeRecord>) {
        val arr = JSONArray()
        list.forEach { r ->
            arr.put(
                JSONObject()
                    .put("ts", r.ts)
                    .put("from", r.socFrom)
                    .put("to", r.socTo)
                    .put("cost", r.costYuan)
            )
        }
        file.writeText(arr.toString())
    }
}
