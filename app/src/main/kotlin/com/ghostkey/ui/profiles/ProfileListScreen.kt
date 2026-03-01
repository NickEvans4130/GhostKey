package com.ghostkey.ui.profiles

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {
                navController.navigate(Screen.ProfileEditor.createRoute("new"))
            }) {
                Icon(Icons.Default.Add, contentDescription = "Create profile")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Text("Style Profiles", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(8.dp))
            }
            if (profiles.isEmpty()) {
                item {
                    Text(
                        "No profiles. Tap + to create one.",
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
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(profile.aliasName, style = MaterialTheme.typography.titleMedium)
                            Text(
                                "Formality: ${(profile.formalityLevel * 100).toInt()}% | ${profile.spellingVariant.name}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        if (profile.linkedGhostIdAlias) {
                            Icon(
                                Icons.Default.Link,
                                contentDescription = "GhostID linked",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}
