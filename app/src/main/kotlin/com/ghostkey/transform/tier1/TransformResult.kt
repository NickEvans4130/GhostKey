package com.ghostkey.transform.tier1

data class TransformResult(
    val originalText: String,
    val transformedText: String,
    val changes: List<TransformChange> = emptyList(),
    val flags: List<TransformFlag> = emptyList()
) {
    val wasModified: Boolean get() = originalText != transformedText
}

data class TransformChange(
    val processorName: String,
    val original: String,
    val transformed: String,
    val startIndex: Int
)

data class TransformFlag(
    val type: FlagType,
    val message: String,
    val startIndex: Int,
    val endIndex: Int
)

enum class FlagType {
    AVOIDED_PHRASE,
    PASSIVE_VOICE,
    SENTENCE_TOO_LONG,
    SENTENCE_TOO_SHORT,
    ACTIVE_VOICE_SUGGESTION
}
