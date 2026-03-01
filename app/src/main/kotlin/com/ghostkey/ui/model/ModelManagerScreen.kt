package com.ghostkey.ui.model

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ghostkey.transform.tier2.ModelStatus

@Composable
fun ModelManagerScreen(viewModel: ModelViewModel = hiltViewModel()) {
    val status by viewModel.status.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Gemma 2B Model", style = MaterialTheme.typography.titleLarge)

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                val statusText = when (status) {
                    is ModelStatus.NotDownloaded -> "Not downloaded"
                    is ModelStatus.Downloading -> "Downloading: ${((status as ModelStatus.Downloading).progress * 100).toInt()}%"
                    is ModelStatus.Ready -> "Ready"
                    is ModelStatus.Error -> "Error: ${(status as ModelStatus.Error).message}"
                }
                Text("Status: $statusText", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(4.dp))
                Text("Size: ~1.3 GB", style = MaterialTheme.typography.bodySmall)
            }
        }

        when (status) {
            is ModelStatus.NotDownloaded, is ModelStatus.Error -> {
                Button(
                    onClick = { /* Download worker — implemented in feature/tier2-gemma */ },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Download Gemma 2B INT4")
                }
            }
            is ModelStatus.Downloading -> {
                val progress = (status as ModelStatus.Downloading).progress
                LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
            }
            is ModelStatus.Ready -> {
                OutlinedButton(
                    onClick = viewModel::deleteModel,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Delete Model")
                }
            }
        }

        Text(
            text = "The Gemma 2B INT4 model enables paragraph-level style rewrites (Tier 2). " +
                    "Tier 1 rule-based transforms work without the model.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
