package com.trex.liferhythm.data

import android.content.Context
import com.trex.liferhythm.engine.Interval
import com.trex.liferhythm.engine.SleepConfig
import com.trex.liferhythm.engine.SleepEngine
import com.trex.liferhythm.engine.SleepResult
import java.time.LocalDate
import java.time.ZoneId

object Collector {
    data class Snapshot(val sessions: List<Interval>, val results: List<SleepResult>)

    fun compute(ctx: Context, cfg: SleepConfig, days: Int = 8): Snapshot {
        val repo = UsageRepository(ctx)
        if (!repo.hasAccess()) return Snapshot(emptyList(), emptyList())
        val zone = ZoneId.systemDefault()
        val now = System.currentTimeMillis()
        val today = LocalDate.now(zone)
        val from = today.minusDays(days.toLong()).atStartOfDay(zone).toInstant().toEpochMilli()
        val sessions = SleepEngine.mergeSessions(repo.readIntervals(from, now), cfg.sessionGapMs)
        val results = (0 until days).mapNotNull {
            SleepEngine.estimate(sessions, today.minusDays(it.toLong()), zone, cfg)
        }
        return Snapshot(sessions, results)
    }
}