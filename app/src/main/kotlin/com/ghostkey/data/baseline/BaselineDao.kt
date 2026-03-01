package com.ghostkey.data.baseline

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BaselineDao {

    @Query("SELECT * FROM user_style_baseline WHERE id = 1")
    fun getBaseline(): Flow<UserStyleBaseline?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveBaseline(baseline: UserStyleBaseline)
}
