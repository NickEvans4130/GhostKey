package com.ghostkey.di

import android.content.Context
import androidx.room.Room
import com.ghostkey.data.AppDatabase
import com.ghostkey.data.alias.AliasCacheDao
import com.ghostkey.data.baseline.BaselineDao
import com.ghostkey.data.history.HistoryDao
import com.ghostkey.data.profile.StyleProfileDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "ghostkey.db").build()

    @Provides
    fun provideStyleProfileDao(db: AppDatabase): StyleProfileDao = db.styleProfileDao()

    @Provides
    fun provideAliasCacheDao(db: AppDatabase): AliasCacheDao = db.aliasCacheDao()

    @Provides
    fun provideBaselineDao(db: AppDatabase): BaselineDao = db.baselineDao()

    @Provides
    fun provideHistoryDao(db: AppDatabase): HistoryDao = db.historyDao()
}
