/*
 *  Copyright (c) 2025 Hari Srinivasan <harisrini21@gmail.com>
 *
 *  This program is free software; you can redistribute it and/or modify it under
 *  the terms of the GNU General Public License as published by the Free Software
 *  Foundation; either version 3 of the License, or (at your option) any later
 *  version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY
 *  WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 *  PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with
 *  this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.ichi2.anki

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.EditText
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.ichi2.anki.android.input.ShortcutGroup
import com.ichi2.anki.android.input.ShortcutGroupProvider
import com.ichi2.anki.libanki.Collection
import com.ichi2.anki.multimedia.MultimediaActivity
import com.ichi2.anki.multimedia.MultimediaActivityExtra
import com.ichi2.anki.multimedia.MultimediaBottomSheet
import com.ichi2.anki.multimedia.MultimediaImageFragment
import com.ichi2.anki.multimedia.MultimediaViewModel
import com.ichi2.anki.noteeditor.NoteEditorEvent
import com.ichi2.anki.noteeditor.NoteEditorRoute
import com.ichi2.anki.noteeditor.NoteEditorViewModel
import com.ichi2.anki.noteeditor.PreviewerRoute
import com.ichi2.anki.noteeditor.compose.NoteEditorScreenRoute
import com.ichi2.anki.snackbar.BaseSnackbarBuilderProvider
import com.ichi2.anki.snackbar.SnackbarBuilder
import com.ichi2.anki.snackbar.showSnackbar
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import com.ichi2.anki.CardTemplateEditor
import com.ichi2.anki.CollectionManager
import com.ichi2.anki.NotetypeFile
import com.ichi2.anki.multimedia.AudioRecordingFragment
import com.ichi2.anki.multimedia.AudioVideoFragment
import com.ichi2.anki.multimediacard.IMultimediaEditableNote
import com.ichi2.anki.multimediacard.fields.AudioRecordingField
import com.ichi2.anki.multimediacard.fields.EFieldType
import com.ichi2.anki.multimediacard.fields.IField
import com.ichi2.anki.multimediacard.fields.ImageField
import com.ichi2.anki.multimediacard.fields.MediaClipField
import com.ichi2.anki.servicelayer.NoteService
import com.ichi2.anki.previewer.TemplatePreviewerPage
import com.ichi2.anki.previewer.TemplatePreviewerArguments
import com.ichi2.compat.CompatHelper.Companion.getSerializableCompat

import timber.log.Timber
import kotlin.reflect.KClass
import kotlin.reflect.jvm.jvmName

/**
 * This activity contains the NoteEditor logic, and, on x-large screens, the previewer fragment.
 * It also ensures that changes in the note are transmitted to the previewer
 */

