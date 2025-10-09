/***************************************************************************************
 *                                                                                      *
 * Copyright (c) 2012 Norbert Nagold <norbert.nagold@gmail.com>                         *
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

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Activity.RESULT_CANCELED
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.ActionMode
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.view.WindowManager
import android.widget.EditText
import android.widget.ImageButton
import androidx.activity.addCallback
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.CheckResult
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.content.FileProvider
import androidx.core.content.IntentCompat
import androidx.core.content.edit
import androidx.core.os.BundleCompat
import androidx.core.text.HtmlCompat
import androidx.core.util.component1
import androidx.core.util.component2
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.OnReceiveContentListener
import androidx.core.view.WindowInsetsControllerCompat
import androidx.draganddrop.DropHelper
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import anki.config.ConfigKey
import anki.notes.NoteFieldsCheckResponse
import com.google.android.material.snackbar.Snackbar
import com.ichi2.anim.ActivityTransitionAnimation
import com.ichi2.anki.CollectionManager.TR
import com.ichi2.anki.CollectionManager.withCol
import com.ichi2.anki.NoteEditorFragment.Companion.NoteEditorCaller.Companion.fromValue
import com.ichi2.anki.android.input.ShortcutGroup
import com.ichi2.anki.android.input.ShortcutGroupProvider
import com.ichi2.anki.android.input.shortcut
import com.ichi2.anki.bottomsheet.ImageOcclusionBottomSheetFragment
import com.ichi2.anki.common.annotations.NeedsTest
import com.ichi2.anki.common.utils.annotation.KotlinCleanup
import com.ichi2.anki.dialogs.ConfirmationDialog
import com.ichi2.anki.dialogs.DeckSelectionDialog.DeckSelectionListener
import com.ichi2.anki.dialogs.DiscardChangesDialog
import com.ichi2.anki.dialogs.IntegerDialog
import com.ichi2.anki.dialogs.tags.TagsDialog
import com.ichi2.anki.dialogs.tags.TagsDialogFactory
import com.ichi2.anki.dialogs.tags.TagsDialogListener
import com.ichi2.anki.libanki.*
import com.ichi2.anki.libanki.Decks.Companion.CURRENT_DECK
import com.ichi2.anki.libanki.Note.ClozeUtils
import com.ichi2.anki.libanki.Notetypes.Companion.NOT_FOUND_NOTE_TYPE
import com.ichi2.anki.model.CardStateFilter
import com.ichi2.anki.model.SelectableDeck
import com.ichi2.anki.multimedia.AudioRecordingFragment
import com.ichi2.anki.multimedia.AudioVideoFragment
import com.ichi2.anki.multimedia.MultimediaActivity.Companion.MULTIMEDIA_RESULT
import com.ichi2.anki.multimedia.MultimediaActivity.Companion.MULTIMEDIA_RESULT_FIELD_INDEX
import com.ichi2.anki.multimedia.MultimediaActivityExtra
import com.ichi2.anki.multimedia.MultimediaBottomSheet
import com.ichi2.anki.multimedia.MultimediaImageFragment
import com.ichi2.anki.multimedia.MultimediaUtils.createImageFile
import com.ichi2.anki.multimedia.MultimediaViewModel
import com.ichi2.anki.multimediacard.IMultimediaEditableNote
import com.ichi2.anki.multimediacard.fields.AudioRecordingField
import com.ichi2.anki.multimediacard.fields.EFieldType
import com.ichi2.anki.multimediacard.fields.IField
import com.ichi2.anki.multimediacard.fields.ImageField
import com.ichi2.anki.multimediacard.fields.MediaClipField
import com.ichi2.anki.multimediacard.impl.MultimediaEditableNote
import com.ichi2.anki.noteeditor.*
import com.ichi2.anki.observability.undoableOp
import com.ichi2.anki.pages.ImageOcclusion
import com.ichi2.anki.preferences.sharedPrefs
import com.ichi2.anki.previewer.TemplatePreviewerArguments
import com.ichi2.anki.previewer.TemplatePreviewerPage
import com.ichi2.anki.servicelayer.NoteService
import com.ichi2.anki.snackbar.BaseSnackbarBuilderProvider
import com.ichi2.anki.snackbar.SnackbarBuilder
import com.ichi2.anki.snackbar.showSnackbar
import com.ichi2.anki.themes.AnkiTheme
import com.ichi2.anki.utils.ext.showDialogFragment
import com.ichi2.anki.utils.ext.window
import com.ichi2.anki.utils.openUrl
import com.ichi2.compat.CompatHelper.Companion.getSerializableCompat
import com.ichi2.imagecropper.ImageCropper
import com.ichi2.imagecropper.ImageCropper.Companion.CROP_IMAGE_RESULT
import com.ichi2.imagecropper.ImageCropperLauncher
import com.ichi2.themes.Themes
import com.ichi2.utils.*
import com.ichi2.widget.WidgetStatus
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.json.JSONArray
import timber.log.Timber
import java.io.File
import java.util.*
import java.util.function.Consumer
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

const val CALLER_KEY = "caller"

class NoteEditorFragment :
    Fragment(),
    DeckSelectionListener,
    TagsDialogListener,
    BaseSnackbarBuilderProvider,
    DispatchKeyEventListener,
    MenuProvider,
    ShortcutGroupProvider {

    private val viewModel: NoteEditorViewModel by activityViewModels()

    private var changed = false
    private var isTagsEdited = false
    private var isFieldEdited = false
    private var multimediaActionJob: Job? = null
    private val getColUnsafe: Collection get() = CollectionManager.getColUnsafe()
    private var reloadRequired = false
    private var tagsDialogFactory: TagsDialogFactory? = null

    private var editorNote: Note? = null
    private val multimediaViewModel: MultimediaViewModel by activityViewModels()
    private var currentImageOccPath: String? = null
    private var currentEditedCard: Card? = null
    private var selectedTags: MutableList<String>? = null
    private var deckId: DeckId = 0
    private var allNoteTypeIds: List<Long>? = null
    private var noteTypeChangeFieldMap: MutableMap<Int, Int>? = null
    private var noteTypeChangeCardMap: HashMap<Int, Int?>? = null
    private var addNote = false
    private var aedictIntent = false
    private var caller = NoteEditorCaller.NO_CALLER
    private var sourceText: Array<String?>? = null
    private var pastedImageCache: HashMap<String, String> = HashMap()
    private var toggleStickyText: HashMap<Int, String?> = HashMap()
    var clipboard: ClipboardManager? = null

    private val inCardBrowserActivity
        get() = requireArguments().getBoolean(IN_CARD_BROWSER_ACTIVITY)

    private val requestAddLauncher =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult(),
            NoteEditorActivityResultCallback {
                if (it.resultCode != RESULT_CANCELED) {
                    changed = true
                }
            },
        )

    private val multimediaFragmentLauncher =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult(),
            NoteEditorActivityResultCallback { result ->
                if (result.resultCode == RESULT_CANCELED) {
                    Timber.d("Multimedia result canceled")
                    val index = result.data?.extras?.getInt(MULTIMEDIA_RESULT_FIELD_INDEX) ?: return@NoteEditorActivityResultCallback
                    showMultimediaBottomSheet()
                    handleMultimediaActions(index)
                    return@NoteEditorActivityResultCallback
                }

                Timber.d("Getting multimedia result")
                val extras = result.data?.extras ?: return@NoteEditorActivityResultCallback
                handleMultimediaResult(extras)
            },
        )

    private val requestTemplateEditLauncher =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult(),
            NoteEditorActivityResultCallback {
                reloadRequired = true
                editorNote!!.notetype = getColUnsafe.notetypes.get(editorNote!!.noteTypeId)!!
                if (currentEditedCard == null ||
                    !editorNote!!
                        .cardIds(getColUnsafe)
                        .contains(currentEditedCard!!.id)
                ) {
                    if (!addNote) {
                        Timber.d("onActivityResult() template edit return - current card is gone, close note editor")
                        showSnackbar(getString(R.string.template_for_current_card_deleted))
                        closeNoteEditor()
                    } else {
                        Timber.d("onActivityResult() template edit return, in add mode, just re-display")
                    }
                } else {
                    Timber.d("onActivityResult() template edit return - current card exists")
                    currentEditedCard = getColUnsafe.getCard(currentEditedCard!!.id)
                    currentEditedCard!!.note(getColUnsafe)
                    editorNote = currentEditedCard!!.note
                    updateCards(editorNote!!.notetype)
                }
            },
        )

    private val ioEditorLauncher =
        registerForActivityResult(
            ActivityResultContracts.GetContent(),
        ) { uri ->
            if (uri != null) {
                ImportUtils.getFileCachedCopy(requireContext(), uri)?.let { path ->
                    setupImageOcclusionEditor(path)
                }
            }
        }

    private val requestIOEditorCloser =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult(),
            NoteEditorActivityResultCallback { result ->
                if (result.resultCode != RESULT_CANCELED) {
                    changed = true
                    if (!addNote) {
                        reloadRequired = true
                        closeNoteEditor(RESULT_UPDATED_IO_NOTE, null)
                    }
                }
            },
        )

    private val onReceiveContentListener =
        OnReceiveContentListener { view, payload ->
            val (uriContent, remaining) = payload.partition { item -> item.uri != null }

            if (uriContent == null) {
                return@OnReceiveContentListener remaining
            }

            val clip = uriContent.clip
            val description = clip.description

            if (!hasMedia(description)) {
                return@OnReceiveContentListener remaining
            }

            for (uri in clip.items().map { it.uri }) {
                lifecycleScope.launch {
                    try {
                        val pasteAsPng = shouldPasteAsPng()
                        onPaste(view as EditText, uri, description, pasteAsPng)
                    } catch (e: Exception) {
                        Timber.w(e)
                        CrashReportService.sendExceptionReport(e, "NoteEditor::onReceiveContent")
                    }
                }
            }

            return@OnReceiveContentListener remaining
        }

    private inner class NoteEditorActivityResultCallback(
        private val callback: (result: ActivityResult) -> Unit,
    ) : ActivityResultCallback<ActivityResult> {
        override fun onActivityResult(result: ActivityResult) {
            Timber.d("onActivityResult() with result: %s", result.resultCode)
            if (result.resultCode == DeckPicker.RESULT_DB_ERROR) {
                closeNoteEditor(DeckPicker.RESULT_DB_ERROR, null)
            }
            callback(result)
        }
    }

    override fun onDeckSelected(deck: SelectableDeck?) {
        if (deck == null) {
            return
        }
        require(deck is SelectableDeck.Deck)
        deckId = deck.deckId
        viewModel.onDeckSelected(deck.name)
    }

    private enum class AddClozeType {
        SAME_NUMBER,
        INCREMENT_NUMBER,
    }

    @VisibleForTesting
    var addNoteErrorMessage: String? = null

    private fun displayErrorSavingNote() {
        val errorMessage = snackbarErrorText
        if (errorMessage == TR.addingYouHaveAClozeDeletionNote()) {
            noClozeDialog(errorMessage)
        } else {
            showSnackbar(errorMessage)
        }
    }

    private fun noClozeDialog(errorMessage: String) {
        AlertDialog.Builder(requireContext()).show {
            message(text = errorMessage)
            positiveButton(text = TR.actionsSave()) {
                lifecycleScope.launch {
                    saveNoteWithProgress()
                }
            }
            negativeButton(R.string.dialog_cancel)
        }
    }

    @VisibleForTesting
    val snackbarErrorText: String
        get() =
            when {
                addNoteErrorMessage != null -> addNoteErrorMessage!!
                allFieldsHaveContent() -> resources.getString(R.string.note_editor_no_cards_created_all_fields)
                else -> resources.getString(R.string.note_editor_no_cards_created)
            }

    private fun allFieldsHaveContent() = currentFieldStrings.none { it.isNullOrEmpty() }

    override fun onCreate(savedInstanceState: Bundle?) {
        tagsDialogFactory = TagsDialogFactory(this).attachToFragmentManager<TagsDialogFactory>(parentFragmentManager)
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) {
            caller = fromValue(savedInstanceState.getInt(CALLER_KEY))
            addNote = savedInstanceState.getBoolean("addNote")
            deckId = savedInstanceState.getLong("did")
            selectedTags = savedInstanceState.getStringArrayList("tags")
            reloadRequired = savedInstanceState.getBoolean(RELOAD_REQUIRED_EXTRA_KEY)
            pastedImageCache =
                savedInstanceState.getSerializableCompat<HashMap<String, String>>("imageCache")!!
            toggleStickyText =
                savedInstanceState.getSerializableCompat<HashMap<Int, String?>>("toggleSticky")!!
            changed = savedInstanceState.getBoolean(NOTE_CHANGED_EXTRA_KEY)
        } else {
            caller = fromValue(requireArguments().getInt(EXTRA_CALLER, NoteEditorCaller.NO_CALLER.value))
            if (caller == NoteEditorCaller.NO_CALLER) {
                val action = requireActivity().intent.action
                if (ACTION_CREATE_FLASHCARD == action || ACTION_CREATE_FLASHCARD_SEND == action || Intent.ACTION_PROCESS_TEXT == action) {
                    caller = NoteEditorCaller.NOTEEDITOR_INTENT_ADD
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                AnkiTheme {
                    NoteEditorScreen(
                        viewModel = viewModel,
                        onTagsClick = { showTagsDialog() },
                        onCardsClick = { showCardTemplateEditor() },
                        onMediaClick = { fieldIndex ->
                            showMultimediaBottomSheet()
                            handleMultimediaActions(fieldIndex)
                        }
                    )
                }
            }
        }
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = false
        @Suppress("deprecation", "API35 properly handle edge-to-edge")
        requireActivity().window.statusBarColor = Themes.getColorFromAttr(requireContext(), R.attr.appBarColor)
        super.onViewCreated(view, savedInstanceState)
        updateToolbarMargin()

        try {
            setupEditor(getColUnsafe)
        } catch (ex: RuntimeException) {
            Timber.w(ex, "setupEditor")
            requireAnkiActivity().onCollectionLoadError()
            return
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            Timber.i("NoteEditor:: onBackPressed()")
            closeCardEditorWithCheck()
        }

        @Suppress("deprecation", "API35 properly handle edge-to-edge")
        requireActivity().window.navigationBarColor =
            Themes.getColorFromAttr(requireContext(), R.attr.toolbarBackgroundColor)

        (requireActivity() as MenuHost).addMenuProvider(
            this,
            viewLifecycleOwner,
            Lifecycle.State.RESUMED,
        )
    }

    private fun handleNoteTypeChangeInEditMode(newNoteType: NotetypeJson) {
        val noteNoteTypeId = currentEditedCard!!.noteType(getColUnsafe).id
        if (newNoteType.id == noteNoteTypeId) {
            populateFields()
            updateCards(currentEditedCard!!.noteType(getColUnsafe))
        } else {
            val tmpls = newNoteType.templates
            val itemsLength = editorNote!!.items().size
            noteTypeChangeFieldMap = HashUtil.hashMapInit(itemsLength)
            for (i in 0 until itemsLength) {
                noteTypeChangeFieldMap!![i] = i
            }
            val templatesLength = tmpls.length()
            noteTypeChangeCardMap = HashUtil.hashMapInit(templatesLength)
            for (i in 0 until templatesLength) {
                if (i < editorNote!!.numberOfCards(getColUnsafe)) {
                    noteTypeChangeCardMap!![i] = i
                } else {
                    noteTypeChangeCardMap!![i] = null
                }
            }
            updateFieldsFromMap(newNoteType)
            selectedTags = editorNote!!.tags
            updateTags()
            updateFieldsFromStickyText()
        }
        viewModel.onNoteTypeChanged(isClozeType)
    }

    private suspend fun handleImageIntent(data: Intent) {
        val imageUri =
            if (data.action == Intent.ACTION_SEND) {
                BundleCompat.getParcelable(requireArguments(), Intent.EXTRA_STREAM, Uri::class.java)
            } else {
                data.data
            }

        if (imageUri == null) {
            Timber.d("NoteEditor:: Image Uri is null")
            showSnackbar(R.string.something_wrong)
            return
        }

        try {
            requireContext().contentResolver.takePersistableUriPermission(imageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            Timber.d("Persisted URI permission for $imageUri")
        } catch (e: SecurityException) {
            Timber.w(e, "Unable to persist URI permission")
        }

        val cachedImagePath = copyUriToInternalCache(imageUri)
        if (cachedImagePath == null) {
            Timber.w("Failed to cache image")
            showSnackbar(R.string.something_wrong)
            return
        }
        val cachedUri = Uri.fromFile(File(requireContext().cacheDir, cachedImagePath))

        val note = getCurrentMultimediaEditableNote()
        if (note.isEmpty) {
            Timber.w("Note is null, returning")
            return
        }
        openMultimediaImageFragment(
            fieldIndex = 0,
            field = ImageField(),
            multimediaNote = note,
            imageUri = cachedUri,
        )
    }

    private fun copyUriToInternalCache(uri: Uri): String? {
        return try {
            val inputStream = requireContext().contentResolver.openInputStream(uri) ?: return null

            val fileName = ContentResolverUtil.getFileName(requireContext().contentResolver, uri)
            val cacheDir = requireContext().cacheDir
            val destFile = File(cacheDir, fileName)

            val canonicalCacheDir = cacheDir.canonicalFile
            val canonicalDestFile = destFile.canonicalFile

            if (!canonicalDestFile.path.startsWith(canonicalCacheDir.path)) {
                Timber.w("Rejected path due to directory traversal risk: $fileName")
                return null
            }

            destFile.outputStream().use { output ->
                inputStream.copyTo(output)
            }

            Timber.d("copyUriToInternalCache() copied to ${destFile.absolutePath}")
            destFile.name
        } catch (e: Exception) {
            Timber.w(e, "Failed to copy URI to internal cache")
            null
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        addInstanceStateToBundle(outState)
        super.onSaveInstanceState(outState)
    }

    private fun addInstanceStateToBundle(savedInstanceState: Bundle) {
        Timber.i("Saving instance")
        savedInstanceState.putInt(CALLER_KEY, caller.value)
        savedInstanceState.putBoolean("addNote", addNote)
        savedInstanceState.putLong("did", deckId)
        savedInstanceState.putBoolean(NOTE_CHANGED_EXTRA_KEY, changed)
        savedInstanceState.putBoolean(RELOAD_REQUIRED_EXTRA_KEY, reloadRequired)
        savedInstanceState.putSerializable("imageCache", pastedImageCache)
        savedInstanceState.putSerializable("toggleSticky", toggleStickyText)
        if (selectedTags == null) {
            selectedTags = ArrayList(0)
        }
        savedInstanceState.putStringArrayList("tags", selectedTags?.let { ArrayList(it) })
    }

    private fun setupEditor(col: Collection) {
        val intent = requireActivity().intent
        Timber.d("NoteEditor() onCollectionLoaded: caller: %s", caller)
        requireAnkiActivity().registerReceiver()
        imageOcclusionButtonsContainer = requireView().findViewById(R.id.ImageOcclusionButtonsLayout)
        editOcclusionsButton = requireView().findViewById(R.id.EditOcclusionsButton)
        selectImageForOcclusionButton = requireView().findViewById(R.id.SelectImageForOcclusionButton)
        pasteOcclusionImageButton = requireView().findViewById(R.id.PasteImageForOcclusionButton)

        try {
            clipboard = requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        } catch (e: Exception) {
            Timber.w(e)
        }

        aedictIntent = false
        currentEditedCard = null
        when (caller) {
            NoteEditorCaller.NO_CALLER -> {
                Timber.e("no caller could be identified, closing")
                requireActivity().finish()
                return
            }
            NoteEditorCaller.EDIT -> {
                val cardId = requireNotNull(requireArguments().getLong(EXTRA_CARD_ID)) { "EXTRA_CARD_ID" }
                currentEditedCard = col.getCard(cardId)
                editorNote = currentEditedCard!!.note(col)
                addNote = false
            }
            NoteEditorCaller.PREVIEWER_EDIT -> {
                val id = requireArguments().getLong(EXTRA_EDIT_FROM_CARD_ID)
                currentEditedCard = col.getCard(id)
                editorNote = currentEditedCard!!.note(getColUnsafe)
            }
            NoteEditorCaller.STUDYOPTIONS,
            NoteEditorCaller.DECKPICKER,
            NoteEditorCaller.REVIEWER_ADD,
            NoteEditorCaller.CARDBROWSER_ADD,
            NoteEditorCaller.NOTEEDITOR,
            -> {
                addNote = true
            }
            NoteEditorCaller.NOTEEDITOR_INTENT_ADD,
            NoteEditorCaller.INSTANT_NOTE_EDITOR,
            -> {
                fetchIntentInformation(intent)
                if (sourceText == null) {
                    requireActivity().finish()
                    return
                }
                if ("Aedict Notepad" == sourceText!![0] && addFromAedict(sourceText!![1])) {
                    requireActivity().finish()
                    return
                }
                addNote = true
            }
            NoteEditorCaller.IMG_OCCLUSION, NoteEditorCaller.ADD_IMAGE -> {
                addNote = true
            }
        }

        if (addNote) {
            editOcclusionsButton?.visibility = View.GONE
            selectImageForOcclusionButton?.setOnClickListener {
                Timber.i("selecting image for occlusion")
                val imageOcclusionBottomSheet = ImageOcclusionBottomSheetFragment()
                imageOcclusionBottomSheet.listener =
                    object : ImageOcclusionBottomSheetFragment.ImagePickerListener {
                        override fun onCameraClicked() {
                            Timber.i("onCameraClicked")
                            dispatchCameraEvent()
                        }

                        override fun onGalleryClicked() {
                            Timber.i("onGalleryClicked")
                            try {
                                ioEditorLauncher.launch("image/*")
                            } catch (_: ActivityNotFoundException) {
                                Timber.w("No app found to handle onGalleryClicked request")
                                activity?.showSnackbar(R.string.activity_start_failed)
                            }
                        }
                    }
                imageOcclusionBottomSheet.show(
                    parentFragmentManager,
                    "ImageOcclusionBottomSheetFragment",
                )
            }

            pasteOcclusionImageButton?.text = TR.notetypesIoPasteImageFromClipboard()
            pasteOcclusionImageButton?.setOnClickListener {
                if (ClipboardUtil.hasImage(clipboard)) {
                    val uri = ClipboardUtil.getUri(clipboard)
                    val i =
                        Intent().apply {
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            clipData = ClipData.newUri(requireActivity().contentResolver, uri.toString(), uri)
                        }
                    ImportUtils.getFileCachedCopy(requireContext(), i)?.let { path ->
                        setupImageOcclusionEditor(path)
                    }
                } else {
                    showSnackbar(TR.editingNoImageFoundOnClipboard())
                }
            }
        } else {
            selectImageForOcclusionButton?.visibility = View.GONE
            pasteOcclusionImageButton?.visibility = View.GONE
            editOcclusionsButton?.visibility = View.VISIBLE
            editOcclusionsButton?.text = resources.getString(R.string.edit_occlusions)
            editOcclusionsButton?.setOnClickListener {
                setupImageOcclusionEditor()
            }
        }

        allNoteTypeIds = col.notetypes.allIds()
        val allNoteTypes = allNoteTypeIds!!.map { col.notetypes.get(it)!!.name }
        val currentNoteType = editorNote?.notetype?.name ?: col.notetypes.current().name
        viewModel.setNoteTypes(allNoteTypes, currentNoteType)

        val deckNames = col.decks.allNames(includeFiltered = false)
        val selectedDeckName = col.decks.name(deckId)
        viewModel.setDecks(deckNames, selectedDeckName)

        deckId = requireArguments().getLong(EXTRA_DID, deckId)
        val getTextFromSearchView = requireArguments().getString(EXTRA_TEXT_FROM_SEARCH_VIEW)
        setDid(editorNote)
        setNote(editorNote)
        if (addNote) {
            requireAnkiActivity().setToolbarTitle(R.string.menu_add)
            var contents: String? = null
            val tags = requireArguments().getStringArray(EXTRA_TAGS)

            try {
                if (currentNotetypeIsImageOcclusion() && (sourceText != null || caller == NoteEditorCaller.ADD_IMAGE)) {
                    val noteType =
                        col.notetypes.all().first {
                            !it.isImageOcclusion
                        }
                    changeNoteType(noteType.id)
                }
            } catch (e: NoSuchElementException) {
                showSnackbar(R.string.missing_note_type)
                sourceText = null
                caller = NoteEditorCaller.NO_CALLER
                Timber.w(e)
            }

            if (sourceText != null) {
                if (aedictIntent && viewModel.uiState.value.fields.size == 3 && sourceText!![1]!!.contains("[")) {
                    contents =
                        sourceText!![1]!!
                            .replaceFirst("\\[".toRegex(), "\u001f" + sourceText!![0] + "\u001f")
                    contents = contents.substring(0, contents.length - 1)
                } else if (viewModel.uiState.value.fields.isNotEmpty()) {
                    viewModel.onFieldContentChanged(0, sourceText!![0] ?: "")
                    if (viewModel.uiState.value.fields.size > 1) {
                        viewModel.onFieldContentChanged(1, sourceText!![1] ?: "")
                    }
                }
            } else {
                contents = requireArguments().getString(EXTRA_CONTENTS)
            }
            contents?.let { setEditFieldTexts(it) }
            tags?.let { setTags(it) }
            if (caller == NoteEditorCaller.ADD_IMAGE) lifecycleScope.launch { handleImageIntent(intent) }
        } else {
            requireAnkiActivity().setTitle(R.string.cardeditor_title_edit_card)
        }
        if (!addNote && currentEditedCard != null) {
            Timber.i(
                "onCollectionLoaded() Edit note activity successfully started with card id %d",
                currentEditedCard!!.id,
            )
        }
        if (addNote) {
            Timber.i(
                "onCollectionLoaded() Edit note activity successfully started in add card mode with node id %d",
                editorNote!!.id,
            )
        }

        if (!addNote) {
            requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        }

        if (viewModel.uiState.value.fields.isNotEmpty()) {
            if (!getTextFromSearchView.isNullOrEmpty()) {
                viewModel.onFieldContentChanged(0, getTextFromSearchView)
            }
        }

        if (caller == NoteEditorCaller.IMG_OCCLUSION) {
            val saveImageUri = BundleCompat.getParcelable(requireArguments(), EXTRA_IMG_OCCLUSION, Uri::class.java)
            if (saveImageUri != null) {
                ImportUtils.getFileCachedCopy(requireContext(), saveImageUri)?.let { path ->
                    setupImageOcclusionEditor(path)
                }
            } else {
                Timber.w("Image uri is null")
            }
        }
    }

    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { isPictureTaken ->
            if (isPictureTaken) {
                currentImageOccPath?.let { imagePath ->
                    val photoFile = File(imagePath)
                    val imageUri: Uri =
                        FileProvider.getUriForFile(
                            requireContext(),
                            requireActivity().packageName + ".apkgfileprovider",
                            photoFile,
                        )
                    startCrop(imageUri)
                }
            } else {
                Timber.d("Camera aborted or some interruption")
            }
        }

    private val cropImageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            when (result.resultCode) {
                Activity.RESULT_OK -> {

                    result.data?.let {
                        val cropResultData =
                            IntentCompat.getParcelableExtra(
                                it,
                                CROP_IMAGE_RESULT,
                                ImageCropper.CropResultData::class.java,
                            )
                        Timber.d("Cropped image data: $cropResultData")
                        if (cropResultData?.uriPath == null) return@registerForActivityResult
                        setupImageOcclusionEditor(cropResultData.uriPath)
                    }
                }

                else -> {
                    Timber.v("Unable to crop the image")
                }
            }
        }

    private fun startCrop(imageUri: Uri) {
        Timber.i("launching crop")
        val intent = ImageCropperLauncher.ImageUri(imageUri).getIntent(requireContext())
        cropImageLauncher.launch(intent)
    }

    private fun dispatchCameraEvent() {
        val photoFile: File? =
            try {
                requireContext().createImageFile()
            } catch (e: Exception) {
                Timber.w(e, "Error creating the file")
                return
            }

        currentImageOccPath = photoFile?.absolutePath

        photoFile?.let {
            val photoURI: Uri =
                FileProvider.getUriForFile(
                    requireContext(),
                    requireActivity().packageName + ".apkgfileprovider",
                    it,
                )
            cameraLauncher.launch(photoURI)
        }
    }

    private fun modifyCurrentSelection(formatter: TextFormatter) {
        val currentFocus = requireActivity().currentFocus as? EditText ?: return
        val selectionStart = currentFocus.selectionStart
        val selectionEnd = currentFocus.selectionEnd

        val start = min(selectionStart, selectionEnd)
        val end = max(selectionStart, selectionEnd)
        val text = currentFocus.text?.toString() ?: ""

        val beforeText = text.substring(0, start)
        val selectedText = text.substring(start, end)
        val afterText = text.substring(end)
        val (newText, newStart, newEnd) = formatter.format(selectedText)

        val length = beforeText.length + newText.length + afterText.length
        val newFieldContent =
            StringBuilder(length).append(beforeText).append(newText).append(afterText)
        currentFocus.setText(newFieldContent)
        currentFocus.setSelection(start + newStart, start + newEnd)
    }

    @SuppressLint("CheckResult")
    private fun displayFontSizeDialog() {
        val results = resources.getStringArray(R.array.html_size_codes)

        AlertDialog.Builder(requireContext()).show {
            setItems(R.array.html_size_code_labels) { _, index ->
                val formatter =
                    TextWrapper(
                        prefix = "<span style=\"font-size:${results[index]}\">",
                        suffix = "</span>",
                    )
                modifyCurrentSelection(formatter)
            }
            title(R.string.menu_font_size)
        }
    }

    @SuppressLint("CheckResult")
    private fun displayInsertHeadingDialog() {
        val headingList = arrayOf("h1", "h2", "h3", "h4", "h5")
        AlertDialog.Builder(requireContext()).show {
            setItems(headingList) { _, index ->
                val charSequence = headingList[index]
                val formatter = TextWrapper(prefix = "<$charSequence>", suffix = "</$charSequence>")
                modifyCurrentSelection(formatter)
            }
            title(R.string.insert_heading)
        }
    }

    private fun onNoteAdded() {
        var closeEditorAfterSave = false
        var closeIntent: Intent? = null
        changed = true
        sourceText = null
        refreshNoteData()
        showSnackbar(TR.addingAdded(), Snackbar.LENGTH_SHORT)

        if (caller == NoteEditorCaller.NOTEEDITOR || aedictIntent) {
            closeEditorAfterSave = true
        } else if (caller == NoteEditorCaller.NOTEEDITOR_INTENT_ADD) {
            closeEditorAfterSave = true
            closeIntent = Intent().apply { putExtra(EXTRA_ID, requireArguments().getString(EXTRA_ID)) }
        } else if (viewModel.uiState.value.fields.isNotEmpty()) {
            // TODO: Focus first field
        }

        if (closeEditorAfterSave) {
            if (caller == NoteEditorCaller.NOTEEDITOR_INTENT_ADD || aedictIntent) {
                showThemedToast(requireContext(), R.string.note_message, shortLength = true)
            }
            closeNoteEditor(closeIntent ?: Intent())
        } else {
            isFieldEdited = false
            isTagsEdited = false
        }
    }

    private suspend fun saveNoteWithProgress() {
        requireActivity().withProgress(resources.getString(R.string.saving_facts)) {
            undoableOp {
                notetypes.save(editorNote!!.notetype)
                addNote(editorNote!!, deckId)
            }
        }
        onNoteAdded()
        updateFieldsFromStickyText()
    }

    @VisibleForTesting
    @NeedsTest("14664: 'first field must not be empty' no longer applies after saving the note")
    @KotlinCleanup("fix !! on oldNoteType/newNoteType")
    suspend fun saveNote() {
        val res = resources
        if (selectedTags == null) {
            selectedTags = ArrayList(0)
        }
        saveToggleStickyMap()

        if (addNote) {
            for (i in viewModel.uiState.value.fields.indices) {
                updateField(i)
            }
            Timber.d("setting 'last deck' of note type %s to %d", editorNote!!.notetype.name, deckId)
            editorNote!!.notetype.did = deckId
            editorNote!!.setTagsFromStr(getColUnsafe, tagsAsString(selectedTags!!))
            val tags = JSONArray()
            for (t in selectedTags!!) {
                tags.put(t)
            }

            reloadRequired = true

            lifecycleScope.launch {
                val noteFieldsCheck = checkNoteFieldsResponse(editorNote!!)
                if (noteFieldsCheck is NoteFieldsCheckResult.Failure) {
                    addNoteErrorMessage = noteFieldsCheck.localizedMessage ?: getString(R.string.something_wrong)
                    displayErrorSavingNote()
                    return@launch
                }
                addNoteErrorMessage = null
                saveNoteWithProgress()
            }
        } else {
            val newNoteType = currentlySelectedNotetype
            val oldNoteType = currentEditedCard?.noteType(getColUnsafe)
            if (newNoteType?.id != oldNoteType?.id) {
                reloadRequired = true
                if (noteTypeChangeCardMap!!.size < editorNote!!.numberOfCards(getColUnsafe) ||
                    noteTypeChangeCardMap!!.containsValue(
                        null,
                    )
                ) {
                    val dialog = ConfirmationDialog()
                    dialog.setArgs(res.getString(R.string.confirm_map_cards_to_nothing))
                    val confirm =
                        Runnable {
                            changeNoteType(oldNoteType!!, newNoteType!!)
                        }
                    dialog.setConfirm(confirm)
                    showDialogFragment(dialog)
                } else {
                    changeNoteType(oldNoteType!!, newNoteType!!)
                }
                return
            }
            var modified = false
            if (currentEditedCard != null && currentEditedCard!!.currentDeckId() != deckId) {
                reloadRequired = true
                undoableOp { setDeck(listOf(currentEditedCard!!.id), deckId) }
                currentEditedCard!!.load(getColUnsafe)
                editorNote = currentEditedCard!!.note(getColUnsafe)
                currentEditedCard!!.did = deckId
                modified = true
                Timber.d("deck ID updated to '%d'", deckId)
            }
            for (i in viewModel.uiState.value.fields.indices) {
                modified = modified or updateField(i)
            }
            for (t in selectedTags!!) {
                modified = modified || !editorNote!!.hasTag(getColUnsafe, tag = t)
            }
            modified = modified || editorNote!!.tags.size > selectedTags!!.size

            if (!modified) {
                closeNoteEditor()
                return
            }

            editorNote!!.setTagsFromStr(getColUnsafe, tagsAsString(selectedTags!!))
            changed = true

            if (caller == NoteEditorCaller.PREVIEWER_EDIT || caller == NoteEditorCaller.EDIT) {
                requireActivity().withProgress {
                    undoableOp {
                        updateNote(currentEditedCard!!.note(this@undoableOp))
                    }
                }
            }
            closeNoteEditor()
            return
        }
    }

    @NeedsTest("test changing note type")
    private fun changeNoteType(
        oldNotetype: NotetypeJson,
        newNotetype: NotetypeJson,
    ) = launchCatchingTask {
        if (!requireAnkiActivity().userAcceptsSchemaChange()) return@launchCatchingTask

        val noteId = editorNote!!.id
        undoableOp {
            notetypes.change(oldNotetype, noteId, newNotetype, noteTypeChangeFieldMap!!, noteTypeChangeCardMap!!)
        }
        withCol { editorNote!!.load(this@withCol) }
        closeNoteEditor()
    }

    override fun onCreateMenu(
        menu: Menu,
        menuInflater: MenuInflater,
    ) {
        menuInflater.inflate(R.menu.note_editor, menu)
        onPrepareMenu(menu)
    }

    override fun onPrepareMenu(menu: Menu) {
        if (addNote) {
            menu.findItem(R.id.action_copy_note).isVisible = false
            val iconVisible = allowSaveAndPreview()
            menu.findItem(R.id.action_save).isVisible = iconVisible
            menu.findItem(R.id.action_preview).isVisible = iconVisible
        } else {
            menu.findItem(R.id.action_add_note_from_note_editor).isVisible = !inCardBrowserActivity
        }
        if (viewModel.uiState.value.fields.isNotEmpty()) {
            for (i in viewModel.uiState.value.fields.indices) {
                val fieldText = viewModel.uiState.value.fields[i].content
                if (fieldText.isNotEmpty()) {
                    menu.findItem(R.id.action_copy_note).isEnabled = true
                    break
                } else if (i == viewModel.uiState.value.fields.size - 1) {
                    menu.findItem(R.id.action_copy_note).isEnabled = false
                }
            }
        }
        menu.findItem(R.id.action_show_toolbar).isChecked = viewModel.uiState.value.isToolbarVisible
        menu.findItem(R.id.action_capitalize).isChecked =
            sharedPrefs().getBoolean(PREF_NOTE_EDITOR_CAPITALIZE, true)
        menu.findItem(R.id.action_scroll_toolbar).isVisible = false
    }

    private fun allowSaveAndPreview(): Boolean =
        when {
            addNote && currentNotetypeIsImageOcclusion() -> false
            else -> true
        }

    override fun onMenuItemSelected(item: MenuItem): Boolean {
        Timber.d("NoteEditor::onMenuItemSelected")
        when (item.itemId) {
            R.id.action_preview -> {
                Timber.i("NoteEditor:: Preview button pressed")
                if (allowSaveAndPreview()) {
                    launchCatchingTask { performPreview() }
                }
                return true
            }
            R.id.action_save -> {
                Timber.i("NoteEditor:: Save note button pressed")
                if (allowSaveAndPreview()) {
                    launchCatchingTask { saveNote() }
                }
                return true
            }
            R.id.action_add_note_from_note_editor -> {
                Timber.i("NoteEditor:: Add Note button pressed")
                addNewNote()
                return true
            }
            R.id.action_copy_note -> {
                Timber.i("NoteEditor:: Copy Note button pressed")
                copyNote()
                return true
            }
            R.id.action_font_size -> {
                Timber.i("NoteEditor:: Font Size button pressed")
                val fontSizeDialog = IntegerDialog()
                fontSizeDialog.setArgs(getString(R.string.menu_font_size), editTextFontSize, 2)
                fontSizeDialog.setCallbackRunnable { fontSizeSp: Int? -> setFontSize(fontSizeSp) }
                showDialogFragment(fontSizeDialog)
                return true
            }
            R.id.action_show_toolbar -> {
                viewModel.toggleToolbarVisibility()
                return true
            }
            R.id.action_capitalize -> {
                Timber.i("NoteEditor:: Capitalize button pressed. New State: %b", !item.isChecked)
                item.isChecked = !item.isChecked // Needed for Android 9
                toggleCapitalize(item.isChecked)
                return true
            }
        }
        return false
    }

    private fun toggleCapitalize(value: Boolean) {
        this.sharedPrefs().edit {
            putBoolean(PREF_NOTE_EDITOR_CAPITALIZE, value)
        }
        // TODO: Update fields
    }

    private fun setFontSize(fontSizeSp: Int?) {
        if (fontSizeSp == null || fontSizeSp <= 0) {
            return
        }
        Timber.i("Setting font size to %d", fontSizeSp)
        this.sharedPrefs().edit { putInt(PREF_NOTE_EDITOR_FONT_SIZE, fontSizeSp) }
        // TODO: Update fields
    }

    private val editTextFontSize: String
        get() {
            return "18" // Bogus for now
        }

    private fun addNewNote() {
        launchNoteEditor(NoteEditorLauncher.AddNote(deckId)) { }
    }

    fun copyNote() {
        launchNoteEditor(NoteEditorLauncher.CopyNote(deckId, fieldsText, selectedTags)) { }
    }

    private fun launchNoteEditor(
        arguments: NoteEditorLauncher,
        intentEnricher: Consumer<Bundle>,
    ) {
        val intent = arguments.toIntent(requireContext())
        val bundle = arguments.toBundle()
        intentEnricher.accept(bundle)
        requestAddLauncher.launch(intent)
    }

    @VisibleForTesting
    @NeedsTest("previewing newlines")
    @NeedsTest("cards with a cloze notetype but no cloze in fields are previewed as empty card")
    @NeedsTest("clozes that don't start at '1' are correctly displayed")
    suspend fun performPreview() {
        val convertNewlines = shouldReplaceNewlines()

        fun String?.toFieldText(): String = NoteService.convertToHtmlNewline(this.toString(), convertNewlines)
        val fields = viewModel.uiState.value.fields.mapTo(mutableListOf()) { it.content.toFieldText() }
        val tags = selectedTags ?: mutableListOf()

        val ord =
            if (editorNote!!.notetype.isCloze) {
                val tempNote = withCol { Note.fromNotetypeId(this@withCol, editorNote!!.notetype.id) }
                tempNote.fields = fields
                val clozeNumbers = withCol { clozeNumbersInNote(tempNote) }
                if (clozeNumbers.isNotEmpty()) {
                    clozeNumbers.first() - 1
                } else {
                    0
                }
            } else {
                currentEditedCard?.ord ?: 0
            }

        val args =
            TemplatePreviewerArguments(
                notetypeFile = NotetypeFile(requireContext(), editorNote!!.notetype),
                fields = fields,
                tags = tags,
                id = editorNote!!.id,
                ord = ord,
                fillEmpty = false,
            )
        val intent = TemplatePreviewerPage.getIntent(requireContext(), args)
        startActivity(intent)
    }

    private fun setTags(tags: Array<String>) {
        selectedTags = tags.toCollection(ArrayList())
        updateTags()
    }

    private fun closeCardEditorWithCheck() {
        if (hasUnsavedChanges()) {
            showDiscardChangesDialog()
        } else {
            closeNoteEditor()
        }
    }

    private fun showDiscardChangesDialog() {
        DiscardChangesDialog.showDialog(requireContext()) {
            Timber.i("NoteEditor:: OK button pressed to confirm discard changes")
            closeNoteEditor()
        }
    }

    private fun closeNoteEditor(intent: Intent = Intent()) {
        val result: Int =
            if (changed) {
                Activity.RESULT_OK
            } else {
                RESULT_CANCELED
            }
        if (reloadRequired) {
            intent.putExtra(RELOAD_REQUIRED_EXTRA_KEY, true)
        }
        if (changed) {
            intent.putExtra(NOTE_CHANGED_EXTRA_KEY, true)
        }
        closeNoteEditor(result, intent)
    }

    private fun closeNoteEditor(
        result: Int,
        intent: Intent?,
    ) {
        requireActivity().apply {
            if (intent != null) {
                setResult(result, intent)
            } else {
                setResult(result)
            }
            CardTemplateNotetype.clearTempNoteTypeFiles()

            if (inCardBrowserActivity) {
                Timber.i("not closing activity: fragmented")
                return
            }

            Timber.i("Closing note editor")

            val animation =
                BundleCompat.getParcelable(
                    requireArguments(),
                    AnkiActivity.FINISH_ANIMATION_EXTRA,
                    ActivityTransitionAnimation.Direction::class.java,
                )
            if (animation != null) {
                requireAnkiActivity().finishWithAnimation(animation)
            } else {
                finish()
            }
        }
    }

    private fun showTagsDialog() {
        val selTags = selectedTags?.let { ArrayList(it) } ?: arrayListOf()
        val dialog =
            with(requireContext()) {
                tagsDialogFactory!!.newTagsDialog().withArguments(
                    context = this,
                    type = TagsDialog.DialogType.EDIT_TAGS,
                    checkedTags = selTags,
                )
            }
        showDialogFragment(dialog)
    }

    override fun onSelectedTags(
        selectedTags: List<String>,
        indeterminateTags: List<String>,
        stateFilter: CardStateFilter,
    ) {
        if (this.selectedTags != selectedTags) {
            isTagsEdited = true
        }
        this.selectedTags = selectedTags as ArrayList<String>?
        updateTags()
    }

    private fun showCardTemplateEditor() {
        val intent = Intent(requireContext(), CardTemplateEditor::class.java)
        intent.putExtra("noteTypeId", currentlySelectedNotetype!!.id)
        Timber.d(
            "showCardTemplateEditor() for model %s",
            intent.getLongExtra("noteTypeId", NOT_FOUND_NOTE_TYPE),
        )
        if (!addNote && currentEditedCard != null) {
            intent.putExtra("noteId", currentEditedCard!!.nid)
            Timber.d("showCardTemplateEditor() with note %s", currentEditedCard!!.nid)
            intent.putExtra("ordId", currentEditedCard!!.ord)
            Timber.d("showCardTemplateEditor() with ord %s", currentEditedCard!!.ord)
        }
        requestTemplateEditLauncher.launch(intent)
    }

    @VisibleForTesting
    fun insertStringInField(
        fieldEditText: EditText?,
        formattedValue: String?,
    ) {
        if (fieldEditText!!.hasFocus()) {
            val start = fieldEditText.selectionStart
            val end = fieldEditText.selectionEnd
            fieldEditText.text.replace(min(start, end), max(start, end), formattedValue)
        } else {
            fieldEditText.text.append(formattedValue)
        }
    }

    private suspend fun getCurrentMultimediaEditableNote(): MultimediaEditableNote {
        val note = NoteService.createEmptyNote(editorNote!!.notetype)
        val fields = currentFieldStrings.requireNoNulls()
        withCol { NoteService.updateMultimediaNoteFromFields(this@withCol, fields, editorNote!!.noteTypeId, note) }

        return note
    }

    private suspend fun shouldPasteAsPng() = withCol { config.getBool(ConfigKey.Bool.PASTE_IMAGES_AS_PNG) }

    val currentFields: Fields
        get() = editorNote!!.notetype.fields

    @get:CheckResult
    val currentFieldStrings: Array<String?>
        get() = viewModel.uiState.value.fields.map { it.content }.toTypedArray()

    private fun populateFields() {
        val fieldStates = editorNote!!.fields.mapIndexed { index, field ->
            NoteEditorFieldState(
                label = field.name,
                content = editorNote!!.values()[index],
                isSticky = field.sticky
            )
        }
        viewModel.setFields(fieldStates)
    }

    private fun getActionModeCallback(
        textBox: EditText,
        clozeMenuId: Int,
    ): ActionMode.Callback =
        CustomActionModeCallback(
            isClozeType,
            getString(R.string.multimedia_editor_popup_cloze),
            clozeMenuId,
            onActionItemSelected = { mode, item ->
                if (item.itemId == clozeMenuId) {
                    convertSelectedTextToCloze(textBox, AddClozeType.INCREMENT_NUMBER)
                    mode.finish()
                    true
                } else {
                    false
                }
            },
        )

    @VisibleForTesting
    fun showMultimediaBottomSheet() {
        Timber.d("Showing MultimediaBottomSheet fragment")
        val multimediaBottomSheet = MultimediaBottomSheet()
        multimediaBottomSheet.show(parentFragmentManager, "MultimediaBottomSheet")
    }

    private fun handleMultimediaActions(fieldIndex: Int) {
        multimediaActionJob?.cancel()

        multimediaActionJob =
            lifecycleScope.launch {
                val note: MultimediaEditableNote = getCurrentMultimediaEditableNote()
                if (note.isEmpty) return@launch

                multimediaViewModel.multimediaAction.first { action ->
                    when (action) {
                        MultimediaBottomSheet.MultimediaAction.SELECT_IMAGE_FILE -> {
                            Timber.i("Selected Image option")
                            val field = ImageField()
                            note.setField(fieldIndex, field)
                            openMultimediaImageFragment(fieldIndex = fieldIndex, field, note)
                        }

                        MultimediaBottomSheet.MultimediaAction.SELECT_AUDIO_FILE -> {
                            Timber.i("Selected audio clip option")
                            val field = MediaClipField()
                            note.setField(fieldIndex, field)
                            val mediaIntent =
                                AudioVideoFragment.getIntent(
                                    requireContext(),
                                    MultimediaActivityExtra(fieldIndex, field, note),
                                    AudioVideoFragment.MediaOption.AUDIO_CLIP,
                                )

                            multimediaFragmentLauncher.launch(mediaIntent)
                        }

                        MultimediaBottomSheet.MultimediaAction.OPEN_DRAWING -> {
                            Timber.i("Selected Drawing option")
                            val field = ImageField()
                            note.setField(fieldIndex, field)

                            val drawingIntent =
                                MultimediaImageFragment.getIntent(
                                    requireContext(),
                                    MultimediaActivityExtra(fieldIndex, field, note),
                                    MultimediaImageFragment.ImageOptions.DRAWING,
                                )

                            multimediaFragmentLauncher.launch(drawingIntent)
                        }

                        MultimediaBottomSheet.MultimediaAction.SELECT_AUDIO_RECORDING -> {
                            Timber.i("Selected audio recording option")
                            val field = AudioRecordingField()
                            note.setField(fieldIndex, field)
                            val audioRecordingIntent =
                                AudioRecordingFragment.getIntent(
                                    requireContext(),
                                    MultimediaActivityExtra(fieldIndex, field, note),
                                )

                            multimediaFragmentLauncher.launch(audioRecordingIntent)
                        }

                        MultimediaBottomSheet.MultimediaAction.SELECT_VIDEO_FILE -> {
                            Timber.i("Selected video clip option")
                            val field = MediaClipField()
                            note.setField(fieldIndex, field)
                            val mediaIntent =
                                AudioVideoFragment.getIntent(
                                    requireContext(),
                                    MultimediaActivityExtra(fieldIndex, field, note),
                                    AudioVideoFragment.MediaOption.VIDEO_CLIP,
                                )

                            multimediaFragmentLauncher.launch(mediaIntent)
                        }

                        MultimediaBottomSheet.MultimediaAction.OPEN_CAMERA -> {
                            Timber.i("Selected Camera option")

                            val field = ImageField()
                            note.setField(fieldIndex, field)
                            val imageIntent =
                                MultimediaImageFragment.getIntent(
                                    requireContext(),
                                    MultimediaActivityExtra(fieldIndex, field, note),
                                    MultimediaImageFragment.ImageOptions.CAMERA,
                                )

                            multimediaFragmentLauncher.launch(imageIntent)
                        }
                    }
                    true
                }
            }
    }

    private fun openMultimediaImageFragment(
        fieldIndex: Int,
        field: IField,
        multimediaNote: IMultimediaEditableNote,
        imageUri: Uri? = null,
    ) {
        val multimediaExtra = MultimediaActivityExtra(fieldIndex, field, multimediaNote, imageUri?.toString())

        val imageIntent =
            MultimediaImageFragment.getIntent(
                requireContext(),
                multimediaExtra,
                MultimediaImageFragment.ImageOptions.GALLERY,
            )

        multimediaFragmentLauncher.launch(imageIntent)
    }

    private fun handleMultimediaResult(extras: Bundle) {
        val index = extras.getInt(MULTIMEDIA_RESULT_FIELD_INDEX)
        val field =
            extras.getSerializableCompat<IField>(MULTIMEDIA_RESULT)
                ?: return

        if (field.type != EFieldType.TEXT || field.mediaFile != null) {
            addMediaFileToField(index, field)
        } else {
            Timber.i("field imagePath and audioPath are both null")
        }
    }

    private fun addMediaFileToField(
        index: Int,
        field: IField,
    ) {
        lifecycleScope.launch {
            val note = getCurrentMultimediaEditableNote()
            note.setField(index, field)
            // TODO: Update the field in the view model
            changed = true
        }
    }

    private fun onPaste(
        editText: EditText,
        uri: Uri,
        description: ClipDescription,
        pasteAsPng: Boolean,
    ): Boolean {
        val mediaTag =
            MediaRegistration.onPaste(
                requireContext(),
                uri,
                description,
                pasteAsPng,
                showError = { type -> showSnackbar(type.toHumanReadableString(requireContext())) },
            ) ?: return false

        insertStringInField(editText, mediaTag)
        return true
    }

    private fun onToggleStickyText(
        index: Int,
    ) {
        val updatedStickyState = !currentFields[index].sticky
        currentFields[index].sticky = updatedStickyState
        val text = viewModel.uiState.value.fields[index].content
        if (updatedStickyState) {
            toggleStickyText[index] = text
            Timber.d("Saved Text:: %s", toggleStickyText[index])
        } else {
            toggleStickyText.remove(index)
        }
        launchCatchingTask {
            withCol {
                this.notetypes.save(editorNote!!.notetype)
            }
        }
    }

    @NeedsTest("13719: moving from a note type with more fields to one with fewer fields")
    private fun saveToggleStickyMap() {
        for ((key) in toggleStickyText.toMap()) {
            if (key < viewModel.uiState.value.fields.size) {
                toggleStickyText[key] = viewModel.uiState.value.fields[key].content
            } else {
                toggleStickyText.remove(key)
            }
        }
    }

    private fun updateFieldsFromStickyText() {
        for ((key, value) in toggleStickyText) {
            if (key < viewModel.uiState.value.fields.size) {
                viewModel.onFieldContentChanged(key, value ?: "")
            }
        }
    }

    @VisibleForTesting
    fun clearField(index: Int) {
        setFieldValueFromUi(index, "")
    }

    private fun updateFieldsFromMap(newNotetype: NotetypeJson?) {
        populateFields()
        updateCards(newNotetype)
    }

    private fun allowFieldRemapping(): Boolean {
        return editorNote!!.items().size > 2
    }

    val fieldsFromSelectedNote: Array<Array<String>>
        get() = editorNote!!.items()

    private fun setEditFieldTexts(contents: String?) {
        var fields: List<String>? = null
        val len: Int
        if (contents == null) {
            len = 0
        } else {
            fields = Utils.splitFields(contents)
            len = fields.size
        }
        for (i in viewModel.uiState.value.fields.indices) {
            if (i < len) {
                viewModel.onFieldContentChanged(i, fields!![i])
            } else {
                viewModel.onFieldContentChanged(i, "")
            }
        }
    }

    private fun setDuplicateFieldStyles() {
        if (viewModel.uiState.value.fields.isEmpty()) return
        val fieldIndex = 0
        val oldValue = editorNote!!.fields[fieldIndex]
        updateField(fieldIndex)
        val dupeCode = editorNote!!.fieldsCheck(getColUnsafe)
        // TODO: Change bottom line color of text field
        editorNote!!.values()[fieldIndex] = oldValue
    }

    @KotlinCleanup("remove 'requireNoNulls'")
    val fieldsText: String
        get() {
            val fields = viewModel.uiState.value.fields.map { it.content }.toTypedArray()
            return Utils.joinFields(fields)
        }

    private fun getCurrentFieldText(index: Int): String {
        return viewModel.uiState.value.fields[index].content
    }

    private fun setDid(note: Note?) {
        fun calculateDeckId(): DeckId {
            if (deckId != 0L) return deckId
            if (note != null && !addNote && currentEditedCard != null) {
                return currentEditedCard!!.currentDeckId()
            }

            if (!getColUnsafe.config.getBool(ConfigKey.Bool.ADDING_DEFAULTS_TO_CURRENT_DECK)) {
                return getColUnsafe.notetypes.current().let {
                    Timber.d("Adding to deck of note type, noteType: %s", it.name)
                    return@let it.did
                }
            }

            val currentDeckId = getColUnsafe.config.get(CURRENT_DECK) ?: 1L
            return if (getColUnsafe.decks.isFiltered(currentDeckId)) {
                1
            } else {
                currentDeckId
            }
        }

        deckId = calculateDeckId()
        viewModel.onDeckSelected(getColUnsafe.decks.name(deckId))
    }

    private fun refreshNoteData() {
        setNote(null)
    }

    private fun setNote(
        note: Note?
    ) {
        editorNote =
            if (note == null || addNote) {
                getColUnsafe.run {
                    val notetype = notetypes.current()
                    Note.fromNotetypeId(this@run, notetype.id)
                }
            } else {
                note
            }
        if (selectedTags == null) {
            selectedTags = editorNote!!.tags
        }
        setDid(note)
        updateTags()
        updateCards(editorNote!!.notetype)
        populateFields()
        updateFieldsFromStickyText()
    }

    private val toolbarDialog: AlertDialog.Builder
        get() =
            AlertDialog
                .Builder(requireContext())
                .neutralButton(R.string.help) {
                    requireContext().openUrl(R.string.link_manual_note_format_toolbar)
                }.negativeButton(R.string.dialog_cancel)

    private fun displayAddToolbarDialog() {
        val v = layoutInflater.inflate(R.layout.note_editor_toolbar_add_custom_item, null)
        toolbarDialog.show {
            title(R.string.add_toolbar_item)
            setView(v)
            positiveButton(R.string.dialog_positive_create) {
                val etIcon = v.findViewById<EditText>(R.id.note_editor_toolbar_item_icon)
                val et = v.findViewById<EditText>(R.id.note_editor_toolbar_before)
                val et2 = v.findViewById<EditText>(R.id.note_editor_toolbar_after)
                viewModel.addCustomToolbarButton(etIcon.text.toString(), et.text.toString(), et2.text.toString())
            }
        }
    }

    private fun displayEditToolbarDialog(currentButton: CustomToolbarButton) {
        val view = layoutInflater.inflate(R.layout.note_editor_toolbar_edit_custom_item, null)
        val etIcon = view.findViewById<EditText>(R.id.note_editor_toolbar_item_icon)
        val et = view.findViewById<EditText>(R.id.note_editor_toolbar_before)
        val et2 = view.findViewById<EditText>(R.id.note_editor_toolbar_after)
        val btnDelete = view.findViewById<ImageButton>(R.id.note_editor_toolbar_btn_delete)
        etIcon.setText(currentButton.buttonText)
        et.setText(currentButton.prefix)
        et2.setText(currentButton.suffix)
        val editToolbarDialog =
            toolbarDialog
                .setView(view)
                .positiveButton(R.string.save) {
                    viewModel.editCustomToolbarButton(
                        etIcon.text.toString(),
                        et.text.toString(),
                        et2.text.toString(),
                        currentButton,
                    )
                }.create()
        btnDelete.setOnClickListener {
            suggestRemoveButton(
                currentButton,
                editToolbarDialog,
            )
        }
        editToolbarDialog.show()
    }

    private fun suggestRemoveButton(
        button: CustomToolbarButton,
        editToolbarItemDialog: AlertDialog,
    ) {
        AlertDialog.Builder(requireContext()).show {
            title(R.string.remove_toolbar_item)
            positiveButton(R.string.dialog_positive_delete) {
                editToolbarItemDialog.dismiss()
                viewModel.removeCustomToolbarButton(button)
            }
            negativeButton(R.string.dialog_cancel)
        }
    }

    private fun updateToolbarMargin() {
        val editorLayout = view?.findViewById<View>(R.id.note_editor_layout) ?: return
        val bottomMargin = if (viewModel.uiState.value.isToolbarVisible) {
            resources.getDimension(R.dimen.note_editor_toolbar_height).toInt()
        } else {
            0
        }
        val params = editorLayout.layoutParams as MarginLayoutParams
        params.bottomMargin = bottomMargin
        editorLayout.layoutParams = params
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        updateToolbarMargin()
    }

    override val shortcuts
        get() =
            ShortcutGroup(
                listOf(
                    shortcut("Ctrl+ENTER") { getString(R.string.save) },
                    shortcut("Ctrl+D") { getString(R.string.select_deck) },
                    shortcut("Ctrl+L") { getString(R.string.card_template_editor_group) },
                    shortcut("Ctrl+N") { getString(R.string.select_note_type) },
                    shortcut("Ctrl+Shift+T") { getString(R.string.tag_editor) },
                    shortcut("Ctrl+Shift+C") { getString(R.string.multimedia_editor_popup_cloze) },
                    shortcut("Ctrl+P") { getString(R.string.card_editor_preview_card) },
                ),
                R.string.note_editor_group,
            )

    private fun updateTags() {
        if (selectedTags == null) {
            selectedTags = ArrayList(0)
        }
        val tags = getColUnsafe.tags
            .join(getColUnsafe.tags.canonify(selectedTags!!))
            .trim()
            .replace(" ", ", ")
        viewModel.setTagsLabel(resources.getString(R.string.CardEditorTags, tags))
    }

    @KotlinCleanup("make non-null")
    private fun updateCards(noteType: NotetypeJson?) {
        Timber.d("updateCards()")
        val tmpls = noteType!!.templates
        var cardsList = StringBuilder()
        Timber.d("updateCards() template count is %s", tmpls.length())
        for ((i, tmpl) in tmpls.withIndex()) {
            var name = tmpl.jsonObject.optString("name")
            if (!addNote &&
                tmpls.length() > 1 &&
                noteType.jsonObject === editorNote!!.notetype.jsonObject &&
                currentEditedCard != null &&
                currentEditedCard!!.template(getColUnsafe).jsonObject.optString("name") == name
            ) {
                name = "<u>$name</u>"
            }
            cardsList.append(name)
            if (i < tmpls.length() - 1) {
                cardsList.append(", ")
            }
        }
        if (!addNote && tmpls.length() < editorNote!!.notetype.templates.length()) {
            cardsList = StringBuilder("<font color='red'>$cardsList</font>")
        }
        viewModel.setCardsLabel(
            HtmlCompat.fromHtml(
                resources.getString(R.string.CardEditorCards, cardsList.toString()),
                HtmlCompat.FROM_HTML_MODE_LEGACY,
            ).toString()
        )
    }

    private fun updateField(index: Int): Boolean {
        val field = viewModel.uiState.value.fields[index]
        val correctedFieldContent = NoteService.convertToHtmlNewline(field.content, shouldReplaceNewlines())
        if (editorNote!!.values()[index] != correctedFieldContent) {
            editorNote!!.values()[index] = correctedFieldContent
            return true
        }
        return false
    }

    private fun tagsAsString(tags: List<String>): String = tags.joinToString(" ")

    private val currentlySelectedNotetype: NotetypeJson?
        get() =
            allNoteTypeIds?.let {
                val name = viewModel.uiState.value.selectedNoteType
                getColUnsafe.notetypes.byName(name)
            }

    private fun updateFieldsFromMap(newNotetype: NotetypeJson?) {
        populateFields()
        updateCards(newNotetype)
    }

    private fun allowFieldRemapping(): Boolean {
        return editorNote!!.items().size > 2
    }

    val fieldsFromSelectedNote: Array<Array<String>>
        get() = editorNote!!.items()

    private fun currentNotetypeIsImageOcclusion() = currentlySelectedNotetype?.isImageOcclusion == true

    private fun setupImageOcclusionEditor(imagePath: String = "") {
        val kind: String
        val id: Long
        if (addNote) {
            kind = "add"
            id =
                if (currentNotetypeIsImageOcclusion()) {
                    currentlySelectedNotetype!!.id
                } else {
                    0
                }
        } else {
            kind = "edit"
            id = editorNote?.id!!
        }
        val intent = ImageOcclusion.getIntent(requireContext(), kind, id, imagePath, deckId)
        requestIOEditorCloser.launch(intent)
    }

    private fun changeNoteType(newId: NoteTypeId) {
        val oldNoteTypeId = getColUnsafe.notetypes.current().id
        Timber.i("Changing note type to '%d", newId)

        if (oldNoteTypeId == newId) {
            return
        }

        val noteType = getColUnsafe.notetypes.get(newId)
        if (noteType == null) {
            Timber.w("New note type %s not found, not changing note type", newId)
            return
        }

        getColUnsafe.notetypes.setCurrent(noteType)
        val currentDeck = getColUnsafe.decks.current()
        currentDeck.put("mid", newId)
        getColUnsafe.decks.save(currentDeck)

        if (!getColUnsafe.config.getBool(ConfigKey.Bool.ADDING_DEFAULTS_TO_CURRENT_DECK)) {
            deckId = noteType.did
            viewModel.onDeckSelected(getColUnsafe.decks.name(deckId))
        }

        refreshNoteData()
        setDuplicateFieldStyles()
        viewModel.onNoteTypeChanged(isClozeType)
    }

    override val baseSnackbarBuilder: Snackbar.() -> Unit = {
    }

    private fun convertSelectedTextToCloze(
        textBox: EditText,
        addClozeType: AddClozeType,
    ) {
        var nextClozeIndex = nextClozeIndex
        if (addClozeType == AddClozeType.SAME_NUMBER) {
            nextClozeIndex -= 1
        }
        val prefix = "{{c" + max(1, nextClozeIndex) + "::"
        val suffix = "}}"
        modifyCurrentSelection(TextWrapper(prefix, suffix))
    }

    private val nextClozeIndex: Int
        get() {
            val fieldValues: MutableList<String> =
                ArrayList(
                    viewModel.uiState.value.fields.size,
                )
            for (field in viewModel.uiState.value.fields) {
                fieldValues.add(field.content)
            }
            return ClozeUtils.getNextClozeIndex(fieldValues)
        }
    private val isClozeType: Boolean
        get() = currentlySelectedNotetype!!.isCloze

    @VisibleForTesting
    fun setFieldValueFromUi(
        i: Int,
        newText: String?,
    ) {
        viewModel.onFieldContentChanged(i, newText ?: "")
        isFieldEdited = true
    }

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    fun getFieldForTest(index: Int): NoteEditorFieldState = viewModel.uiState.value.fields[index]

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    fun setCurrentlySelectedNoteType(noteTypeId: NoteTypeId) {
        val name = getColUnsafe.notetypes.get(noteTypeId)!!.name
        viewModel.onNoteTypeSelected(name)
    }

    companion object {
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
        const val NOTE_CHANGED_EXTRA_KEY = "noteChanged"
        const val RELOAD_REQUIRED_EXTRA_KEY = "reloadRequired"
        const val EXTRA_IMG_OCCLUSION = "image_uri"
        const val IN_CARD_BROWSER_ACTIVITY = "inCardBrowserActivity"

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
                fun fromValue(value: Int) = NoteEditorCaller.entries.first { it.value == value }
            }
        }

        const val RESULT_UPDATED_IO_NOTE = 11

        const val PREF_NOTE_EDITOR_SCROLL_TOOLBAR = "noteEditorScrollToolbar"
        private const val PREF_NOTE_EDITOR_SHOW_TOOLBAR = "noteEditorShowToolbar"
        private const val PREF_NOTE_EDITOR_NEWLINE_REPLACE = "noteEditorNewlineReplace"
        private const val PREF_NOTE_EDITOR_CAPITALIZE = "note_editor_capitalize"
        private const val PREF_NOTE_EDITOR_FONT_SIZE = "note_editor_font_size"
        private const val PREF_NOTE_EDITOR_CUSTOM_BUTTONS = "note_editor_custom_buttons"

        fun newInstance(launcher: NoteEditorLauncher): NoteEditorFragment =
            NoteEditorFragment().apply {
                this.arguments = launcher.toBundle()
            }

        private fun shouldReplaceNewlines(): Boolean =
            AnkiDroidApp.instance
                .sharedPrefs()
                .getBoolean(PREF_NOTE_EDITOR_NEWLINE_REPLACE, true)

        @VisibleForTesting
        @CheckResult
        fun intentLaunchedWithImage(intent: Intent): Boolean {
            if (intent.action != Intent.ACTION_SEND && intent.action != Intent.ACTION_VIEW) return false
            if (ImportUtils.isInvalidViewIntent(intent)) return false
            return intent.resolveMimeType()?.startsWith("image/") == true
        }
    }
}