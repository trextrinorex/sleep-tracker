package com.trex.liferhythm

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.trex.liferhythm.data.Collector
import com.trex.liferhythm.data.Record
import com.trex.liferhythm.data.Store
import com.trex.liferhythm.data.UsageRepository
import com.trex.liferhythm.data.toRecord
import com.trex.liferhythm.engine.Interval
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray

class MainViewModel(app: Application) : AndroidViewModel(app) {
    private val store = Store(app)

    var onboarded by mutableStateOf(store.onboarded())
        private set
    var hasAccess by mutableStateOf(false)
        private set
    var loading by mutableStateOf(false)
        private set
    var records by mutableStateOf<List<Record>>(emptyList())
        private set
    var sessions by mutableStateOf<List<Interval>>(emptyList())
        private set
    var config by mutableStateOf(store.config())
        private set

    fun refresh() {
        val ctx = getApplication<Application>()
        hasAccess = UsageRepository(ctx).hasAccess()
        records = store.records()
        if (!hasAccess || loading) return
        loading = true
        val cfg = config
        viewModelScope.launch {
            try {
                val snap = withContext(Dispatchers.Default) { Collector.compute(ctx, cfg) }
                store.saveAll(snap.results.map { it.toRecord() })
                sessions = snap.sessions
                records = store.records()
            } finally {
                loading = false
            }
        }
    }

    fun finishOnboarding() {
        store.setOnboarded()
        onboarded = true
        refresh()
    }

    fun setNight(start: Int, end: Int) {
        store.saveNight(start, end)
        config = store.config()
        refresh()
    }

    fun deleteAll() {
        store.clearData()
        config = store.config()
        records = emptyList()
    }

    fun exportCsv(): String {
        val zone = ZoneId.systemDefault()
        val sb = StringBuilder("date,bedtime,wake,time_in_bed_min,estimated_sleep_min,awakenings,confidence\n")
        for (r in records.sortedBy { it.date }) {
            sb.append(LocalDate.ofEpochDay(r.date)).append(',')
                .append(Instant.ofEpochMilli(r.bedtime).atZone(zone).toLocalDateTime()).append(',')
                .append(Instant.ofEpochMilli(r.wake).atZone(zone).toLocalDateTime()).append(',')
                .append(r.spanMs / 60_000L).append(',')
                .append(r.sleepMs / 60_000L).append(',')
                .append(r.awakenings).append(',')
                .append(r.confidence).append('\n')
        }
        return sb.toString()
    }

    fun exportJson(): String {
        val arr = JSONArray()
        records.sortedBy { it.date }.forEach { arr.put(it.toJson()) }
        return arr.toString(2)
    }
}