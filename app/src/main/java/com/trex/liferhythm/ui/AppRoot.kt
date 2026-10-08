package com.trex.liferhythm.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.trex.liferhythm.MainViewModel

@Composable
fun AppRoot(vm: MainViewModel) {
    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        if (!vm.onboarded) OnboardingScreen(vm) else MainScaffold(vm)
    }
}

@Composable
private fun MainScaffold(vm: MainViewModel) {
    var tab by rememberSaveable { mutableStateOf(0) }
    val tabs = listOf(
        "Tonight" to Icons.Filled.Home,
        "Timeline" to Icons.Filled.List,
        "History" to Icons.Filled.DateRange,
        "Settings" to Icons.Filled.Settings
    )
    val scheme = MaterialTheme.colorScheme
    Scaffold(
        containerColor = scheme.background,
        bottomBar = {
            NavigationBar(containerColor = scheme.surface) {
                tabs.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = tab == index,
                        onClick = { tab = index },
                        icon = { Icon(item.second, contentDescription = item.first) },
                        label = { Text(item.first) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = scheme.primary,
                            selectedTextColor = scheme.primary,
                            indicatorColor = scheme.surfaceVariant,
                            unselectedIconColor = scheme.onSurfaceVariant,
                            unselectedTextColor = scheme.onSurfaceVariant
                        )
                    )
                }
            }
        }
    ) { pad ->
        Box(Modifier.fillMaxSize().padding(pad)) {
            when (tab) {
                0 -> TonightScreen(vm)
                1 -> TimelineScreen(vm)
                2 -> HistoryScreen(vm)
                else -> SettingsScreen(vm)
            }
        }
    }
}