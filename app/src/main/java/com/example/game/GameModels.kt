package com.example.game

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect

data class Platform(
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float = 16f
) {
    val rect: Rect = Rect(x, y, x + width, y + height)
}

data class Lava(
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float
) {
    val rect: Rect = Rect(x, y, x + width, y + height)
}

data class KeyItem(
    val x: Float,
    val y: Float,
    val collected: Boolean = false
) {
    val rect: Rect = Rect(x - 12f, y - 12f, x + 12f, y + 12f)
}

data class Portal(
    val input: Offset,
    val output: Offset
)

data class GameLevel(
    val index: Int,
    val startX: Float,
    val startY: Float,
    val endX: Float,
    val endY: Float,
    val platforms: List<Platform>,
    val text: String,
    val lavas: List<Lava> = emptyList(),
    val keys: List<KeyItem> = emptyList(),
    val portals: List<Portal> = emptyList()
) {
    val goalRect: Rect = Rect(endX - 16f, endY - 16f, endX + 16f, endY + 16f)
}

object LevelsData {
    val levelsList = listOf(
        // Level 1
        GameLevel(
            index = 1,
            startX = 25f,
            startY = 400f,
            endX = 550f,
            endY = 425f,
            platforms = listOf(
                Platform(0f, 450f, 600f)
            ),
            text = "Reach the GOAL to finish the LEVEL."
        ),
        // Level 2
        GameLevel(
            index = 2,
            startX = 25f,
            startY = 200f,
            endX = 550f,
            endY = 375f,
            platforms = listOf(
                Platform(0f, 250f, 250f),
                Platform(300f, 400f, 300f)
            ),
            text = "Avoid contact with RED LAVA.",
            lavas = listOf(
                Lava(0f, 500f, 600f, 100f)
            )
        ),
        // Level 3
        GameLevel(
            index = 3,
            startX = 25f,
            startY = 200f,
            endX = 550f,
            endY = 225f,
            platforms = listOf(
                Platform(0f, 250f, 200f),
                Platform(300f, 250f, 300f)
            ),
            text = "Press the UP button to JUMP.",
            lavas = listOf(
                Lava(0f, 500f, 600f, 100f)
            )
        ),
        // Level 4
        GameLevel(
            index = 4,
            startX = 25f,
            startY = 400f,
            endX = 550f,
            endY = 75f,
            platforms = listOf(
                Platform(0f, 450f, 250f),
                Platform(400f, 450f, 200f),
                Platform(0f, 100f, 600f)
            ),
            text = "Falling through the FLOOR loops you to the top.",
            lavas = emptyList()
        ),
        // Level 5
        GameLevel(
            index = 5,
            startX = 25f,
            startY = 400f,
            endX = 550f,
            endY = 325f,
            platforms = listOf(
                Platform(0f, 450f, 600f),
                Platform(0f, 350f, 600f)
            ),
            text = "You can jump upwards through BLOCKS.",
            lavas = emptyList()
        ),
        // Level 6
        GameLevel(
            index = 6,
            startX = 25f,
            startY = 200f,
            endX = 550f,
            endY = 325f,
            platforms = listOf(
                Platform(0f, 250f, 150f),
                Platform(450f, 350f, 150f)
            ),
            text = "Falling off a LEDGE allows you 1 AIR JUMP.",
            lavas = listOf(
                Lava(290f, 0f, 20f, 200f),
                Lava(0f, 500f, 600f, 100f)
            )
        ),
        // Level 7
        GameLevel(
            index = 7,
            startX = 25f,
            startY = 300f,
            endX = 550f,
            endY = 325f,
            platforms = listOf(
                Platform(0f, 350f, 150f),
                Platform(450f, 350f, 150f)
            ),
            text = "Sometimes one or more KEYs are required.",
            keys = listOf(
                KeyItem(75f, 200f),
                KeyItem(300f, 200f)
            ),
            lavas = listOf(
                Lava(0f, 500f, 600f, 100f)
            )
        ),
        // Level 8
        GameLevel(
            index = 8,
            startX = 25f,
            startY = 200f,
            endX = 550f,
            endY = 225f,
            platforms = listOf(
                Platform(0f, 250f, 100f),
                Platform(100f, 410f, 100f),
                Platform(200f, 250f, 400f)
            ),
            text = "You can utilize bouncing to get a higher AIR JUMP.",
            keys = listOf(
                KeyItem(150f, 390f)
            ),
            lavas = listOf(
                Lava(0f, 500f, 600f, 100f)
            )
        ),
        // Level 9
        GameLevel(
            index = 9,
            startX = 25f,
            startY = 150f,
            endX = 50f,
            endY = 375f,
            platforms = listOf(
                Platform(0f, 200f, 530f),
                Platform(0f, 410f, 110f)
            ),
            text = "Bounce off the WALLs to change direction quickly.",
            lavas = listOf(
                Lava(0f, 500f, 600f, 100f)
            )
        ),
        // Level 10
        GameLevel(
            index = 10,
            startX = 0f,
            startY = 300f,
            endX = 575f,
            endY = 325f,
            platforms = listOf(
                Platform(0f, 350f, 50f),
                Platform(500f, 350f, 100f)
            ),
            text = "PORTALS provide quick one-way transport.",
            lavas = listOf(
                Lava(0f, 500f, 600f, 100f)
            ),
            portals = listOf(
                Portal(input = Offset(25f, 200f), output = Offset(525f, 200f))
            )
        ),
        // Level 11
        GameLevel(
            index = 11,
            startX = 233f,
            startY = 287f,
            endX = 312f,
            endY = 453f,
            platforms = listOf(
                Platform(205f, 337f, 105f),
                Platform(265f, 482f, 95f),
                Platform(428f, 259f, 115f)
            ),
            text = "Good luck with this one.",
            lavas = listOf(
                Lava(112f, 352f, 52f, 62f),
                Lava(420f, 316f, 54f, 41f)
            ),
            keys = listOf(
                KeyItem(482f, 185f),
                KeyItem(54f, 254f)
            )
        )
    )
}
