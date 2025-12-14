/*
 * Copyright (c) 2024 Ashish Yadav <mailtoashish693@gmail.com>
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.ichi2.anki.dialogs

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.ichi2.anki.Flag
import com.ichi2.anki.R
import com.ichi2.anki.dialogs.compose.FlagRenameScreen
import com.ichi2.utils.customView
import com.ichi2.utils.title
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * A DialogFragment for renaming flags.
 */
class FlagRenameDialog : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val composeView = ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                var flags by remember { mutableStateOf<List<Pair<Flag, String>>>(emptyList()) }

                // Initial load
                LaunchedEffect(Unit) {
                    try {
                        flags = loadFlags()
                    } catch (e: Exception) {
                        Timber.e(e, "Failed to load flags")
                    }
                }

                FlagRenameScreen(
                    flags = flags, onRename = { flag, newName ->
                        val trimmedName = newName.trim()
                        if (trimmedName.isEmpty()) return@FlagRenameScreen

                        lifecycleScope.launch {
                            try {
                                // Optimistic update
                                val updatedFlags = flags.map {
                                    if (it.first == flag) flag to trimmedName else it
                                }
                                flags = updatedFlags

                                flag.rename(trimmedName)
                            } catch (e: Exception) {
                                Timber.e(e, "Failed to rename flag")
                                // Revert optimistic update on failure
                                try {
                                    flags = loadFlags()
                                } catch (e: Exception) {
                                    Timber.e(e, "Failed to reload flags after rename failure")
                                }
                            }
                        }
                    })
            }
        }

        return AlertDialog.Builder(requireContext()).apply {
            customView(view = composeView, 4, 4, 4, 4)
            title(R.string.rename_flag)
        }.create()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        activity?.invalidateOptionsMenu()
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.clearFlags(
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM,
        )
    }

    private suspend fun loadFlags(): List<Pair<Flag, String>> {
        return Flag.queryDisplayNames().filter { it.key != Flag.NONE }
            .map { (flag, displayName) -> flag to displayName }
    }
}
