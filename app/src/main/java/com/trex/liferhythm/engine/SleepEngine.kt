package com.trex.liferhythm.engine

import java.time.LocalDate
import java.time.ZoneId
import kotlin.math.max
import kotlin.math.min

const val MIN = 60_000L
const val HOUR = 60 * MIN

data class Interval(val start: Long, val end: Long) {
    val duration: Long get() = end - start
}

enum class Confidence(val label: String) {
    HIGH("High"),
    MEDIUM("Medium"),
    LOW("Low")
}

data class SleepConfig(
    val nightStartHour: Int = 21,
    val nightEndHour: Int = 9,
    val minSleepMs: Long = 3 * HOUR,
    val sessionGapMs: Long = 3 * MIN,
    val shortPickupMs: Long = 5 * MIN,
    val quietBeforeMs: Long = 60 * MIN,
    val quietAfterMs: Long = 90 * MIN
)

data class SleepResult(
    val wakeDate: LocalDate,
    val bedtime: Long,
    val wakeTime: Long,
    val awakenings: List<Interval>,
    val confidence: Confidence,
    val reason: String
) {
    val awakeMs: Long get() = awakenings.sumOf { it.duration }
    val spanMs: Long get() = wakeTime - bedtime
    val sleepMs: Long get() = spanMs - awakeMs
}

data class DayStats(
    val screenMs: Long,
    val sessions: Int,
    val longestBreakMs: Long,
    val first: Long?,
    val last: Long?
)

data class DayRow(val start: Long, val end: Long, val active: Boolean)

object SleepEngine {
    fun mergeSessions(raw: List<Interval>, gapMs: Long): List<Interval> {
        val sorted = raw.filter { it.end >= it.start }.sortedBy { it.start }
        if (sorted.isEmpty()) return emptyList()
        val out = ArrayList<Interval>()
        var cur = sorted[0]
        for (i in 1 until sorted.size) {
            val next = sorted[i]
            if (next.start - cur.end <= gapMs) cur = Interval(cur.start, max(cur.end, next.end))
            else { out.add(cur); cur = next }
        }
        out.add(cur)
        return out
    }

    fun nightWindow(date: LocalDate, zone: ZoneId, cfg: SleepConfig): Pair<Long, Long> {
        val end = date.atTime(cfg.nightEndHour, 0).atZone(zone).toInstant().toEpochMilli()
        val startDay = if (cfg.nightStartHour >= cfg.nightEndHour) date.minusDays(1) else date
        val start = startDay.atTime(cfg.nightStartHour, 0).atZone(zone).toInstant().toEpochMilli()
        return Pair(start, end)
    }

    fun estimate(sessions: List<Interval>, date: LocalDate, zone: ZoneId, cfg: SleepConfig): SleepResult? {
        val n = sessions.size
        if (n < 2) return null
        val (ns, ne) = nightWindow(date, zone, cfg)
        val awake = BooleanArray(n)
        for (i in 1 until n - 1) {
            val s = sessions[i]
            val before = s.start - sessions[i - 1].end
            val after = sessions[i + 1].start - s.end
            if (s.start in ns..ne && s.duration < cfg.shortPickupMs &&
                before >= cfg.quietBeforeMs && after >= cfg.quietAfterMs) awake[i] = true
        }

        val solid = (0 until n).filter { !awake[it] }
        var bestA = -1
        var bestB = -1
        var bestOverlap = 0L
        var bestLen = 0L
        for (k in 0 until solid.size - 1) {
            val a = solid[k]
            val b = solid[k + 1]
            val gs = sessions[a].end
            val ge = sessions[b].start
            val len = ge - gs
            if (len < cfg.minSleepMs) continue
            val overlap = min(ge, ne) - max(gs, ns)
            if (overlap < 2 * HOUR) continue
            if (overlap > bestOverlap || (overlap == bestOverlap && len > bestLen)) {
                bestA = a; bestB = b; bestOverlap = overlap; bestLen = len
            }
        }
        if (bestA < 0) return null

        val gs = sessions[bestA].end
        val ge = sessions[bestB].start
        val awakenings = (bestA + 1 until bestB).map { sessions[it] }
        var score = 0
        if (bestLen >= 6 * HOUR) score += 2 else if (bestLen >= 4 * HOUR) score += 1
        if (gs >= ns - 2 * HOUR && gs <= ne) score += 1
        if (ge >= ns && ge <= ne + 2 * HOUR) score += 1
        if (awakenings.size <= 3) score += 1
        val confidence = when { score >= 4 -> Confidence.HIGH; score >= 2 -> Confidence.MEDIUM; else -> Confidence.LOW }
        val reason = when (confidence) {
            Confidence.HIGH -> "A long overnight phone-free stretch, with activity right before and after."
            Confidence.MEDIUM -> "Overnight inactivity was found, but part of it falls outside your night window or is broken up."
            Confidence.LOW -> "A long quiet period was found, but it may not represent sleep."
        }
        return SleepResult(date, gs, ge, awakenings, confidence, reason)
    }

    fun dayStats(sessions: List<Interval>, date: LocalDate, zone: ZoneId, now: Long): DayStats {
        val ds = date.atStartOfDay(zone).toInstant().toEpochMilli()
        val de = min(date.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli(), now)
        if (de <= ds) return DayStats(0L, 0, 0L, null, null)
        val clipped = ArrayList<Interval>()
        for (s in sessions) {
            val st = max(s.start, ds); val en = min(s.end, de)
            if (en >= st) clipped.add(Interval(st, en))
        }
        if (clipped.isEmpty()) return DayStats(0L, 0, de - ds, null, null)
        var prevEnd = ds; var longest = 0L
        for (c in clipped) { longest = max(longest, c.start - prevEnd); prevEnd = c.end }
        longest = max(longest, de - prevEnd)
        return DayStats(clipped.sumOf { it.duration }, clipped.size, longest, clipped.first().start, clipped.last().end)
    }

    fun dayRows(sessions: List<Interval>, ds: Long, de: Long, minBreakMs: Long = 30 * MIN): List<DayRow> {
        if (de <= ds) return emptyList()
        val rows = ArrayList<DayRow>()
        var prevEnd = ds
        for (s in sessions) {
            val st = max(s.start, ds); val en = min(s.end, de)
            if (en < st) continue
            if (st - prevEnd >= minBreakMs) rows.add(DayRow(prevEnd, st, false))
            rows.add(DayRow(st, en, true))
            prevEnd = max(prevEnd, en)
        }
        if (de - prevEnd >= minBreakMs) rows.add(DayRow(prevEnd, de, false))
        return rows
    }
}