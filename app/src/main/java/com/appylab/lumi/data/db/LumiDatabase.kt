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
        AppStateEntity::class,
        GlowUpEntity::class,
        ColorAnalysisEntity::class,
        FeatureDetailEntity::class,
        NotificationEntity::class
    ],
    version = 11,
    exportSchema = false
)
abstract class LumiDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
    abstract fun faceAnalysisDao(): FaceAnalysisDao
    abstract fun savedTipDao(): SavedTipDao
    abstract fun appStateDao(): AppStateDao
    abstract fun glowUpDao(): GlowUpDao
    abstract fun colorAnalysisDao(): ColorAnalysisDao
    abstract fun featureDetailDao(): FeatureDetailDao
    abstract fun notificationDao(): NotificationDao

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

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE app_state ADD COLUMN scanCountToday INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE app_state ADD COLUMN scanCountDate TEXT NOT NULL DEFAULT ''")
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE face_analysis ADD COLUMN browType TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE face_analysis ADD COLUMN noseShape TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE face_analysis ADD COLUMN lipType TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE face_analysis ADD COLUMN faceShapeDescription TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE face_analysis ADD COLUMN undertoneDescription TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE face_analysis ADD COLUMN celebrityMatchesJson TEXT NOT NULL DEFAULT '[]'")
            }
        }

        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE user_profile ADD COLUMN email TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE user_profile ADD COLUMN location TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE app_state ADD COLUMN notifScanReminders INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE app_state ADD COLUMN notifPromotions INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE app_state ADD COLUMN notifUpdates INTEGER NOT NULL DEFAULT 1")
            }
        }

        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE user_profile ADD COLUMN age INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE user_profile ADD COLUMN skinTypePref TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE user_profile ADD COLUMN skinTonePref TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE user_profile ADD COLUMN undertonePref TEXT NOT NULL DEFAULT ''")
            }
        }

        private val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """CREATE TABLE IF NOT EXISTS glow_up (
                        id TEXT PRIMARY KEY NOT NULL,
                        faceAnalysisId INTEGER NOT NULL,
                        userId INTEGER NOT NULL DEFAULT 1,
                        originalImageUrl TEXT NOT NULL DEFAULT '',
                        glowUpImageUrl TEXT,
                        glowUpImageStatus TEXT NOT NULL DEFAULT 'PENDING',
                        score INTEGER NOT NULL DEFAULT 0,
                        improvementAreasJson TEXT NOT NULL DEFAULT '[]',
                        stepGuidesJson TEXT NOT NULL DEFAULT '[]',
                        createdAt INTEGER NOT NULL DEFAULT 0
                    )"""
                )
            }
        }

        private val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """CREATE TABLE IF NOT EXISTS color_analysis (
                        faceAnalysisId INTEGER PRIMARY KEY NOT NULL,
                        colorSeason TEXT NOT NULL DEFAULT '',
                        personalPaletteJson TEXT NOT NULL DEFAULT '[]',
                        avoidColorsJson TEXT NOT NULL DEFAULT '[]',
                        clothingRecsJson TEXT NOT NULL DEFAULT '[]',
                        hairColorRecsJson TEXT NOT NULL DEFAULT '[]',
                        lipColorsJson TEXT NOT NULL DEFAULT '[]',
                        eyeColorsJson TEXT NOT NULL DEFAULT '[]',
                        isSaved INTEGER NOT NULL DEFAULT 0,
                        createdAt INTEGER NOT NULL DEFAULT 0
                    )"""
                )
                db.execSQL(
                    """CREATE TABLE IF NOT EXISTS feature_detail (
                        faceAnalysisId INTEGER PRIMARY KEY NOT NULL,
                        symmetryScore INTEGER NOT NULL DEFAULT 75,
                        improvementPriorityJson TEXT NOT NULL DEFAULT '[]',
                        createdAt INTEGER NOT NULL DEFAULT 0
                    )"""
                )
            }
        }

        private val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE user_profile ADD COLUMN passwordHash TEXT")
                db.execSQL("ALTER TABLE color_analysis ADD COLUMN savedAt INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """CREATE TABLE IF NOT EXISTS notification (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        type TEXT NOT NULL,
                        title TEXT NOT NULL,
                        body TEXT NOT NULL,
                        timestamp INTEGER NOT NULL,
                        isRead INTEGER NOT NULL DEFAULT 0
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
                    .addMigrations(
                        MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4,
                        MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7,
                        MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10,
                        MIGRATION_10_11
                    )
                    .build()
                    .also { instance = it }
            }
    }
}
