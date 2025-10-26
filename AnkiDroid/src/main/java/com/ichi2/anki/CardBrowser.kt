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
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.ViewModelProvider
import anki.collection.OpChanges
import com.ichi2.anim.ActivityTransitionAnimation.Direction
import com.ichi2.anki.browser.BrowserColumnSelectionFragment
import com.ichi2.anki.browser.CardBrowserLaunchOptions
import com.ichi2.anki.browser.CardBrowserViewModel
import com.ichi2.anki.browser.CardOrNoteId
import com.ichi2.anki.browser.MySearchesContract
import com.ichi2.anki.browser.RepositionCardFragment
import com.ichi2.anki.browser.RepositionCardsRequest
import com.ichi2.anki.browser.SharedPreferencesLastDeckIdRepository
import com.ichi2.anki.browser.compose.CardBrowserLayout
import com.ichi2.anki.browser.compose.FilterByTagsDialog
import com.ichi2.anki.browser.toCardBrowserLaunchOptions
import com.ichi2.anki.dialogs.BrowserOptionsDialog
import com.ichi2.anki.dialogs.CreateDeckDialog
import com.ichi2.anki.dialogs.DeckSelectionDialog
import com.ichi2.anki.dialogs.FlagRenameDialog
import com.ichi2.anki.dialogs.SimpleMessageDialog
import com.ichi2.anki.export.ExportDialogFragment
import com.ichi2.anki.libanki.CardId
import com.ichi2.anki.libanki.Collection
import com.ichi2.anki.libanki.DeckId
import com.ichi2.anki.model.CardsOrNotes
import com.ichi2.anki.model.SelectableDeck
import com.ichi2.anki.noteeditor.NoteEditorLauncher
import com.ichi2.anki.observability.ChangeManager
import com.ichi2.anki.pages.CardInfoDestination
import com.ichi2.anki.previewer.PreviewerFragment
import com.ichi2.anki.scheduling.ForgetCardsDialog
import com.ichi2.anki.scheduling.SetDueDateDialog
import com.ichi2.anki.snackbar.showSnackbar
import com.ichi2.anki.ui.compose.theme.AnkiDroidTheme
import timber.log.Timber

/**
 * A Jetpack Compose-based Activity for browsing cards.
 *
 * This activity is the entry point for the card browser feature. It hosts the [CardBrowserLayout]
 * Composable, which is responsible for rendering the UI. It retains the [CardBrowserViewModel]
 * for state management and business logic.
 */
