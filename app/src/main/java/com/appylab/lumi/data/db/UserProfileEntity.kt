package com.appylab.lumi.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Int = 1,
    val hasCompletedOnboarding: Boolean = false,
    val authType: String = "",
    val ageRange: String = "",
    val beautyGoals: String = "",
    val skinConcerns: String = "",
    val cameraPermissionGranted: Boolean = false,
    val notificationPermissionGranted: Boolean = false,
    val displayName: String = "",
    val photoUrl: String = "",
    // Added v6
    val email: String = "",
    val location: String = "",
    // Added v7 — personal details
    val age: Int = 0,
    val skinTypePref: String = "",
    val skinTonePref: String = "",
    val undertonePref: String = ""
)
