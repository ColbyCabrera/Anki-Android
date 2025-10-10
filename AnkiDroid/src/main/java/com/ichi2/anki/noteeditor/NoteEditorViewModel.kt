
package com.ichi2.anki.noteeditor

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ichi2.anki.libanki.Collection
import com.ichi2.anki.libanki.Note
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoteEditorViewModel @Inject constructor(
    private val sharedPreferences: SharedPreferences
) : ViewModel() {
    private val _uiState = MutableStateFlow(NoteEditorUiState())
    val uiState: StateFlow<NoteEditorUiState> = _uiState.asStateFlow()

    private var note: Note? = null

    fun initialize(col: Collection, launcher: NoteEditorLauncher) {
        viewModelScope.launch {
            val addNote: Boolean
            var noteToEdit: Note?

            if (launcher is NoteEditorLauncher.EditCard) {
                addNote = false
                val card = col.getCard(launcher.cardId)
                noteToEdit = card?.note()
            } else {
                addNote = true
                val notetypeId = col.decks.current().getLong("mid")
                noteToEdit = col.newNote(col.notetypes.get(notetypeId))
            }

            note = noteToEdit
            if (note == null) {
                // TODO: Handle error
                return@launch
            }

            val deckNames = col.decks.allNames()
            val selectedDeckId = noteToEdit.did
            val selectedDeckName = col.decks.name(selectedDeckId)

            val noteTypeIds = col.notetypes.allIds()
            val noteTypeNames = noteTypeIds.map { col.notetypes.get(it).getString("name") }
            val selectedNoteTypeName = noteToEdit.notetype().getString("name")

            val fieldStates = noteToEdit.fields().mapIndexed { index, field ->
                NoteEditorFieldState(
                    label = field.getString("name"),
                    content = noteToEdit.values()[index],
                    isSticky = field.getBoolean("sticky")
                )
            }

            val tags = col.tags.join(col.tags.canonify(noteToEdit.tags))
                .trim()
                .replace(" ", ", ")

            val cardsLabel = buildCardsLabel(col, noteToEdit, addNote)

            _uiState.update {
                it.copy(
                    isToolbarVisible = sharedPreferences.getBoolean(PREF_NOTE_EDITOR_SHOW_TOOLBAR, true),
                    customButtons = getToolbarButtonsFromPreferences(),
                    isCloze = noteToEdit.notetype().getBoolean("isCloze"),
                    decks = deckNames,
                    selectedDeck = selectedDeckName,
                    noteTypes = noteTypeNames,
                    selectedNoteType = selectedNoteTypeName,
                    fields = fieldStates,
                    tagsLabel = "Tags: $tags",
                    cardsLabel = cardsLabel
                )
            }
        }
    }

    private fun buildCardsLabel(col: Collection, note: Note, addNote: Boolean): String {
        val tmpls = note.notetype().getJSONArray("tmpls")
        var cardsList = StringBuilder()
        for (i in 0 until tmpls.length()) {
            val tmpl = tmpls.getJSONObject(i)
            var name = tmpl.optString("name")
            if (!addNote &&
                tmpls.length() > 1 &&
                note.noteType()!!.getLong("id") == note.notetype().getLong("id") &&
                note.cards().isNotEmpty() &&
                note.cards()[0].template().optString("name") == name
            ) {
                name = "<u>$name</u>"
            }
            cardsList.append(name)
            if (i < tmpls.length() - 1) {
                cardsList.append(", ")
            }
        }
        return "Cards: $cardsList"
    }

    fun onNoteTypeChanged(isCloze: Boolean) {
        _uiState.update { it.copy(isCloze = isCloze) }
    }

    fun onDeckSelected(deck: String) {
        _uiState.update { it.copy(selectedDeck = deck) }
    }

    fun onNoteTypeSelected(noteType: String) {
        _uiState.update { it.copy(selectedNoteType = noteType) }
        // TODO: Handle changing note type logic
    }

    fun onFieldContentChanged(fieldIndex: Int, newContent: String) {
        _uiState.update {
            val newFields = it.fields.toMutableList()
            newFields[fieldIndex] = newFields[fieldIndex].copy(content = newContent)
            it.copy(fields = newFields)
        }
    }

    fun onFieldStickyChanged(fieldIndex: Int) {
        _uiState.update {
            val newFields = it.fields.toMutableList()
            val oldField = newFields[fieldIndex]
            newFields[fieldIndex] = oldField.copy(isSticky = !oldField.isSticky)
            it.copy(fields = newFields)
        }
    }

    fun toggleToolbarVisibility() {
        val newVisibility = !_uiState.value.isToolbarVisible
        sharedPreferences.edit().putBoolean(PREF_NOTE_EDITOR_SHOW_TOOLBAR, newVisibility).apply()
        _uiState.update { it.copy(isToolbarVisible = newVisibility) }
    }

    fun addCustomToolbarButton(buttonText: String, prefix: String, suffix: String) {
        if (prefix.isEmpty() && suffix.isEmpty()) return
        val currentButtons = _uiState.value.customButtons
        val newButton = CustomToolbarButton(currentButtons.size, buttonText, prefix, suffix)
        saveToolbarButtons(currentButtons + newButton)
    }

    fun editCustomToolbarButton(buttonText: String, prefix: String, suffix: String, currentButton: CustomToolbarButton) {
        val newButtons = _uiState.value.customButtons.toMutableList()
        val currentButtonIndex = currentButton.index

        newButtons[currentButtonIndex] =
            CustomToolbarButton(
                index = currentButtonIndex,
                buttonText = buttonText.ifEmpty { currentButton.buttonText },
                prefix = prefix.ifEmpty { currentButton.prefix },
                suffix = suffix.ifEmpty { currentButton.suffix },
            )

        saveToolbarButtons(newButtons)
    }

    fun removeCustomToolbarButton(button: CustomToolbarButton) {
        val newButtons = _uiState.value.customButtons.toMutableList()
        newButtons.removeAt(button.index)
        saveToolbarButtons(newButtons)
    }

    private fun getToolbarButtonsFromPreferences(): List<CustomToolbarButton> {
        val set = sharedPreferences.getStringSet(PREF_NOTE_EDITOR_CUSTOM_BUTTONS, HashUtil.hashSetInit(0))
        return CustomToolbarButton.fromStringSet(set!!)
    }

    private fun saveToolbarButtons(buttons: List<CustomToolbarButton>) {
        sharedPreferences.edit().putStringSet(PREF_NOTE_EDITOR_CUSTOM_BUTTONS, CustomToolbarButton.toStringSet(ArrayList(buttons))).apply()
        _uiState.update { it.copy(customButtons = buttons) }
    }

    companion object {
        private const val PREF_NOTE_EDITOR_SHOW_TOOLBAR = "noteEditorShowToolbar"
        private const val PREF_NOTE_EDITOR_CUSTOM_BUTTONS = "note_editor_custom_buttons"
    }
}

data class NoteEditorFieldState(
    val label: String,
    val content: String,
    val isSticky: Boolean = false
)

data class NoteEditorUiState(
    val isToolbarVisible: Boolean = true,
    val isCloze: Boolean = false,
    val customButtons: List<CustomToolbarButton> = emptyList(),
    val decks: List<String> = emptyList(),
    val selectedDeck: String = "",
    val noteTypes: List<String> = emptyList(),
    val selectedNoteType: String = "",
    val fields: List<NoteEditorFieldState> = emptyList(),
    val tagsLabel: String = "",
    val cardsLabel: String = ""
)

data class CustomToolbarButton(
    val index: Int,
    val buttonText: String,
    val prefix: String,
    val suffix: String
) {
    companion object {
        fun fromStringSet(set: Set<String>): List<CustomToolbarButton> {
            return emptyList()
        }

        fun toStringSet(buttons: List<CustomToolbarButton>): Set<String> {
            return emptySet()
        }
    }
}
