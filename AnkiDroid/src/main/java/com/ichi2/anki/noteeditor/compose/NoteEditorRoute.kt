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

        // Check for arguments in Bundle first, then direct extras
        val bundleArgs = intent?.getBundleExtra(NoteEditorActivity.FRAGMENT_ARGS_EXTRA)

        if (intent != null) {
            // Extract from Bundle or Intent directly
            val cardId = bundleArgs?.getLong("CARD_ID", -1L)?.takeIf { it != -1L }
                ?: intent.getLongExtra("CARD_ID", -1L).takeIf { it != -1L }

            val deckId = bundleArgs?.getLong("DECK_ID", 0L) ?: intent.getLongExtra("DECK_ID", 0L)

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
                    } else {
                        // TODO: Show toast or snackbar that note must be saved first?
                        // Or maybe preview the note content without a card ID (if supported)
                    }
                }
            }, overflowItems = buildList {
                add(
                    NoteEditorSimpleOverflowItem(
                    id = "cards",
                    title = stringResource(
                        R.string.note_editor_cards,
                        "1"
                    ), // TODO: Get actual card count
                    onClick = { /* TODO: Open Cards Template Editor */ }))

                if (!isAdding) {
                    add(
                        NoteEditorSimpleOverflowItem(
                        id = "add", title = stringResource(R.string.add), onClick = {
                            // TODO: Save and switch to add mode
                        }))
                    add(
                        NoteEditorSimpleOverflowItem(
                        id = "copy_note",
                        title = stringResource(R.string.copy_note),
                        onClick = {
                            // TODO: Copy note logic
                        }))
                }

                add(
                    NoteEditorSimpleOverflowItem(
                    id = "font_size",
                    title = stringResource(R.string.font_size),
                    onClick = { /* TODO: Show font size dialog */ }))

                add(
                    NoteEditorToggleOverflowItem(
                    id = "capitalize",
                    title = stringResource(R.string.note_editor_capitalize),
                    checked = capitalizeChecked,
                    onCheckedChange = {
                        capitalizeChecked = it
                        // TODO: Save to prefs
                    }))

                add(
                    NoteEditorToggleOverflowItem(
                    id = "scroll_toolbar",
                    title = stringResource(R.string.menu_scroll_toolbar),
                    checked = scrollToolbarChecked,
                    onCheckedChange = {
                        scrollToolbarChecked = it
                        // TODO: Save to prefs
                    }))

                add(
                    NoteEditorToggleOverflowItem(
                    id = "show_toolbar",
                    title = stringResource(R.string.show_toolbar),
                    checked = showToolbar,
                    onCheckedChange = { viewModel.toggleToolbarVisibility() }))
            })
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
            Timber.d("Multimedia clicked for field $index")
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
        onAddCustomButtonClick = { viewModel.addCustomButton() },
        onMathjaxClick = { viewModel.insertMathJax() },
        onHorizontalRuleClick = { viewModel.insertHorizontalRule() },
        customToolbarButtons = toolbarButtons,
        isToolbarVisible = showToolbar,
        allTags = tagsState,
        deckTags = deckTags,
        onUpdateTags = { viewModel.updateTags(it) },
        onAddTag = { viewModel.addTag(it) },
        topBar = topBar
    )
}
