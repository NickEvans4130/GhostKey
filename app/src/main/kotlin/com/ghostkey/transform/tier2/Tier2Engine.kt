package com.ghostkey.transform.tier2

import android.util.Log
import com.ghostkey.data.profile.StyleProfile
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import javax.inject.Singleton

// Full implementation in feature/tier2-gemma
@Singleton
class Tier2Engine @Inject constructor(
    private val modelManager: ModelManager
) {
    companion object {
        private const val TAG = "Tier2Engine"
        private const val TIMEOUT_MS = 30_000L
    }

    suspend fun rewrite(text: String, profile: StyleProfile): Result<String> {
        val modelPath = modelManager.getModelPath()
            ?: return Result.failure(IllegalStateException("Model not downloaded"))

        return withTimeoutOrNull(TIMEOUT_MS) {
            runCatching {
                val prompt = PromptBuilder.build(text, profile)
                Log.d(TAG, "Tier 2 rewrite requested — model: $modelPath")
                // MediaPipe LlmInference wired in feature/tier2-gemma
                text
            }
        } ?: Result.failure(Exception("Rewrite timed out"))
    }
}
