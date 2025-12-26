package com.ichi2.anki.ui.compose.preferences

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun SliderPreferenceContent(
    title: String,
    summary: String?,
    value: Int,
    valueFrom: Int,
    valueTo: Int,
    stepSize: Float,
    displayValue: Boolean,
    displayFormat: String?,
    onValueChange: (Int) -> Unit,
    icon: Painter? = null,
    isIconSpaceReserved: Boolean = false,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    // We use a local state for the slider to ensure smooth dragging,
    // and only commit the change when dragging stops (or as needed).
    var sliderPosition by remember(value) { mutableFloatStateOf(value.toFloat()) }

    // XML ComposeView handles horizontal padding (?attr/listPreferredItemPaddingStart/End)
    Row(
        modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(
                painter = icon,
                contentDescription = null,
                tint = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface.copy(
                    alpha = 0.38f
                ),
                modifier = Modifier
                    .padding(end = 16.dp) // Standard icon padding
                    .size(24.dp)
            )
        } else if (isIconSpaceReserved) {
            Box(
                modifier = Modifier
                    .padding(end = 16.dp)
                    .size(24.dp)
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(top = 12.dp, bottom = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(
                            alpha = 0.38f
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (!summary.isNullOrEmpty()) {
                        Text(
                            text = summary,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface.copy(
                                alpha = 0.38f
                            ),
                            modifier = Modifier.padding(top = 2.dp),
                            maxLines = 4,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                if (displayValue) {
                    val displayText =
                        displayFormat?.let { String.format(it, sliderPosition.toInt()) }
                            ?: sliderPosition.toInt().toString()
                    Text(
                        text = displayText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface.copy(
                            alpha = 0.38f
                        ),
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }

            // Material3 Slider
            Slider(
                value = sliderPosition,
                onValueChange = {
                    sliderPosition = it
                },
                onValueChangeFinished = {
                    onValueChange(sliderPosition.toInt())
                },
                valueRange = valueFrom.toFloat()..valueTo.toFloat(),
                steps = if (stepSize > 0) maxOf(
                    0, ((valueTo - valueFrom) / stepSize).toInt() - 1
                ) else 0,
                modifier = Modifier.fillMaxWidth(),
                enabled = enabled,
                colors = SliderDefaults.colors(
                    activeTickColor = Color.Transparent,
                    inactiveTickColor = Color.Transparent,
                    disabledActiveTickColor = Color.Transparent,
                    disabledInactiveTickColor = Color.Transparent
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewSliderPreferenceContent() {
    MaterialTheme {
        SliderPreferenceContent(
            title = "Text Size",
            summary = "Adjust the text size for readability",
            value = 100,
            valueFrom = 50,
            valueTo = 200,
            stepSize = 10f,
            displayValue = true,
            displayFormat = "%d%%",
            onValueChange = {},
            isIconSpaceReserved = true
        )
    }
}
