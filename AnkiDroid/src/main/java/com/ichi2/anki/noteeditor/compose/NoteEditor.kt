/* **************************************************************************************
 * Copyright (c) 2009 Andrew Dubya <andrewdubya@gmail.com>                              *
 * Copyright (c) 2009 Nicolas Raoul <nicolas.raoul@gmail.com>                           *
 * Copyright (c) 2009 Edu Zamora <edu.zasu@gmail.com>                                   *
 * Copyright (c) 2009 Daniel Svard <daniel.svard@gmail.com>                             *
 * Copyright (c) 2010 Norbert Nagold <norbert.nagold@gmail.com>                         *
 * Copyright (c) 2014 Timothy Rae <perceptualchaos2@gmail.com>
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
package com.ichi2.anki.noteeditor.compose

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Attachment
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExposedDropdownMenuAnchorType.Companion.PrimaryNotEditable
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ichi2.anki.R
import com.ichi2.anki.noteeditor.ToolbarButtonModel

/**
 * Data class representing the state of a note editor field
 */
data class NoteFieldState(
    val name: String,
    val value: TextFieldValue,
    val isSticky: Boolean = false,
    val hint: String = "",
    val index: Int
)

/**
 * Data class representing the complete note editor state
 */
data class NoteEditorState(
    val fields: List<NoteFieldState>,
    val tags: List<String>,
    val selectedDeckName: String,
    val selectedNoteTypeName: String,
    val isAddingNote: Boolean = true,
    val isClozeType: Boolean = false,
    val isImageOcclusion: Boolean = false,
    val cardsInfo: String = "",
    val focusedFieldIndex: Int? = null,
    val isTagsButtonEnabled: Boolean = true,
    val isCardsButtonEnabled: Boolean = true
)

/**
 * Main Note Editor Screen with Material 3 Components
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorScreen(
    modifier: Modifier = Modifier,
    state: NoteEditorState,
    availableDecks: List<String>,
    availableNoteTypes: List<String>,
    onFieldValueChange: (Int, TextFieldValue) -> Unit,
    onFieldFocus: (Int) -> Unit,
    onTagsClick: () -> Unit,
    onCardsClick: () -> Unit,
    onDeckSelected: (String) -> Unit,
    onNoteTypeSelected: (String) -> Unit,
    onMultimediaClick: (Int) -> Unit,
    onToggleStickyClick: (Int) -> Unit,
    onSaveClick: () -> Unit,
    onPreviewClick: () -> Unit,
    onBoldClick: () -> Unit,
    onItalicClick: () -> Unit,
    onUnderlineClick: () -> Unit,
    onHorizontalRuleClick: () -> Unit = {},
    onHeadingClick: () -> Unit = {},
    onFontSizeClick: () -> Unit = {},
    onMathjaxClick: () -> Unit = {},
    onMathjaxLongClick: (() -> Unit)? = null,
    onClozeClick: () -> Unit,
    onClozeIncrementClick: () -> Unit,
    onCustomButtonClick: (ToolbarButtonModel) -> Unit,
    onCustomButtonLongClick: (ToolbarButtonModel) -> Unit,
    onAddCustomButtonClick: () -> Unit,
    customToolbarButtons: List<ToolbarButtonModel>,
    isToolbarVisible: Boolean,
    onImageOcclusionSelectImage: () -> Unit = {},
    onImageOcclusionPasteImage: () -> Unit = {},
    onImageOcclusionEdit: () -> Unit = {},
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    topBar: (@Composable () -> Unit)? = null,
) {
    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp),
        topBar = {
            topBar?.invoke()
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                )
            }
        },
        bottomBar = {
            NoteEditorToolbar(
                isClozeType = state.isClozeType,
                onBoldClick = onBoldClick,
                onItalicClick = onItalicClick,
                onUnderlineClick = onUnderlineClick,
                onHorizontalRuleClick = onHorizontalRuleClick,
                onHeadingClick = onHeadingClick,
                onFontSizeClick = onFontSizeClick,
                onMathjaxClick = onMathjaxClick,
                onMathjaxLongClick = onMathjaxLongClick,
                onClozeClick = onClozeClick,
                onClozeIncrementClick = onClozeIncrementClick,
                onCustomButtonClick = onCustomButtonClick,
                onCustomButtonLongClick = onCustomButtonLongClick,
                onAddCustomButtonClick = onAddCustomButtonClick,
                customButtons = customToolbarButtons,
                isVisible = isToolbarVisible
            )
        }) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Note Type Selector
            NoteTypeSelector(
                selectedNoteType = state.selectedNoteTypeName,
                availableNoteTypes = availableNoteTypes,
                onNoteTypeSelected = onNoteTypeSelected
            )

            // Deck Selector
            DeckSelector(
                selectedDeck = state.selectedDeckName,
                availableDecks = availableDecks,
                onDeckSelected = onDeckSelected
            )

            // Fields Editor
            state.fields.forEach { field ->
                NoteFieldEditor(
                    field = field,
                    onValueChange = { newValue ->
                        onFieldValueChange(field.index, newValue)
                    },
                    onMultimediaClick = { onMultimediaClick(field.index) },
                    onToggleStickyClick = { onToggleStickyClick(field.index) },
                    showStickyButton = state.isAddingNote,
                    onFocus = { onFieldFocus(field.index) },
                    isFocused = state.focusedFieldIndex == field.index
                )
            }

            // Image Occlusion Buttons (if applicable)
            if (state.isImageOcclusion && state.isAddingNote) {
                ImageOcclusionButtons(
                    onSelectImage = onImageOcclusionSelectImage,
                    onPasteImage = onImageOcclusionPasteImage
                )
            } else if (state.isImageOcclusion && !state.isAddingNote) {
                Button(
                    onClick = onImageOcclusionEdit, modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.edit_occlusions))
                }
            }

            // Tags Button
            Button(
                onClick = onTagsClick,
                modifier = Modifier.fillMaxWidth(),
                enabled = state.isTagsButtonEnabled
            ) {
                Text(
                    text = if (state.tags.isEmpty()) {
                        stringResource(R.string.add_tag)
                    } else {
                        "Tags: ${state.tags.joinToString(", ")}"
                    }
                )
            }

            // Cards Button
            Button(
                onClick = onCardsClick,
                modifier = Modifier.fillMaxWidth(),
                enabled = state.isCardsButtonEnabled
            ) {
                Text(state.cardsInfo.ifEmpty { stringResource(R.string.CardEditorCards) })
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * Note Type Selector Dropdown
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun NoteTypeSelector(
    selectedNoteType: String,
    availableNoteTypes: List<String>,
    onNoteTypeSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedNoteType,
            label = { Text(stringResource(R.string.CardEditorModel)) },
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor(
                    type = PrimaryNotEditable, enabled = true
                )
                .fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            )
        )
        ExposedDropdownMenu(
            expanded = expanded, onDismissRequest = { expanded = false }) {
            availableNoteTypes.forEach { noteType ->
                DropdownMenuItem(text = { Text(noteType) }, onClick = {
                    onNoteTypeSelected(noteType)
                    expanded = false
                })
            }
        }
    }
}

/**
 * Deck Selector Dropdown
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeckSelector(
    selectedDeck: String,
    availableDecks: List<String>,
    onDeckSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedDeck,
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.CardEditorNoteDeck)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor(
                    type = PrimaryNotEditable, enabled = true
                )
                .fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            )
        )
        ExposedDropdownMenu(
            expanded = expanded, onDismissRequest = { expanded = false }) {
            availableDecks.forEach { deck ->
                DropdownMenuItem(text = { Text(deck) }, onClick = {
                    onDeckSelected(deck)
                    expanded = false
                })
            }
        }
    }
}

/**
 * Individual Field Editor with multimedia and sticky support
 */