// TODO: Move intent handling to [NoteEditorActivity]
class NoteEditorActivity : AnkiActivity(), BaseSnackbarBuilderProvider, DispatchKeyEventListener,
    ShortcutGroupProvider {
    override val baseSnackbarBuilder: SnackbarBuilder = { }

    private val noteEditorViewModel: NoteEditorViewModel by viewModels()
    private val multimediaViewModel: MultimediaViewModel by viewModels()

    private var multimediaActionJob: Job? = null

    private val multimediaFragmentLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null && result.data!!.extras != null) {
                handleMultimediaResult(result.data!!.extras!!)
            }
        }

    private val requestTemplateEditLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // Reload required handled by ViewModel/Fragment result
                // But we might need to refresh the editor if the note type changed
                // For now, assume ViewModel handles reloading data if needed
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        if (showedActivityFailedScreen(savedInstanceState)) {
            return
        }
        super.onCreate(savedInstanceState)
        if (!ensureStoragePermissions()) {
            return
        }

        lifecycleScope.launch {
            noteEditorViewModel.events.collectLatest { event ->
                when (event) {
                    is NoteEditorEvent.ShowMultimediaPicker -> handleMultimediaActions(event.fieldIndex)
                    is NoteEditorEvent.ShowCardTemplateEditor -> showCardTemplateEditor()
                    is NoteEditorEvent.ShowMathJaxDialog -> displayInsertMathJaxEquationsDialog()
                    is NoteEditorEvent.ShowHeadingDialog -> displayInsertHeadingDialog()
                    is NoteEditorEvent.ShowFontSizeDialog -> displayFontSizeDialog()
                    is NoteEditorEvent.ShowAddCustomButtonDialog -> displayAddToolbarDialog()
                    is NoteEditorEvent.NavigateToPreview -> performPreview()
                }
            }
        }

        setContent {
            val backStack = rememberNavBackStack(NoteEditorRoute)

            com.ichi2.anki.ui.compose.theme.AnkiDroidTheme {
                NavDisplay(backStack = backStack, onBack = {
                    if (backStack.size > 1) {
                        backStack.removeAt(backStack.lastIndex)
                    } else {
                        finish()
                    }
                }, entryProvider = entryProvider {
                    entry<NoteEditorRoute> {
                        NoteEditorScreenRoute(
                            onNavigateBack = { finish() },
                            onNavigateToPreview = { cardId -> backStack.add(PreviewerRoute(cardId)) }
                        )
                    }
                    entry<PreviewerRoute> {
                        androidx.compose.material3.Text("Previewer Placeholder")
                    }
                })
            }
        }
    }

    private fun handleMultimediaActions(fieldIndex: Int) {
        multimediaActionJob?.cancel()
        multimediaActionJob = lifecycleScope.launch {
            val note = noteEditorViewModel.getCurrentMultimediaEditableNote()

            if (note.numberOfFields == 0) return@launch

            multimediaViewModel.multimediaAction.first { action ->
                when (action) {
                    MultimediaBottomSheet.MultimediaAction.SELECT_IMAGE_FILE -> {
                        Timber.i("Selected Image option")
                        val field = ImageField()
                        note.setField(fieldIndex, field)
                        openMultimediaImageFragment(fieldIndex, field, note)
                    }
                    MultimediaBottomSheet.MultimediaAction.SELECT_AUDIO_FILE -> {
                        Timber.i("Selected audio clip option")
                        val field = MediaClipField()
                        note.setField(fieldIndex, field)
                        val mediaIntent = AudioVideoFragment.getIntent(
                            this@NoteEditorActivity,
                            MultimediaActivityExtra(fieldIndex, field, note),
                            AudioVideoFragment.MediaOption.AUDIO_CLIP,
                        )
                        multimediaFragmentLauncher.launch(mediaIntent)
                    }
                    MultimediaBottomSheet.MultimediaAction.OPEN_DRAWING -> {
                        Timber.i("Selected Drawing option")
                        val field = ImageField()
                        note.setField(fieldIndex, field)
                        val drawingIntent = MultimediaImageFragment.getIntent(
                            this@NoteEditorActivity,
                            MultimediaActivityExtra(fieldIndex, field, note),
                            MultimediaImageFragment.ImageOptions.DRAWING,
                        )
                        multimediaFragmentLauncher.launch(drawingIntent)
                    }
                    MultimediaBottomSheet.MultimediaAction.SELECT_AUDIO_RECORDING -> {
                        Timber.i("Selected audio recording option")
                        val field = AudioRecordingField()
                        note.setField(fieldIndex, field)
                        val audioRecordingIntent = AudioRecordingFragment.getIntent(
                            this@NoteEditorActivity,
                            MultimediaActivityExtra(fieldIndex, field, note),
                        )
                        multimediaFragmentLauncher.launch(audioRecordingIntent)
                    }
                    MultimediaBottomSheet.MultimediaAction.SELECT_VIDEO_FILE -> {
                        Timber.i("Selected video clip option")
                        val field = MediaClipField()
                        note.setField(fieldIndex, field)
                        val mediaIntent = AudioVideoFragment.getIntent(
                            this@NoteEditorActivity,
                            MultimediaActivityExtra(fieldIndex, field, note),
                            AudioVideoFragment.MediaOption.VIDEO_CLIP,
                        )
                        multimediaFragmentLauncher.launch(mediaIntent)
                    }
                    MultimediaBottomSheet.MultimediaAction.OPEN_CAMERA -> {
                        Timber.i("Selected Camera option")
                        val field = ImageField()
                        note.setField(fieldIndex, field)
                        val imageIntent = MultimediaImageFragment.getIntent(
                            this@NoteEditorActivity,
                            MultimediaActivityExtra(fieldIndex, field, note),
                            MultimediaImageFragment.ImageOptions.CAMERA,
                        )
                        multimediaFragmentLauncher.launch(imageIntent)
                    }
                }
                true
            }
        }
        val multimediaBottomSheet = MultimediaBottomSheet()
        multimediaBottomSheet.show(supportFragmentManager, "MultimediaBottomSheet")
    }

    private fun openMultimediaImageFragment(
        fieldIndex: Int,
        field: IField,
        multimediaNote: IMultimediaEditableNote,
        imageUri: Uri? = null,
    ) {
        val multimediaExtra =
            MultimediaActivityExtra(fieldIndex, field, multimediaNote, imageUri?.toString())

        val imageIntent = MultimediaImageFragment.getIntent(
            this,
            multimediaExtra,
            MultimediaImageFragment.ImageOptions.GALLERY,
        )

        multimediaFragmentLauncher.launch(imageIntent)
    }

    private fun handleMultimediaResult(extras: Bundle) {
        val index = extras.getInt(MultimediaActivity.MULTIMEDIA_RESULT_FIELD_INDEX)
        val field = extras.getSerializableCompat<IField>(MultimediaActivity.MULTIMEDIA_RESULT) ?: return

        if (field.type != EFieldType.TEXT || field.mediaFile != null) {
            noteEditorViewModel.addMediaFileToField(index, field)
        } else {
            Timber.i("field imagePath and audioPath are both null")
        }
    }

    private fun showCardTemplateEditor() {
        val intent = Intent(this, CardTemplateEditor::class.java)
        val noteTypeName = noteEditorViewModel.noteEditorState.value.selectedNoteTypeName
        val noteTypeId = CollectionManager.getColUnsafe().notetypes.all().find { it.name == noteTypeName }?.id

        if (noteTypeId == null) {
            Timber.w("showCardTemplateEditor(): noteTypeId is null")
            showSnackbar(getString(R.string.note_type_not_found_for_template_editor))
            return
        }

        intent.putExtra("noteTypeId", noteTypeId)
        // Note: For simplicity in migration, we might skip passing noteId/ordId if complex to retrieve without currentEditedCard
        // But we can try to get it from ViewModel if available, or just launch editor for the type
        requestTemplateEditLauncher.launch(intent)
    }

    private fun displayInsertMathJaxEquationsDialog() {
        data class MathJaxOption(
            val label: String, val prefix: String, val suffix: String
        )

        val options = arrayOf(
            MathJaxOption(getString(R.string.mathjax_block), prefix = "\\[", suffix = "\\]"),
            MathJaxOption(getString(R.string.mathjax_chemistry), prefix = "\\( \\ce{", suffix = "} \\)")
        )

        AlertDialog.Builder(this)
            .setTitle(R.string.insert_mathjax)
            .setItems(options.map { it.label }.toTypedArray()) { _, index ->
                val option = options.getOrNull(index) ?: return@setItems
                applyFormatter(option.prefix, option.suffix)
            }
            .create()
            .show()
    }

    private fun displayInsertHeadingDialog() {
        val headingTags = arrayOf("h1", "h2", "h3", "h4", "h5")
        AlertDialog.Builder(this)
            .setTitle(R.string.insert_heading)
            .setItems(headingTags) { _, index ->
                val tag = headingTags.getOrNull(index) ?: return@setItems
                applyFormatter("<$tag>", "</$tag>")
            }
            .create()
            .show()
    }

    private fun displayFontSizeDialog() {
        val sizeCodes = resources.getStringArray(R.array.html_size_codes)
        AlertDialog.Builder(this)
            .setTitle(R.string.menu_font_size)
            .setItems(R.array.html_size_code_labels) { _, index ->
                val size = sizeCodes.getOrNull(index) ?: return@setItems
                applyFormatter("<span style=\"font-size:$size\">", "</span>")
            }
            .create()
            .show()
    }

    private fun displayAddToolbarDialog() {
        val v = layoutInflater.inflate(R.layout.note_editor_toolbar_add_custom_item, null)
        AlertDialog.Builder(this)
            .setTitle(R.string.add_toolbar_item)
            .setView(v)
            .setPositiveButton(R.string.dialog_positive_create) { _, _ ->
                val etIcon = v.findViewById<EditText>(R.id.note_editor_toolbar_item_icon)
                val et = v.findViewById<EditText>(R.id.note_editor_toolbar_before)
                val et2 = v.findViewById<EditText>(R.id.note_editor_toolbar_after)
                noteEditorViewModel.addCustomToolbarButton(
                    etIcon.text.toString(),
                    et.text.toString(),
                    et2.text.toString()
                )
            }
            .create()
            .show()
    }

    private fun applyFormatter(prefix: String, suffix: String) {
        noteEditorViewModel.formatSelection(prefix, suffix)
    }

    private suspend fun performPreview() {
        // Simplified preview logic for Compose
        val fields = noteEditorViewModel.noteEditorState.value.fields.map { fieldState ->
             NoteService.convertToHtmlNewline(fieldState.value.text, false) // simplified
        }.toMutableList()

        val tags = noteEditorViewModel.noteEditorState.value.tags.toMutableList()
        val notetype = CollectionManager.getColUnsafe().notetypes.current() // Simplified
        val noteId = 0L // Simplified

        val args = TemplatePreviewerArguments(
            notetypeFile = NotetypeFile(this, notetype),
            fields = fields,
            tags = tags,
            id = noteId,
            ord = 0,
            fillEmpty = false,
        )
        val intent = TemplatePreviewerPage.getIntent(this, args)
        startActivity(intent)
    }

    override fun onCollectionLoaded(col: Collection) {
        super.onCollectionLoaded(col)
        Timber.d("onCollectionLoaded()")
        registerReceiver()
    }

    override val shortcuts: ShortcutGroup
        get() = ShortcutGroup(emptyList(), R.string.app_name)

    companion object {
        const val FRAGMENT_ARGS_EXTRA = "fragmentArgs"
        const val FRAGMENT_NAME_EXTRA = "fragmentName"
        // FRAGMENT_TAG unused
        // const val FRAGMENT_TAG = "NoteEditorFragmentTag"

        /**
         * Creates an Intent to launch the NoteEditor activity with a specific fragment class and arguments.
         *
         * @param context The context from which the intent will be launched
         * @param fragmentClass The Kotlin class of the Fragment to instantiate
         * @param arguments Optional bundle of arguments to pass to the fragment
         * @param intentAction Optional action to set on the intent
         * @return An Intent configured to launch NoteEditor with the specified fragment
         */
        fun getIntent(
            context: Context,
            fragmentClass: KClass<out Fragment>,
            arguments: Bundle? = null,
            intentAction: String? = null,
        ): Intent = Intent(context, NoteEditorActivity::class.java).apply {
            putExtra(FRAGMENT_NAME_EXTRA, fragmentClass.jvmName)
            putExtra(FRAGMENT_ARGS_EXTRA, arguments)
            action = intentAction
        }
    }
}
