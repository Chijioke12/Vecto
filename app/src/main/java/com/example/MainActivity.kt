package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.LevelProgress
import com.example.game.*
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF07051C) // Pure luxury midnight theme
                ) {
                    AppContent()
                }
            }
        }
    }
}

@Composable
fun AppContent() {
    val gameViewModel: GameViewModel = viewModel()
    val screenState by gameViewModel.screenState.collectAsStateWithLifecycle()
    val progressList by gameViewModel.progressList.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize()) {
        // Shared dynamic space floating dust particles backplate
        SpaceBackgroundParticles()

        when (val state = screenState) {
            is ScreenState.MainMenu -> {
                MainMenuScreen(
                    progressList = progressList,
                    onPlayClicked = {
                        // Find first unlocked incomplete level or default to level 1
                        val firstUnlocked = progressList.firstOrNull { it.unlocked && !it.completed }?.levelIndex
                            ?: progressList.firstOrNull { it.unlocked }?.levelIndex
                            ?: 1
                        gameViewModel.startLevel(firstUnlocked)
                    },
                    onSelectClicked = { gameViewModel.navigateToLevelSelect() },
                    onResetClicked = { gameViewModel.resetAllDbProgress() }
                )
            }
            is ScreenState.LevelSelect -> {
                LevelSelectScreen(
                    progressList = progressList,
                    onLevelSelected = { levelIndex -> gameViewModel.startLevel(levelIndex) },
                    onBackClicked = { gameViewModel.navigateToMainMenu() }
                )
            }
            is ScreenState.Playing -> {
                GameplayScreen(
                    levelIndex = state.levelIndex,
                    viewModel = gameViewModel
                )
            }
        }
    }
}

// 60fps Ambient Drift Particle Canvas
@Composable
fun SpaceBackgroundParticles() {
    val transition = rememberInfiniteTransition(label = "particles")
    val driftY by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(25000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "driftY"
    )

    // Generate deterministic background stars coordinates
    val stars = remember {
        List(40) {
            Offset(
                x = Random.nextFloat() * 1200f,
                y = Random.nextFloat() * 2000f
            ) to Random.nextFloat() * 2.5f + 1f
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        stars.forEach { (pos, radius) ->
            // wrap around scrolling
            val finalX = pos.x % (width.coerceAtLeast(1f))
            val finalY = (pos.y + driftY) % (height.coerceAtLeast(1f))

            drawCircle(
                color = Color(0xFF00D2FF).copy(alpha = if (radius > 2f) 0.4f else 0.2f),
                radius = radius,
                center = Offset(finalX, finalY)
            )
        }
    }
}

@Composable
fun MainMenuScreen(
    progressList: List<LevelProgress>,
    onPlayClicked: () -> Unit,
    onSelectClicked: () -> Unit,
    onResetClicked: () -> Unit
) {
    var showResetDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(modifier = Modifier.height(30.dp))

        // Large Cyber Neon Title Shield
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Glow Border Title
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF0F0E26).copy(alpha = 0.85f))
                    .border(2.dp, Color(0xFF00D2FF), RoundedCornerShape(16.dp))
                    .padding(vertical = 18.dp, horizontal = 28.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "VECTOR",
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF00D2FF),
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 6.sp,
                    )
                    Text(
                        text = "PLATFORMER",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF9D00),
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 4.sp,
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Premium Physics-Based Arcade Game",
                color = Color.White.copy(alpha = 0.55f),
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )
        }

        // Center Hero Display - Interactive spinning player model
        val infiniteTransition = rememberInfiniteTransition(label = "core_anim")
        val rotateAngle by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(4000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "player_rotate"
        )
        val floatOffset by infiniteTransition.animateFloat(
            initialValue = -8f,
            targetValue = 8f,
            animationSpec = infiniteRepeatable(
                animation = tween(1500, easing = SineJoin),
                repeatMode = RepeatMode.Reverse
            ),
            label = "player_float"
        )

        Box(
            modifier = Modifier
                .size(150.dp)
                .graphicsLayer {
                    translationY = floatOffset
                },
            contentAlignment = Alignment.Center
        ) {
            // Pulsing background radar rings
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF00D2FF).copy(alpha = 0.08f))
            )

            Image(
                painter = painterResource(id = R.drawable.ic_player),
                contentDescription = "Player Sprite",
                modifier = Modifier
                    .size(80.dp)
                    .rotate(rotateAngle)
            )
        }

        // Play actions & reset
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Main Action Play Button
            Button(
                onClick = onPlayClicked,
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(58.dp)
                    .testTag("menu_play_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00D2FF),
                    contentColor = Color(0xFF07051C)
                ),
                shape = RoundedCornerShape(12.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "START RUSH",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
            }

            // Stage Select Button
            OutlinedButton(
                onClick = onSelectClicked,
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(54.dp)
                    .testTag("menu_select_button"),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color.White.copy(alpha = 0.5f)),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "SELECT STAGE",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
            }

            // Reset data button
            Text(
                text = "RESET PROGRESS",
                color = Color.White.copy(alpha = 0.35f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(top = 10.dp)
                    .clickable { showResetDialog = true }
                    .testTag("menu_reset_text")
            )
        }
    }

    // Confirmation dialogues
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFFF9D00)) },
            title = {
                Text(
                    text = "Reset Database Progress?",
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            },
            text = {
                Text(
                    text = "Are you absolutely sure you want to lock all stages and clear high score completion records? This cannot be undone."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onResetClicked()
                        showResetDialog = false
                    },
                    modifier = Modifier.testTag("confirm_reset_button")
                ) {
                    Text("YES, RESET", color = Color(0xFFFF3B30), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showResetDialog = false }
                ) {
                    Text("CANCEL", color = MaterialTheme.colorScheme.onSurface)
                }
            },
            containerColor = Color(0xFF161530),
            titleContentColor = Color.White,
            textContentColor = Color.White.copy(alpha = 0.8f)
        )
    }
}

