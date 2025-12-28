/*
 * Copyright (c) 2025 The AnkiDroid open source project
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.ichi2.anki.ui.windows.reviewer.whiteboard.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ichi2.anki.R
import com.ichi2.anki.ui.compose.theme.AnkiDroidTheme
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrushOptionsPopup(
    strokeWidth: Float,
    onStrokeWidthChange: (Float) -> Unit,
    color: Int,
    onColorClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.wrapContentSize(),
        shape = RoundedCornerShape(16.dp),
        elevation = androidx.compose.material3.CardDefaults.elevatedCardElevation(
            defaultElevation = dimensionResource(id = R.dimen.study_screen_elevation)
        ),
        colors = androidx.compose.material3.CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(bottom = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Value Indicator
            Text(
                text = strokeWidth.roundToInt().toString(),
                style = MaterialTheme.typography.bodyMedium, // textAppearanceBody2
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 8.dp)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Slider
                val sliderActiveColor = Color(color)

                // For inactive track, we use the legacy color resource if possible,
                // but since it's a selector referencing attrs, we might need a fallback.
                // However, colorResource should handle it if the context theme is correct.
                // If not, we can approximate it.
                // The xml selector uses ?attr/colorControlActivated with alpha 0.24.
                // In M3 dynamic color, 'primary' is the closest to 'colorControlActivated'.
                val sliderInactiveColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.24f)

                Slider(
                    value = strokeWidth,
                    onValueChange = onStrokeWidthChange,
                    valueRange = 1f..60f,
                    steps = 0, // Continuous
                    modifier = Modifier
                        .width(220.dp),
                    colors = SliderDefaults.colors(
                        thumbColor = sliderActiveColor,
                        activeTrackColor = sliderActiveColor,
                        inactiveTrackColor = sliderInactiveColor,
                        activeTickColor = Color.Transparent,
                        inactiveTickColor = Color.Transparent
                    )
                )

                // Color Button with dynamic icon size
                Box(
                    modifier = Modifier
                        .size(56.dp) // minWidth/Height 56dp
                        .clip(CircleShape) // Add clip for ripple
                        .clickable(onClick = onColorClick),
                    contentAlignment = Alignment.Center
                ) {
                    // The icon
                    // Mimic @drawable/brush_preview_background
                    // Fill: Oval, color = currentBrush.color
                    // Border: Oval, stroke 1dp, color #808080
                    // Size: dynamic based on strokeWidth (pixels converted to Dp)

                    val iconSize = with(LocalDensity.current) { strokeWidth.toDp() }

                    Box(
                        modifier = Modifier
                            .size(iconSize)
                            .clip(CircleShape)
                            .background(Color(color))
                            .border(1.dp, Color(0xFF808080), CircleShape)
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun PreviewBrushOptionsPopup() {
    AnkiDroidTheme {
        BrushOptionsPopup(
            strokeWidth = 28f,
            onStrokeWidthChange = {},
            color = android.graphics.Color.RED,
            onColorClick = {}
        )
    }
}
