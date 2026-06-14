package com.example.game

import android.app.Application
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.GameDatabase
import com.example.data.LevelProgress
import com.example.data.ProgressRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

sealed interface ScreenState {
    object MainMenu : ScreenState
    object LevelSelect : ScreenState
    data class Playing(val levelIndex: Int) : ScreenState
}

class GameViewModel(application: Application) : AndroidViewModel(application) {
    private val db = GameDatabase.getDatabase(application)
    private val repository = ProgressRepository(db.levelProgressDao())

    // App state
    private val _screenState = MutableStateFlow<ScreenState>(ScreenState.MainMenu)
    val screenState: StateFlow<ScreenState> = _screenState.asStateFlow()

    // Database levels progress
    private val _progressList = MutableStateFlow<List<LevelProgress>>(emptyList())
    val progressList: StateFlow<List<LevelProgress>> = _progressList.asStateFlow()

    // Active Level configuration
    private val _currentLevel = MutableStateFlow<GameLevel>(LevelsData.levelsList.first())
    val currentLevel: StateFlow<GameLevel> = _currentLevel.asStateFlow()

    // Physics / Gameplay States
    val playerX = MutableStateFlow(25f)
    val playerY = MutableStateFlow(400f)
    val playerVx = MutableStateFlow(0f)
    val playerVy = MutableStateFlow(0f)
    val isGrounded = MutableStateFlow(false)
    val airJumpsRemaining = MutableStateFlow(1)
    val keysState = MutableStateFlow<List<KeyItem>>(emptyList())
    val isLevelCleared = MutableStateFlow(false)
    val showRespawnEffect = MutableStateFlow(false)
    val deathsCount = MutableStateFlow(0)
    val screenShakeTicks = MutableStateFlow(0)

    // Cooldown for portal teleports
    private var portalCooldown = 0

    // Timing metrics
    private val _levelStartTime = MutableStateFlow(0L)
    val levelElapsedTime = MutableStateFlow(0L)

    // Overlay controllers
    val showLevelStartText = MutableStateFlow(true)

    // Game loop control
    private var gameLoopJob: Job? = null
    private var isPlaying = false

    init {
        viewModelScope.launch {
            repository.initializeIfEmpty()
            repository.allProgress.collectLatest { list ->
                _progressList.value = list
            }
        }
    }

    fun navigateToMainMenu() {
        stopGame()
        _screenState.value = ScreenState.MainMenu
    }

    fun navigateToLevelSelect() {
        stopGame()
        _screenState.value = ScreenState.LevelSelect
    }

    fun startLevel(levelIndex: Int) {
        val level = LevelsData.levelsList.firstOrNull { it.index == levelIndex } ?: return
        _currentLevel.value = level
        _screenState.value = ScreenState.Playing(levelIndex)
        initLevelState(level)
        startGameLoop()
    }

    private fun initLevelState(level: GameLevel) {
        playerX.value = level.startX
        playerY.value = level.startY
        playerVx.value = 0f
        playerVy.value = 0f
        isGrounded.value = false
        airJumpsRemaining.value = 1
        keysState.value = level.keys.map { it.copy() }
        isLevelCleared.value = false
        showRespawnEffect.value = false
        isLevelCleared.value = false
        showLevelStartText.value = true
        _levelStartTime.value = System.currentTimeMillis()
        levelElapsedTime.value = 0L
        portalCooldown = 0
        screenShakeTicks.value = 0
    }

    // Input handlers
    private var inputLeft = false
    private var inputRight = false

    fun setInputLeft(pressed: Boolean) {
        inputLeft = pressed
    }

    fun setInputRight(pressed: Boolean) {
        inputRight = pressed
    }

    fun jump() {
        if (isLevelCleared.value) return

        if (isGrounded.value) {
            playerVy.value = -7.5f
            isGrounded.value = false
        } else if (airJumpsRemaining.value > 0) {
            playerVy.value = -7.0f
            airJumpsRemaining.value -= 1
        }
    }

    private fun startGameLoop() {
        isPlaying = true
        gameLoopJob?.cancel()
        gameLoopJob = viewModelScope.launch {
            // Delay start text fading
            launch {
                delay(2200)
                showLevelStartText.value = false
            }

            while (isPlaying) {
                if (!isLevelCleared.value) {
                    levelElapsedTime.value = System.currentTimeMillis() - _levelStartTime.value
                    physicsTick()
                }
                delay(16) // ~60fps tick rate
            }
        }
    }

    private fun stopGame() {
        isPlaying = false
        gameLoopJob?.cancel()
        inputLeft = false
        inputRight = false
    }

    private suspend fun triggerDeath() {
        deathsCount.value += 1
        screenShakeTicks.value = 12 // Shake screen duration
        showRespawnEffect.value = true
        playerVx.value = 0f
        playerVy.value = 0f

        val level = _currentLevel.value
        playerX.value = level.startX
        playerY.value = level.startY
        isGrounded.value = false
        airJumpsRemaining.value = 1 // default air jumps remaining
        
        // Reset key positions
        keysState.value = level.keys.map { it.copy() }
        
        delay(300)
        showRespawnEffect.value = false
    }

