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
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.material3.FloatingToolbarDefaults.ScreenOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
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
import com.ichi2.anki.browser.CardOrNoteId
import com.ichi2.anki.browser.ColumnHeading
import com.ichi2.anki.model.SortType
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
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
    val columnHeadings by viewModel.flowOfColumnHeadings.collectAsStateWithLifecycle()
    val selectedRows by viewModel.flowOfSelectedRows.collectAsStateWithLifecycle(initialValue = emptySet())
    val hasSelection by viewModel.flowOfSelectedRows.map { it.isNotEmpty() }.collectAsStateWithLifecycle(initialValue = false)
    var showFilterSheet by remember { mutableStateOf(false) }
    var showMoreOptionsMenu by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }
    var showFlagMenu by remember { mutableStateOf(false) }
    var showSetFlagMenu by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        Column(modifier = modifier) {
            CardBrowserHeader(columns = columnHeadings)
            HorizontalDivider()
            if (browserRows.isEmpty()) {
                EmptyCardBrowser()
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(
                        items = browserRows, key = { it.id }
                    ) { row ->
                        CardBrowserRow(
                            row = row.browserRow,
                            isSelected = selectedRows.contains(CardOrNoteId(row.id)),
                            modifier = Modifier.combinedClickable(
                                onClick = { onCardClicked(row) },
                                onLongClick = {
                                    viewModel.handleRowLongPress(
                                        CardBrowserViewModel.RowSelection(
                                            rowId = CardOrNoteId(row.id),
                                            topOffset = 0
                                        )
                                    )
                                }
                            )
                        )
                        HorizontalDivider()
                    }
                }
            }
        }

        BrowserToolbar(
            onAddNote = onAddNote,
            onDeselect = { viewModel.deselectAll() },
            hasSelection = hasSelection,
            onPreview = onPreview,
            onSelectAll = onSelectAll,
            onFilter = { showFilterSheet = true },
            onMark = { viewModel.toggleMarkForSelectedRows() },
            onSetFlag = { showSetFlagMenu = true },
            onOptions = onOptions,
            onMoreOptions = { showMoreOptionsMenu = true },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = -ScreenOffset - 16.dp)
        )

        if (showFilterSheet) {
            FilterBottomSheet(
                onDismissRequest = { showFilterSheet = false },
                onFilter = {
                    onFilter(it)
                },
                onFlagFilter = {
                    showFlagMenu = true
                }
            )
        }

        if (showMoreOptionsMenu) {
            MoreOptionsBottomSheet(
                onDismissRequest = { showMoreOptionsMenu = false },
                onChangeDisplayOrder = {
                    showMoreOptionsMenu = false
                    showSortMenu = true
                },
                onCreateFilteredDeck = {
                    onCreateFilteredDeck()
                    showMoreOptionsMenu = false
                }
            )
        }

        if (showSortMenu) {
            SelectableSortOrderBottomSheet(
                viewModel = viewModel,
                onDismiss = { showSortMenu = false }
            )
        }

        if (showFlagMenu) {
            FlagFilterBottomSheet(
                onDismiss = { showFlagMenu = false },
                onFilter = {
                    onFilter(it)
                    showFlagMenu = false
                }
            )
        }

        if (showSetFlagMenu) {
            SetFlagBottomSheet(
                onDismiss = { showSetFlagMenu = false },
                onSetFlag = {
                    viewModel.setFlagForSelectedRows(it)
                    showSetFlagMenu = false
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BrowserToolbar(
    onAddNote: () -> Unit,
    onDeselect: () -> Unit,
    hasSelection: Boolean,
    onPreview: () -> Unit,
    onSelectAll: () -> Unit,
    onFilter: () -> Unit,
    onMark: () -> Unit,
    onSetFlag: () -> Unit,
    onOptions: () -> Unit,
    onMoreOptions: () -> Unit,
    modifier: Modifier = Modifier
) {
    HorizontalFloatingToolbar(
        modifier = modifier,
        expanded = true,
        floatingActionButton = {
            FloatingToolbarDefaults.VibrantFloatingActionButton(
                onClick = if (hasSelection) onDeselect else onAddNote,
                shape = FloatingActionButtonDefaults.smallShape,
            ) {
                if (hasSelection) {
                    Icon(
                        painter = painterResource(R.drawable.deselect_24px),
                        contentDescription = stringResource(R.string.card_browser_deselect_all)
                    )
                } else {
                    Icon(
                        painter = painterResource(R.drawable.add_24px),
                        contentDescription = stringResource(R.string.add_card)
                    )
                }
            }
        },
        colors = FloatingToolbarDefaults.vibrantFloatingToolbarColors(),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onPreview) {
                Icon(
                    painter = painterResource(R.drawable.preview_24px),
                    contentDescription = stringResource(R.string.more_options)
                )
            }
            IconButton(onClick = onSelectAll) {
                Icon(
                    painter = painterResource(R.drawable.select_all_24px),
                    contentDescription = stringResource(R.string.more_options)
                )
            }
            if (hasSelection) {
                IconButton(onClick = onMark) {
                    Icon(
                        painter = painterResource(R.drawable.star_24px),
                        contentDescription = stringResource(R.string.menu_mark_note)
                    )
                }
                IconButton(onClick = onSetFlag) {
                    Icon(
                        painter = painterResource(R.drawable.flag_24px),
                        contentDescription = stringResource(R.string.menu_flag)
                    )
                }
            } else {
                IconButton(onClick = onFilter) {
                    Icon(
                        painter = painterResource(R.drawable.filter_alt_24px),
                        contentDescription = stringResource(R.string.filter)
                    )
                }
                IconButton(onClick = onOptions) {
                    Icon(
                        painter = painterResource(R.drawable.tune_24px),
                        contentDescription = stringResource(R.string.more_options)
                    )
                }
            }
            IconButton(onClick = onMoreOptions) {
                Icon(
                    Icons.Filled.MoreVert,
                    contentDescription = stringResource(R.string.more_options)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBottomSheet(
    onDismissRequest: () -> Unit,
    onFilter: (String) -> Unit,
    onFlagFilter: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        ListItem(
            headlineContent = { Text("Filter marked") },
            modifier = Modifier.clickable {
                onFilter("tag:marked")
                scope.launch { sheetState.hide() }.invokeOnCompletion {
                    if (!sheetState.isVisible) {
                        onDismissRequest()
                    }
                }
            }
        )
        ListItem(
            headlineContent = { Text("Filter suspended") },
            modifier = Modifier.clickable {
                onFilter("is:suspended")
                scope.launch { sheetState.hide() }.invokeOnCompletion {
                    if (!sheetState.isVisible) {
                        onDismissRequest()
                    }
                }
            }
        )
        ListItem(
            headlineContent = { Text("Filter by tag") },
            modifier = Modifier.clickable {
                // TODO
                scope.launch { sheetState.hide() }.invokeOnCompletion {
                    if (!sheetState.isVisible) {
                        onDismissRequest()
                    }
                }
            }
        )
        ListItem(
            headlineContent = { Text("Filter by flag") },
            modifier = Modifier.clickable { onFlagFilter() }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreOptionsBottomSheet(
    onDismissRequest: () -> Unit,
    onChangeDisplayOrder: () -> Unit,
    onCreateFilteredDeck: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        ListItem(
            headlineContent = { Text(stringResource(R.string.card_browser_change_display_order)) },
            modifier = Modifier.clickable { onChangeDisplayOrder() }
        )
        ListItem(
            headlineContent = { Text(stringResource(R.string.new_dynamic_deck)) },
            modifier = Modifier.clickable {
                onCreateFilteredDeck()
                scope.launch { sheetState.hide() }.invokeOnCompletion {
                    if (!sheetState.isVisible) {
                        onDismissRequest()
                    }
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectableSortOrderBottomSheet(viewModel: CardBrowserViewModel, onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    val currentSortType = viewModel.order
    val sortLabels = stringArrayResource(id = R.array.card_browser_order_labels)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        LazyColumn {
            items(SortType.entries) { sortType ->
                ListItem(
                    headlineContent = { Text(text = sortLabels[sortType.cardBrowserLabelIndex]) },
                    leadingContent = {
                        RadioButton(
                            selected = currentSortType == sortType,
                            onClick = {
                                viewModel.changeCardOrder(sortType)
                                scope.launch { sheetState.hide() }.invokeOnCompletion {
                                    if (!sheetState.isVisible) {
                                        onDismiss()
                                    }
                                }
                            }
                        )
                    },
                    modifier = Modifier.clickable {
                        viewModel.changeCardOrder(sortType)
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            if (!sheetState.isVisible) {
                                onDismiss()
                            }
                        }
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlagFilterBottomSheet(onDismiss: () -> Unit, onFilter: (String) -> Unit) {
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var flagLabels by remember { mutableStateOf<Map<Flag, String>>(emptyMap()) }
    LaunchedEffect(true) {
        flagLabels = Flag.queryDisplayNames()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        LazyColumn {
            items(Flag.entries.filter { it != Flag.NONE }) { flag ->
                ListItem(
                    headlineContent = { Text(flagLabels[flag] ?: "") },
                    leadingContent = {
                        Icon(
                            painter = painterResource(id = flag.drawableRes),
                            contentDescription = "Filter by flag",
                            tint = colorResource(
                                id = flag.browserColorRes
                                    ?: R.color.transparent
                            )
                        )
                    },
                    modifier = Modifier.clickable {
                        onFilter("flag:${flag.code}")
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            if (!sheetState.isVisible) {
                                onDismiss()
                            }
                        }
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetFlagBottomSheet(onDismiss: () -> Unit, onSetFlag: (Flag) -> Unit) {
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var flagLabels by remember { mutableStateOf<Map<Flag, String>>(emptyMap()) }
    LaunchedEffect(true) {
        flagLabels = Flag.queryDisplayNames()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        LazyColumn {
            items(Flag.entries) { flag ->
                ListItem(
                    headlineContent = { Text(flagLabels[flag] ?: "") },
                    leadingContent = {
                        Icon(
                            painter = painterResource(id = flag.drawableRes),
                            contentDescription = "Set flag"
                        )
                    },
                    modifier = Modifier.clickable {
                        onSetFlag(flag)
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            if (!sheetState.isVisible) {
                                onDismiss()
                            }
                        }
                    }
                )
            }
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
    row: BrowserRow,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
) {
    val backgroundColor: Color = when {
        isSelected && row.color == BrowserRow.Color.COLOR_DEFAULT -> MaterialTheme.colorScheme.primaryContainer
        row.color != BrowserRow.Color.COLOR_DEFAULT -> {
            when (row.color) {
                BrowserRow.Color.COLOR_MARKED -> MaterialTheme.colorScheme.tertiaryContainer
                BrowserRow.Color.COLOR_FLAG_RED -> Color(0xFFFFCDD2)
                BrowserRow.Color.COLOR_FLAG_ORANGE -> Color(0xFFFFE0B2)
                BrowserRow.Color.COLOR_FLAG_GREEN -> Color(0xFFC8E6C9)
                BrowserRow.Color.COLOR_FLAG_BLUE -> Color(0xFFBBDEFB)
                BrowserRow.Color.COLOR_FLAG_PINK -> Color(0xFFF8BBD0)
                BrowserRow.Color.COLOR_FLAG_TURQUOISE -> Color(0xFFB2EBF2)
                BrowserRow.Color.COLOR_FLAG_PURPLE -> Color(0xFFE1BEE7)
                else -> MaterialTheme.colorScheme.surface
            }
        }
        else -> MaterialTheme.colorScheme.surface
    }

    val contentColor: Color = when (backgroundColor) {
        MaterialTheme.colorScheme.primaryContainer -> MaterialTheme.colorScheme.onPrimaryContainer
        MaterialTheme.colorScheme.tertiaryContainer -> MaterialTheme.colorScheme.onTertiaryContainer
        Color(0xFFFFCDD2), // COLOR_FLAG_RED
        Color(0xFFFFE0B2), // COLOR_FLAG_ORANGE
        Color(0xFFC8E6C9), // COLOR_FLAG_GREEN
        Color(0xFFBBDEFB), // COLOR_FLAG_BLUE
        Color(0xFFF8BBD0), // COLOR_FLAG_PINK
        Color(0xFFB2EBF2), // COLOR_FLAG_TURQUOISE
        Color(0xFFE1BEE7) // COLOR_FLAG_PURPLE
        -> Color.Black
        else -> MaterialTheme.colorScheme.onSurface
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = backgroundColor,
        contentColor = contentColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
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
}
