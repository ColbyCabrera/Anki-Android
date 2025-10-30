/* **************************************************************************************
 * Copyright (c) 2025 Colby Cabrera <colbycabrera@gmail.com>                            *
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
package com.ichi2.anki.noteeditor

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ichi2.anki.CollectionManager
import com.ichi2.anki.libanki.Card
import com.ichi2.anki.libanki.Collection
import com.ichi2.anki.libanki.DeckId
import com.ichi2.anki.libanki.Note
import com.ichi2.anki.libanki.NotetypeJson
import com.ichi2.anki.libanki.Note.ClozeUtils
import com.ichi2.anki.noteeditor.compose.NoteEditorState
import com.ichi2.anki.noteeditor.compose.NoteFieldState
import com.ichi2.anki.noteeditor.ToolbarButtonModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.math.max
import kotlin.math.min

enum class ClozeInsertionMode {
    SAME_NUMBER,
    INCREMENT_NUMBER
}

/**
 * ViewModel for the Note Editor screen
 * Manages note editing state and business logic
 */
class NoteEditorViewModel : ViewModel() {
    
    private val _noteEditorState = MutableStateFlow(
        NoteEditorState(
            fields = emptyList(),
            tags = emptyList(),
            selectedDeckName = "",
            selectedNoteTypeName = "",
            isAddingNote = true,
            isClozeType = false,
            isImageOcclusion = false,
            cardsInfo = "",
            focusedFieldIndex = null
        )
    )
    val noteEditorState: StateFlow<NoteEditorState> = _noteEditorState.asStateFlow()

    private val _availableDecks = MutableStateFlow<List<String>>(emptyList())
    val availableDecks: StateFlow<List<String>> = _availableDecks.asStateFlow()

    private val _availableNoteTypes = MutableStateFlow<List<String>>(emptyList())
    val availableNoteTypes: StateFlow<List<String>> = _availableNoteTypes.asStateFlow()

    private val _toolbarButtons = MutableStateFlow<List<ToolbarButtonModel>>(emptyList())
    val toolbarButtons: StateFlow<List<ToolbarButtonModel>> = _toolbarButtons.asStateFlow()

    private val _showToolbar = MutableStateFlow(true)
    val showToolbar: StateFlow<Boolean> = _showToolbar.asStateFlow()

    private var currentNote: Note? = null
    private var currentCard: Card? = null
    private var deckId: DeckId = 0L

    /**
     * Initialize the editor with a new or existing note
     */
    fun initializeEditor(
        col: Collection,
        cardId: Long? = null,
        deckId: Long? = null,
        isAddingNote: Boolean = true
    ) {
        viewModelScope.launch {
            try {
                this@NoteEditorViewModel.deckId = deckId ?: 1L
                
                // Load note
                currentNote = if (cardId != null && !isAddingNote) {
                    currentCard = col.getCard(cardId)
                    currentCard!!.note(col)
                } else {
                    val notetype = col.notetypes.current()
                    Note.fromNotetypeId(col, notetype.id)
                }

                // Load available decks and note types
                _availableDecks.value = col.decks.allNamesAndIds().map { it.name }
                _availableNoteTypes.value = col.notetypes.all().map { it.name }

                // Update UI state
                updateStateFromNote(col, isAddingNote)
            } catch (e: Exception) {
                Timber.e(e, "Error initializing note editor")
            }
        }
    }

    /**
     * Update field value
     */
    fun updateFieldValue(index: Int, value: TextFieldValue) {
        _noteEditorState.update { currentState ->
            val position = currentState.fields.indexOfFirst { it.index == index }
            if (position == -1) {
                return@update currentState
            }
            val updatedFields = currentState.fields.toMutableList()
            updatedFields[position] = updatedFields[position].copy(value = value)
            val focusedIndex = currentState.focusedFieldIndex ?: index
            currentState.copy(fields = updatedFields, focusedFieldIndex = focusedIndex)
        }
    }

    /**
     * Toggle sticky state for a field
     */
    fun toggleStickyField(index: Int) {
        _noteEditorState.update { currentState ->
            val position = currentState.fields.indexOfFirst { it.index == index }
            if (position == -1) {
                return@update currentState
            }
            val updatedFields = currentState.fields.toMutableList()
            val currentField = updatedFields[position]
            updatedFields[position] = currentField.copy(isSticky = !currentField.isSticky)
            currentState.copy(fields = updatedFields)
        }
    }

    /**
     * Update tags
     */
    fun updateTags(tags: List<String>) {
        _noteEditorState.update { it.copy(tags = tags) }
    }

    fun onFieldFocus(index: Int) {
        _noteEditorState.update { currentState ->
            if (currentState.fields.any { it.index == index }) {
                currentState.copy(focusedFieldIndex = index)
            } else {
                currentState
            }
        }
    }

    /**
     * Select a deck
     */
    fun selectDeck(deckName: String) {
        _noteEditorState.update { it.copy(selectedDeckName = deckName) }
    }

    /**
     * Select a note type
     */
    fun selectNoteType(noteTypeName: String) {
        viewModelScope.launch {
            try {
                val col = CollectionManager.getColUnsafe()
                val notetype = col.notetypes.all().find { it.name == noteTypeName }
                if (notetype != null) {
                    currentNote = Note.fromNotetypeId(col, notetype.id)
                    updateStateFromNote(col, _noteEditorState.value.isAddingNote)
                }
            } catch (e: Exception) {
                Timber.e(e, "Error changing note type")
            }
        }
    }

    fun setToolbarButtons(buttons: List<ToolbarButtonModel>) {
        _toolbarButtons.value = buttons
    }

