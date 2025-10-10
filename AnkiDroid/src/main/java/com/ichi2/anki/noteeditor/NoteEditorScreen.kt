
package com.ichi2.anki.noteeditor

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.ichi2.anki.ui.compose.theme.AnkiDroidTheme

@Composable
fun NoteEditorScreen(
    viewModel: NoteEditorViewModel = hiltViewModel(),
    onTagsClick: () -> Unit,
    onCardsClick: () -> Unit,
    onMediaClick: (Int) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()

    AnkiDroidTheme {
        Scaffold { paddingValues ->
            Column(modifier = Modifier.padding(paddingValues)) {
                /*
                NoteEditorSelectors(
                    decks = uiState.decks,
                    selectedDeck = uiState.selectedDeck,
                    onDeckSelected = { viewModel.onDeckSelected(it) },
                    noteTypes = uiState.noteTypes,
                    selectedNoteType = uiState.selectedNoteType,
                    onNoteTypeSelected = { viewModel.onNoteTypeSelected(it) }
                )
                LazyColumn {
                    itemsIndexed(uiState.fields) { index, field ->
                        NoteEditorField(
                            label = field.label,
                            content = field.content,
                            onContentChange = { newContent ->
                                viewModel.onFieldContentChanged(index, newContent)
                            },
                            onMediaClick = { onMediaClick(index) },
                            onStickyClick = { viewModel.onFieldStickyChanged(index) },
                            isSticky = field.isSticky
                        )
                    }
                }
                NoteEditorActions(
                    tags = uiState.tagsLabel,
                    onTagsClick = onTagsClick,
                    cards = uiState.cardsLabel,
                    onCardsClick = onCardsClick
                )
                */
            }
        }
    }
}
