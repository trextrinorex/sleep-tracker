package com.trex.liferhythm.ui
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.trex.liferhythm.MainViewModel
@Composable fun OnboardingScreen(vm: MainViewModel) {
    val ctx = LocalContext.current
    Column(Modifier.fillMaxSize().padding(28.dp), verticalArrangement = Arrangement.SpaceBetween) {
        Column {
            Eyebrow("LifeRhythm")
            Spacer(Modifier.height(30.dp))
            Text("Your day has a rhythm.", style = MaterialTheme.typography.displayMedium)
            Spacer(Modifier.height(20.dp))
            Text("LifeRhythm estimates sleep from long quiet stretches in your phone usage. It is private and works offline.")
            Spacer(Modifier.height(28.dp))
            Text(if (vm.hasAccess) "Usage Access is connected." else "Usage Access is required for estimates.")
            Spacer(Modifier.height(12.dp))
            QuietButton("Open Usage Access settings", { openUsageSettings(ctx) }, Modifier.fillMaxWidth())
        }
        GoldButton("Begin", { vm.finishOnboarding() })
    }
}