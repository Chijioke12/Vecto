package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "level_progress")
data class LevelProgress(
    @PrimaryKey val levelIndex: Int,
    val unlocked: Boolean,
    val completed: Boolean,
    val stars: Int,
    val bestTimeMs: Long
)
