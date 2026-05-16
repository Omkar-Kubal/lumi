package com.appylab.lumi.data.model

enum class SubscriptionTier { FREE, PRO }

data class FaceAnalysis(
    val id: Long,
    val userId: Int,
    val glowUpScore: Int,
    val faceShape: String,
    val skinTone: String,
    val undertone: String,
    val eyeShape: String,
    val imageUrl: String,
    val timestamp: Long
)

data class BeautyTip(
    val id: Int,
    val text: String,
    val category: String
)

data class TrendingLook(
    val id: Int,
    val tag: String,
    val title: String,
    val subtitle: String,
    val imageUrl: String
)
