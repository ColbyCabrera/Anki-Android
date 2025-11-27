/* **************************************************************************************
 * Copyright (c) 2025 Colby Cabrera <colbycabrera.wd@gmail.com>                            *
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

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ichi2.anki.CollectionManager
import com.ichi2.anki.NoteEditorActivity
import com.ichi2.anki.R
import com.ichi2.anki.noteeditor.NoteEditorViewModel
import kotlinx.coroutines.launch
import timber.log.Timber

@Composable
fun NoteEditorScreenRoute(
    viewModel: NoteEditorViewModel = viewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToPreview: (Long) -> Unit
) {
    val context = LocalContext.current
    val scope = androidx.compose.runtime.rememberCoroutineScope()

    val noteEditorState by viewModel.noteEditorState.collectAsState()
    val availableDecks by viewModel.availableDecks.collectAsState()
    val availableNoteTypes by viewModel.availableNoteTypes.collectAsState()
    val toolbarButtons by viewModel.toolbarButtons.collectAsState()
    val showToolbar by viewModel.showToolbar.collectAsState()
    val tagsState by viewModel.tagsState.collectAsState()
    val deckTags by viewModel.deckTags.collectAsState()

    // Launchers
    rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode != Activity.RESULT_CANCELED) {
            result.data?.extras ?: return@rememberLauncherForActivityResult
            // TODO: Handle multimedia result properly.
            // Currently NoteEditorViewModel doesn't expose a direct method for this,
            // but we can implement it later or assuming the user just wants the editor to work for now.
        }
    }

    rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        // Handle template edit result (reload required)
    }

    // State for title and preferences
    var isAdding by remember { androidx.compose.runtime.mutableStateOf(true) }
    var capitalizeChecked by remember { androidx.compose.runtime.mutableStateOf(true) } // Default true, should load from prefs
    var scrollToolbarChecked by remember { androidx.compose.runtime.mutableStateOf(true) } // Default true

    // Initial Setup
    LaunchedEffect(Unit) {
        val activity = context as? Activity
        val intent = activity?.intent
        val arguments = intent?.getBundleExtra(NoteEditorActivity.FRAGMENT_ARGS_EXTRA)

        if (intent != null && arguments != null) {
            arguments.getInt("CALLER", 0) // NoteEditorCaller.NO_CALLER.value

            val cardId = arguments.getLong("CARD_ID", -1L).takeIf { it != -1L }
            val deckId = arguments.getLong("DECK_ID", 0L)

            // Determine if adding or editing based on cardId presence
            isAdding = cardId == null

            // Get collection
            try {
                val col = CollectionManager.getColUnsafe()
                viewModel.initializeEditor(
                    col = col, cardId = cardId, deckId = deckId, isAddingNote = isAdding
                )
            } catch (e: Exception) {
                Timber.e(e, "Failed to initialize editor")
            }
        } else {
            // Fallback or error
            Timber.w("No arguments found for NoteEditor")
            try {
                val col = CollectionManager.getColUnsafe()
                viewModel.initializeEditor(col = col)
            } catch (e: Exception) {
                Timber.e(e, "Failed to initialize editor (fallback)")
            }
        }
    }

    val topBar: @Composable () -> Unit = {
        NoteEditorTopAppBar(
            title = if (isAdding) stringResource(R.string.cardeditor_title_add_note) else stringResource(
            R.string.cardeditor_title_edit_card
        ), onBackClick = onNavigateBack, onSaveClick = {
            scope.launch {
                val result = viewModel.saveNote()
                if (result is com.ichi2.anki.NoteFieldsCheckResult.Success) {
                    onNavigateBack()
                }
            }
        }, onPreviewClick = {
            scope.launch {
                val cardId = viewModel.getCurrentCardId()
                if (cardId != null) {
                    onNavigateToPreview(cardId)
                }
            }
        }, overflowItems = listOf(
            NoteEditorSimpleOverflowItem(
                id = "cards",
                title = stringResource(R.string.note_editor_cards),
                onClick = { /* TODO: Open Cards Template Editor */ }), NoteEditorToggleOverflowItem(
                id = "capitalize",
                title = stringResource(R.string.note_editor_capitalize),
                checked = capitalizeChecked,
                onCheckedChange = {
                    capitalizeChecked = it
                    // TODO: Save to prefs
                }), NoteEditorToggleOverflowItem(
                id = "scroll_toolbar",
                title = stringResource(R.string.menu_scroll_toolbar),
                checked = scrollToolbarChecked,
                onCheckedChange = {
                    scrollToolbarChecked = it
                    // TODO: Save to prefs
                })
        )
        )
    }

    NoteEditorScreen(
        state = noteEditorState,
        availableDecks = availableDecks,
        availableNoteTypes = availableNoteTypes,
        onFieldValueChange = { index, value ->
            viewModel.updateFieldValue(index, value)
        },
        onFieldFocus = { index ->
            viewModel.onFieldFocus(index)
        },
        onCardsClick = {
            // Launch template editor
        },
        onDeckSelected = { viewModel.selectDeck(it) },
        onNoteTypeSelected = { viewModel.selectNoteType(it) },
        onMultimediaClick = { index ->
            // TODO: Implement proper multimedia picker
            // For now, we can't use MultimediaActivity directly without constructing complex objects
            // We should implement a simpler picker here or in ViewModel
            Timber.d("Multimedia clicked for field $index")
            // val intent = Intent(context, MultimediaActivity::class.java)
            // intent.putExtra(MultimediaActivity.MULTIMEDIA_RESULT_FIELD_INDEX, index)
            // multimediaLauncher.launch(intent)
        },
        onToggleStickyClick = { viewModel.toggleStickyField(it) },
        onSaveClick = {
            scope.launch {
                val result = viewModel.saveNote()
                if (result is com.ichi2.anki.NoteFieldsCheckResult.Success) {
                    onNavigateBack()
                }
            }
        },
        onPreviewClick = {
            scope.launch {
                val cardId = viewModel.getCurrentCardId()
                if (cardId != null) {
                    onNavigateToPreview(cardId)
                }
            }
        },
        onBoldClick = { viewModel.formatSelection("<b>", "</b>") },
        onItalicClick = { viewModel.formatSelection("<i>", "</i>") },
        onUnderlineClick = { viewModel.formatSelection("<u>", "</u>") },
        onClozeClick = { viewModel.insertCloze(com.ichi2.anki.noteeditor.ClozeInsertionMode.SAME_NUMBER) },
        onClozeIncrementClick = { viewModel.insertCloze(com.ichi2.anki.noteeditor.ClozeInsertionMode.INCREMENT_NUMBER) },
        onCustomButtonClick = { viewModel.applyToolbarButton(it) },
        onCustomButtonLongClick = { /* TODO */ },
        onAddCustomButtonClick = { /* TODO */ },
        customToolbarButtons = toolbarButtons,
        isToolbarVisible = showToolbar,
        allTags = tagsState,
        deckTags = deckTags,
        onUpdateTags = { viewModel.updateTags(it) },
        onAddTag = { viewModel.addTag(it) },
        topBar = topBar
    )
}
