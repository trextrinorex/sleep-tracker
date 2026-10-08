package com.trex.liferhythm.ui
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.trex.liferhythm.MainViewModel
import com.trex.liferhythm.data.Record
@Composable fun HistoryScreen(vm: MainViewModel) {
    val records = vm.records.sortedByDescending { it.date }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { ScreenTitle("History", "Your estimated nights, newest first.") }
        items(records, key = { it.date }) { r -> HistoryRow(r) }
        if (records.isEmpty()) item { LrCard { Eyebrow("Nothing here yet"); Spacer(Modifier.height(8.dp)); Text("Use the phone normally after connecting Usage Access.") } }
    }
}
@Composable private fun HistoryRow(r: Record) {
    LrCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) { Text(fmtDay(r.date)); Text(fmtDur(r.sleepMs), style = MaterialTheme.typography.headlineMedium) }
            Text(r.confidenceEnum().label)
        }
        Spacer(Modifier.height(8.dp))
        Text(fmtTime(r.bedtime) + " → " + fmtTime(r.wake))
        Text(r.awakenings.toString() + " awakenings", color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}