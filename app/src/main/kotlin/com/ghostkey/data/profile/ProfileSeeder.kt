package com.ghostkey.data.profile

import com.ghostkey.data.alias.AliasCache
import java.util.UUID

object ProfileSeeder {

    fun fromAlias(alias: AliasCache): StyleProfile {
        val nationality = alias.nationality?.lowercase() ?: ""
        val age = alias.age
        val occupation = alias.occupation?.lowercase() ?: ""

        // Spelling variant + intensifier defaults from nationality
        var spellingVariant = SpellingVariant.NONE
        var oxfordComma = true
        var preferredIntensifier = "very"

        when {
            nationality.contains("british") || nationality.contains("uk") ||
            nationality.contains("english") || nationality.contains("scottish") ||
            nationality.contains("welsh") || nationality.contains("irish") -> {
                spellingVariant = SpellingVariant.BRITISH
                oxfordComma = false
                preferredIntensifier = "rather"
            }
            nationality.contains("american") || nationality.contains("us") ||
            nationality.contains("canadian") || nationality.contains("australian") -> {
                spellingVariant = SpellingVariant.AMERICAN
                oxfordComma = true
                preferredIntensifier = "really"
            }
        }

        // Age-based defaults
        var contractionFrequency = 0.6f
        var formalityLevel = 0.5f
        var ellipsisFrequency = 0.1f
        var exclamationFrequency = 0.1f
        var vocabularyComplexity = 0.5f
        var avgSentenceLengthTarget = 16

        if (age != null) {
            when {
                age < 25 -> {
                    contractionFrequency = 0.9f
                    formalityLevel = 0.2f
                    ellipsisFrequency = 0.4f
                    exclamationFrequency = 0.35f
                    vocabularyComplexity = 0.3f
                    avgSentenceLengthTarget = 12
                }
                age in 25..44 -> {
                    contractionFrequency = 0.6f
                    formalityLevel = 0.45f
                    avgSentenceLengthTarget = 16
                }
                age >= 45 -> {
                    contractionFrequency = 0.3f
                    formalityLevel = 0.7f
                    vocabularyComplexity = 0.65f
                    avgSentenceLengthTarget = 20
                }
            }
        }

        // Occupation-based overrides
        var passiveVoiceFrequency = 0.2f
        val isLegalMedical = occupation.containsAny(
            "lawyer", "solicitor", "barrister", "legal", "attorney",
            "doctor", "physician", "surgeon", "medical", "nurse", "pharmacist"
        )
        val isAcademic = occupation.containsAny(
            "professor", "academic", "researcher", "scientist", "analyst"
        )
        if (isLegalMedical) {
            vocabularyComplexity = 0.8f
            passiveVoiceFrequency = 0.5f
            formalityLevel = maxOf(formalityLevel, 0.7f)
        }
        if (isAcademic) {
            vocabularyComplexity = 0.75f
            passiveVoiceFrequency = 0.4f
            formalityLevel = maxOf(formalityLevel, 0.65f)
        }

        val educationLevel = when (alias.educationLevel?.lowercase()) {
            "school" -> EducationLevel.SCHOOL
            "undergraduate" -> EducationLevel.UNDERGRADUATE
            "postgraduate" -> EducationLevel.POSTGRADUATE
            "doctoral" -> EducationLevel.DOCTORAL
            else -> null
        }

        val now = System.currentTimeMillis()
        return StyleProfile(
            id = alias.id,
            aliasName = alias.aliasName,
            linkedGhostIdAlias = true,
            contractionFrequency = contractionFrequency,
            vocabularyComplexity = vocabularyComplexity,
            oxfordComma = oxfordComma,
            preferredIntensifier = preferredIntensifier,
            spellingVariant = spellingVariant,
            avgSentenceLengthTarget = avgSentenceLengthTarget,
            sentenceLengthVariance = 0.4f,
            passiveVoiceFrequency = passiveVoiceFrequency,
            subordinateClauseFrequency = 0.3f,
            emDashVsParentheses = 0.5f,
            ellipsisFrequency = ellipsisFrequency,
            exclamationFrequency = exclamationFrequency,
            oxfordCommaEnabled = oxfordComma,
            formalityLevel = formalityLevel,
            politenessMarkers = formalityLevel > 0.5f,
            numberStyle = NumberStyle.MIXED,
            personaNationality = alias.nationality,
            personaAge = alias.age,
            personaOccupation = alias.occupation,
            personaEducationLevel = educationLevel,
            avoidedPhrases = emptyList(),
            signaturePhrases = emptyList(),
            createdAt = now,
            lastUsed = now
        )
    }

    private fun String.containsAny(vararg terms: String): Boolean =
        terms.any { this.contains(it) }
}