@Composable
fun NoteFieldEditor(
    field: NoteFieldState,
    onValueChange: (TextFieldValue) -> Unit,
    onMultimediaClick: () -> Unit,
    onToggleStickyClick: () -> Unit,
    showStickyButton: Boolean,
    onFocus: () -> Unit,
    isFocused: Boolean,
    modifier: Modifier = Modifier
) {
    val defaultContainerColor = MaterialTheme.colorScheme.surfaceVariant
    val focusedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
    val containerColor by animateColorAsState(
        targetValue = if (isFocused) focusedContainerColor else defaultContainerColor,
        label = "noteFieldBackground"
    )

    Card(
        modifier = modifier.fillMaxWidth(), colors = CardDefaults.cardColors(
            containerColor = containerColor
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = field.name,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Row {
                    IconButton(onClick = onMultimediaClick) {
                        Icon(
                            imageVector = Icons.Default.Attachment,
                            contentDescription = "Add multimedia",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (showStickyButton) {
                        IconButton(onClick = onToggleStickyClick) {
                            Icon(
                                imageVector = Icons.Default.PushPin,
                                contentDescription = "Toggle sticky",
                                tint = if (field.isSticky) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                }
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = field.value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { focusState ->
                        if (focusState.isFocused) {
                            onFocus()
                        }
                    },
                placeholder = { Text(field.hint) },
                minLines = 2,
                maxLines = 10,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    }
}

/**
 * Image Occlusion specific buttons
 */
@Composable
fun ImageOcclusionButtons(
    onSelectImage: () -> Unit, onPasteImage: () -> Unit, modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = onSelectImage, modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.AddAPhoto,
                contentDescription = null,
                modifier = Modifier.padding(end = 8.dp)
            )
            Text(stringResource(R.string.choose_an_image))
        }
        Button(
            onClick = onPasteImage, modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.ContentPaste,
                contentDescription = null,
                modifier = Modifier.padding(end = 8.dp)
            )
            Text(stringResource(R.string.paste_image_from_clipboard))
        }
    }
}

/**
 * Preview for development
 */
@Preview(showBackground = true)
@Composable
fun NoteEditorScreenPreview() {
    MaterialTheme {
        NoteEditorScreen(
            state = NoteEditorState(
                fields = listOf(
                    NoteFieldState(
                        name = "Front", value = TextFieldValue("Sample front text"), index = 0
                    ), NoteFieldState(
                        name = "Back", value = TextFieldValue("Sample back text"), index = 1
                    )
                ),
                tags = listOf("Tag1", "Tag2"),
                selectedDeckName = "Default",
                selectedNoteTypeName = "Basic",
                cardsInfo = "Cards: 1"
            ),
            availableDecks = listOf("Default", "Deck 2", "Deck 3"),
            availableNoteTypes = listOf("Basic", "Basic (and reversed card)", "Cloze"),
            onFieldValueChange = { _, _ -> },
            onFieldFocus = {},
            onTagsClick = { },
            onCardsClick = { },
            onDeckSelected = { },
            onNoteTypeSelected = { },
            onMultimediaClick = { },
            onToggleStickyClick = { },
            onSaveClick = { },
            onPreviewClick = { },
            onBoldClick = {},
            onItalicClick = {},
            onUnderlineClick = {},
            onHorizontalRuleClick = {},
            onHeadingClick = {},
            onFontSizeClick = {},
            onMathjaxClick = {},
            onMathjaxLongClick = {},
            onClozeClick = {},
            onClozeIncrementClick = {},
            onCustomButtonClick = {},
            onCustomButtonLongClick = {},
            onAddCustomButtonClick = {},
            customToolbarButtons = emptyList(),
            isToolbarVisible = true
        )
    }
}
