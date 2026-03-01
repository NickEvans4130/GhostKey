package com.ghostkey.data.alias

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alias_cache")
data class AliasCache(
    @PrimaryKey val id: String,
    val aliasName: String,
    val avatarUrl: String?,
    val nationality: String?,
    val age: Int?,
    val occupation: String?,
    val educationLevel: String?,
    val accentColour: String?,
    val cachedAt: Long = System.currentTimeMillis()
)
