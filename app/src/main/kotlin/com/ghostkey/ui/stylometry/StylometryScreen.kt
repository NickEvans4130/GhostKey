package com.ghostkey.ui.stylometry

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ghostkey.ui.theme.Cyan
import com.ghostkey.ui.theme.Amber

private val RADAR_LABELS = listOf(
    "Sent. Length", "Type-Token", "Contractions",
    "Formality", "Passive", "Ellipsis", "Exclamation", "Avg Word Len"
)

@Composable
fun StylometryScreen(viewModel: StylometryViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()
    val baseline = state.baseline

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Stylometry Analysis", style = MaterialTheme.typography.titleLarge)

        if (baseline == null || baseline.wordsSampled < 50) {
            Text(
                "Not enough data yet. Type at least 50 words to see your writing baseline.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            val userValues = listOf(
                baseline.meanSentenceLength / 35f,
                baseline.typeTokenRatio,
                baseline.contractionFrequency,
                0f,
                baseline.passiveVoiceRatio,
                baseline.ellipsisFrequency * 10f,
                baseline.exclamationFrequency * 10f,
                baseline.avgWordLength / 8f
            )
            val aliasValues = List(8) { 0f }

            RadarChart(
                userValues = userValues,
                aliasValues = aliasValues,
                labels = RADAR_LABELS,
                modifier = Modifier.size(280.dp).align(Alignment.CenterHorizontally)
            )

            Text(
                "Divergence score: ${(state.divergenceScore * 100).toInt()}%",
                style = MaterialTheme.typography.labelLarge
            )
            Text(
                "Words sampled: ${baseline.wordsSampled}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
