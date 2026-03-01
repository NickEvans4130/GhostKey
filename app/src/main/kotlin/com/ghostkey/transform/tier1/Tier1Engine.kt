package com.ghostkey.transform.tier1

import com.ghostkey.data.profile.StyleProfile
import com.ghostkey.util.profileRandom
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Tier1Engine @Inject constructor() {

    fun transform(text: String, profile: StyleProfile): TransformResult {
        if (text.isBlank()) return TransformResult(text, text)

        val random = profile.profileRandom()
        val sentences = SentenceTokeniser.tokenise(text)
        val changes = mutableListOf<TransformChange>()
        val flags = mutableListOf<TransformFlag>()

        val transformed = sentences.joinToString(" ") { sentence ->
            var s = sentence

            // Pipeline order per spec:
            // 1. Spelling variant
            s = SpellingVariantProcessor.process(s, profile.spellingVariant)
            // 2. Contraction normalisation
            s = ContractionProcessor.process(s, profile.contractionFrequency, random)
            // 3. Number style
            s = NumberStyleProcessor.process(s, profile.numberStyle, profile.formalityLevel)
            // 4. Intensifier substitution
            s = IntensifierProcessor.process(s, profile.preferredIntensifier, 0.5f, random)
            // 5. Ellipsis / exclamation
            s = PunctuationProcessor.processEllipsis(s, profile.ellipsisFrequency, random)
            s = PunctuationProcessor.processExclamation(s, profile.exclamationFrequency, random)
            // 6. Em dash / parentheses
            s = PunctuationProcessor.processEmDash(s, profile.emDashVsParentheses)
            // 7. Oxford comma
            s = PunctuationProcessor.processOxfordComma(s, profile.oxfordCommaEnabled)
            // 8. Formality markers
            s = FormalityProcessor.process(s, profile.formalityLevel)
            // 9. Avoided phrase detection (flags only)
            profile.avoidedPhrases.forEach { phrase ->
                val idx = s.indexOf(phrase, ignoreCase = true)
                if (idx >= 0) {
                    flags.add(
                        TransformFlag(
                            type = FlagType.AVOIDED_PHRASE,
                            message = "Avoided phrase: \"$phrase\"",
                            startIndex = idx,
                            endIndex = idx + phrase.length
                        )
                    )
                }
            }

            s
        }

        return TransformResult(
            originalText = text,
            transformedText = transformed,
            changes = changes,
            flags = flags
        )
    }
}
