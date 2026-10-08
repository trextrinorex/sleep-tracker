package com.trex.liferhythm.data

import android.app.AppOpsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.os.Build
import android.os.Process
import com.trex.liferhythm.engine.Interval
import com.trex.liferhythm.engine.MIN

class UsageRepository(private val ctx: Context) {
    fun hasAccess(): Boolean {
        val ops = ctx.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = if (Build.VERSION.SDK_INT >= 29) {
            ops.unsafeCheckOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), ctx.packageName)
        } else {
            @Suppress("DEPRECATION")
            ops.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), ctx.packageName)
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun readIntervals(from: Long, to: Long): List<Interval> {
        val usm = ctx.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val events = usm.queryEvents(from, to) ?: return emptyList()
        val e = UsageEvents.Event()
        val start = HashMap<String, Long>()
        val depth = HashMap<String, Int>()
        val out = ArrayList<Interval>()

        while (events.hasNextEvent()) {
            events.getNextEvent(e)
            val t = e.timeStamp
            val pkg = e.packageName ?: continue
            when (e.eventType) {
                EVENT_RESUMED -> {
                    if (pkg in IGNORED) continue
                    val d = (depth[pkg] ?: 0) + 1
                    depth[pkg] = d
                    if (d == 1) start[pkg] = t
                }
                EVENT_PAUSED -> {
                    val d = (depth[pkg] ?: 0) - 1
                    if (d <= 0) {
                        depth.remove(pkg)
                        val s = start.remove(pkg)
                        if (s != null && t >= s) out.add(Interval(s, t))
                    } else depth[pkg] = d
                }
                EVENT_SCREEN_OFF -> {
                    for (s in start.values) if (t >= s) out.add(Interval(s, t))
                    start.clear()
                    depth.clear()
                }
                EVENT_UNLOCK -> out.add(Interval(t, t + 20_000L))
            }
        }

        val now = System.currentTimeMillis()
        for (s in start.values) {
            val end = if (to >= now) now else minOf(to, s + 10 * MIN)
            if (end > s) out.add(Interval(s, end))
        }
        return out
    }

    private companion object {
        const val EVENT_RESUMED = 1
        const val EVENT_PAUSED = 2
        const val EVENT_SCREEN_OFF = 16
        const val EVENT_UNLOCK = 18
        val IGNORED = setOf("com.android.systemui", "android")
    }
}