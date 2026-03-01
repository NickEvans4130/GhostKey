package com.ghostkey.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ghostkey.data.alias.AliasCache
import com.ghostkey.data.baseline.UserStyleBaseline
import com.ghostkey.data.profile.StyleProfile
import com.ghostkey.data.profile.StyleProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class DashboardUiState(
    val activeProfile: StyleProfile? = null,
    val profiles: List<StyleProfile> = emptyList(),
    val isStyleActive: Boolean = true,
    val wordsTypedToday: Int = 0,
    val transformsAppliedToday: Int = 0
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val profileRepository: StyleProfileRepository
) : ViewModel() {

    val uiState: StateFlow<DashboardUiState> = profileRepository.getAllProfiles()
        .map { profiles -> DashboardUiState(profiles = profiles, activeProfile = profiles.firstOrNull()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardUiState())
}
