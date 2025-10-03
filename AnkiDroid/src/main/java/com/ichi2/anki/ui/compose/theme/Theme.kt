package com.ichi2.anki.ui.compose.theme

import android.os.Build
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.ichi2.anki.R
import com.ichi2.anki.ui.compose.AppShapes
import com.ichi2.anki.ui.compose.AppTypography
import com.ichi2.themes.Themes

@ColorInt
@Composable
private fun ankiColor(@AttrRes attr: Int): Int {
    val context = LocalContext.current
    val typedValue = TypedValue()
    context.theme.resolveAttribute(attr, typedValue, true)
    return typedValue.data
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
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

    val ankiColors = AnkiColors(
        againButton = Color(ankiColor(R.attr.againButtonBackground)),
        hardButton = Color(ankiColor(R.attr.hardButtonBackground)),
        goodButton = Color(ankiColor(R.attr.goodButtonBackground)),
        easyButton = Color(ankiColor(R.attr.easyButtonBackground)),
        newCount = Color(ankiColor(R.attr.newCountColor)),
        learnCount = Color(ankiColor(R.attr.learnCountColor)),
        reviewCount = Color(ankiColor(R.attr.reviewCountColor)),
        topBar = Color(ankiColor(R.attr.topBarColor))
    )

    CompositionLocalProvider(LocalAnkiColors provides ankiColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            shapes = AppShapes,
            motionScheme = MotionScheme.expressive(),
            content = content
        )
    }
}