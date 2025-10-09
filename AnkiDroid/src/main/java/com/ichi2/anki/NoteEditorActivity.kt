package com.ichi2.anki

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.input.TextFieldValue
import androidx.fragment.app.Fragment
import anki.config.ConfigKey
import com.ichi2.anki.dialogs.tags.TagsDialog
import com.ichi2.anki.dialogs.tags.TagsDialogFactory
import com.ichi2.anki.dialogs.tags.TagsDialogListener
import com.ichi2.anki.libanki.Card
import com.ichi2.anki.libanki.Collection
import com.ichi2.anki.libanki.Decks.Companion.CURRENT_DECK
import com.ichi2.anki.libanki.Note
import com.ichi2.anki.model.CardStateFilter
import com.ichi2.anki.model.SelectableDeck
import com.ichi2.anki.noteeditor.NoteEditorField
import com.ichi2.anki.noteeditor.NoteEditorScreen
import com.ichi2.anki.noteeditor.NoteEditorViewModel
import com.ichi2.anki.snackbar.BaseSnackbarBuilderProvider
import com.ichi2.anki.snackbar.SnackbarBuilder
import com.ichi2.anki.ui.theme.AnkiTheme
import com.ichi2.anki.utils.ext.showDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import kotlin.reflect.KClass
import kotlin.reflect.jvm.jvmName

