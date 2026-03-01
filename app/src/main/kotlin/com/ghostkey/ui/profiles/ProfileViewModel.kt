package com.ghostkey.ui.profiles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ghostkey.data.profile.StyleProfile
import com.ghostkey.data.profile.StyleProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: StyleProfileRepository
) : ViewModel() {

    val profiles: StateFlow<List<StyleProfile>> = repository.getAllProfiles()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun deleteProfile(profile: StyleProfile) = viewModelScope.launch {
        repository.deleteProfile(profile)
    }

    fun saveProfile(profile: StyleProfile) = viewModelScope.launch {
        repository.saveProfile(profile)
    }
}