@Composable
fun LevelSelectScreen(
    progressList: List<LevelProgress>,
    onLevelSelected: (Int) -> Unit,
    onBackClicked: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(16.dp)
    ) {
        // Compact Back Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackClicked,
                modifier = Modifier
                    .background(Color.White.copy(alpha = 0.08f), CircleShape)
                    .size(40.dp)
                    .testTag("select_back_button")
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back To Menu",
                    tint = Color.White
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "STAGE SELECT",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 2.sp
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Grid list of levels
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(progressList, key = { it.levelIndex }) { progress ->
                LevelProgressCard(
                    progress = progress,
                    onClick = {
                        if (progress.unlocked) {
                            onLevelSelected(progress.levelIndex)
                        }
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LevelProgressCard(
    progress: LevelProgress,
    onClick: () -> Unit
) {
    val isLocked = !progress.unlocked
    val levelData = LevelsData.levelsList.firstOrNull { it.index == progress.levelIndex }

    Card(
        onClick = onClick,
        enabled = !isLocked,
        modifier = Modifier
            .fillMaxWidth()
            .height(135.dp)
            .testTag("level_card_${progress.levelIndex}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isLocked) Color(0xFF100E22).copy(alpha = 0.5f)
            else Color(0xFF1A173A),
            disabledContainerColor = Color(0xFF100E22).copy(alpha = 0.4f)
        ),
        border = if (!isLocked) {
            val borderColor = if (progress.completed) Color(0xFF00D2FF).copy(alpha = 0.5f) else Color.White.copy(alpha = 0.15f)
            RowExt.borderOutline(borderColor)
        } else null
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isLocked) {
                // Locked layout overlay
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Locked Stage",
                        tint = Color.White.copy(alpha = 0.3f),
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "STAGE ${progress.levelIndex}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.35f),
                        fontFamily = FontFamily.Monospace
                    )
                }
            } else {
                // Active Level State Data
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.Start
                ) {
                    // Level Title tag indicator
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "STAGE ${progress.levelIndex}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = if (progress.completed) Color(0xFF00D2FF) else Color.White,
                            fontFamily = FontFamily.Monospace
                        )

                        if (progress.completed) {
                            Text(
                                text = "CLEARED",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00FF88),
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    // Level tutorial hint text preview
                    levelData?.let {
                        Text(
                            text = it.text,
                            maxLines = 2,
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.5f),
                            lineHeight = 14.sp,
                            modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                        )
                    }

                    // Completed Stars and High Score representation
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Stars visual
                        Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                            for (star in 1..3) {
                                val isLit = star <= progress.stars
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = if (isLit) Color(0xFFFFD700) else Color.White.copy(alpha = 0.15f),
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }

                        // Best Time visual text
                        if (progress.bestTimeMs > 0L) {
                            val secs = progress.bestTimeMs / 1000f
                            Text(
                                text = String.format("%.2fs", secs),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFF9D00),
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GameplayScreen(
    levelIndex: Int,
    viewModel: GameViewModel
) {
    val level by viewModel.currentLevel.collectAsStateWithLifecycle()
    val playerX by viewModel.playerX.collectAsStateWithLifecycle()
    val playerY by viewModel.playerY.collectAsStateWithLifecycle()
    val playerVx by viewModel.playerVx.collectAsStateWithLifecycle()
    val isGrounded by viewModel.isGrounded.collectAsStateWithLifecycle()
    val keys by viewModel.keysState.collectAsStateWithLifecycle()
    val isCleared by viewModel.isLevelCleared.collectAsStateWithLifecycle()
    val showRespawnEffect by viewModel.showRespawnEffect.collectAsStateWithLifecycle()
    val elapsedMs by viewModel.levelElapsedTime.collectAsStateWithLifecycle()
    val deaths by viewModel.deathsCount.collectAsStateWithLifecycle()
    val screenShake by viewModel.screenShakeTicks.collectAsStateWithLifecycle()
    val showTextOverlay by viewModel.showLevelStartText.collectAsStateWithLifecycle()

    val context = LocalContext.current

    // Screen Shake Offset Calculation
    val shakeModifier = if (screenShake > 0) {
        val rx = Random.nextInt(-7, 7).dp
        val ry = Random.nextInt(-7, 7).dp
        Modifier.offset(x = rx, y = ry)
    } else Modifier

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color(0xFF07051C)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(bottom = 12.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Dynamic Compact Stats HUD Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Exit/Back button
                IconButton(
                    onClick = { viewModel.navigateToLevelSelect() },
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.08f), CircleShape)
                        .size(38.dp)
                        .testTag("game_exit_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Exit to Select Screen",
                        tint = Color.White
                    )
                }

                // Stage descriptor label
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "STAGE ${level.index}",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        color = Color(0xFF00D2FF)
                    )
                    // Stars representation of the level progress rules
                    Row(
                        modifier = Modifier.padding(top = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        val starsLit = when {
                            elapsedMs <= 8000L -> 3
                            elapsedMs <= 16000L -> 2
                            else -> 1
                        }
                        for (star in 1..3) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = if (star <= starsLit) Color(0xFFFFD700) else Color.White.copy(alpha = 0.2f),
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                }

                // Interactive Restart button
                IconButton(
                    onClick = { viewModel.restartLevel() },
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.08f), CircleShape)
                        .size(38.dp)
                        .testTag("game_restart_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Restart Level Progress",
                        tint = Color.White
                    )
                }
            }

            // Descriptive tutorial / status bar
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(bottom = 6.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF141334)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp, horizontal = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = level.text,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = "FAILURES: $deaths",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF3B30),
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Level Dynamic Timer HUD Row
            Row(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF0C0A20))
                    .border(0.5.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                    .padding(vertical = 4.dp, horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TIME:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.5f),
                    fontFamily = FontFamily.Monospace
                )
                val totalSecs = elapsedMs / 1000f
                Text(
                    text = String.format("%05.2fs", totalSecs),
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp,
                    color = if (elapsedMs <= 8000L) Color(0xFF00FF88) else if (elapsedMs <= 16000L) Color(0xFFFF9D00) else Color(0xFFFF3B30),
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.weight(0.1f))

            // 2. Play Field Box (Centered virtual canvas 600 wide by 500 high)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.5f)
                    .then(shakeModifier)
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                // Game Grid Frame Box
                BoxWithConstraints(
                    modifier = Modifier
                        .aspectRatio(1.2f) // Width=600, Height=500 -> 1.2 aspect ratio
                        .clip(RoundedCornerShape(14.dp))
                        .border(
                            width = 2.dp,
                            brush = Brush.verticalGradient(
                                colors = listOf(Color(0xFF00D2FF), Color(0xFF9E00FF))
                            ),
                            shape = RoundedCornerShape(14.dp)
                        )
                        .background(Color(0xFF0B091D))
                        .testTag("game_board_canvas")
                ) {
                    val scaleX = maxWidth.value / 600f
                    val scaleY = maxHeight.value / 500f

                    // Render Goal Exit
                    GoalPortalItem(
                        level = level,
                        scaleX = scaleX,
                        scaleY = scaleY,
                        unlocked = keys.all { it.collected }
                    )

                    // Render Portals
                    level.portals.forEach { portal ->
                        PortalItem(
                            portal = portal,
                            scaleX = scaleX,
                            scaleY = scaleY
                        )
                    }

                    // Render Keys
                    keys.forEach { key ->
                        if (!key.collected) {
                            KeyItemOverlay(
                                key = key,
                                scaleX = scaleX,
                                scaleY = scaleY
                            )
                        }
                    }

                    // Render Lava blocks
                    level.lavas.forEach { lava ->
                        LavaBlockOverlay(
                            lava = lava,
                            scaleX = scaleX,
                            scaleY = scaleY
                        )
                    }

                    // Render Platforms
                    level.platforms.forEach { platform ->
                        PlatformBlockOverlay(
                            platform = platform,
                            scaleX = scaleX,
                            scaleY = scaleY
                        )
                    }

                    // Render Player Icon
                    PlayerSpriteItem(
                        px = playerX,
                        py = playerY,
                        vx = playerVx,
                        scaleX = scaleX,
                        scaleY = scaleY,
                        isGrounded = isGrounded
                    )

                    // Dynamic Splash Screen flash Overlay on Respawn / death
                    androidx.compose.animation.AnimatedVisibility(
                        visible = showRespawnEffect,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.White.copy(alpha = 0.4f))
                        )
                    }

                    // Stage Start Animated Title Subtitle overlay
                    androidx.compose.animation.AnimatedVisibility(
                        visible = showTextOverlay,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.65f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "STAGE ${level.index}",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color(0xFF00D2FF),
                                    letterSpacing = 2.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = level.text,
                                    fontSize = 13.sp,
                                    color = Color.White,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 24.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(0.1f))

            // 3. Tactile On-Screen Responsive Multi-Touch Gamepad
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                // Bottom row dividing controls left and right
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // LEFT D-PAD CONTAINER
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Left run button
                        TactileButton(
                            icon = Icons.Default.KeyboardArrowLeft,
                            description = "Move Left",
                            onPress = { isPressed ->
                                viewModel.setInputLeft(isPressed)
                            },
                            tag = "button_left"
                        )

                        // Right run button
                        TactileButton(
                            icon = Icons.Default.KeyboardArrowRight,
                            description = "Move Right",
                            onPress = { isPressed ->
                                viewModel.setInputRight(isPressed)
                            },
                            tag = "button_right"
                        )
                    }

                    // RIGHT JUMP BUTTON CONTAINER
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF9E00FF))
                            .border(2.dp, Color(0xFFE040FB), CircleShape)
                            .pointerInput(Unit) {
                                // High responsiveness multi-touch trigger for JUMP
                                awaitPointerEventScope {
                                    while (true) {
                                        val event = awaitPointerEvent()
                                        if (event.type == androidx.compose.ui.input.pointer.PointerEventType.Press) {
                                            viewModel.jump()
                                        }
                                    }
                                }
                            }
                            .testTag("button_jump"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "JUMP",
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            color = Color.White,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }

    // Success Screen Dialogue Overlay
    if (isCleared) {
        AlertDialog(
            onDismissRequest = {},
            containerColor = Color(0xFF0F0E2A),
            confirmButton = {
                Button(
                    onClick = { viewModel.nextLevel() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("dialog_next_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FF88), contentColor = Color(0xFF07051C)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = if (levelIndex < 11) "NEXT LEVEL" else "BACK TO MENU",
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF07051C)
                    )
                }
            },
            title = {
                Text(
                    text = "STAGE CLEAR!",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    fontSize = 24.sp,
                    color = Color(0xFF00FF88),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                val totalSecs = elapsedMs / 1000f
                val finalStars = when {
                    elapsedMs <= 8000L -> 3
                    elapsedMs <= 16000L -> 2
                    else -> 1
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        for (star in 1..3) {
                            val active = star <= finalStars
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = if (active) Color(0xFFFFD700) else Color.White.copy(alpha = 0.1f),
                                modifier = Modifier
                                    .size(42.dp)
                                    .scale(if (active) 1.2f else 1.0f)
                            )
                        }
                    }

                    Text(
                        text = String.format("COMPLETION TIME: %.2fs", totalSecs),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = when (finalStars) {
                            3 -> "Perfect Speed Run!"
                            2 -> "Great Effort, Gold Star!"
                            else -> "Clear! Speed up to earn larger stars."
                        },
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        )
    }
}

// Low level simultaneous holding pointer button detector
@Composable
fun TactileButton(
    icon: ImageVector,
    description: String,
    onPress: (Boolean) -> Unit,
    tag: String
) {
    var isDown by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .size(64.dp)
            .clip(CircleShape)
            .background(if (isDown) Color(0xFF00D2FF).copy(alpha = 0.45f) else Color.White.copy(alpha = 0.1f))
            .border(1.5.dp, if (isDown) Color(0xFF00D2FF) else Color.White.copy(alpha = 0.2f), CircleShape)
            .pointerInput(onPress) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        val anyPressed = event.changes.any { it.pressed }
                        if (isDown != anyPressed) {
                            isDown = anyPressed
                            onPress(anyPressed)
                        }
                    }
                }
            }
            .testTag(tag),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = description,
            tint = Color.White,
            modifier = Modifier.size(36.dp)
        )
    }
}

