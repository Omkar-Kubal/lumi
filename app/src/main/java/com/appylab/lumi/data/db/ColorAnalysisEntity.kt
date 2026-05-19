package com.appylab.lumi.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "color_analysis")
data class ColorAnalysisEntity(
    @PrimaryKey val faceAnalysisId: Long,
    val colorSeason: String = "",
    val personalPaletteJson: String = "[]",
    val avoidColorsJson: String = "[]",
    val clothingRecsJson: String = "[]",
    val hairColorRecsJson: String = "[]",
    val lipColorsJson: String = "[]",
    val eyeColorsJson: String = "[]",
    val isSaved: Boolean = false,
    val savedAt: Long = 0L,
    val createdAt: Long = System.currentTimeMillis()
)
