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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.*
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CardBrowserLayout(
    viewModel: CardBrowserViewModel,
    onNavigateUp: () -> Unit,
    onCardClicked: (BrowserRowWithId) -> Unit,
    onAddNote: () -> Unit,
    onPreview: () -> Unit
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
    var showMoreMenu by remember { mutableStateOf(false) }
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
                TopAppBar(
                    title = {
                        TextField(
                            value = searchQuery,
                            onValueChange = { query -> viewModel.setSearchQuery(query) },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Search") },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(onSearch = { viewModel.search(searchQuery) })
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { isSearchOpen = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close Search")
                        }
                    }
                )
            } else {
                TopAppBar(
                    title = {
                        Row {
                            TextButton(onClick = { showDeckMenu = true }) {
                                val selectedDeck by viewModel.flowOfDeckSelection.collectAsStateWithLifecycle(null)
                                val deckName = when (val deck = selectedDeck) {
                                    is SelectableDeck.Deck -> deck.name
                                    else -> stringResource(R.string.card_browser_all_decks)
                                }
                                Text(deckName)
                                Icon(
                                    Icons.Default.ArrowDropDown,
                                    contentDescription = "Select Deck"
                                )
                            }
                            DropdownMenu(
                                expanded = showDeckMenu,
                                onDismissRequest = { showDeckMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.card_browser_all_decks)) },
                                    onClick = {
                                        coroutineScope.launch {
                                            viewModel.setSelectedDeck(SelectableDeck.AllDecks)
                                        }
                                        showDeckMenu = false
                                    }
                                )
                                DeckHierarchyMenu(
                                    deckHierarchy = deckHierarchy,
                                    expandedDecks = expandedDecks,
                                    onDeckSelected = { deck ->
                                        coroutineScope.launch {
                                            viewModel.setSelectedDeck(deck)
                                        }
                                        showDeckMenu = false
                                    }
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateUp) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Navigate Up"
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { isSearchOpen = true }) {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                        }
                        IconButton(onClick = onAddNote) {
                            Icon(Icons.Default.Add, contentDescription = "Add Note")
                        }
                        IconButton(onClick = { showMoreMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More Options")
                        }
                        DropdownMenu(
                            expanded = showMoreMenu,
                            onDismissRequest = { showMoreMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Change display order") },
                                onClick = {
                                    // TODO
                                    showMoreMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Filter marked") },
                                onClick = {
                                    // TODO
                                    showMoreMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Filter suspended") },
                                onClick = {
                                    // TODO
                                    showMoreMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Filter by tag") },
                                onClick = {
                                    // TODO
                                    showMoreMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Filter by flag") },
                                onClick = {
                                    // TODO: Nested menu
                                    showMoreMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Preview") },
                                onClick = {
                                    onPreview()
                                    showMoreMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Select all") },
                                onClick = {
                                    // TODO
                                    showMoreMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Options") },
                                onClick = {
                                    // TODO
                                    showMoreMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Create Filtered Deck...") },
                                onClick = {
                                    // TODO
                                    showMoreMenu = false
                                }
                            )
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        if (isTablet) {
            Row(
                Modifier.padding(paddingValues)
            ) {
                CardBrowserScreen(
                    viewModel = viewModel,
                    onCardClicked = onCardClicked,
                    modifier = Modifier.weight(1f)
                )
                NoteEditor(
                    modifier = Modifier.weight(1f)
                )
            }
        } else {
            CardBrowserScreen(
                viewModel = viewModel,
                onCardClicked = onCardClicked,
                modifier = Modifier.padding(paddingValues)
            )
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
            text = {
                Row {
                    if (hasChildren) {
                        Icon(
                            if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Expand"
                        )
                    }
                    Text(deck.name.substringAfterLast("::"))
                }
            },
            onClick = {
                if (hasChildren) {
                    expandedDecks[deck.name] = !isExpanded
                } else {
                    onDeckSelected(deck)
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
