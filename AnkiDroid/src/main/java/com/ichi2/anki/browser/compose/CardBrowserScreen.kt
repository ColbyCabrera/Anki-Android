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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.FloatingToolbarDefaults.ScreenOffset
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import anki.search.BrowserRow
import com.ichi2.anki.Flag
import com.ichi2.anki.R
import com.ichi2.anki.browser.BrowserRowWithId
import com.ichi2.anki.browser.CardBrowserViewModel
import com.ichi2.anki.browser.ColumnHeading
import com.ichi2.anki.model.SortType
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CardBrowserScreen(
    viewModel: CardBrowserViewModel,
    onCardClicked: (BrowserRowWithId) -> Unit,
    modifier: Modifier = Modifier,
    onAddNote: () -> Unit,
    onPreview: () -> Unit,
    onFilter: (String) -> Unit,
    onSelectAll: () -> Unit,
    onOptions: () -> Unit,
    onCreateFilteredDeck: () -> Unit
) {
    val browserRows by viewModel.browserRows.collectAsStateWithLifecycle()
    val columnHeadings by viewModel.flowOfColumnHeadings.collectAsStateWithLifecycle(initialValue = emptyList())
    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var showSortMenu by remember { mutableStateOf(false) }
    var showFlagMenu by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        Column(modifier = modifier) {
            CardBrowserHeader(columns = columnHeadings)
            HorizontalDivider()
            if (browserRows.isEmpty()) {
                EmptyCardBrowser()
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(
                        items = browserRows, key = { it.id }) { row ->
                        CardBrowserRow(
                            row = row.browserRow,
                            columns = columnHeadings,
                            modifier = Modifier.clickable { onCardClicked(row) })
                        HorizontalDivider()
                    }
                }
            }
        }

        HorizontalFloatingToolbar(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = -ScreenOffset - 16.dp),
            expanded = true,
            floatingActionButton = {
                FloatingToolbarDefaults.VibrantFloatingActionButton(onClick = onAddNote) {
                    Icon(
                        painter = painterResource(R.drawable.add_24px),
                        contentDescription = stringResource(R.string.add_card)
                    )
                }
            },
            colors = FloatingToolbarDefaults.vibrantFloatingToolbarColors(),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onPreview,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.preview_24px),
                        contentDescription = stringResource(R.string.more_options)
                    )
                }
                IconButton(
                    onClick = onSelectAll,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.select_all_24px),
                        contentDescription = stringResource(R.string.more_options)
                    )
                }
                IconButton(
                    onClick = { showBottomSheet = true },
                ) {
                    Icon(
                        painter = painterResource(R.drawable.filter_alt_24px),
                        contentDescription = stringResource(R.string.filter)
                    )
                }
                IconButton(
                    onClick = onOptions
                ) {
                    Icon(
                        painter = painterResource(R.drawable.tune_24px),
                        contentDescription = stringResource(R.string.more_options)
                    )
                }
                IconButton(
                    onClick = ,
                ) {
                    Icon(
                        Icons.Filled.MoreVert,
                        contentDescription = stringResource(R.string.more_options)
                    )
                }

            }
        }
        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = {
                    showBottomSheet = false
                },
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
            ) {
                ListItem(
                    headlineContent = { Text(stringResource(R.string.card_browser_change_display_order)) },
                    modifier = Modifier.clickable {
                        showSortMenu = true
                    })
                if (showSortMenu) {
                    SelectableSortOrder(
                        viewModel = viewModel, onDismiss = {
                            showSortMenu = false
                        })
                }
                ListItem(
                    headlineContent = { Text("Filter marked") },
                    modifier = Modifier.clickable {
                        onFilter("tag:marked")
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            if (!sheetState.isVisible) {
                                showBottomSheet = false
                            }
                        }
                    })
                ListItem(
                    headlineContent = { Text("Filter suspended") },
                    modifier = Modifier.clickable {
                        onFilter("is:suspended")
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            if (!sheetState.isVisible) {
                                showBottomSheet = false
                            }
                        }
                    })
                ListItem(
                    headlineContent = { Text("Filter by tag") },
                    modifier = Modifier.clickable {
                        // TODO
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            if (!sheetState.isVisible) {
                                showBottomSheet = false
                            }
                        }
                    })
                ListItem(
                    headlineContent = { Text("Filter by flag") },
                    modifier = Modifier.clickable { showFlagMenu = true })
                if (showFlagMenu) {
                    var flagLabels by remember { mutableStateOf<Map<Flag, String>>(emptyMap()) }
                    LaunchedEffect(true) {
                        if (showFlagMenu) {
                            flagLabels = Flag.queryDisplayNames()
                        }
                    }
                    DropdownMenu(expanded = true, onDismissRequest = { showFlagMenu = false }) {
                        Flag.entries.filter { it != Flag.NONE }.forEach { flag ->
                            DropdownMenuItem(text = { Text(flagLabels[flag] ?: "") }, leadingIcon = {
                                Icon(
                                    painter = painterResource(id = flag.drawableRes),
                                    contentDescription = "Filter by flag"
                                )
                            }, onClick = {
                                onFilter("flag:${flag.code}")
                                scope.launch { sheetState.hide() }.invokeOnCompletion {
                                    if (!sheetState.isVisible) {
                                        showBottomSheet = false
                                    }
                                }
                            })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SelectableSortOrder(viewModel: CardBrowserViewModel, onDismiss: () -> Unit) {
    val currentSortType = viewModel.order
    val sortLabels = stringArrayResource(id = R.array.card_browser_order_labels)
    DropdownMenu(expanded = true, onDismissRequest = onDismiss) {
        SortType.entries.forEach { sortType ->
            DropdownMenuItem(text = {
                Row {
                    RadioButton(
                        selected = currentSortType == sortType, onClick = {
                            viewModel.changeCardOrder(sortType)
                            onDismiss()
                        })
                    Text(text = sortLabels[sortType.cardBrowserLabelIndex])
                }
            }, onClick = {
                viewModel.changeCardOrder(sortType)
                onDismiss()
            })
        }
    }
}

@Composable
fun EmptyCardBrowser(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CardBrowserEmpty()
    }
}

@Composable
fun CardBrowserEmpty(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(id = R.string.card_browser_no_cards_in_deck), modifier = modifier
    )
}

@Composable
fun CardBrowserHeader(columns: List<ColumnHeading>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        columns.forEach { column ->
            Text(
                text = column.label,
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.weight(1f) // Basic weighting
            )
        }
    }
}

@Composable
fun CardBrowserRow(
    row: BrowserRow, columns: List<ColumnHeading>, modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // This is a simplified representation. The original adapter handles HTML.
        // For a full conversion, a composable that can render HTML would be needed.
        row.cellsList.forEach { cell ->
            Text(
                text = cell.text,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                modifier = Modifier.weight(1f) // Basic weighting
            )
        }
    }
}