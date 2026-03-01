package com.ghostkey.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// Full settings implemented in feature/companion-polish
@Composable
fun SettingsScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Settings", style = MaterialTheme.typography.titleLarge)
        HorizontalDivider()

        SettingsGroup("Keyboard Appearance") {
            Text("Dark / Light / AMOLED theme — feature/companion-polish", style = MaterialTheme.typography.bodyMedium)
        }
        SettingsGroup("Transform Behaviour") {
            Text("Tier 1 / Tier 2 toggles — feature/companion-polish", style = MaterialTheme.typography.bodyMedium)
        }
        SettingsGroup("GhostID Integration") {
            Text("Status, refresh, permission — feature/ghostid-integration", style = MaterialTheme.typography.bodyMedium)
        }
        SettingsGroup("Privacy") {
            Text("Logging toggle, baseline toggle — feature/companion-polish", style = MaterialTheme.typography.bodyMedium)
        }
        SettingsGroup("About") {
            Text("GhostKey v0.1.0", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun SettingsGroup(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        content()
        HorizontalDivider(modifier = Modifier.padding(top = 8.dp))
    }
}
