package com.trex.liferhythm.ui
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.trex.liferhythm.MainViewModel
@Composable fun SettingsScreen(vm: MainViewModel) {
    val ctx = LocalContext.current
    var confirm by remember { mutableStateOf(false) }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { ScreenTitle("Settings", "Tune the estimate and manage your data.") }
        item { LrCard {
            Eyebrow("Night window")
            HourStepper("Night begins", vm.config.nightStartHour) { vm.setNight(it, vm.config.nightEndHour) }
            HourStepper("Night ends", vm.config.nightEndHour) { vm.setNight(vm.config.nightStartHour, it) }
        }}
        item { LrCard {
            Eyebrow("Permission")
            StatRow("Usage Access", if (vm.hasAccess) "Connected" else "Not connected", false)
            QuietButton("Open Usage Access settings", { openUsageSettings(ctx) }, Modifier.fillMaxWidth())
        }}
        item { LrCard {
            Eyebrow("Your data")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                QuietButton("Export CSV", { shareText(ctx, "LifeRhythm CSV", vm.exportCsv()) }, Modifier.weight(1f))
                QuietButton("Export JSON", { shareText(ctx, "LifeRhythm JSON", vm.exportJson()) }, Modifier.weight(1f))
            }
            TextButton({ confirm = true }, Modifier.fillMaxWidth()) { Text("Delete all my data", color = MaterialTheme.colorScheme.error) }
        }}
    }
    if (confirm) AlertDialog(onDismissRequest = { confirm = false }, title = { Text("Delete all data?") },
        text = { Text("This removes saved LifeRhythm nights and resets the night window.") },
        confirmButton = { TextButton({ vm.deleteAll(); confirm = false }) { Text("Delete", color = MaterialTheme.colorScheme.error) } },
        dismissButton = { TextButton({ confirm = false }) { Text("Cancel") } })
}
@Composable private fun HourStepper(label: String, hour: Int, onChange: (Int) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, Modifier.weight(1f))
        TextButton({ onChange((hour + 23) % 24) }) { Text("-") }
        Text(fmtHour(hour))
        TextButton({ onChange((hour + 1) % 24) }) { Text("+") }
    }
}