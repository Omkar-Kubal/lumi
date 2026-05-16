package com.appylab.lumi.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [UserProfileEntity::class],
    version = 1,
    exportSchema = false
)
abstract class LumiDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao

    companion object {
        @Volatile
        private var instance: LumiDatabase? = null

        fun getInstance(context: Context): LumiDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    LumiDatabase::class.java,
                    "lumi.db"
                ).build().also { instance = it }
            }
    }
}
