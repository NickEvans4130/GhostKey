package com.ghostkey.transform.tier2

import com.ghostkey.data.profile.StyleProfile

object PromptBuilder {

    fun build(text: String, profile: StyleProfile): String {
        val formalityDesc = when {
            profile.formalityLevel < 0.3f -> "casual and conversational"
            profile.formalityLevel < 0.6f -> "neutral and balanced"
            else -> "formal and professional"
        }
        val sentenceDesc = "average ${profile.avgSentenceLengthTarget} words per sentence"

        return """
You are a writing style assistant. Rewrite the following text to match this persona:
- Nationality: ${profile.personaNationality ?: "unspecified"}
- Age: ${profile.personaAge ?: "unspecified"}
- Occupation: ${profile.personaOccupation ?: "unspecified"}
- Formality: $formalityDesc
- Sentence style: $sentenceDesc
- Spelling: ${profile.spellingVariant.name.lowercase()}

Rules:
- Preserve all factual content exactly
- Change only style, not meaning
- Do not add or remove information
- Output ONLY the rewritten text, no explanation

Text to rewrite:
$text
        """.trimIndent()
    }
}
