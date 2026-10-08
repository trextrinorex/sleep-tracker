package com.trex.liferhythm.ui
import com.trex.liferhythm.data.Record
import com.trex.liferhythm.engine.MIN
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlin.math.roundToInt
import kotlin.math.sqrt
private val timeFmt = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
private val dayFmt = DateTimeFormatter.ofPattern("EEE, d MMM")
fun fmtDur(ms: Long): String {
    val total = maxOf(ms, 0L) / MIN
    val h = total / 60
    val m = total % 60
    return if (h > 0) h.toString() + "h " + m + "m" else m.toString() + "m"
}
fun fmtTime(ms: Long, zone: ZoneId = ZoneId.systemDefault()) = Instant.ofEpochMilli(ms).atZone(zone).format(timeFmt)
fun fmtMinuteOfDay(minutes: Int) = LocalTime.of((minutes / 60) % 24, minutes % 60).format(timeFmt)
fun fmtHour(hour: Int) = LocalTime.of(hour % 24, 0).format(timeFmt)
fun fmtDay(epochDay: Long) = LocalDate.ofEpochDay(epochDay).format(dayFmt)
data class Rhythm(val bedtimeMin: Int, val wakeMin: Int, val avgSleepMs: Long, val bedtimeSpreadMin: Int, val nights: Int)
private fun minuteOfDay(ms: Long, zone: ZoneId): Int { val t = Instant.ofEpochMilli(ms).atZone(zone); return t.hour * 60 + t.minute }
fun computeRhythm(all: List<Record>, zone: ZoneId): Rhythm? {
    val recent = all.sortedBy { it.date }.takeLast(14)
    if (recent.size < 3) return null
    val bed = recent.map { (minuteOfDay(it.bedtime, zone) - 720 + 1440) % 1440 }
    val avg = bed.average()
    val spread = sqrt(bed.map { (it - avg) * (it - avg) }.average())
    return Rhythm((avg.roundToInt() + 720) % 1440, recent.map { minuteOfDay(it.wake, zone) }.average().roundToInt() % 1440,
        recent.map { it.sleepMs }.average().toLong(), spread.roundToInt(), recent.size)
}