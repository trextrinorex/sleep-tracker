package com.trex.liferhythm.ui
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.trex.liferhythm.MainViewModel
@Composable
fun TimelineScreen(vm: MainViewModel) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp)) {
        item {
            ScreenTitle("Timeline", "Recent phone activity.")
            Spacer(Modifier.height(20.dp))
            LrCard {
                Eyebrow("Activity")
                Spacer(Modifier.height(8.dp))
                Text(vm.sessions.size.toString() + " sessions detected.")
            }
        }
    }
}