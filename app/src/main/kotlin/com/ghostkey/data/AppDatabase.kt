package com.ghostkey.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.ghostkey.data.alias.AliasCache
import com.ghostkey.data.alias.AliasCacheDao
import com.ghostkey.data.baseline.BaselineDao
import com.ghostkey.data.baseline.UserStyleBaseline
import com.ghostkey.data.history.HistoryDao
import com.ghostkey.data.history.TransformEvent
import com.ghostkey.data.history.TransformSession
import com.ghostkey.data.profile.StyleProfile
import com.ghostkey.data.profile.StyleProfileConverters
import com.ghostkey.data.profile.StyleProfileDao

@Database(
    entities = [
        StyleProfile::class,
        AliasCache::class,
        UserStyleBaseline::class,
        TransformSession::class,
        TransformEvent::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(StyleProfileConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun styleProfileDao(): StyleProfileDao
    abstract fun aliasCacheDao(): AliasCacheDao
    abstract fun baselineDao(): BaselineDao
    abstract fun historyDao(): HistoryDao
}
