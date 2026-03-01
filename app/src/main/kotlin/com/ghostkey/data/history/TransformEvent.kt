package com.ghostkey.data.history

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transform_events")
data class TransformEvent(
    @PrimaryKey val id: String,
    val sessionId: String,
    val timestamp: Long,
    val eventType: String,
    val original: String,
    val transformed: String,
    val processorName: String
)
