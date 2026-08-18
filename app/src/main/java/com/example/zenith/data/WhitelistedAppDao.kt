package com.example.zenith.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WhitelistedAppDao {
    @Query("SELECT * FROM whitelisted_apps ORDER BY appName ASC")
    fun getAllWhitelistedApps(): Flow<List<WhitelistedApp>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApp(app: WhitelistedApp)

    @Delete
    suspend fun deleteApp(app: WhitelistedApp)

    @Query("SELECT EXISTS(SELECT 1 FROM whitelisted_apps WHERE packageName = :packageName)")
    suspend fun isAppWhitelisted(packageName: String): Boolean
}
