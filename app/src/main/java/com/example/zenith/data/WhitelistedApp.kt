package com.example.zenith.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "whitelisted_apps")
data class WhitelistedApp(
    @PrimaryKey val packageName: String,
    val appName: String
)
