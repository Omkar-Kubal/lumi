package com.appylab.lumi.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "glow_up")
data class GlowUpEntity(
    @PrimaryKey val id: String,
    val faceAnalysisId: Long,
    val userId: Int,
    val originalImageUrl: String,
    val glowUpImageUrl: String?,
    /** PENDING | GENERATING | COMPLETE | FAILED */
    val glowUpImageStatus: String,
    val score: Int,
    /** JSON array of improvement area objects */
    val improvementAreasJson: String,
    /** JSON array of step guide objects */
    val stepGuidesJson: String,
    val createdAt: Long = System.currentTimeMillis()
)
