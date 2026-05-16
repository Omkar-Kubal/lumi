package com.appylab.lumi.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedTipDao {
    @Query("SELECT tipId FROM saved_tip")
    fun observeAll(): Flow<List<Int>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun save(tip: SavedTipEntity)

    @Delete
    suspend fun remove(tip: SavedTipEntity)
}
