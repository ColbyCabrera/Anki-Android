
package com.ichi2.anki.deckpicker.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ichi2.anki.R

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SyncProgressDialog(
    title: String,
    message: String,
    onCancel: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(text = title)
                LinearWavyProgressIndicator(modifier = Modifier.fillMaxWidth().padding(top = 12.dp))
            }
        },
        text = { Text(text = message) },
        confirmButton = { },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text(text = stringResource(id = R.string.dialog_cancel))
            }
        }
    )
}

@Preview
@Composable
fun SyncProgressDialogPreview() {
    var showDialog by remember { mutableStateOf(true) }

    if (showDialog) {
        SyncProgressDialog(
            title = "Syncing",
            message = "Syncing in progress...",
            onCancel = { showDialog = false }
        )
    }
}
