package com.appylab.lumi.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_tip")
data class SavedTipEntity(
    @PrimaryKey val tipId: Int
)
