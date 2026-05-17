package com.appylab.lumi.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "face_analysis")
data class FaceAnalysisEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Int = 1,
    val glowUpScore: Int = 0,
    val faceShape: String = "",
    val skinTone: String = "",
    val undertone: String = "",
    val eyeShape: String = "",
    val imageUrl: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    // Result screen fields (added v5)
    val browType: String = "",
    val noseShape: String = "",
    val lipType: String = "",
    val faceShapeDescription: String = "",
    val undertoneDescription: String = "",
    val celebrityMatchesJson: String = "[]"
)
