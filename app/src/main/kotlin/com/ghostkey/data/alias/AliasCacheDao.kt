package com.ghostkey.data.alias

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AliasCacheDao {

    @Query("SELECT * FROM alias_cache ORDER BY aliasName ASC")
    fun getAllAliases(): Flow<List<AliasCache>>

    @Query("SELECT * FROM alias_cache WHERE id = :id")
    suspend fun getAliasById(id: String): AliasCache?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(aliases: List<AliasCache>)

    @Query("DELETE FROM alias_cache")
    suspend fun clearAll()
}
