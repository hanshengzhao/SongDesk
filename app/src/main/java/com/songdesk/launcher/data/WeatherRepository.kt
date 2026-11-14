package com.songdesk.launcher.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class WeatherData(
    val city: String,
    val tempC: Int,
    val minC: Int,
    val maxC: Int,
    val condition: String,   // 文字描述（如"晴朗"）
    val aqi: Int = -1,
    val aqiLabel: String? = null,
    val alertTitle: String? = null,
) {
    val emoji: String
        get() = when {
            condition.contains("雷") -> "⛈"
            condition.contains("雪") -> "❄"
            condition.contains("雨") -> "🌧"
            condition.contains("雾") || condition.contains("霾") -> "🌫"
            condition.contains("阴") -> "☁"
            condition.contains("云") -> "⛅"
            condition.contains("晴") -> "☀"
            else -> "🌤"
        }
}

/**
 * 实时天气：MSN 天气（国内可达，接口与公共 key 来自对迪粉桌面的逆向）优先，
 * Open-Meteo 兜底；坐标后续可换车机 GPS。全部失败返回 null。
 */
class WeatherRepository(
    private val latitude: Double = 34.747,   // 默认郑州，后续接车机 GPS
    private val longitude: Double = 113.625,
    private val city: String = "郑州",
) {
    suspend fun load(): WeatherData? = withContext(Dispatchers.IO) {
        loadMsn() ?: loadOpenMeteo().also {
            if (it == null) android.util.Log.e(TAG, "天气拉取失败: msn=${lastMsnErr} openmeteo=${lastOmeteoErr}")
        }
    }

    private var lastMsnErr: String? = null
    private var lastOmeteoErr: String? = null

    /** MSN 天气（中国天气网数据源，api.msn.cn 国内 CDN） */
    private fun loadMsn(): WeatherData? = runCatching {
        val url = URL(
            "https://api.msn.cn/weather/overview" +
                "?apikey=$MSN_KEY&market=zh-cn&locale=zh-cn" +
                "&lat=$latitude&lon=$longitude"
        )
        val conn = url.openConnection() as HttpURLConnection
        conn.connectTimeout = 6000
        conn.readTimeout = 6000
        conn.setRequestProperty("User-Agent", "Mozilla/5.0")
        val json = conn.inputStream.bufferedReader().use { it.readText() }
        val w = JSONObject(json)
            .getJSONArray("value").getJSONObject(0)
            .getJSONArray("responses").getJSONObject(0)
            .getJSONArray("weather").getJSONObject(0)
        val current = w.getJSONObject("current")
        val daily = w.getJSONObject("forecast")
            .getJSONArray("days").getJSONObject(0).getJSONObject("daily")
        val alerts = w.optJSONArray("alerts")
        WeatherData(
            city = city,
            tempC = current.getDouble("temp").toInt(),
            condition = current.optString("cap", "—"),
            minC = daily.getDouble("tempLo").toInt(),
            maxC = daily.getDouble("tempHi").toInt(),
            aqi = current.optDouble("aqi", -1.0).toInt(),
            aqiLabel = current.optString("aqiSeverity", null),
            alertTitle = if (alerts != null && alerts.length() > 0)
                alerts.getJSONObject(0).optString("shortCap", null) else null,
        )
    }.onFailure { lastMsnErr = "${it.javaClass.simpleName}: ${it.message}" }.getOrNull()

    /** Open-Meteo 兜底（海外可达场景） */
    private fun loadOpenMeteo(): WeatherData? = runCatching {
        val url = URL(
            "https://api.open-meteo.com/v1/forecast" +
                "?latitude=$latitude&longitude=$longitude" +
                "&current=temperature_2m,weather_code,is_day" +
                "&daily=temperature_2m_max,temperature_2m_min" +
                "&timezone=auto&forecast_days=1"
        )
        val conn = url.openConnection() as HttpURLConnection
        conn.connectTimeout = 6000
        conn.readTimeout = 6000
        val json = conn.inputStream.bufferedReader().use { it.readText() }
        val root = JSONObject(json)
        val current = root.getJSONObject("current")
        val daily = root.getJSONObject("daily")
        val code = current.getInt("weather_code")
        WeatherData(
            city = city,
            tempC = current.getDouble("temperature_2m").toInt(),
            minC = daily.getJSONArray("temperature_2m_min").getDouble(0).toInt(),
            maxC = daily.getJSONArray("temperature_2m_max").getDouble(0).toInt(),
            condition = wmoText(code),
        )
    }.onFailure { lastOmeteoErr = "${it.javaClass.simpleName}: ${it.message}" }.getOrNull()

    private fun wmoText(code: Int) = when (code) {
        0 -> "晴"
        1 -> "基本晴"
        2 -> "多云"
        3 -> "阴"
        45, 48 -> "雾"
        in 51..57 -> "毛毛雨"
        in 61..67 -> "雨"
        in 71..77, 85, 86 -> "雪"
        in 80..82 -> "阵雨"
        in 95..99 -> "雷雨"
        else -> "—"
    }

    private companion object {
        /** 迪粉桌面内置的 MSN 公共 key（逆向自其混淆字符串） */
        const val MSN_KEY = "j5i4gDqHL6nGYwx5wi5kRhXjtf2c5qgFX9fzfk0TOo"
        const val TAG = "SongDeskWeather"
    }
}
