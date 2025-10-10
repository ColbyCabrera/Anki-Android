
package com.ichi2.anki.noteeditor

import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ichi2.anki.libanki.Note
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.ichi2.anki.libanki.Collection as AnkiCollection

@HiltViewModel
class NoteEditorViewModel @Inject constructor(
    private val sharedPreferences: SharedPreferences,
) : ViewModel() {
    private val _uiState = MutableStateFlow(NoteEditorUiState())
    val uiState: StateFlow<NoteEditorUiState> = _uiState.asStateFlow()

    private var note: Note? = null

    fun initialize(col: AnkiCollection, launcher: NoteEditorLauncher) {
        viewModelScope.launch {
            val addNote: Boolean
            var noteToEdit: Note?

            if (launcher is NoteEditorLauncher.EditCard) {
                addNote = false
                val card = col.getCard(launcher.cardId)
                noteToEdit = card.note(col)
            } else {
                addNote = true
                val notetypeId = col.decks.current().getLong("mid")
                noteToEdit = col.newNote(col.notetypes.get(notetypeId)!!)
            }

            note = noteToEdit

            val deckNames = col.decks.allNamesAndIds().map { it.name }
            val selectedDeckId = noteToEdit.cards(col).first().did
            val selectedDeckName = col.decks.name(selectedDeckId)

            val noteTypeNames = col.notetypes.allNamesAndIds().map { it.name }.toList()
            val selectedNoteTypeName = noteToEdit.notetype.name

            val fieldStates = noteToEdit.fields.mapIndexed { index, field ->
                NoteEditorFieldState(
                    label = noteToEdit.notetype.fields[index].name,
                    content = field,
                    isSticky = noteToEdit.notetype.fields[index].sticky,
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
                    isCloze = noteToEdit.notetype.isCloze,
                    decks = deckNames,
                    selectedDeck = selectedDeckName,
                    noteTypes = noteTypeNames,
                    selectedNoteType = selectedNoteTypeName,
                    fields = fieldStates,
                    tagsLabel = "Tags: $tags",
                    cardsLabel = cardsLabel,
                )
            }
        }
    }

    private fun buildCardsLabel(col: AnkiCollection, note: Note, addNote: Boolean): String {
        val tmpls = note.notetype.templates
        val cardsList = StringBuilder()
        for ((i, tmpl) in tmpls.withIndex()) {
            var name = tmpl.name
            if (!addNote &&
                tmpls.length() > 1 &&
                note.noteTypeId == note.notetype.id &&
                note.cards(col).isNotEmpty() &&
                note.cards(col)[0].template(col).name == name
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
        sharedPreferences.edit { putBoolean(PREF_NOTE_EDITOR_SHOW_TOOLBAR, newVisibility) }
        _uiState.update { it.copy(isToolbarVisible = newVisibility) }
    }

    fun addCustomToolbarButton(buttonText: String, prefix: String, suffix: String) {
        if (prefix.isEmpty() && suffix.isEmpty()) return
        val currentButtons = _uiState.value.customButtons
        val newButton = CustomToolbarButton(currentButtons.size, buttonText, prefix, suffix)
        saveToolbarButtons(currentButtons + newButton)
    }

    fun editCustomToolbarButton(buttonText: String, prefix: String, suffix: String, currentButton: CustomToolbarButton) {
        val newButtons = _uiState.value.customButtons.map {
            if (it.index == currentButton.index) {
                it.copy(
                    buttonText = buttonText.ifEmpty { currentButton.buttonText },
                    prefix = prefix.ifEmpty { currentButton.prefix },
                    suffix = suffix.ifEmpty { currentButton.suffix },
                )
            } else {
                it
            }
        }
        saveToolbarButtons(newButtons)
    }

    fun removeCustomToolbarButton(button: CustomToolbarButton) {
        val newButtons = _uiState.value.customButtons.filter { it.index != button.index }
        saveToolbarButtons(newButtons)
    }

    private fun getToolbarButtonsFromPreferences(): List<CustomToolbarButton> {
        val set = sharedPreferences.getStringSet(PREF_NOTE_EDITOR_CUSTOM_BUTTONS, emptySet())
        return CustomToolbarButton.fromStringSet(set!!)
    }

    private fun saveToolbarButtons(buttons: List<CustomToolbarButton>) {
        sharedPreferences.edit { putStringSet(PREF_NOTE_EDITOR_CUSTOM_BUTTONS, CustomToolbarButton.toStringSet(buttons)) }
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
    val isSticky: Boolean = false,
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
    val cardsLabel: String = "",
)

data class CustomToolbarButton(
    val index: Int,
    val buttonText: String,
    val prefix: String,
    val suffix: String,
) {
    companion object {
        private const val DELIMITER = "|"

        fun fromStringSet(set: Set<String>): List<CustomToolbarButton> {
            return set.mapNotNull {
                val parts = it.split(DELIMITER)
                if (parts.size == 4) {
                    CustomToolbarButton(
                        index = parts[0].toInt(),
                        buttonText = parts[1],
                        prefix = parts[2],
                        suffix = parts[3],
                    )
                } else {
                    null
                }
            }.sortedBy { it.index }
        }

        fun toStringSet(buttons: List<CustomToolbarButton>): Set<String> {
            return buttons.map {
                "${it.index}$DELIMITER${it.buttonText}$DELIMITER${it.prefix}$DELIMITER${it.suffix}"
            }.toSet()
        }
    }
}