@Composable
fun BoxScope.PlayerSpriteItem(
    px: Float,
    py: Float,
    vx: Float,
    scaleX: Float,
    scaleY: Float,
    isGrounded: Boolean
) {
    // Interactive squish and stretch calculations
    val sizeX = if (!isGrounded) 20f else 24f
    val sizeY = if (!isGrounded) 24f else 20f

    Image(
        painter = painterResource(id = R.drawable.ic_player),
        contentDescription = "Player Avatar",
        modifier = Modifier
            .offset(
                x = ((px - sizeX/2) * scaleX).dp,
                y = ((py - sizeY/2) * scaleY).dp
            )
            .size((sizeX * scaleX).dp, (sizeY * scaleY).dp)
    )
}

@Composable
fun BoxScope.PlatformBlockOverlay(
    platform: Platform,
    scaleX: Float,
    scaleY: Float
) {
    Image(
        painter = painterResource(id = R.drawable.ic_platform),
        contentDescription = "Platform",
        modifier = Modifier
            .offset(
                x = (platform.x * scaleX).dp,
                y = (platform.y * scaleY).dp
            )
            .size(
                width = (platform.width * scaleX).dp,
                height = (platform.height * scaleY).dp
            ),
        colorFilter = ColorFilter.tint(Color(0xFF5C6BC0).copy(alpha = 0.85f)) // subtle cyan tech highlights
    )
}

