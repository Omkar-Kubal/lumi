package com.appylab.lumi.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        UserProfileEntity::class,
        FaceAnalysisEntity::class,
        SavedTipEntity::class,
        AppStateEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class LumiDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
    abstract fun faceAnalysisDao(): FaceAnalysisDao
    abstract fun savedTipDao(): SavedTipDao
    abstract fun appStateDao(): AppStateDao

    companion object {
        @Volatile
        private var instance: LumiDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE user_profile ADD COLUMN displayName TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE user_profile ADD COLUMN photoUrl TEXT NOT NULL DEFAULT ''")
                db.execSQL(
                    """CREATE TABLE IF NOT EXISTS face_analysis (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        userId INTEGER NOT NULL DEFAULT 1,
                        glowUpScore INTEGER NOT NULL DEFAULT 0,
                        faceShape TEXT NOT NULL DEFAULT '',
                        skinTone TEXT NOT NULL DEFAULT '',
                        undertone TEXT NOT NULL DEFAULT '',
                        eyeShape TEXT NOT NULL DEFAULT '',
                        imageUrl TEXT NOT NULL DEFAULT '',
                        timestamp INTEGER NOT NULL DEFAULT 0
                    )"""
                )
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS saved_tip (tipId INTEGER PRIMARY KEY NOT NULL)"
                )
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """CREATE TABLE IF NOT EXISTS app_state (
                        id INTEGER PRIMARY KEY NOT NULL,
                        subscriptionTier TEXT NOT NULL DEFAULT 'FREE',
                        unreadNotificationCount INTEGER NOT NULL DEFAULT 0,
                        freeScanUsed INTEGER NOT NULL DEFAULT 0,
                        resultsUnviewed INTEGER NOT NULL DEFAULT 0
                    )"""
                )
            }
        }

        fun getInstance(context: Context): LumiDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    LumiDatabase::class.java,
                    "lumi.db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build()
                    .also { instance = it }
            }
    }
}
