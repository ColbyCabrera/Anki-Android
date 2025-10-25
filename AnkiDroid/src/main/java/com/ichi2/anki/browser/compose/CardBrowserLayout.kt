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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ichi2.anki.R
import com.ichi2.anki.browser.BrowserRowWithId
import com.ichi2.anki.browser.CardBrowserViewModel
import com.ichi2.anki.model.SelectableDeck
import com.ichi2.anki.noteeditor.compose.NoteEditor
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
    onCreateFilteredDeck: () -> Unit
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
                        placeholder = { Text("Search") },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = {
                            viewModel.search(searchQuery)
                        })
                    )
                }, navigationIcon = {
                    IconButton(onClick = { isSearchOpen = false }) {
                        Icon(Icons.Default.Close, contentDescription = "Close Search")
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
                            Text(deckName)
                            Icon(
                                Icons.Default.ArrowDropDown, contentDescription = "Select Deck"
                            )
                        }
                        DropdownMenu(
                            expanded = showDeckMenu, onDismissRequest = { showDeckMenu = false }) {
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
                            Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Navigate Up"
                        )
                    }
                }, actions = {
                    IconButton(onClick = { isSearchOpen = true }) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
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
                    onCreateFilteredDeck = onCreateFilteredDeck
                )
                NoteEditor(
                    modifier = Modifier.weight(1f)
                )
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
                    onCreateFilteredDeck = onCreateFilteredDeck
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

        DropdownMenuItem(text = {
            Row {
                if (hasChildren) {
                    Icon(
                        if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Expand"
                    )
                }
                Text(deck.name.substringAfterLast("::"))
            }
        }, onClick = {
            if (hasChildren) {
                expandedDecks[deck.name] = !isExpanded
            } else {
                onDeckSelected(deck)
            }
        })
        if (isExpanded && hasChildren) {
            Column(modifier = Modifier.padding(start = 16.dp)) {
                DeckHierarchyMenu(deckHierarchy, expandedDecks, onDeckSelected, deck.name)
            }
        }
    }
}
