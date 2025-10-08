package com.ichi2.anki.noteeditor

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp

@Composable
fun NoteEditorFields(
    fields: List<NoteEditorField>,
    onFieldChanged: (Int, TextFieldValue) -> Unit,
    onFieldFocused: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier) {
        itemsIndexed(fields) { index, field ->
            NoteEditorField(
                field = field,
                onValueChanged = { onFieldChanged(index, it) },
                onFocusChanged = { if (it) onFieldFocused(index) }
            )
        }
    }
}

@Composable
fun NoteEditorField(
    field: NoteEditorField,
    onValueChanged: (TextFieldValue) -> Unit,
    onFocusChanged: (Boolean) -> Unit
) {
    val focusRequester = remember { FocusRequester() }

    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        OutlinedTextField(
            value = field.value,
            onValueChange = onValueChanged,
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
                .onFocusChanged { focusState ->
                    if (focusState.isFocused) {
                        onFocusChanged(true)
                    }
                },
            label = { Text(text = field.name) }
        )
    }

    LaunchedEffect(field.isFocused) {
        if (field.isFocused) {
            focusRequester.requestFocus()
        }
    }
}