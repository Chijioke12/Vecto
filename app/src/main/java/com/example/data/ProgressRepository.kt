package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class ProgressRepository(private val dao: LevelProgressDao) {
    val allProgress: Flow<List<LevelProgress>> = dao.getAllProgressFlow()

    suspend fun initializeIfEmpty() {
        // Collect once to see if database has any progress
        val currentList = dao.getAllProgressFlow().first()
        if (currentList.isEmpty()) {
            // Seed levels 1 to 11
            for (level in 1..11) {
                dao.insertProgress(
                    LevelProgress(
                        levelIndex = level,
                        unlocked = level == 1, // Only first level is unlocked by default
                        completed = false,
                        stars = 0,
                        bestTimeMs = 0L
                    )
                )
            }
        }
    }

    suspend fun saveProgress(levelIndex: Int, stars: Int, timeMs: Long) {
        val existing = dao.getProgressForLevel(levelIndex)
        val bestTime = if (existing != null && existing.bestTimeMs > 0L) {
            minOf(existing.bestTimeMs, timeMs)
        } else {
            timeMs
        }
        val maxStars = existing?.let { maxOf(it.stars, stars) } ?: stars

        // Save current level as completed
        dao.insertProgress(
            LevelProgress(
                levelIndex = levelIndex,
                unlocked = true,
                completed = true,
                stars = maxStars,
                bestTimeMs = bestTime
            )
        )

        // Unlock next level if available
        if (levelIndex < 11) {
            val nextLevelIndex = levelIndex + 1
            val nextExisting = dao.getProgressForLevel(nextLevelIndex)
            dao.insertProgress(
                LevelProgress(
                    levelIndex = nextLevelIndex,
                    unlocked = true,
                    completed = nextExisting?.completed ?: false,
                    stars = nextExisting?.stars ?: 0,
                    bestTimeMs = nextExisting?.bestTimeMs ?: 0L
                )
            )
        }
    }

    suspend fun resetAllProgress() {
        dao.clearAll()
        for (level in 1..11) {
            dao.insertProgress(
                LevelProgress(
                    levelIndex = level,
                    unlocked = level == 1,
                    completed = false,
                    stars = 0,
                    bestTimeMs = 0L
                )
            )
        }
    }
}