@Composable
fun BoxScope.LavaBlockOverlay(
    lava: Lava,
    scaleX: Float,
    scaleY: Float
) {
    val infiniteTransition = rememberInfiniteTransition(label = "lava")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.82f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "lava_glow"
    )

    Image(
        painter = painterResource(id = R.drawable.ic_lava),
        contentDescription = "Dangerous Lava Spikes",
        modifier = Modifier
            .offset(
                x = (lava.x * scaleX).dp,
                y = (lava.y * scaleY).dp
            )
            .size(
                width = (lava.width * scaleX).dp,
                height = (lava.height * scaleY).dp
            )
            .graphicsLayer {
                alpha = pulseAlpha
            }
    )
}

@Composable
fun BoxScope.KeyItemOverlay(
    key: KeyItem,
    scaleX: Float,
    scaleY: Float
) {
    val infiniteTransition = rememberInfiniteTransition(label = "key_anim")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = SineJoin),
            repeatMode = RepeatMode.Reverse
        ),
        label = "key_float"
    )

    Image(
        painter = painterResource(id = R.drawable.ic_key),
        contentDescription = "Mission Key Item",
        modifier = Modifier
            .offset(
                x = ((key.x - 12f) * scaleX).dp,
                y = (((key.y - 12f) + floatOffset) * scaleY).dp
            )
            .size((24f * scaleX).dp, (24f * scaleY).dp)
    )
}

