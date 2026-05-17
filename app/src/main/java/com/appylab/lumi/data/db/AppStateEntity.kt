package com.appylab.lumi.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_state")
data class AppStateEntity(
    @PrimaryKey val id: Int = 1,
    val subscriptionTier: String = "FREE",
    val unreadNotificationCount: Int = 0,
    val freeScanUsed: Boolean = false,
    val resultsUnviewed: Boolean = false,
    val scanCountToday: Int = 0,
    val scanCountDate: String = "",
    // Added v6
    val notifScanReminders: Boolean = true,
    val notifPromotions: Boolean = false,
    val notifUpdates: Boolean = true
)