open class CardBrowser : AnkiActivity(), ChangeManager.Subscriber,
    DeckSelectionDialog.DeckSelectionListener {

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
            if (data != null && (data.getBooleanExtra(
                    NoteEditorFragment.RELOAD_REQUIRED_EXTRA_KEY,
                    false
                ) || data.getBooleanExtra(NoteEditorFragment.NOTE_CHANGED_EXTRA_KEY, false))
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

        setContentView(R.layout.card_browser_activity)
        findViewById<ComposeView>(R.id.compose_view).setContent {
            AnkiDroidTheme {
                Box(modifier = Modifier.fillMaxSize()) {
                    var showBrowserOptionsDialog by rememberSaveable { mutableStateOf(false) }
                    var showFilterByTagsDialog by rememberSaveable { mutableStateOf(false) }
                    val selectedTags by viewModel.selectedTags.collectAsState()

                    if (showBrowserOptionsDialog) {
                        BrowserOptionsDialog(
                            onDismissRequest = {
                                showBrowserOptionsDialog = false
                            },
                            onConfirm = { cardsOrNotes, isTruncated, shouldIgnoreAccents ->
                                viewModel.setCardsOrNotes(cardsOrNotes)
                                viewModel.setTruncated(isTruncated)
                                viewModel.setIgnoreAccents(shouldIgnoreAccents)
                            },
                            initialCardsOrNotes = viewModel.cardsOrNotes,
                            initialIsTruncated = viewModel.isTruncated,
                            initialShouldIgnoreAccents = viewModel.shouldIgnoreAccents,
                            onManageColumnsClicked = {
                                val dialog =
                                    BrowserColumnSelectionFragment.createInstance(viewModel.cardsOrNotes)
                                dialog.show(supportFragmentManager, null)
                            },
                            onRenameFlagClicked = {
                                val flagRenameDialog = FlagRenameDialog()
                                flagRenameDialog.show(supportFragmentManager, "FlagRenameDialog")
                            })
                    }
                    if (showFilterByTagsDialog) {
                        FilterByTagsDialog(
                            onDismissRequest = { showFilterByTagsDialog = false },
                            onConfirm = { tags ->
                                viewModel.filterByTags(tags)
                                showFilterByTagsDialog = false
                            },
                            allTags = viewModel.allTags,
                            initialSelection = selectedTags
                        )
                    }
                    CardBrowserLayout(
                        viewModel = viewModel,
                        onNavigateUp = { finish() },
                        onCardClicked = { row ->
                            if (viewModel.isInMultiSelectMode) {
                                viewModel.toggleRowSelection(
                                    CardBrowserViewModel.RowSelection(
                                        rowId = CardOrNoteId(row.id), topOffset = 0
                                    )
                                )
                            } else {
                                launchCatchingTask {
                                    val cardId = viewModel.queryDataForCardEdit(row.id)
                                    openNoteEditorForCard(cardId)
                                }
                            }
                        },
                        onAddNote = {
                            onAddNoteActivityResult.launch(addNoteLauncher.toIntent(this@CardBrowser))
                        },
                        onPreview = {
                            launchCatchingTask {
                                val intentData = viewModel.queryPreviewIntentData()
                                val intent = PreviewerFragment.getIntent(
                                    this@CardBrowser, intentData.idsFile, intentData.currentIndex
                                )
                                onPreviewCardsActivityResult.launch(intent)
                            }
                        },
                        onFilter = viewModel::search,
                        onSelectAll = {
                            viewModel.toggleSelectAllOrNone()
                        },
                        onOptions = {
                            showBrowserOptionsDialog = true
                        },
                        onCreateFilteredDeck = {
                            showCreateFilteredDeckDialog()
                        },
                        onEditNote = {
                            openNoteEditorForCard(viewModel.currentCardId)
                        },
                        onCardInfo = {
                            val cardId = viewModel.currentCardId
                            val destination =
                                CardInfoDestination(cardId, getString(R.string.card_info_title))
                            startActivity(destination.toIntent(this@CardBrowser))
                        },
                        onChangeDeck = {
                            showChangeDeckDialog()
                        },
                        onReposition = {
                            repositionSelectedCards()
                        },
                        onSetDueDate = {
                            rescheduleSelectedCards()
                        },
                        onEditTags = {
                            showEditTagsDialog()
                        },
                        onGradeNow = {},
                        onResetProgress = {
                            onResetProgress()
                        },
                        onExportCard = {
                            exportSelected()
                        },
                        onFilterByTag = {
                            showFilterByTagsDialog = true
                        })
                }
            }
        }
    }

    override fun onCollectionLoaded(col: Collection) {
        super.onCollectionLoaded(col)
        Timber.d("onCollectionLoaded(): Collection loaded, ViewModel will start search.")
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
                        val intent = PreviewerFragment.getIntent(
                            this@CardBrowser,
                            intentData.idsFile,
                            intentData.currentIndex
                        )
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

    fun showCreateFilteredDeckDialog() {
        val createFilteredDeckDialog = CreateDeckDialog(
            this, R.string.new_deck, CreateDeckDialog.DeckDialogType.FILTERED_DECK, null
        )
        createFilteredDeckDialog.onNewDeckCreated = {}
        launchCatchingTask {
            withProgress {
                createFilteredDeckDialog.showFilteredDeckDialog()
            }
        }
    }

    private fun createViewModel(
        launchOptions: CardBrowserLaunchOptions?
    ) = ViewModelProvider(
        viewModelStore,
        CardBrowserViewModel.factory(
            lastDeckIdRepository = AnkiDroidApp.instance.sharedPrefsLastDeckIdRepository,
            cacheDir = cacheDir,
            options = launchOptions,
            isFragmented = false,
        ),
        defaultViewModelCreationExtras,
    )[CardBrowserViewModel::class.java]

    override fun opExecuted(changes: OpChanges, handler: Any?) {
        if (handler === this || handler === viewModel) {
            return
        }

        if (changes.browserTable || changes.noteText || changes.card) {
            viewModel.launchSearchForCards()
        }
    }

    override val shortcuts = null

    companion object {
        fun clearLastDeckId() = SharedPreferencesLastDeckIdRepository.clearLastDeckId()

        @VisibleForTesting
        fun createAddNoteLauncher(
            viewModel: CardBrowserViewModel,
            inCardBrowserActivity: Boolean = false,
        ): NoteEditorLauncher =
            NoteEditorLauncher.AddNoteFromCardBrowser(viewModel, inCardBrowserActivity)
    }

    private fun showChangeDeckDialog() = launchCatchingTask {
        if (!viewModel.hasSelectedAnyRows()) {
            Timber.i("Not showing Change Deck - No Cards")
            return@launchCatchingTask
        }
        val selectableDecks = viewModel.getAvailableDecks()
        val dialog = DeckSelectionDialog.newInstance(
            getString(R.string.move_all_to_deck),
            null,
            false,
            selectableDecks,
        )
        dialog.show(supportFragmentManager, "deck_selection_dialog")
    }

    private fun rescheduleSelectedCards() {
        if (!viewModel.hasSelectedAnyRows()) {
            Timber.i("Attempted reschedule - no cards selected")
            return
        }
        if (warnUserIfInNotesOnlyMode()) return

        launchCatchingTask {
            val allCardIds = viewModel.queryAllSelectedCardIds()
            SetDueDateDialog.newInstance(allCardIds)
                .show(supportFragmentManager, "set_due_date_dialog")
        }
    }

    private fun repositionSelectedCards(): Boolean {
        Timber.i("CardBrowser:: Reposition button pressed")
        if (warnUserIfInNotesOnlyMode()) return false
        launchCatchingTask {
            when (val repositionCardsResult = viewModel.prepareToRepositionCards()) {
                is RepositionCardsRequest.ContainsNonNewCardsError -> {
                    SimpleMessageDialog.newInstance(
                        title = getString(R.string.vague_error),
                        message = getString(R.string.reposition_card_not_new_error),
                        reload = false,
                    ).show(supportFragmentManager, "reposition_error_dialog")
                    return@launchCatchingTask
                }

                is RepositionCardsRequest.RepositionData -> {
                    val top = repositionCardsResult.queueTop
                    val bottom = repositionCardsResult.queueBottom
                    if (top == null || bottom == null) {
                        return@launchCatchingTask
                    }
                    val repositionDialog = RepositionCardFragment.newInstance(
                        queueTop = top,
                        queueBottom = bottom,
                        random = repositionCardsResult.random,
                        shift = repositionCardsResult.shift,
                    )
                    repositionDialog.show(supportFragmentManager, "reposition_dialog")
                }
            }
        }
        return true
    }

    private fun onResetProgress() {
        if (warnUserIfInNotesOnlyMode()) return
        ForgetCardsDialog().show(supportFragmentManager, "reset_progress_dialog")
    }

    private fun exportSelected() {
        val (type, selectedIds) = viewModel.querySelectionExportData() ?: return
        ExportDialogFragment.newInstance(type, selectedIds)
            .show(supportFragmentManager, "exportDialog")
    }

    private fun showEditTagsDialog() {
        if (!viewModel.hasSelectedAnyRows()) {
            Timber.d("showEditTagsDialog: called with empty selection")
        }
    }

    private fun showFilterByTagsDialog() {
    }

    /**
     * If the user is in notes only mode, and there are notes selected,
     * show a snackbar explaining that the operation is not possible.
     * @return true if the user was warned, false otherwise.
     */
    fun warnUserIfInNotesOnlyMode(): Boolean {
        if (viewModel.cardsOrNotes == CardsOrNotes.NOTES && viewModel.hasSelectedAnyRows()) {
            showSnackbar(
                getString(R.string.card_browser_unavailable_when_notes_mode),
                duration = 5000,
            ) {
                setAction(getString(R.string.cards)) {
                    viewModel.setCardsOrNotes(CardsOrNotes.CARDS)
                }
            }
            return true
        }
        return false
    }

    override fun onDeckSelected(deck: SelectableDeck?) {
        val did = (deck as? SelectableDeck.Deck)?.deckId ?: return
        moveSelectedCardsToDeck(did)
    }

    private fun moveSelectedCardsToDeck(did: DeckId) = launchCatchingTask {
        val changed = withProgress { viewModel.moveSelectedCardsToDeck(did).await() }
        viewModel.search(viewModel.searchQuery.value)
        val message = resources.getQuantityString(
            R.plurals.card_browser_cards_moved,
            changed.count,
            changed.count
        )
        showSnackbar(message) {
            this.setAction(R.string.undo) { launchCatchingTask { undoAndShowSnackbar() } }
        }
    }
}
