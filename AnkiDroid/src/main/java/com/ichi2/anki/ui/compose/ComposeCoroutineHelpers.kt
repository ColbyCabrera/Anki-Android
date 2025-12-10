/*
 Copyright (c) 2011 Colby Cabrera <gdthyispro@gmail.com>

 This program is free software; you can redistribute it and/or modify it under
 the terms of the GNU General Public License as published by the Free Software
 Foundation; either version 3 of the License, or (at your option) any later
 version.

 This program is distributed in the hope that it is useful, but WITHOUT ANY
 WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 PARTICULAR PURPOSE. See the GNU General Public License for more details.

 You should have received a copy of the GNU General Public License along with
 this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.ichi2.anki.ui.compose

import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import com.ichi2.anki.AnkiActivity
import com.ichi2.anki.ui.compose.components.LoadingIndicator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun <T> AnkiActivity.withComposeProgress(op: suspend () -> T): T {
    val view = ComposeView(this)
    view.setContent {
        LoadingIndicator()
    }

    val rootView = findViewById<ViewGroup>(android.R.id.content)
    withContext(Dispatchers.Main) {
        rootView.addView(view)
    }

    try {
        return op()
    } finally {
        withContext(Dispatchers.Main) {
            rootView.removeView(view)
        }
    }
}
