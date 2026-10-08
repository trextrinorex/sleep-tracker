package com.trex.liferhythm.data

import android.content.Context
import com.trex.liferhythm.engine.Confidence
import com.trex.liferhythm.engine.SleepConfig
import com.trex.liferhythm.engine.SleepResult
import org.json.JSONArray
import org.json.JSONObject

data class Record(
    val date: Long,
    val bedtime: Long,
    val wake: Long,
    val awakeMs: Long,
    val awakenings: Int,
    val confidence: String,
    val reason: String
) {
    val spanMs: Long get() = wake - bedtime
    val sleepMs: Long get() = spanMs - awakeMs

    fun confidenceEnum(): Confidence =
        try {
            Confidence.valueOf(confidence)
        } catch (e: IllegalArgumentException) {
            Confidence.LOW
        }

    fun toJson(): JSONObject = JSONObject()
        .put("date", date)
        .put("bedtime", bedtime)
        .put("wake", wake)
        .put("awakeMs", awakeMs)
        .put("awakenings", awakenings)
        .put("confidence", confidence)
        .put("reason", reason)

    companion object {
        fun fromJson(o: JSONObject) = Record(
            date = o.getLong("date"),
            bedtime = o.getLong("bedtime"),
            wake = o.getLong("wake"),
            awakeMs = o.getLong("awakeMs"),
            awakenings = o.getInt("awakenings"),
            confidence = o.getString("confidence"),
            reason = o.optString("reason", "")
        )
    }
}

fun SleepResult.toRecord() = Record(
    date = wakeDate.toEpochDay(),
    bedtime = bedtime,
    wake = wakeTime,
    awakeMs = awakeMs,
    awakenings = awakenings.size,
    confidence = confidence.name,
    reason = reason
)

class Store(ctx: Context) {
    private val sp = ctx.getSharedPreferences("liferhythm", Context.MODE_PRIVATE)

    fun onboarded(): Boolean = sp.getBoolean("onboarded", false)
    fun setOnboarded() = sp.edit().putBoolean("onboarded", true).apply()

    fun config(): SleepConfig = SleepConfig(
        nightStartHour = sp.getInt("night_start", 21),
        nightEndHour = sp.getInt("night_end", 9)
    )

    fun saveNight(start: Int, end: Int) {
        sp.edit().putInt("night_start", start).putInt("night_end", end).apply()
    }

    fun records(): List<Record> {
        val raw = sp.getString("records", "[]") ?: "[]"
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { Record.fromJson(arr.getJSONObject(it)) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveAll(newOnes: List<Record>) {
        if (newOnes.isEmpty()) return
        val map = LinkedHashMap<Long, Record>()
        records().forEach { map[it.date] = it }
        newOnes.forEach { map[it.date] = it }
        val merged = map.values.sortedBy { it.date }.takeLast(400)
        val arr = JSONArray()
        merged.forEach { arr.put(it.toJson()) }
        sp.edit().putString("records", arr.toString()).apply()
    }

    fun clearData() {
        sp.edit().remove("records").remove("night_start").remove("night_end").apply()
    }
}