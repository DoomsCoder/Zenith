package com.example.zenith.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [FocusSession::class, DistractionEvent::class, WhitelistedApp::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun focusSessionDao() : FocusSessionDao

    abstract fun distractionEventDao() : DistractionEventDao

    abstract fun whitelistedAppDao() : WhitelistedAppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "zenith_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

