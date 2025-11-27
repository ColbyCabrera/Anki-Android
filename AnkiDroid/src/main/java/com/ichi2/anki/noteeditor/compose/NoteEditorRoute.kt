package com.ichi2.anki.noteeditor.compose

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ichi2.anki.AnkiActivity
import com.ichi2.anki.CollectionManager
import com.ichi2.anki.NoteEditorActivity
import com.ichi2.anki.R
import com.ichi2.anki.multimedia.MultimediaActivity
import com.ichi2.anki.noteeditor.NoteEditorViewModel
import com.ichi2.anki.noteeditor.NoteEditorRoute
import timber.log.Timber

@Composable
fun NoteEditorScreenRoute(
    viewModel: NoteEditorViewModel = viewModel(),
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? NoteEditorActivity

    val noteEditorState by viewModel.noteEditorState.collectAsState()
    val availableDecks by viewModel.availableDecks.collectAsState()
    val availableNoteTypes by viewModel.availableNoteTypes.collectAsState()

    // Launchers
    val multimediaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_CANCELED) {
            // Handle cancellation (logic from Fragment)
            val index = result.data?.extras?.getInt(MultimediaActivity.MULTIMEDIA_RESULT_FIELD_INDEX) ?: return@rememberLauncherForActivityResult
            // TODO: Show multimedia bottom sheet (needs porting)
        } else {
            // Handle result
            val extras = result.data?.extras ?: return@rememberLauncherForActivityResult
            // TODO: Handle multimedia result (needs porting logic to ViewModel or here)
        }
    }

    val templateEditLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        // Handle template edit result (reload required)
    }

    // Initial Setup
    LaunchedEffect(Unit) {
        // Initialize ViewModel if needed (logic from setupComposeEditor)
        // This might need arguments passed from the route
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
        onMultimediaClick = { /* TODO */ },
        onToggleStickyClick = { viewModel.toggleStickyField(it) },
        onSaveClick = { /* TODO */ },
        onPreviewClick = { /* TODO */ },
        onBoldClick = { /* TODO */ },
        onItalicClick = { /* TODO */ },
        onUnderlineClick = { /* TODO */ },
        onClozeClick = { /* TODO */ },
        onClozeIncrementClick = { /* TODO */ },
        onCustomButtonClick = { /* TODO */ },
        onCustomButtonLongClick = { /* TODO */ },
        onAddCustomButtonClick = { /* TODO */ },
        customToolbarButtons = emptyList(), // TODO: Get from ViewModel
        isToolbarVisible = true, // TODO: Get from ViewModel
        allTags = com.ichi2.anki.compose.TagsState.Loading, // TODO: Get from ViewModel
        deckTags = emptySet(), // TODO: Get from ViewModel
        onUpdateTags = { viewModel.updateTags(it) },
        onAddTag = { viewModel.addTag(it) }
    )
}
