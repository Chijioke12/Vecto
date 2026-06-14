package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface LevelProgressDao {
    @Query("SELECT * FROM level_progress ORDER BY levelIndex ASC")
    fun getAllProgressFlow(): kotlinx.coroutines.flow.Flow<List<LevelProgress>>

    @Query("SELECT * FROM level_progress WHERE levelIndex = :levelIndex")
    suspend fun getProgressForLevel(levelIndex: Int): LevelProgress?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgress(progress: LevelProgress)

    @Query("UPDATE level_progress SET unlocked = 1 WHERE levelIndex = :levelIndex")
    suspend fun unlockLevel(levelIndex: Int)

    @Query("DELETE FROM level_progress")
    suspend fun clearAll()
}
