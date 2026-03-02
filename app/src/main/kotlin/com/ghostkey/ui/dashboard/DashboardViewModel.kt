package com.ghostkey.ui.dashboard

import android.content.Context
import android.view.inputmethod.InputMethodManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ghostkey.data.profile.StyleProfile
import com.ghostkey.data.profile.StyleProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class DashboardUiState(
    val activeProfile: StyleProfile? = null,
    val profiles: List<StyleProfile> = emptyList(),
    val isStyleActive: Boolean = true,
    val wordsTypedToday: Int = 0,
    val transformsAppliedToday: Int = 0,
    val isImeEnabled: Boolean = false,
    val isImeSelected: Boolean = false
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val profileRepository: StyleProfileRepository
) : ViewModel() {

    private val _imeState = MutableStateFlow(queryImeState())

    val uiState: StateFlow<DashboardUiState> = combine(
        profileRepository.getAllProfiles(),
        _imeState
    ) { profiles, ime ->
        DashboardUiState(
            profiles = profiles,
            activeProfile = profiles.firstOrNull(),
            isImeEnabled = ime.first,
            isImeSelected = ime.second
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardUiState())

    fun refreshImeStatus() {
        _imeState.value = queryImeState()
    }

    private fun queryImeState(): Pair<Boolean, Boolean> {
        val imm = context.getSystemService(InputMethodManager::class.java)
        val enabled = imm.enabledInputMethodList.any { it.packageName == context.packageName }
        val selected = android.provider.Settings.Secure.getString(
            context.contentResolver,
            android.provider.Settings.Secure.DEFAULT_INPUT_METHOD
        )?.startsWith("${context.packageName}/") == true
        return Pair(enabled, selected)
    }
}