@Composable
fun BoxScope.GoalPortalItem(
    level: GameLevel,
    scaleX: Float,
    scaleY: Float,
    unlocked: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "goal_anim")
    val spinAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "goal_spin"
    )
    
    val pulseSize by infiniteTransition.animateFloat(
        initialValue = 28f,
        targetValue = 35f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "goal_pulse"
    )

    Image(
        painter = painterResource(id = R.drawable.ic_goal),
        contentDescription = "Level Exit Goal",
        modifier = Modifier
            .offset(
                x = ((level.endX - pulseSize / 2f) * scaleX).dp,
                y = ((level.endY - pulseSize / 2f) * scaleY).dp
            )
            .size((pulseSize * scaleX).dp, (pulseSize * scaleY).dp)
            .rotate(spinAngle),
        colorFilter = if (!unlocked) {
            ColorFilter.tint(Color(0xFFFF3B30)) // Show red when keys are outstanding!
        } else null
    )
}

@Composable
fun BoxScope.PortalItem(
    portal: Portal,
    scaleX: Float,
    scaleY: Float
) {
    val infiniteTransition = rememberInfiniteTransition(label = "portal_anim")
    val rotationVal by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "portal_rotation"
    )

    // Render Blue input portal gate
    Image(
        painter = painterResource(id = R.drawable.ic_portal_in),
        contentDescription = "Blue Portal Entrada",
        modifier = Modifier
            .offset(
                x = ((portal.input.x - 16f) * scaleX).dp,
                y = ((portal.input.y - 16f) * scaleY).dp
            )
            .size((32f * scaleX).dp, (1.2f * 32f * scaleY).dp)
            .rotate(rotationVal)
    )

    // Render Orange output portal gate
    Image(
        painter = painterResource(id = R.drawable.ic_portal_out),
        contentDescription = "Orange Portal Exit",
        modifier = Modifier
            .offset(
                x = ((portal.output.x - 16f) * scaleX).dp,
                y = ((portal.output.y - 16f) * scaleY).dp
            )
            .size((32f * scaleX).dp, (1.2f * 32f * scaleY).dp)
            .rotate(-rotationVal) // rotate in the opposite direction for contrast!
    )
}

// Custom Easing supporting retro-smoothness
private val SineJoin = Easing { fraction ->
    kotlin.math.sin(fraction * Math.PI - Math.PI / 2).toFloat() * 0.5f + 0.5f
}

// Helper object supporting standard card borders elegantly
object RowExt {
    @Composable
    fun borderOutline(color: Color) = androidx.compose.foundation.BorderStroke(1.5.dp, color)
}
