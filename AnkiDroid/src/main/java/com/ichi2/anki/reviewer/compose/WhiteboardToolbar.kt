/*
 * Copyright (c) 2025 Brayan Oliveira <69634269+brayandso@users.noreply.github.com>
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
package com.ichi2.anki.reviewer.compose

import android.view.View
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ichi2.anki.R
import com.ichi2.anki.ui.windows.reviewer.whiteboard.WhiteboardViewModel
import com.ichi2.anki.ui.windows.reviewer.whiteboard.compose.AddBrushButton
import com.ichi2.anki.ui.windows.reviewer.whiteboard.compose.ColorBrushButton

/**
 * Compose-based whiteboard toolbar with undo/redo, eraser, and brush selection.
 */
@Composable
fun WhiteboardToolbar(
    viewModel: WhiteboardViewModel,
    onBrushClick: (View, Int) -> Unit,
    onBrushLongClick: (Int) -> Unit,
    onAddBrush: () -> Unit,
    onEraserClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val canUndo by viewModel.canUndo.collectAsState()
    val canRedo by viewModel.canRedo.collectAsState()
    val brushes by viewModel.brushes.collectAsState()
    val activeBrushIndex by viewModel.activeBrushIndex.collectAsState()
    val isEraserActive by viewModel.isEraserActive.collectAsState()

    val colorNormal = MaterialTheme.colorScheme.onSurface
    val colorHighlight = MaterialTheme.colorScheme.surfaceVariant

    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = MaterialTheme.shapes.large,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Undo button
            IconButton(
                onClick = { viewModel.undo() },
                enabled = canUndo
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Undo,
                    contentDescription = stringResource(R.string.undo),
                    tint = if (canUndo) colorNormal else colorNormal.copy(alpha = 0.38f)
                )
            }

            // Redo button
            IconButton(
                onClick = { viewModel.redo() },
                enabled = canRedo
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Redo,
                    contentDescription = stringResource(R.string.redo),
                    tint = if (canRedo) colorNormal else colorNormal.copy(alpha = 0.38f)
                )
            }

            // Eraser button
            IconButton(
                onClick = onEraserClick
            ) {
                Icon(
                    painter = painterResource(R.drawable.eraser),
                    contentDescription = stringResource(R.string.eraser),
                    tint = if (isEraserActive) MaterialTheme.colorScheme.primary else colorNormal
                )
            }

            // Divider
            HorizontalDivider(
                modifier = Modifier
                    .height(32.dp)
                    .width(1.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )

            Spacer(modifier = Modifier.width(4.dp))

            // Brush palette with horizontal scroll
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                val view = LocalView.current
                brushes.forEachIndexed { index, brush ->
                    ColorBrushButton(
                        brush = brush,
                        isSelected = (index == activeBrushIndex && !isEraserActive),
                        onClick = { onBrushClick(view, index) },
                        onLongClick = { onBrushLongClick(index) },
                        colorNormal = colorNormal,
                        colorHighlight = colorHighlight
                    )
                }

                AddBrushButton(
                    onClick = onAddBrush,
                    colorNormal = colorNormal,
                    tooltip = stringResource(R.string.add_brush)
                )
            }
        }
    }
}