    private suspend fun physicsTick() {
        val level = _currentLevel.value

        // Screen shake decay
        if (screenShakeTicks.value > 0) {
            screenShakeTicks.value -= 1
        }

        // Horizontal input acceleration
        var vx = playerVx.value
        val speedMultiplier = 0.55f
        if (inputLeft) {
            vx -= speedMultiplier
        } else if (inputRight) {
            vx += speedMultiplier
        } else {
            vx *= 0.82f // inertia drag
        }

        // Limit maximum running velocity
        val maxSpeed = 4.8f
        vx = vx.coerceIn(-maxSpeed, maxSpeed)
        playerVx.value = vx

        // Gravity acceleration
        var vy = playerVy.value
        vy += 0.28f // standard gravity coefficient
        vy = vy.coerceIn(-12f, 12f)
        playerVy.value = vy

        // 1. Move Player Horizontally
        var px = playerX.value + vx
        val pRadius = 11f // Half-size of bounding circle/box

        // Loop horizontally bounds or wall-bounce
        if (px - pRadius < 0f) {
            px = pRadius
            vx = -vx * 0.72f // bounce back off left edge!
        } else if (px + pRadius > 600f) {
            px = 600f - pRadius
            vx = -vx * 0.72f // bounce back off right edge!
        }
        playerX.value = px
        playerVx.value = vx

        // Check platform side blockages (only simple left/right blocks if standing inside)
        val playerXRect = Rect(px - pRadius, playerY.value - pRadius, px + pRadius, playerY.value + pRadius)
        for (plat in level.platforms) {
            if (playerXRect.overlaps(plat.rect)) {
                // If moving into platform on right, push left
                if (vx > 0f && px < plat.x) {
                    px = plat.x - pRadius
                    vx = -vx * 0.55f // bounce off platform side
                } else if (vx < 0f && px > plat.x + plat.width) {
                    px = plat.x + plat.width + pRadius
                    vx = -vx * 0.55f // bounce off platform side
                }
            }
        }
        playerX.value = px
        playerVx.value = vx

        // 2. Move Player Vertically
        var py = playerY.value + vy
        
        // Loop vertically through floor:
        if (py - pRadius > 500f) {
            py = pRadius // loops cleanly to the top
            vy = 0f
            // Entering loop allows an air-jump
            airJumpsRemaining.value = 1
        } else if (py - pRadius < 0f) {
            py = pRadius
            vy = 0.2f
        }
        playerY.value = py
        playerVy.value = vy

        // Check platforms collision vertically
        // Dynamic: Only land on platform tops if falling down (vy >= 0) to allow playing Level 5 jump upwards through blocks!
        val playerYRect = Rect(playerX.value - pRadius, py - pRadius, playerX.value + pRadius, py + pRadius)
        var groundedThisFrame = false
        if (vy >= 0f) {
            for (plat in level.platforms) {
                // Was prior player above the platform top?
                val priorFoot = playerY.value + pRadius
                if (playerYRect.overlaps(plat.rect) && priorFoot <= plat.rect.top + 10f) {
                    py = plat.rect.top - pRadius
                    vy = 0f
                    groundedThisFrame = true
                }
            }
        }
        playerY.value = py
        playerVy.value = vy
        isGrounded.value = groundedThisFrame

        if (groundedThisFrame) {
            airJumpsRemaining.value = 1 // reset air-jump
        }

        // 3. Lava Contact Check
        val playerFullRect = Rect(playerX.value - pRadius, playerY.value - pRadius, playerX.value + pRadius, playerY.value + pRadius)
        for (lava in level.lavas) {
            if (playerFullRect.overlaps(lava.rect)) {
                triggerDeath()
                return
            }
        }

        // 4. Key Collection Check
        val updatedKeys = keysState.value.map { key ->
            if (!key.collected && playerFullRect.overlaps(key.rect)) {
                key.copy(collected = true)
            } else {
                key
            }
        }
        keysState.value = updatedKeys

        // 5. Portals Transport Check
        if (portalCooldown > 0) {
            portalCooldown--
        } else {
            for (portal in level.portals) {
                val inputCenter = portal.input
                val distance = (Offset(playerX.value, playerY.value) - inputCenter).getDistance()
                if (distance < 24f) { // overlap with portal input
                    // Teleport to output gate!
                    playerX.value = portal.output.x
                    playerY.value = portal.output.y
                    portalCooldown = 40 // prevent loop recursions
                    playerVx.value = playerVx.value * 1.2f // portal burst velocity!
                    playerVy.value = playerVy.value.coerceAtMost(-1.5f) // fling up a bit
                    break
                }
            }
        }

        // 6. Goal Reach Check
        val allKeysCollected = keysState.value.all { it.collected }
        if (playerFullRect.overlaps(level.goalRect) && allKeysCollected) {
            // Win level!
            isLevelCleared.value = true
            stopGame()
            
            // Star scoring algorithm
            val finalTime = levelElapsedTime.value
            val stars = when {
                finalTime <= 8000L -> 3
                finalTime <= 16000L -> 2
                else -> 1
            }

            // Save to room Database
            viewModelScope.launch {
                repository.saveProgress(level.index, stars, finalTime)
            }
        }
    }

    fun restartLevel() {
        val level = _currentLevel.value
        initLevelState(level)
        startGameLoop()
    }

    fun nextLevel() {
        val nextIndex = _currentLevel.value.index + 1
        if (nextIndex <= 11) {
            startLevel(nextIndex)
        } else {
            navigateToLevelSelect()
        }
    }

    fun resetAllDbProgress() {
        viewModelScope.launch {
            repository.resetAllProgress()
        }
    }
}
