package com.ichi2.anki.noteeditor

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import com.ichi2.anki.libanki.NotetypeJson
import com.ichi2.anki.model.SelectableDeck
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min

data class NoteEditorField(
    val name: String,
    val value: TextFieldValue,
    val isFocused: Boolean = false
)

data class NoteEditorState(
    val toolbarState: NoteEditorToolbarState = NoteEditorToolbarState(),
    val decks: List<SelectableDeck> = emptyList(),
    val selectedDeck: SelectableDeck? = null,
    val noteTypes: List<NotetypeJson> = emptyList(),
    val selectedNoteType: NotetypeJson? = null,
    val fields: List<NoteEditorField> = emptyList(),
    val tags: String = "",
    val cards: String = ""
)

data class NoteEditorToolbarState(
    val isVisible: Boolean = true,
    val customButtons: List<CustomToolbarButton> = emptyList(),
    val isClozeNoteType: Boolean = false,
)

@HiltViewModel
class NoteEditorViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(NoteEditorState())
    val uiState = _uiState.asStateFlow()

    fun onDataLoaded(
        decks: List<SelectableDeck>,
        selectedDeck: SelectableDeck?,
        noteTypes: List<NotetypeJson>,
        selectedNoteType: NotetypeJson?,
        fields: List<NoteEditorField>,
        tags: String,
        cards: String
    ) {
        _uiState.update {
            it.copy(
                decks = decks,
                selectedDeck = selectedDeck,
                noteTypes = noteTypes,
                selectedNoteType = selectedNoteType,
                fields = fields,
                tags = tags,
                cards = cards
            )
        }
        onClozeNoteTypeChanged(selectedNoteType?.isCloze ?: false)
    }

    fun onClozeNoteTypeChanged(isCloze: Boolean) {
        _uiState.update { it.copy(toolbarState = it.toolbarState.copy(isClozeNoteType = isCloze)) }
    }

    fun onCustomButtonsChanged(buttons: List<CustomToolbarButton>) {
        _uiState.update { it.copy(toolbarState = it.toolbarState.copy(customButtons = buttons)) }
    }

    fun onToolbarVisibilityChanged(isVisible: Boolean) {
        _uiState.update { it.copy(toolbarState = it.toolbarState.copy(isVisible = isVisible)) }
    }

    fun onDeckSelected(deck: SelectableDeck) {
        _uiState.update { it.copy(selectedDeck = deck) }
    }

    fun onNoteTypeSelected(noteType: NotetypeJson) {
        _uiState.update { it.copy(selectedNoteType = noteType) }
        onClozeNoteTypeChanged(noteType.isCloze)
    }

    fun onFieldChanged(index: Int, newValue: TextFieldValue) {
        _uiState.update { currentState ->
            val newFields = currentState.fields.toMutableList()
            if (index < newFields.size) {
                newFields[index] = newFields[index].copy(value = newValue)
            }
            currentState.copy(fields = newFields)
        }
    }

    fun onFieldFocused(index: Int) {
        _uiState.update { currentState ->
            val newFields = currentState.fields.mapIndexed { i, field ->
                field.copy(isFocused = i == index)
            }
            currentState.copy(fields = newFields)
        }
    }

    fun onTagsUpdated(tags: String) {
        _uiState.update { it.copy(tags = tags) }
    }

    fun onCardsUpdated(cards: String) {
        _uiState.update { it.copy(cards = cards) }
    }

    fun formatFocusedField(formatter: TextFormatter) {
        val focusedFieldIndex = uiState.value.fields.indexOfFirst { it.isFocused }
        if (focusedFieldIndex == -1) return

        val focusedField = uiState.value.fields[focusedFieldIndex]
        val newValue = formatter.format(focusedField.value)
        onFieldChanged(focusedFieldIndex, newValue)
    }
}

fun TextFormatter.format(textFieldValue: TextFieldValue): TextFieldValue {
    val selection = textFieldValue.selection
    val text = textFieldValue.text

    val start = min(selection.start, selection.end)
    val end = max(selection.start, selection.end)

    val before = text.substring(0, start)
    val selected = text.substring(start, end)
    val after = text.substring(end)

    val (newText, newStart, newEnd) = format(selected)

    return textFieldValue.copy(
        text = before + newText + after,
        selection = androidx.compose.ui.text.TextRange(start + newStart, start + newEnd)
    )
}