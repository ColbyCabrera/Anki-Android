package com.ichi2.anki.noteeditor

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.ichi2.anki.R
import com.ichi2.anki.libanki.NotetypeJson
import com.ichi2.anki.model.SelectableDeck

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorSelectors(
    decks: List<SelectableDeck>,
    selectedDeck: SelectableDeck?,
    onDeckSelected: (SelectableDeck) -> Unit,
    noteTypes: List<NotetypeJson>,
    selectedNoteType: NotetypeJson?,
    onNoteTypeSelected: (NotetypeJson) -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        // Deck Selector
        var deckMenuExpanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = deckMenuExpanded,
            onExpandedChange = { deckMenuExpanded = !deckMenuExpanded },
            modifier = Modifier.weight(1f)
        ) {
            OutlinedTextField(
                value = selectedDeck?.name ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(R.string.deck_name)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = deckMenuExpanded) },
                modifier = Modifier.menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = deckMenuExpanded,
                onDismissRequest = { deckMenuExpanded = false }
            ) {
                decks.forEach { deck ->
                    DropdownMenuItem(
                        text = { Text(deck.name) },
                        onClick = {
                            onDeckSelected(deck)
                            deckMenuExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(IntrinsicSize.Min))

        // Note Type Selector
        var noteTypeMenuExpanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = noteTypeMenuExpanded,
            onExpandedChange = { noteTypeMenuExpanded = !noteTypeMenuExpanded },
            modifier = Modifier.weight(1f)
        ) {
            OutlinedTextField(
                value = selectedNoteType?.name ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(R.string.note_type)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = noteTypeMenuExpanded) },
                modifier = Modifier.menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = noteTypeMenuExpanded,
                onDismissRequest = { noteTypeMenuExpanded = false }
            ) {
                noteTypes.forEach { noteType ->
                    DropdownMenuItem(
                        text = { Text(noteType.name) },
                        onClick = {
                            onNoteTypeSelected(noteType)
                            noteTypeMenuExpanded = false
                        }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NoteEditorSelectorsPreview() {
    val decks = listOf(
        SelectableDeck.Deck("Default", 0),
        SelectableDeck.Deck("Japanese", 1)
    )
    val noteTypes = listOf(
        NotetypeJson().apply { name = "Basic" },
        NotetypeJson().apply { name = "Basic (and reversed card)" }
    )

    NoteEditorSelectors(
        decks = decks,
        selectedDeck = decks[0],
        onDeckSelected = {},
        noteTypes = noteTypes,
        selectedNoteType = noteTypes[0],
        onNoteTypeSelected = {}
    )
}