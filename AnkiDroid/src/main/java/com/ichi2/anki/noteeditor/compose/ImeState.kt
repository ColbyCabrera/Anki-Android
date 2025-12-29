/* **************************************************************************************
 * Copyright (c) 2025 Colby Cabrera <colbycabrera@gmail.com>                            *
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
package com.ichi2.anki.noteeditor.compose

import android.view.ViewTreeObserver
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

/**
 * Remembers the IME (Input Method Editor/Keyboard) state.
 * Returns a [State] that is true when the keyboard is visible, false otherwise.
 *
 * This composable observes window insets changes to detect when the soft keyboard
 * appears or disappears, which is useful for adjusting UI layout accordingly.
 *
 * @return State<Boolean> representing whether the keyboard is currently visible
 */
@Composable
fun rememberImeState(): State<Boolean> {
    val imeState =
        remember {
            mutableStateOf(false)
        }

    val view = LocalView.current
    DisposableEffect(view) {
        val listener =
            ViewTreeObserver.OnGlobalLayoutListener {
                val isKeyboardOpen =
                    ViewCompat
                        .getRootWindowInsets(view)
                        ?.isVisible(WindowInsetsCompat.Type.ime()) ?: false
                imeState.value = isKeyboardOpen
            }

        view.viewTreeObserver.addOnGlobalLayoutListener(listener)
        onDispose {
            view.viewTreeObserver.removeOnGlobalLayoutListener(listener)
        }
    }
    return imeState
}
