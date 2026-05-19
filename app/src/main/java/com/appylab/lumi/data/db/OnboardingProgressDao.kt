package com.appylab.lumi.data.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface OnboardingProgressDao {

    @Query("SELECT * FROM onboarding_progress WHERE id = 1")
    fun observe(): Flow<OnboardingProgressEntity?>

    @Upsert
    suspend fun upsert(entity: OnboardingProgressEntity)

    @Query("DELETE FROM onboarding_progress")
    suspend fun clear()
}
