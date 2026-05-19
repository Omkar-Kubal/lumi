package com.appylab.lumi.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notification")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String,       // "scan_complete" | "palette_saved" | "glow_up_ready"
    val title: String,
    val body: String,
    val timestamp: Long,
    val isRead: Boolean = false
)
