/***************************************************************************************
 * Copyright (c) 2022 Ankitects Pty Ltd <https://apps.ankiweb.net>                      *
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

package com.ichi2.anki.browser.compose

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ichi2.anki.R
import com.ichi2.anki.browser.BrowserRowWithId
import com.ichi2.anki.browser.CardBrowserViewModel
import com.ichi2.anki.model.SelectableDeck
// TODO: Re-enable NoteEditor in split view after migration is complete
// import com.ichi2.anki.noteeditor.compose.NoteEditor
import kotlinx.coroutines.launch

@OptIn(
    ExperimentalMaterial3WindowSizeClassApi::class,
    ExperimentalMaterial3Api::class,
    ExperimentalMaterial3ExpressiveApi::class
)
@Composable
fun CardBrowserLayout(
    viewModel: CardBrowserViewModel,
    onNavigateUp: () -> Unit,
    onCardClicked: (BrowserRowWithId) -> Unit,
    onAddNote: () -> Unit,
    onPreview: () -> Unit,
    onFilter: (String) -> Unit,
    onSelectAll: () -> Unit,
    onOptions: () -> Unit,
    onCreateFilteredDeck: () -> Unit,
    onEditNote: () -> Unit,
    onCardInfo: () -> Unit,
    onChangeDeck: () -> Unit,
    onReposition: () -> Unit,
    onSetDueDate: () -> Unit,
    onEditTags: () -> Unit,
    onGradeNow: () -> Unit,
    onResetProgress: () -> Unit,
    onExportCard: () -> Unit,
    onFilterByTag: () -> Unit
) {
    val activity = LocalActivity.current
    val isTablet = if (activity != null) {
        val windowSizeClass = calculateWindowSizeClass(activity)
        windowSizeClass.widthSizeClass > WindowWidthSizeClass.Compact
    } else {
        false
    }
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    var isSearchOpen by remember { mutableStateOf(false) }
    var showDeckMenu by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    var availableDecks by remember { mutableStateOf<List<SelectableDeck.Deck>>(emptyList()) }

    // Ensure dropdown is dismissed when composable is disposed
    DisposableEffect(Unit) {
        onDispose {
            showDeckMenu = false
        }
    }

    LaunchedEffect(Unit) {
        availableDecks = viewModel.getAvailableDecks()
    }

    val deckHierarchy = remember(availableDecks) {
        buildDeckHierarchy(availableDecks)
    }

    val expandedDecks = remember { mutableStateMapOf<String, Boolean>() }

    Scaffold(
        topBar = {
            if (isSearchOpen) {
                TopAppBar(title = {
                    TextField(
                        value = searchQuery,
                        onValueChange = { query -> viewModel.setSearchQuery(query) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text(text = stringResource(R.string.card_browser_search_hint)) },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = {
                            viewModel.search(searchQuery)
                        })
                    )
                }, navigationIcon = {
                    IconButton(onClick = { isSearchOpen = false }) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = stringResource(R.string.close)
                        )
                    }
                })
            } else {
                TopAppBar(title = {
                    Row {
                        TextButton(onClick = { showDeckMenu = true }) {
                            val selectedDeck by viewModel.flowOfDeckSelection.collectAsStateWithLifecycle(
                                null
                            )
                            val deckName = when (val deck = selectedDeck) {
                                is SelectableDeck.Deck -> deck.name
                                else -> stringResource(R.string.card_browser_all_decks)
                            }
                            Text(text = deckName, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Icon(
                                Icons.Default.ArrowDropDown,
                                contentDescription = stringResource(R.string.select_deck)
                            )
                        }
                        DropdownMenu(
                            expanded = showDeckMenu,
                            onDismissRequest = { showDeckMenu = false },
                            shape = MaterialTheme.shapes.large
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.card_browser_all_decks)) },
                                onClick = {
                                    coroutineScope.launch {
                                        viewModel.setSelectedDeck(SelectableDeck.AllDecks)
                                    }
                                    showDeckMenu = false
                                })
                            DeckHierarchyMenu(
                                deckHierarchy = deckHierarchy,
                                expandedDecks = expandedDecks,
                                onDeckSelected = { deck ->
                                    coroutineScope.launch {
                                        viewModel.setSelectedDeck(deck)
                                    }
                                    showDeckMenu = false
                                })
                        }
                    }
                }, navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(
                                R.string.back
                            )
                        )
                    }
                }, actions = {
                    IconButton(onClick = { isSearchOpen = true }) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = stringResource(R.string.card_browser_search_hint)
                        )
                    }
                })
            }
        }) { paddingValues ->
        if (isTablet) {
            Row(
                Modifier.padding(paddingValues)
            ) {
                CardBrowserScreen(
                    viewModel = viewModel,
                    onCardClicked = onCardClicked,
                    modifier = Modifier.weight(1f),
                    onAddNote = onAddNote,
                    onPreview = onPreview,
                    onFilter = onFilter,
                    onSelectAll = onSelectAll,
                    onOptions = onOptions,
                    onCreateFilteredDeck = onCreateFilteredDeck,
                    onEditNote = onEditNote,
                    onCardInfo = onCardInfo,
                    onChangeDeck = onChangeDeck,
                    onReposition = onReposition,
                    onSetDueDate = onSetDueDate,
                    onEditTags = onEditTags,
                    onGradeNow = onGradeNow,
                    onResetProgress = onResetProgress,
                    onExportCard = onExportCard,
                    onFilterByTag = onFilterByTag
                )
                // TODO: Re-enable NoteEditor split view after migration is complete
                // NoteEditor(
                //     modifier = Modifier.weight(1f)
                // )
            }
        } else {
            Row(
                modifier = Modifier.padding(
                    top = paddingValues.calculateTopPadding(),
                    bottom = 0.dp
                )
            ) {
                CardBrowserScreen(
                    viewModel = viewModel,
                    onCardClicked = onCardClicked,
                    onAddNote = onAddNote,
                    onPreview = onPreview,
                    onFilter = onFilter,
                    onSelectAll = onSelectAll,
                    onOptions = onOptions,
                    onCreateFilteredDeck = onCreateFilteredDeck,
                    onEditNote = onEditNote,
                    onCardInfo = onCardInfo,
                    onChangeDeck = onChangeDeck,
                    onReposition = onReposition,
                    onSetDueDate = onSetDueDate,
                    onEditTags = onEditTags,
                    onGradeNow = onGradeNow,
                    onResetProgress = onResetProgress,
                    onExportCard = onExportCard,
                    onFilterByTag = onFilterByTag
                )
            }
        }
    }
}

private fun buildDeckHierarchy(decks: List<SelectableDeck.Deck>): Map<String, List<SelectableDeck.Deck>> {
    val hierarchy = mutableMapOf<String, MutableList<SelectableDeck.Deck>>()
    val topLevelDecks = mutableListOf<SelectableDeck.Deck>()

    for (deck in decks) {
        val parts = deck.name.split("::")
        if (parts.size > 1) {
            val parentName = parts.dropLast(1).joinToString("::")
            hierarchy.getOrPut(parentName) { mutableListOf() }.add(deck)
        } else {
            topLevelDecks.add(deck)
        }
    }

    hierarchy[""] = topLevelDecks
    return hierarchy
}

@Composable
private fun DeckHierarchyMenu(
    deckHierarchy: Map<String, List<SelectableDeck.Deck>>,
    expandedDecks: MutableMap<String, Boolean>,
    onDeckSelected: (SelectableDeck.Deck) -> Unit,
    parentName: String = ""
) {
    val children = deckHierarchy[parentName] ?: return

    for (deck in children) {
        val isExpanded = expandedDecks[deck.name] ?: false
        val hasChildren = deckHierarchy.containsKey(deck.name)

        DropdownMenuItem(
            text = { Text(deck.name.substringAfterLast("::")) },
            onClick = { onDeckSelected(deck) },
            trailingIcon = {
                if (hasChildren) {
                    IconButton(onClick = { expandedDecks[deck.name] = !isExpanded }) {
                        Icon(
                            painter = if (isExpanded) painterResource(R.drawable.keyboard_arrow_right_24px)
                            else painterResource(
                                R.drawable.keyboard_arrow_down_24px
                            ), contentDescription = "Expand"
                        )
                    }
                }
            }
        )
        if (isExpanded && hasChildren) {
            Column(modifier = Modifier.padding(start = 16.dp)) {
                DeckHierarchyMenu(deckHierarchy, expandedDecks, onDeckSelected, deck.name)
            }
        }
    }
}
