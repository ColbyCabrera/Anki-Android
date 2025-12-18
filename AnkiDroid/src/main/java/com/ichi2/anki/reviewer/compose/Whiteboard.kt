/*
 * Copyright (c) 2024 Brayan Oliveira <brayandso.dev@gmail.com>
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
 * this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.ichi2.anki.reviewer.compose

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.ichi2.anki.Whiteboard

@Composable
fun Whiteboard(
    modifier: Modifier = Modifier, enabled: Boolean, whiteboard: Whiteboard?
) {
    if (enabled && whiteboard != null) {
        AndroidView(
            factory = {
                (whiteboard.parent as? android.view.ViewGroup)?.removeView(whiteboard)
                whiteboard
            }, modifier = modifier.fillMaxSize()
        )
    }
}
