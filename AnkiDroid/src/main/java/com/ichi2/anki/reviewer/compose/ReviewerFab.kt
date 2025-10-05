/****************************************************************************************
 * Copyright (c) 2009 Edu Zamora <edu.zasu@gmail.com>                                   *
 * Copyright (c) 2009 Casey Link <unnamedrambler@gmail.com>                             *
 * Copyright (c) 2014 Timothy Rae <perceptualchaos2@gmail.com>                          *
 *                                                                                      *
 * This program is free software; you can redistribute it and/or modify it under        *
 * the terms of the GNU General Public License as published by the Free Software        *
 * Foundation; either version 3 of the License, or (at your option) any later           *
 * version.                                                                             *
 *                                                                                      *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY      *
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A      *
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.             *
 *                                                                                      *
 * You should have received a copy of the GNU General Public License along with         *
 * this program.  If not, see <http://www.gnu.org/licenses/>.                           *
 ****************************************************************************************/
package com.ichi2.anki.reviewer.compose

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp

@Composable
fun ExpandableReviewerFab(
    onEdit: () -> Unit,
    onBury: () -> Unit,
    onSuspend: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(targetValue = if (expanded) 45f else 0f)
    val fabSize by animateDpAsState(targetValue = if (expanded) 48.dp else 56.dp)

    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (expanded) {
            FloatingActionButton(
                onClick = onEdit,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(Icons.Filled.Edit, contentDescription = "Edit")
            }
            FloatingActionButton(
                onClick = onBury,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = "Bury") // Placeholder icon
            }
            FloatingActionButton(
                onClick = onSuspend,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(Icons.Filled.Pause, contentDescription = "Suspend")
            }
        }
        FloatingActionButton(
            onClick = { expanded = !expanded },
            modifier = Modifier.size(fabSize)
        ) {
            Icon(
                Icons.Filled.Add,
                contentDescription = "More Options",
                modifier = Modifier.rotate(rotation)
            )
        }
    }
}