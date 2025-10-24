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
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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

    Scaffold(
        topBar = {
            if (isSearchOpen) {
                val onActiveChange = { active: Boolean -> isSearchOpen = active }
                val colors1 = SearchBarDefaults.colors()
                SearchBar(
                    inputField = {
                        SearchBarDefaults.InputField(
                            query = searchQuery,
                            onQueryChange = { query -> viewModel.setSearchQuery(query) },
                            onSearch = { query -> viewModel.search(query) },
                            expanded = isSearchOpen,
                            onExpandedChange = onActiveChange,
                            colors = colors1.inputFieldColors,
                        )
                    },
                    expanded = isSearchOpen,
                    onExpandedChange = onActiveChange,
                    shape = SearchBarDefaults.inputFieldShape,
                    colors = colors1,
                    tonalElevation = SearchBarDefaults.TonalElevation,
                    shadowElevation = SearchBarDefaults.ShadowElevation,
                    windowInsets = SearchBarDefaults.windowInsets,
                    content = { },
                )
            } else {
                TopAppBar(title = { Text("Card Browser") }, navigationIcon = {
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
                    IconButton(onClick = { viewModel.undo() }) {
                        Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = "Undo")
                    }
                    IconButton(onClick = onAddNote) {
                        Icon(Icons.Default.Add, contentDescription = "Add Note")
                    }
                    IconButton(onClick = { showMoreMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More Options")
                    }
                    DropdownMenu(
                        expanded = showMoreMenu, onDismissRequest = { showMoreMenu = false }) {
                        DropdownMenuItem(text = { Text("Preview") }, onClick = {
                            onPreview()
                            showMoreMenu = false
                        })
                        DropdownMenuItem(text = { Text("Card Info") }, onClick = {
                            // TODO: Implement Card Info
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
