package com.ghostkey.transform.stylometry

import com.ghostkey.data.baseline.BaselineDao
import com.ghostkey.data.baseline.UserStyleBaseline
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

// Full implementation in feature/stylometry
@Singleton
class StylometryAnalyser @Inject constructor(
    private val baselineDao: BaselineDao
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val wordBuffer = ArrayDeque<String>(500)

    fun onTextCommitted(text: String) {
        val words = text.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
        words.forEach { word ->
            if (wordBuffer.size >= 500) wordBuffer.removeFirst()
            wordBuffer.addLast(word)
        }
        if (wordBuffer.size >= 50) {
            scope.launch { analyse() }
        }
    }

    private suspend fun analyse() {
        // Feature analysis implemented in feature/stylometry
        val baseline = UserStyleBaseline(wordsSampled = wordBuffer.size)
        baselineDao.saveBaseline(baseline)
    }
}
