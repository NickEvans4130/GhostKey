package com.ghostkey.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ghostkey.data.history.HistoryDao
import com.ghostkey.data.history.TransformSession
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val historyDao: HistoryDao
) : ViewModel() {

    val sessions: StateFlow<List<TransformSession>> = historyDao.getAllSessions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun purgeOlderThan(days: Int) = viewModelScope.launch {
        val cutoff = System.currentTimeMillis() - days * 24 * 60 * 60 * 1000L
        historyDao.purgeSessionsBefore(cutoff)
        historyDao.purgeOrphanedEvents()
    }
}
