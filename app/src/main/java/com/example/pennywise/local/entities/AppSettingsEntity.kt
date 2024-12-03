package com.example.pennywise.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_settings")
data class AppSettingsEntity(
    @PrimaryKey val id: Int = 1, // Single row to store all settings
    val isDarkMode: Boolean
)
