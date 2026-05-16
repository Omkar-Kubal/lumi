package com.appylab.lumi.data.repository

import com.appylab.lumi.data.db.UserProfileDao
import com.appylab.lumi.data.db.UserProfileEntity
import kotlinx.coroutines.flow.Flow

class UserRepository(
    private val userProfileDao: UserProfileDao
) {
    fun observeProfile(): Flow<UserProfileEntity?> = userProfileDao.observe()

    suspend fun upsert(profile: UserProfileEntity) {
        userProfileDao.upsert(profile)
    }
}
