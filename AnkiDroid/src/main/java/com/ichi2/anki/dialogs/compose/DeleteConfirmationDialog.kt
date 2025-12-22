/* **************************************************************************************
 * Copyright (c) 2025 Ankitects Pty Ltd <https://apps.ankiweb.net>                      *
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

package com.ichi2.anki.dialogs.compose

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.ichi2.anki.R
import com.ichi2.anki.ui.compose.theme.AnkiDroidTheme

@Composable
fun DeleteConfirmationDialog(
    quantity: Int,
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(text = pluralStringResource(R.plurals.card_browser_delete_notes, quantity))
        },
        text = {
            if (quantity == 1) {
                // For a single note, we can use the existing message which asks about the note and its cards
                // We pass an empty string because the original resource expects a string (card content),
                // but we don't have it readily available here and don't want to fetch it.
                // The resource is: "Really delete this note and all its cards?\n%s"
                Text(text = stringResource(R.string.delete_note_message, ""))
            } else {
                // The resource R.plurals.card_browser_delete_notes is "Delete note" / "Delete notes".
                // So the title will just be "Delete notes".
                // We should probably add the count in the body text for clarity.
                // card_browser_subtitle_notes_mode is "%d note shown" / "%d notes shown".
                Text(text = pluralStringResource(R.plurals.card_browser_subtitle_notes_mode, quantity, quantity))
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = stringResource(id = R.string.dialog_positive_delete))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(text = stringResource(id = R.string.dialog_cancel))
            }
        },
    )
}

@Preview(showBackground = true)
@Composable
private fun DeleteConfirmationDialogPreview() {
    AnkiDroidTheme {
        DeleteConfirmationDialog(
            quantity = 5,
            onDismissRequest = {},
            onConfirm = {},
        )
    }
}
