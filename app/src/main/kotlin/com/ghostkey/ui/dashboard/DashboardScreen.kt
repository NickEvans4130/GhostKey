package com.ghostkey.ui.dashboard

import android.content.Intent
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.ghostkey.ui.navigation.Screen

@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Re-check IME status whenever the screen resumes (e.g. returning from Settings)
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.refreshImeStatus()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

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

        // Enable / switch keyboard prompt
        val imeButtonLabel = when {
            !state.isImeEnabled -> "Enable GhostKey Keyboard"
            !state.isImeSelected -> "Switch to GhostKey"
            else -> "GhostKey is active"
        }

        Button(
            onClick = {
                if (!state.isImeEnabled) {
                    context.startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    })
                } else if (!state.isImeSelected) {
                    val imm = context.getSystemService(InputMethodManager::class.java)
                    imm.showInputMethodPicker()
                }
                // If already active, button is a no-op
            },
            enabled = !state.isImeSelected,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text(imeButtonLabel)
        }
    }
}
