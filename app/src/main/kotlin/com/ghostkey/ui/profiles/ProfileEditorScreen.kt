package com.ghostkey.ui.profiles

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

// Full editor implemented in feature/style-profiles
@Composable
fun ProfileEditorScreen(
    profileId: String,
    navController: NavController
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = if (profileId == "new") "New Profile" else "Edit Profile",
            style = MaterialTheme.typography.titleLarge
        )
        Text(
            text = "Full profile editor implemented in feature/style-profiles.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        HorizontalDivider()
        Text("Identity", style = MaterialTheme.typography.titleMedium)
        Text("Vocabulary", style = MaterialTheme.typography.titleMedium)
        Text("Sentences", style = MaterialTheme.typography.titleMedium)
        Text("Punctuation", style = MaterialTheme.typography.titleMedium)
        Text("Register", style = MaterialTheme.typography.titleMedium)
        Text("Phrases", style = MaterialTheme.typography.titleMedium)
    }
}
