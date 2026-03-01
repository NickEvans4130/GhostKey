package com.ghostkey.data.profile

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

enum class SpellingVariant { BRITISH, AMERICAN, NONE }
enum class NumberStyle { WORDS, NUMERALS, MIXED }
enum class EducationLevel { SCHOOL, UNDERGRADUATE, POSTGRADUATE, DOCTORAL }

@Entity(tableName = "style_profiles")
@TypeConverters(StyleProfileConverters::class)
data class StyleProfile(
    @PrimaryKey val id: String,
    val aliasName: String,
    val linkedGhostIdAlias: Boolean,

    // Lexical
    val contractionFrequency: Float,
    val vocabularyComplexity: Float,
    val oxfordComma: Boolean,
    val preferredIntensifier: String,
    val spellingVariant: SpellingVariant,

    // Syntactic
    val avgSentenceLengthTarget: Int,
    val sentenceLengthVariance: Float,
    val passiveVoiceFrequency: Float,
    val subordinateClauseFrequency: Float,

    // Punctuation
    val emDashVsParentheses: Float,
    val ellipsisFrequency: Float,
    val exclamationFrequency: Float,
    val oxfordCommaEnabled: Boolean,

    // Register
    val formalityLevel: Float,
    val politenessMarkers: Boolean,
    val numberStyle: NumberStyle,

    // Persona metadata
    val personaNationality: String?,
    val personaAge: Int?,
    val personaOccupation: String?,
    val personaEducationLevel: EducationLevel?,

    // Phrases
    val avoidedPhrases: List<String>,
    val signaturePhrases: List<String>,

    val createdAt: Long,
    val lastUsed: Long
)

class StyleProfileConverters {
    private val json = Json { ignoreUnknownKeys = true }

    @TypeConverter
    fun fromStringList(value: List<String>): String = json.encodeToString(value)

    @TypeConverter
    fun toStringList(value: String): List<String> = json.decodeFromString(value)

    @TypeConverter
    fun fromSpellingVariant(value: SpellingVariant): String = value.name

    @TypeConverter
    fun toSpellingVariant(value: String): SpellingVariant = SpellingVariant.valueOf(value)

    @TypeConverter
    fun fromNumberStyle(value: NumberStyle): String = value.name

    @TypeConverter
    fun toNumberStyle(value: String): NumberStyle = NumberStyle.valueOf(value)

    @TypeConverter
    fun fromEducationLevel(value: EducationLevel?): String? = value?.name

    @TypeConverter
    fun toEducationLevel(value: String?): EducationLevel? = value?.let { EducationLevel.valueOf(it) }
}
