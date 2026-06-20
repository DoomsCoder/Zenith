package com.example.zenith.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * The main database class that ties everything together.
 */
@Database(entities = [FocusSession::class, DistractionEvent::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun focusSessionDao() : FocusSessionDao

    abstract fun distractionEventDao() : DistractionEventDao

    companion object {
        // @Volatile ensures all threads see the same instance immediately
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {

            // If the instance exists, return it. If not, create it safely (synchronized).
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

