package com.ghostkey.data.baseline

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_style_baseline")
data class UserStyleBaseline(
    @PrimaryKey val id: Int = 1,
    val meanSentenceLength: Float = 0f,
    val sentenceLengthStdDev: Float = 0f,
    val typeTokenRatio: Float = 0f,
    val contractionFrequency: Float = 0f,
    val avgWordLength: Float = 0f,
    val commaFrequency: Float = 0f,
    val periodFrequency: Float = 0f,
    val exclamationFrequency: Float = 0f,
    val questionFrequency: Float = 0f,
    val emDashFrequency: Float = 0f,
    val ellipsisFrequency: Float = 0f,
    val passiveVoiceRatio: Float = 0f,
    val wordsSampled: Int = 0,
    val updatedAt: Long = System.currentTimeMillis()
)
