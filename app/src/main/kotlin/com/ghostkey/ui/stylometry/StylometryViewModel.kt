package com.ghostkey.ui.stylometry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ghostkey.data.baseline.BaselineDao
import com.ghostkey.data.baseline.UserStyleBaseline
import com.ghostkey.data.profile.StyleProfileRepository
import com.ghostkey.transform.stylometry.DivergenceCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class StylometryUiState(
    val baseline: UserStyleBaseline? = null,
    val divergenceScore: Float = 0f
)

@HiltViewModel
class StylometryViewModel @Inject constructor(
    baselineDao: BaselineDao,
    profileRepository: StyleProfileRepository,
    private val divergenceCalculator: DivergenceCalculator
) : ViewModel() {

    val uiState: StateFlow<StylometryUiState> = baselineDao.getBaseline()
        .map { baseline -> StylometryUiState(baseline = baseline) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StylometryUiState())
}
