package com.ghostkey.ime.suggestion

import com.ghostkey.transform.tier1.TransformFlag

sealed class SuggestionItem {
    data class WordSuggestion(val word: String) : SuggestionItem()
    data class TransformPreview(val original: String, val transformed: String, val processorName: String) : SuggestionItem()
    data class Warning(val flag: TransformFlag) : SuggestionItem()
    object Tier2Loading : SuggestionItem()
    data class Tier2Ready(val rewrittenText: String) : SuggestionItem()
}

data class SuggestionState(
    val wordSuggestions: List<String> = emptyList(),
    val transformItems: List<SuggestionItem> = emptyList(),
    val tier2Status: Tier2Status = Tier2Status.Idle
)

sealed class Tier2Status {
    object Idle : Tier2Status()
    object Loading : Tier2Status()
    data class Ready(val rewrittenText: String) : Tier2Status()
    data class Error(val message: String) : Tier2Status()
}
