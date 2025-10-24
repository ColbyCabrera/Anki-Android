/****************************************************************************************
 * Copyright (c) 2010 Norbert Nagold <norbert.nagold@gmail.com>                         *
 * Copyright (c) 2012 Kostas Spyropoulos <inigo.aldana@gmail.com>                       *
 * Copyright (c) 2014 Timothy Rae <perceptualchaos2@gmail.com>                          *
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

package com.ichi2.anki

import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.annotation.VisibleForTesting
import androidx.compose.material3.MaterialTheme
import androidx.lifecycle.ViewModelProvider
import anki.collection.OpChanges
import com.ichi2.anim.ActivityTransitionAnimation.Direction
import com.ichi2.anki.browser.CardBrowserLaunchOptions
import com.ichi2.anki.browser.CardBrowserViewModel
import com.ichi2.anki.browser.MySearchesContract
import com.ichi2.anki.browser.SharedPreferencesLastDeckIdRepository
import com.ichi2.anki.browser.compose.CardBrowserLayout
import com.ichi2.anki.browser.toCardBrowserLaunchOptions
import com.ichi2.anki.dialogs.BrowserOptionsDialog
import com.ichi2.anki.libanki.CardId
import com.ichi2.anki.libanki.Collection
import com.ichi2.anki.noteeditor.NoteEditorLauncher
import com.ichi2.anki.observability.ChangeManager
import com.ichi2.anki.previewer.PreviewerFragment
import com.ichi2.anki.ui.compose.theme.AnkiDroidTheme
import timber.log.Timber

/**
 * A Jetpack Compose-based Activity for browsing cards.
 *
 * This activity is the entry point for the card browser feature. It hosts the [CardBrowserLayout]
 * Composable, which is responsible for rendering the UI. It retains the [CardBrowserViewModel]
 * for state management and business logic.
 */
