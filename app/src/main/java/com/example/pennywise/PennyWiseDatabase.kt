package com.example.pennywise

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.pennywise.local.dao.AppSettingsDao
import com.example.pennywise.local.entities.AppSettingsEntity

@Database(entities = [AppSettingsEntity::class], version = 1, exportSchema = false)
abstract class PennyWiseDatabase : RoomDatabase() {
    abstract fun appSettingsDao(): AppSettingsDao

    companion object {
        @Volatile
        private var INSTANCE: PennyWiseDatabase? = null

        fun getDatabase(context: Context): PennyWiseDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PennyWiseDatabase::class.java,
                    "pennywise_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
