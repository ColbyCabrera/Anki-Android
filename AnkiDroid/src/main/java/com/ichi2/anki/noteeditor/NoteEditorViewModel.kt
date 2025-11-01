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
            focusedFieldIndex = null,
            isTagsButtonEnabled = true,
            isCardsButtonEnabled = true
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

    private val _isFieldEdited = MutableStateFlow(false)
    val isFieldEdited: StateFlow<Boolean> = _isFieldEdited.asStateFlow()

    private val _currentNote = MutableStateFlow<Note?>(null)
    private val _currentCard = MutableStateFlow<Card?>(null)
    private val _deckId = MutableStateFlow<DeckId>(0L)

    /**
     * Initialize the editor with a new or existing note
     */
    fun initializeEditor(
        col: Collection,
        cardId: Long? = null,
        deckId: Long? = null,
        isAddingNote: Boolean = true,
        onComplete: (() -> Unit)? = null
    ) {
        viewModelScope.launch {
            try {
                // Load note and determine deck
                if (cardId != null && !isAddingNote) {
                    // Editing an existing card - use the card's deck
                    val card = col.getCard(cardId)
                    _currentCard.value = card
                    _currentNote.value = card.note(col)
                    _deckId.value = card.currentDeckId()
                } else {
                    // Adding a new note - use the provided deckId or calculate it
                    val notetype = col.notetypes.current()
                    _currentNote.value = Note.fromNotetypeId(col, notetype.id)
                    _deckId.value = calculateDeckIdForNewNote(col, deckId, notetype)
                }

                // Load available decks and note types
                _availableDecks.value = col.decks.allNamesAndIds().map { it.name }
                _availableNoteTypes.value = col.notetypes.all().map { it.name }

                // Update UI state
                updateStateFromNote(col, isAddingNote)
            } catch (e: Exception) {
                Timber.e(e, "Error initializing note editor")
            } finally {
                onComplete?.invoke()
            }
        }
    }
    
    /**
     * Calculate the deck ID for a new note based on preferences and context
     */
    private fun calculateDeckIdForNewNote(
        col: Collection,
        providedDeckId: Long?,
        notetype: NotetypeJson
    ): Long {
        // If a specific deck was provided and it's valid, use it
        if (providedDeckId != null && providedDeckId != 0L) {
            return providedDeckId
        }
        
        // Check if we should use the current deck or the note type's deck
        val useCurrentDeck = try {
            col.config.getBool(anki.config.ConfigKey.Bool.ADDING_DEFAULTS_TO_CURRENT_DECK)
        } catch (e: Exception) {
            Timber.w(e, "Error reading config, defaulting to current deck")
            true
        }
        
        if (!useCurrentDeck) {
            // Use the note type's default deck
            return notetype.did
        }
        
        // Use the current deck
        val currentDeckId = try {
            col.config.get(com.ichi2.anki.libanki.Decks.Companion.CURRENT_DECK) ?: 1L
        } catch (e: Exception) {
            Timber.w(e, "Error getting current deck, using default")
            1L
        }
        
        // If current deck is filtered, use default deck instead
        return if (col.decks.isFiltered(currentDeckId)) {
            1L
        } else {
            currentDeckId
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
        // Mark as edited whenever a field value changes
        _isFieldEdited.value = true
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
        viewModelScope.launch {
            try {
                val col = CollectionManager.getColUnsafe()
                val deck = col.decks.allNamesAndIds().find { it.name == deckName }
                if (deck != null) {
                    _deckId.value = deck.id
                    _noteEditorState.update { it.copy(selectedDeckName = deckName) }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error selecting deck")
            }
        }
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
                    // Capture existing note to preserve matching field values
                    val oldNote = _currentNote.value
                    val newNote = Note.fromNotetypeId(col, notetype.id)
                    
                    // Copy field values from old note to new note where field names match
                    if (oldNote != null) {
                        val oldNotetype = oldNote.notetype
                        oldNotetype.fields.forEachIndexed { oldIndex, oldField ->
                            if (oldIndex < oldNote.fields.size) {
                                val oldValue = oldNote.fields[oldIndex]
                                // Find matching field in new notetype
                                val newIndex = newNote.notetype.fields.indexOfFirst { it.name == oldField.name }
                                if (newIndex >= 0 && newIndex < newNote.fields.size) {
                                    newNote.fields[newIndex] = oldValue
                                }
                            }
                        }
                    }
                    
                    _currentNote.value = newNote
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
            val note = _currentNote.value ?: return false
            val currentCard = _currentCard.value

            // Update note fields from state
            val fields = _noteEditorState.value.fields
            fields.forEach { fieldState ->
                val fieldIndex = fieldState.index
                if (fieldIndex in note.fields.indices) {
                    note.fields[fieldIndex] = fieldState.value.text
                }
            }

            // Update tags
            note.setTagsFromStr(col, _noteEditorState.value.tags.joinToString(" "))

            // Save the note
            if (_noteEditorState.value.isAddingNote) {
                col.addNote(note, _deckId.value)
            } else {
                // When editing an existing card, check if deck changed
                if (currentCard != null && currentCard.currentDeckId() != _deckId.value) {
                    // Move card to new deck
                    col.setDeck(listOf(currentCard.id), _deckId.value)
                    // Refresh the card object to reflect database changes
                    currentCard.load(col)
                    // Update the cached card
                    _currentCard.value = currentCard
                    Timber.d("Card deck updated to %d", _deckId.value)
                }
                
                // Explicitly ignore OpChanges - UI updates happen through reactive state
                col.updateNote(note).let { }
            }

            true
        } catch (e: Exception) {
            Timber.e(e, "Error saving note")
            false
        }
    }

    fun formatSelection(prefix: String, suffix: String): Boolean {
        val targetIndex = determineFocusIndex() ?: return false
        val result = updateFieldValueInternal(targetIndex) { value ->
            val text = value.text
            val selection = value.selection
            val start = selection.start.coerceIn(0, text.length)
            val end = selection.end.coerceIn(0, text.length)
            val rangeStart = min(start, end)
            val rangeEnd = max(start, end)
            val before = text.take(rangeStart)
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
        if (result) {
            _isFieldEdited.value = true
        }
        return result
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
        val note = _currentNote.value ?: return
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
            if (_deckId.value == 0L) {
                // If deckId is not set, use the default deck
                col.decks.name(1L)
            } else {
                col.decks.name(_deckId.value)
            }
        } catch (e: Exception) {
            Timber.w(e, "Error getting deck name for deck ID ${_deckId.value}, using default deck")
            try {
                // Fall back to the default deck (ID 1)
                col.decks.name(1L)
            } catch (e2: Exception) {
                Timber.e(e2, "Error getting default deck name")
                "Default"
            }
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

    /**
     * Reset the field edited flag (e.g., after successful save)
     */
    fun resetFieldEditedFlag() {
        _isFieldEdited.value = false
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

    /**
     * Update the cards info display after template changes
     */
    fun updateCardsInfo(cardsInfo: String) {
        _noteEditorState.update { currentState ->
            currentState.copy(cardsInfo = cardsInfo)
        }
    }

    /**
     * Enable or disable the Tags button
     */
    fun setTagsButtonEnabled(enabled: Boolean) {
        _noteEditorState.update { currentState ->
            currentState.copy(isTagsButtonEnabled = enabled)
        }
    }

    /**
     * Enable or disable the Cards button
     */
    fun setCardsButtonEnabled(enabled: Boolean) {
        _noteEditorState.update { currentState ->
            currentState.copy(isCardsButtonEnabled = enabled)
        }
    }
}
