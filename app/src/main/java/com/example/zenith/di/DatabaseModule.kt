package com.example.zenith.di

import android.content.Context
import com.example.zenith.data.AppDatabase
import com.example.zenith.data.DistractionEventDao
import com.example.zenith.data.FocusSessionDao
import com.example.zenith.data.SettingsRepository
import com.example.zenith.data.WhitelistedAppDao
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
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    @Provides
    fun provideFocusSessionDao(db: AppDatabase): FocusSessionDao {
        return db.focusSessionDao()
    }

    @Provides
    fun provideDistractionEventDao(db: AppDatabase): DistractionEventDao {
        return db.distractionEventDao()
    }

    @Provides
    fun provideWhitelistedAppDao(db: AppDatabase): WhitelistedAppDao {
        return db.whitelistedAppDao()
    }
}