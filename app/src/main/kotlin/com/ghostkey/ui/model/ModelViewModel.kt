package com.ghostkey.ui.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ghostkey.transform.tier2.ModelManager
import com.ghostkey.transform.tier2.ModelStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ModelViewModel @Inject constructor(
    private val modelManager: ModelManager
) : ViewModel() {

    val status: StateFlow<ModelStatus> = modelManager.status

    fun deleteModel() = viewModelScope.launch {
        modelManager.deleteModel()
    }
}
