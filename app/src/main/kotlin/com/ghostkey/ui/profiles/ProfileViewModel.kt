package com.ghostkey.ui.profiles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ghostkey.data.alias.GhostIdProvider
import com.ghostkey.data.profile.ProfileSeeder
import com.ghostkey.data.profile.StyleProfile
import com.ghostkey.data.profile.StyleProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class GhostIdSyncState {
    object Idle : GhostIdSyncState()
    object Syncing : GhostIdSyncState()
    data class Success(val imported: Int) : GhostIdSyncState()
    data class Unavailable(val reason: String) : GhostIdSyncState()
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: StyleProfileRepository,
    private val ghostIdProvider: GhostIdProvider
) : ViewModel() {

    val profiles: StateFlow<List<StyleProfile>> = repository.getAllProfiles()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val syncState = MutableStateFlow<GhostIdSyncState>(GhostIdSyncState.Idle)

    fun syncFromGhostId() {
        if (syncState.value is GhostIdSyncState.Syncing) return
        viewModelScope.launch {
            syncState.value = GhostIdSyncState.Syncing
            ghostIdProvider.syncAliases().fold(
                onSuccess = { aliases ->
                    val existing = profiles.value.map { it.id }.toSet()
                    var imported = 0
                    aliases.forEach { alias ->
                        if (alias.id !in existing) {
                            repository.saveProfile(ProfileSeeder.fromAlias(alias))
                            imported++
                        }
                    }
                    syncState.value = GhostIdSyncState.Success(imported)
                },
                onFailure = { e ->
                    syncState.value = GhostIdSyncState.Unavailable(
                        e.message ?: "GhostID not available"
                    )
                }
            )
        }
    }

    fun deleteProfile(profile: StyleProfile) = viewModelScope.launch {
        repository.deleteProfile(profile)
    }

    fun saveProfile(profile: StyleProfile) = viewModelScope.launch {
        repository.saveProfile(profile)
    }
}
