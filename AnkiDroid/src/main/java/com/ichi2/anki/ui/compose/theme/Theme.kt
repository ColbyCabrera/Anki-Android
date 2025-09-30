package com.ichi2.anki.ui.compose.theme

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.ichi2.anki.ui.compose.AppShapes
import com.ichi2.anki.ui.compose.AppTypography
import com.ichi2.themes.Themes

@Composable
fun AnkiDroidTheme(
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val currentAnkiTheme = Themes.currentTheme
    val colorScheme = if (currentAnkiTheme.isNightMode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            dynamicDarkColorScheme(context)
        } else {
            darkColorScheme()
        }
    } else {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            dynamicLightColorScheme(context)
        } else {
            lightColorScheme()
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}