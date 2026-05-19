package com.appylab.lumi.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Tracks which onboarding step the user was last on.
 * Used to resume mid-onboarding if the app is killed.
 * currentStep 0 = not started, 1–8 = step number.
 */
@Entity(tableName = "onboarding_progress")
data class OnboardingProgressEntity(
    @PrimaryKey val id: Int = 1,
    val currentStep: Int = 1,
    val updatedAt: Long = 0L
)
