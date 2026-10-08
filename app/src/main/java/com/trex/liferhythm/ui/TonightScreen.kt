package com.trex.liferhythm.ui
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.trex.liferhythm.MainViewModel
@Composable
fun TonightScreen(vm: MainViewModel) {
    val ctx = LocalContext.current
    val latest = vm.records.maxByOrNull { it.date }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { ScreenTitle("Tonight", "Your latest estimate.") }
        if (!vm.hasAccess) item {
            LrCard {
                Eyebrow("Usage Access")
                Spacer(Modifier.height(8.dp))
                Text("Connect Usage Access so the app can read phone activity.")
                Spacer(Modifier.height(12.dp))
                GoldButton("Open settings", { openUsageSettings(ctx) })
            }
        }
        item {
            LrCard {
                Eyebrow("Latest night")
                Spacer(Modifier.height(8.dp))
                if (latest == null) Text("No estimate yet.", style = MaterialTheme.typography.headlineMedium)
                else {
                    Text(fmtDur(latest.sleepMs), style = MaterialTheme.typography.displayLarge)
                    Text("Estimated duration")
                    Spacer(Modifier.height(12.dp))
                    StatRow("Start", fmtTime(latest.bedtime))
                    StatRow("End", fmtTime(latest.wake))
                    StatRow("Confidence", latest.confidenceEnum().label, false)
                }
            }
        }
    }
}