    fun setToolbarVisibility(isVisible: Boolean) {
        _showToolbar.value = isVisible
    }

    /**
     * Save the note
     */
    suspend fun saveNote(): Boolean {
        return try {
            val col = CollectionManager.getColUnsafe()
            val note = currentNote ?: return false

            // Update note fields from state
            val fields = _noteEditorState.value.fields
            fields.forEachIndexed { index, fieldState ->
                if (index < note.fields.size) {
                    note.fields[index] = fieldState.value.text
                }
            }

            // Update tags
            note.setTagsFromStr(col, _noteEditorState.value.tags.joinToString(" "))

            // Save the note
            if (_noteEditorState.value.isAddingNote) {
                col.addNote(note, deckId)
            } else {
                col.updateNote(note)
            }

            true
        } catch (e: Exception) {
            Timber.e(e, "Error saving note")
            false
        }
    }

    fun formatSelection(prefix: String, suffix: String): Boolean {
        val targetIndex = determineFocusIndex() ?: return false
        return updateFieldValueInternal(targetIndex) { value ->
            val text = value.text
            val selection = value.selection
            val start = selection.start.coerceIn(0, text.length)
            val end = selection.end.coerceIn(0, text.length)
            val rangeStart = min(start, end)
            val rangeEnd = max(start, end)
            val before = text.substring(0, rangeStart)
            val selected = text.substring(rangeStart, rangeEnd)
            val after = text.substring(rangeEnd)

            if (selected.isEmpty()) {
                val newText = buildString {
                    append(before)
                    append(prefix)
                    append(suffix)
                    append(after)
                }
                val cursor = rangeStart + prefix.length
                value.copy(text = newText, selection = TextRange(cursor, cursor))
            } else {
                val newText = buildString {
                    append(before)
                    append(prefix)
                    append(selected)
                    append(suffix)
                    append(after)
                }
                val newStart = rangeStart + prefix.length
                val newEnd = newStart + selected.length
                value.copy(text = newText, selection = TextRange(newStart, newEnd))
            }
        }
    }

    fun insertCloze(mode: ClozeInsertionMode): Boolean {
        val baseIndex = calculateNextClozeIndex()
        val clozeIndex = when (mode) {
            ClozeInsertionMode.SAME_NUMBER -> max(1, baseIndex - 1)
            ClozeInsertionMode.INCREMENT_NUMBER -> baseIndex
        }
        return formatSelection("{{c${clozeIndex}::", "}}")
    }

    fun applyToolbarButton(button: ToolbarButtonModel): Boolean =
        formatSelection(button.prefix, button.suffix)

    fun applyToolbarShortcut(shortcutDigit: Int): Boolean {
        val buttons = _toolbarButtons.value
        if (buttons.isEmpty()) {
            return false
        }
        val target = buttons.firstOrNull { button ->
            val visualIndex = button.index + 1
            val mod = visualIndex % 10
            if (shortcutDigit == 0) {
                mod == 0
            } else {
                mod == shortcutDigit
            }
        } ?: return false
        return applyToolbarButton(target)
    }

    /**
     * Update state from the current note
     */
    private fun updateStateFromNote(col: Collection, isAddingNote: Boolean) {
        val note = currentNote ?: return
        val notetype = note.notetype

        val fields = note.fields.mapIndexed { index, value ->
            val field = notetype.fields[index]
            NoteFieldState(
                name = field.name,
                value = TextFieldValue(value),
                isSticky = field.sticky,
                hint = "",
                index = index
            )
        }

        val deckName = try {
            col.decks.name(deckId)
        } catch (e: Exception) {
            "Default"
        }

        _noteEditorState.update { currentState ->
            val newFocus =
                currentState.focusedFieldIndex?.takeIf { focus ->
                    fields.any { it.index == focus }
                } ?: fields.firstOrNull()?.index
            currentState.copy(
                fields = fields,
                tags = note.tags,
                selectedDeckName = deckName,
                selectedNoteTypeName = notetype.name,
                isAddingNote = isAddingNote,
                isClozeType = notetype.isCloze,
                isImageOcclusion = notetype.isImageOcclusion,
                cardsInfo = if (isAddingNote) {
                    ""
                } else {
                    "Cards: ${note.numberOfCards(col)}"
                },
                focusedFieldIndex = newFocus
            )
        }
    }

    /**
     * Toggle toolbar visibility
     */
    fun toggleToolbarVisibility() {
        _showToolbar.update { !it }
    }

    private fun determineFocusIndex(): Int? {
        val state = _noteEditorState.value
        val focus = state.focusedFieldIndex
        if (focus != null && state.fields.any { it.index == focus }) {
            return focus
        }
        return state.fields.firstOrNull()?.index
    }

    private fun updateFieldValueInternal(
        fieldIndex: Int,
        transform: (TextFieldValue) -> TextFieldValue?,
    ): Boolean {
        var applied = false
        _noteEditorState.update { currentState ->
            val position = currentState.fields.indexOfFirst { it.index == fieldIndex }
            if (position == -1) {
                return@update currentState
            }
            val field = currentState.fields[position]
            val newValue = transform(field.value)
            if (newValue == null || newValue == field.value) {
                return@update currentState
            }
            val updatedFields = currentState.fields.toMutableList()
            updatedFields[position] = field.copy(value = newValue)
            applied = true
            currentState.copy(fields = updatedFields, focusedFieldIndex = fieldIndex)
        }
        return applied
    }

    private fun calculateNextClozeIndex(): Int =
        try {
            val values = _noteEditorState.value.fields.map { it.value.text }
            ClozeUtils.getNextClozeIndex(values)
        } catch (e: Exception) {
            Timber.w(e, "Error calculating next cloze index")
            1
        }
}
