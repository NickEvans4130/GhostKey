package com.ghostkey.data.history

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transform_sessions")
data class TransformSession(
    @PrimaryKey val id: String,
    val aliasId: String,
    val startedAt: Long,
    val endedAt: Long?,
    val wordsTyped: Int = 0,
    val transformsApplied: Int = 0,
    val tier2Rewrites: Int = 0,
    val avoidedPhrasesCaught: Int = 0
)
