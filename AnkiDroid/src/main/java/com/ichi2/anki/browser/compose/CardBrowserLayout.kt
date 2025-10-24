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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ichi2.anki.R
import com.ichi2.anki.browser.BrowserRowWithId
import com.ichi2.anki.browser.CardBrowserViewModel
import com.ichi2.anki.noteeditor.compose.NoteEditor

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
                            Text(stringResource(R.string.card_browser_all_decks))
                            Icon(
                                Icons.Default.ArrowDropDown, contentDescription = "Select Deck"
                            )
                        }
                        DropdownMenu(
                            expanded = showDeckMenu, onDismissRequest = { showDeckMenu = false }) {
                            // TODO: Populate with decks from ViewModel
                        }
                    }
                }, navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Navigate Up"
                        )
                    }
                }, actions = {
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
                        expanded = showMoreMenu, onDismissRequest = { showMoreMenu = false }) {
                        DropdownMenuItem(text = { Text("Change display order") }, onClick = {
                            // TODO
                            showMoreMenu = false
                        })
                        DropdownMenuItem(text = { Text("Filter marked") }, onClick = {
                            // TODO
                            showMoreMenu = false
                        })
                        DropdownMenuItem(text = { Text("Filter suspended") }, onClick = {
                            // TODO
                            showMoreMenu = false
                        })
                        DropdownMenuItem(text = { Text("Filter by tag") }, onClick = {
                            // TODO
                            showMoreMenu = false
                        })
                        DropdownMenuItem(text = { Text("Filter by flag") }, onClick = {
                            // TODO: Nested menu
                            showMoreMenu = false
                        })
                        DropdownMenuItem(text = { Text("Preview") }, onClick = {
                            onPreview()
                            showMoreMenu = false
                        })
                        DropdownMenuItem(text = { Text("Select all") }, onClick = {
                            // TODO
                            showMoreMenu = false
                        })
                        DropdownMenuItem(text = { Text("Options") }, onClick = {
                            // TODO
                            showMoreMenu = false
                        })
                        DropdownMenuItem(text = { Text("Create Filtered Deck...") }, onClick = {
                            // TODO
                            showMoreMenu = false
                        })
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