open class CardBrowser :
    AnkiActivity(),
    ChangeManager.Subscriber {

    private lateinit var viewModel: CardBrowserViewModel

    // Provides an instance of NoteEditorLauncher for editing a note
    private val editNoteLauncher: NoteEditorLauncher
        get() = NoteEditorLauncher.EditCard(viewModel.currentCardId, Direction.DEFAULT, false)

    private val addNoteLauncher: NoteEditorLauncher
        get() = NoteEditorLauncher.AddNoteFromCardBrowser(viewModel, inCardBrowserActivity = false)

    private val onMySearches = registerForActivityResult(MySearchesContract()) { query ->
        if (query != null) {
            viewModel.search(query)
        }
    }

    private var onEditCardActivityResult =
        registerForActivityResult(StartActivityForResult()) { result: ActivityResult ->
            Timber.i("onEditCardActivityResult: resultCode=%d", result.resultCode)
            if (result.resultCode == DeckPicker.RESULT_DB_ERROR) {
                setResult(DeckPicker.RESULT_DB_ERROR)
                finish()
                return@registerForActivityResult
            }
            if (result.resultCode == RESULT_OK) {
                viewModel.onCurrentNoteEdited()
            }
        }

    private var onAddNoteActivityResult =
        registerForActivityResult(StartActivityForResult()) { result: ActivityResult ->
            Timber.i("onAddNoteActivityResult: resultCode=%d", result.resultCode)
            if (result.resultCode == DeckPicker.RESULT_DB_ERROR) {
                setResult(DeckPicker.RESULT_DB_ERROR)
                finish()
                return@registerForActivityResult
            }
            if (result.resultCode == RESULT_OK) {
                viewModel.search(viewModel.searchQuery.value)
            }
        }

    private var onPreviewCardsActivityResult =
        registerForActivityResult(StartActivityForResult()) { result: ActivityResult ->
            Timber.d("onPreviewCardsActivityResult: resultCode=%d", result.resultCode)
            if (result.resultCode == DeckPicker.RESULT_DB_ERROR) {
                setResult(DeckPicker.RESULT_DB_ERROR)
                finish()
                return@registerForActivityResult
            }
            val data = result.data
            if (data != null &&
                (
                    data.getBooleanExtra(NoteEditorFragment.RELOAD_REQUIRED_EXTRA_KEY, false) ||
                        data.getBooleanExtra(NoteEditorFragment.NOTE_CHANGED_EXTRA_KEY, false)
                    )
            ) {
                viewModel.search(viewModel.searchQuery.value)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        if (showedActivityFailedScreen(savedInstanceState)) {
            return
        }
        super.onCreate(savedInstanceState)
        if (!ensureStoragePermissions()) {
            return
        }

        enableEdgeToEdge()

        val launchOptions = intent?.toCardBrowserLaunchOptions()
        viewModel = createViewModel(launchOptions)

        startLoadingCollection()

        setContent {
            AnkiDroidTheme {
                CardBrowserLayout(
                    viewModel = viewModel,
                    onNavigateUp = { finish() },
                    onCardClicked = { row ->
                        launchCatchingTask {
                            val cardId = viewModel.queryDataForCardEdit(row.id)
                            openNoteEditorForCard(cardId)
                        }
                    },
                    onAddNote = {
                        onAddNoteActivityResult.launch(addNoteLauncher.toIntent(this@CardBrowser))
                    },
                    onPreview = {
                        launchCatchingTask {
                            val intentData = viewModel.queryPreviewIntentData()
                            val intent = PreviewerFragment.getIntent(this@CardBrowser, intentData.idsFile, intentData.currentIndex)
                            onPreviewCardsActivityResult.launch(intent)
                        }
                    },
                    onFilter = viewModel::search,
                    onSelectAll = {
                        // TODO
                    },
                    onOptions = {
                        val dialog = BrowserOptionsDialog.newInstance(viewModel.cardsOrNotes, viewModel.isTruncated)
                        dialog.show(supportFragmentManager, "BrowserOptionsDialog")
                    },
                    onCreateFilteredDeck = {
                        // TODO
                    }
                )
            }
        }
    }

    override fun onCollectionLoaded(col: Collection) {
        super.onCollectionLoaded(col)
        Timber.d("onCollectionLoaded(): Collection loaded, ViewModel will start search.")
        // The ViewModel observes the collection loading state and will trigger the initial search.
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        if (event == null) {
            return super.onKeyUp(keyCode, event)
        }
        when (keyCode) {
            KeyEvent.KEYCODE_E -> {
                if (event.isCtrlPressed) {
                    onAddNoteActivityResult.launch(addNoteLauncher.toIntent(this))
                    return true
                }
            }
            KeyEvent.KEYCODE_F -> {
                if (event.isCtrlPressed) {
                    onMySearches.launch(Unit)
                    return true
                }
            }
            KeyEvent.KEYCODE_P -> {
                if (event.isCtrlPressed && event.isShiftPressed) {
                    launchCatchingTask {
                        val intentData = viewModel.queryPreviewIntentData()
                        val intent = PreviewerFragment.getIntent(this@CardBrowser, intentData.idsFile, intentData.currentIndex)
                        onPreviewCardsActivityResult.launch(intent)
                    }
                    return true
                }
            }
            KeyEvent.KEYCODE_Z -> {
                if (event.isCtrlPressed) {
                    viewModel.undo()
                    return true
                }
            }
        }
        return super.onKeyUp(keyCode, event)
    }

    /** Opens the note editor for a card. */
    fun openNoteEditorForCard(cardId: CardId) {
        viewModel.currentCardId = cardId
        onEditCardActivityResult.launch(editNoteLauncher.toIntent(this))
    }

    private fun createViewModel(
        launchOptions: CardBrowserLaunchOptions?
    ) = ViewModelProvider(
        viewModelStore,
        CardBrowserViewModel.factory(
            lastDeckIdRepository = AnkiDroidApp.instance.sharedPrefsLastDeckIdRepository,
            cacheDir = cacheDir,
            options = launchOptions,
            isFragmented = false, // Not using fragments in the Compose version
        ),
        defaultViewModelCreationExtras,
    )[CardBrowserViewModel::class.java]

    override fun opExecuted(changes: OpChanges, handler: Any?) {
        if (handler === this || handler === viewModel) {
            return
        }

        // A database change from another component occurred, force a refresh
        if (changes.browserTable || changes.noteText || changes.card) {
            viewModel.launchSearchForCards()
        }
    }

    override val shortcuts = null

    companion object {
        // Values related to persistent state data
        fun clearLastDeckId() = SharedPreferencesLastDeckIdRepository.clearLastDeckId()

        @VisibleForTesting
        fun createAddNoteLauncher(
            viewModel: CardBrowserViewModel,
            inCardBrowserActivity: Boolean = false,
        ): NoteEditorLauncher = NoteEditorLauncher.AddNoteFromCardBrowser(viewModel, inCardBrowserActivity)
    }
}