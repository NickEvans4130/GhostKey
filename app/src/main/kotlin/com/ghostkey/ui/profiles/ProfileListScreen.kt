package com.ghostkey.ui.profiles

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ghostkey.ui.navigation.Screen

@Composable
fun ProfileListScreen(
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val profiles by viewModel.profiles.collectAsState()
    val syncState by viewModel.syncState.collectAsState()

    // Auto-sync from GhostID on first load when there are no profiles
    LaunchedEffect(Unit) {
        viewModel.syncFromGhostId()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                // Leave bottom room for FAB
                .padding(bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(top = 16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Style Profiles", style = MaterialTheme.typography.titleLarge)
                    IconButton(
                        onClick = viewModel::syncFromGhostId,
                        enabled = syncState !is GhostIdSyncState.Syncing
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Sync from GhostID")
                    }
                }

                // Sync status banner
                when (val s = syncState) {
                    is GhostIdSyncState.Syncing -> {
                        Spacer(Modifier.height(4.dp))
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        Text(
                            "Syncing from GhostID...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    is GhostIdSyncState.Success -> {
                        if (s.imported > 0) {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Imported ${s.imported} profile${if (s.imported > 1) "s" else ""} from GhostID",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    is GhostIdSyncState.Unavailable -> {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "GhostID unavailable — create profiles manually",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    else -> {}
                }

                Spacer(Modifier.height(8.dp))
            }

            if (profiles.isEmpty() && syncState !is GhostIdSyncState.Syncing) {
                item {
                    Text(
                        "No profiles yet. Tap + to create one or sync from GhostID.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            items(profiles, key = { it.id }) { profile ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { navController.navigate(Screen.ProfileEditor.createRoute(profile.id)) }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(profile.aliasName, style = MaterialTheme.typography.titleMedium)
                            Text(
                                buildString {
                                    append("Formality: ${(profile.formalityLevel * 100).toInt()}%")
                                    append(" | ${profile.spellingVariant.name.lowercase()}")
                                    profile.personaNationality?.let { append(" | $it") }
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (profile.linkedGhostIdAlias) {
                            Icon(
                                Icons.Default.Link,
                                contentDescription = "GhostID linked",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            }
        }

        // FAB clear of the outer bottom nav bar
        FloatingActionButton(
            onClick = { navController.navigate(Screen.ProfileEditor.createRoute("new")) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Create profile")
        }
    }
}
