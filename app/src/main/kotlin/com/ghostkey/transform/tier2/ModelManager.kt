package com.ghostkey.transform.tier2

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

sealed class ModelStatus {
    object NotDownloaded : ModelStatus()
    data class Downloading(val progress: Float) : ModelStatus()
    object Ready : ModelStatus()
    data class Error(val message: String) : ModelStatus()
}

@Singleton
class ModelManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val MODEL_FILENAME = "gemma-2b-it-gpu-int4.bin"
        private const val MODEL_URL = "https://huggingface.co/google/gemma-2b-it-gpu-int4/resolve/main/gemma-2b-it-gpu-int4.bin"
    }

    private val _status = MutableStateFlow<ModelStatus>(ModelStatus.NotDownloaded)
    val status: StateFlow<ModelStatus> = _status

    val modelFile: File get() = File(context.filesDir, "models/$MODEL_FILENAME")

    init {
        _status.value = if (modelFile.exists()) ModelStatus.Ready else ModelStatus.NotDownloaded
    }

    fun getModelPath(): String? = if (modelFile.exists()) modelFile.absolutePath else null

    fun deleteModel() {
        modelFile.delete()
        _status.value = ModelStatus.NotDownloaded
    }
}
