package com.ghostkey.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ghostkey.ui.navigation.Screen

@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "GhostKey",
            style = MaterialTheme.typography.titleLarge
        )

        // Active alias card
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = state.activeProfile?.aliasName ?: "No active profile",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Words today: ${state.wordsTypedToday} | Transforms: ${state.transformsAppliedToday}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        // Quick-switch profiles row
        if (state.profiles.isNotEmpty()) {
            Text("Profiles", style = MaterialTheme.typography.labelLarge)
            state.profiles.take(4).forEach { profile ->
                OutlinedButton(
                    onClick = { navController.navigate(Screen.ProfileEditor.createRoute(profile.id)) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(profile.aliasName)
                }
            }
        } else {
            Text(
                text = "No profiles yet. Create one in Profiles.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(Modifier.weight(1f))

        // Enable keyboard prompt
        Button(
            onClick = { /* Deep link to IME settings — implemented in feature/companion-polish */ },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Enable GhostKey Keyboard")
        }
    }
}
