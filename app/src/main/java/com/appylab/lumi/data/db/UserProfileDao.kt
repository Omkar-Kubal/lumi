package com.appylab.lumi.data.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profile WHERE id = 1")
    fun observe(): Flow<UserProfileEntity?>

    @Upsert
    suspend fun upsert(profile: UserProfileEntity)

    @Query("UPDATE user_profile SET passwordHash = :hash WHERE id = 1")
    suspend fun updatePasswordHash(hash: String)

    @Query("SELECT passwordHash FROM user_profile WHERE id = 1")
    suspend fun getPasswordHash(): String?

    @Query("UPDATE user_profile SET lastLoginAt = :timestamp WHERE id = 1")
    suspend fun updateLastLogin(timestamp: Long)
}