@AndroidEntryPoint
class NoteEditorActivity :
    AnkiActivity(),
    BaseSnackbarBuilderProvider,
    TagsDialogListener {
    override val baseSnackbarBuilder: SnackbarBuilder = { }

    private val viewModel: NoteEditorViewModel by viewModels()
    private var tagsDialogFactory: TagsDialogFactory? = null
    private var addNote: Boolean = false
    private var mCurrentEditedCard: Card? = null

    private val mainToolbar: androidx.appcompat.widget.Toolbar
        get() = findViewById(R.id.toolbar)

    override fun onCreate(savedInstanceState: Bundle?) {
        if (showedActivityFailedScreen(savedInstanceState)) {
            return
        }
        super.onCreate(savedInstanceState)
        if (!ensureStoragePermissions()) {
            return
        }

        tagsDialogFactory = TagsDialogFactory(this).attachToFragmentManager<TagsDialogFactory>(supportFragmentManager)

        setContentView(R.layout.note_editor)
        val composeView = findViewById<ComposeView>(R.id.note_editor_compose_view)
        composeView.setContent {
            AnkiTheme {
                NoteEditorScreen(
                    viewModel = viewModel,
                    onShowTagsDialog = ::showTagsDialog,
                    onShowCardTemplateEditor = ::showCardTemplateEditor
                )
            }
        }

        enableToolbar()

        mainToolbar.setNavigationOnClickListener {
            Timber.i("NoteEditor:: Back button on the menu was pressed")
            onBackPressedDispatcher.onBackPressed()
        }

        startLoadingCollection()
    }

    override fun onCollectionLoaded(col: Collection) {
        super.onCollectionLoaded(col)
        Timber.d("onCollectionLoaded()")
        registerReceiver()

        var caller = NoteEditorCaller.fromValue(intent.getIntExtra(EXTRA_CALLER, NoteEditorCaller.NO_CALLER.value))
        if (caller == NoteEditorCaller.NO_CALLER) {
            val action = intent.action
            if (ACTION_CREATE_FLASHCARD == action || ACTION_CREATE_FLASHCARD_SEND == action || Intent.ACTION_PROCESS_TEXT == action) {
                caller = NoteEditorCaller.NOTEEDITOR_INTENT_ADD
            }
        }

        var editorNote: Note? = null
        var sourceText: Array<String?>? = null

        when (caller) {
            NoteEditorCaller.NO_CALLER -> {
                Timber.e("no caller could be identified, closing")
                finish()
                return
            }
            NoteEditorCaller.EDIT -> {
                val cardId = intent.getLongExtra(EXTRA_CARD_ID, -1)
                mCurrentEditedCard = col.getCard(cardId)
                editorNote = mCurrentEditedCard!!.note(col)
                addNote = false
            }
            NoteEditorCaller.PREVIEWER_EDIT -> {
                val id = intent.getLongExtra(EXTRA_EDIT_FROM_CARD_ID, -1)
                mCurrentEditedCard = col.getCard(id)
                editorNote = mCurrentEditedCard!!.note(col)
            }
            NoteEditorCaller.STUDYOPTIONS,
            NoteEditorCaller.DECKPICKER,
            NoteEditorCaller.REVIEWER_ADD,
            NoteEditorCaller.CARDBROWSER_ADD,
            NoteEditorCaller.NOTEEDITOR -> {
                addNote = true
            }
            NoteEditorCaller.NOTEEDITOR_INTENT_ADD,
            NoteEditorCaller.INSTANT_NOTE_EDITOR -> {
                addNote = true
                val extras = intent.extras
                if (extras != null) {
                    val fetchedSourceText = arrayOfNulls<String>(2)
                    if (Intent.ACTION_PROCESS_TEXT == intent.action) {
                        val stringExtra = extras.getString(Intent.EXTRA_PROCESS_TEXT)
                        fetchedSourceText[0] = stringExtra ?: ""
                        fetchedSourceText[1] = ""
                    } else if (ACTION_CREATE_FLASHCARD == intent.action) {
                        fetchedSourceText[0] = extras.getString(SOURCE_TEXT)
                        fetchedSourceText[1] = extras.getString(TARGET_TEXT)
                    } else {
                        var first: String? = extras.getString(Intent.EXTRA_SUBJECT) ?: ""
                        var second: String? = extras.getString(Intent.EXTRA_TEXT) ?: ""
                        if (first.isNullOrEmpty()) {
                            first = second
                            second = ""
                        }
                        fetchedSourceText[0] = first
                        fetchedSourceText[1] = second
                    }
                    sourceText = fetchedSourceText
                }
            }
            NoteEditorCaller.IMG_OCCLUSION, NoteEditorCaller.ADD_IMAGE -> {
                addNote = true
            }
        }

        if (editorNote == null) {
            val notetype = col.notetypes.current()
            editorNote = Note.fromNotetypeId(col, notetype.id)
            addNote = true
        }

        val finalNote = editorNote!!

        val decks = col.decks.allSorted().map { SelectableDeck.Deck(it.getString("name"), it.getLong("id")) }
        var deckId = intent.getLongExtra(EXTRA_DID, 0)
        if (deckId == 0L) {
            deckId = if (!col.config.getBool(ConfigKey.Bool.ADDING_DEFAULTS_TO_CURRENT_DECK)) {
                finalNote.notetype.did
            } else {
                col.config.get(CURRENT_DECK) ?: 1L
            }
        }
        val selectedDeck = decks.find { it.deckId == deckId }

        val fields = finalNote.fields.map { field ->
            NoteEditorField(
                name = field.name,
                value = TextFieldValue(finalNote.getField(field.ord)),
                isFocused = false
            )
        }.toMutableList()

        if (sourceText != null) {
            if (fields.isNotEmpty()) {
                fields[0] = fields[0].copy(value = TextFieldValue(sourceText!![0] ?: ""))
            }
            if (fields.size > 1) {
                fields[1] = fields[1].copy(value = TextFieldValue(sourceText!![1] ?: ""))
            }
        }

        val getTextFromSearchView = intent.getStringExtra(EXTRA_TEXT_FROM_SEARCH_VIEW)
        if (!getTextFromSearchView.isNullOrEmpty() && fields.isNotEmpty()) {
            fields[0] = fields[0].copy(value = TextFieldValue(getTextFromSearchView))
        }

        val tags = finalNote.tags.joinToString(", ")
        val cards = finalNote.notetype.templates.map { it.name }.joinToString(", ")

        viewModel.onDataLoaded(
            decks = decks,
            selectedDeck = selectedDeck,
            noteTypes = col.notetypes.all(),
            selectedNoteType = finalNote.notetype,
            fields = fields,
            tags = tags,
            cards = cards
        )

        if (addNote) {
            setTitle(R.string.menu_add)
        } else {
            setTitle(R.string.cardeditor_title_edit_card)
        }
    }

    private fun showTagsDialog() {
        val selTags = viewModel.uiState.value.tags.split(",").map { it.trim() }.let { ArrayList(it) }
        val dialog =
            with(this) {
                tagsDialogFactory!!.newTagsDialog().withArguments(
                    context = this,
                    type = TagsDialog.DialogType.EDIT_TAGS,
                    checkedTags = selTags,
                )
            }
        showDialogFragment(dialog)
    }

    private fun showCardTemplateEditor() {
        // TODO: Implement this
    }

    override fun onSelectedTags(
        selectedTags: List<String>,
        indeterminateTags: List<String>,
        stateFilter: CardStateFilter
    ) {
        viewModel.onTagsUpdated(selectedTags.joinToString(", "))
    }

    companion object {
        const val FRAGMENT_ARGS_EXTRA = "fragmentArgs"
        const val FRAGMENT_NAME_EXTRA = "fragmentName"
        const val FRAGMENT_TAG = "NoteEditorFragmentTag"
        const val SOURCE_TEXT = "SOURCE_TEXT"
        const val TARGET_TEXT = "TARGET_TEXT"
        const val EXTRA_CALLER = "CALLER"
        const val EXTRA_CARD_ID = "CARD_ID"
        const val EXTRA_CONTENTS = "CONTENTS"
        const val EXTRA_TAGS = "TAGS"
        const val EXTRA_ID = "ID"
        const val EXTRA_DID = "DECK_ID"
        const val EXTRA_TEXT_FROM_SEARCH_VIEW = "SEARCH"
        const val EXTRA_EDIT_FROM_CARD_ID = "editCid"
        const val ACTION_CREATE_FLASHCARD = "org.openintents.action.CREATE_FLASHCARD"
        const val ACTION_CREATE_FLASHCARD_SEND = "android.intent.action.SEND"
        enum class NoteEditorCaller(
            val value: Int,
        ) {
            NO_CALLER(0),
            EDIT(1),
            STUDYOPTIONS(2),
            DECKPICKER(3),
            REVIEWER_ADD(11),
            CARDBROWSER_ADD(7),
            NOTEEDITOR(8),
            PREVIEWER_EDIT(9),
            NOTEEDITOR_INTENT_ADD(10),
            IMG_OCCLUSION(12),
            ADD_IMAGE(13),
            INSTANT_NOTE_EDITOR(14),
            ;

            companion object {
                fun fromValue(value: Int) = NoteEditorCaller.values().first { it.value == value }
            }
        }

        fun getIntent(
            context: Context,
            fragmentClass: KClass<out Fragment>,
            arguments: Bundle? = null,
            intentAction: String? = null,
        ): Intent =
            Intent(context, NoteEditorActivity::class.java).apply {
                putExtra(FRAGMENT_NAME_EXTRA, fragmentClass.jvmName)
                putExtra(FRAGMENT_ARGS_EXTRA, arguments)
                action = intentAction
            }
    }
}