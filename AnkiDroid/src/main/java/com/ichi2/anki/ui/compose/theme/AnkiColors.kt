package com.ichi2.anki.ui.compose.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * A class to hold custom colors for the AnkiDroid theme that are not part of the standard
 * MaterialTheme color scheme.
 */
data class AnkiColors(
    val againButton: Color,
    val hardButton: Color,
    val goodButton: Color,
    val easyButton: Color,
    val newCount: Color,
    val learnCount: Color,
    val reviewCount: Color,
    val topBar: Color
)

val LocalAnkiColors = staticCompositionLocalOf<AnkiColors> {
    error("No AnkiColors provided")
}