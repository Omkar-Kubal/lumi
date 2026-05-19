package com.appylab.lumi.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "feature_detail")
data class FeatureDetailEntity(
    @PrimaryKey val faceAnalysisId: Long,
    val symmetryScore: Int = 75,
    val improvementPriorityJson: String = "[]",
    val createdAt: Long = System.currentTimeMillis()
)
