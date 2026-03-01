package com.ghostkey.data.profile

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface StyleProfileDao {

    @Query("SELECT * FROM style_profiles ORDER BY lastUsed DESC")
    fun getAllProfiles(): Flow<List<StyleProfile>>

    @Query("SELECT * FROM style_profiles WHERE id = :id")
    suspend fun getProfileById(id: String): StyleProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: StyleProfile)

    @Update
    suspend fun updateProfile(profile: StyleProfile)

    @Delete
    suspend fun deleteProfile(profile: StyleProfile)

    @Query("DELETE FROM style_profiles WHERE id = :id")
    suspend fun deleteProfileById(id: String)
}
