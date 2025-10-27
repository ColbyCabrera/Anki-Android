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
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
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
import com.ichi2.anki.browser.CardBrowserViewModel.SearchState

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
    val browserRows by viewModel.browserRows.collectAsStateWithLifecycle()
    val columnHeadings by viewModel.flowOfColumnHeadings.collectAsStateWithLifecycle()
    val selectedRows by viewModel.flowOfSelectedRows.collectAsStateWithLifecycle(initialValue = emptySet())
    val hasSelection by viewModel.flowOfSelectedRows.map { it.isNotEmpty() }.collectAsStateWithLifecycle(initialValue = false)
    val searchState by viewModel.flowOfSearchState.collectAsStateWithLifecycle(initialValue = SearchState.Initializing)
    var showFilterSheet by remember { mutableStateOf(false) }
    var showMoreOptionsMenu by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }
    var showFlagMenu by remember { mutableStateOf(false) }
    var showSetFlagMenu by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Box(modifier = modifier) {
        Column(modifier = modifier) {
            CardBrowserHeader(columns = columnHeadings)
            HorizontalDivider()
            when (searchState) {
                is SearchState.Initializing, is SearchState.Searching -> CardBrowserLoading()
                is SearchState.Completed -> {
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
                is SearchState.Error -> {
                    CardBrowserErrorState()
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
                },
                onFilterByTag = onFilterByTag
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
                },
                hasSelection = hasSelection,
                onEditNote = {
                    onEditNote()
                    showMoreOptionsMenu = false
                },
                onDeleteNote = {
                    scope.launch {
                        viewModel.deleteSelectedNotes()
                        showMoreOptionsMenu = false
                    }
                },
                onCardInfo = {
                    onCardInfo()
                    showMoreOptionsMenu = false
                },
                onToggleSuspend = {
                    scope.launch {
                        viewModel.toggleSuspendCards()
                        showMoreOptionsMenu = false
                    }
                },
                onToggleBury = {
                    scope.launch {
                        viewModel.toggleBury()
                        showMoreOptionsMenu = false
                    }
                },
                onChangeDeck = {
                    onChangeDeck()
                    showMoreOptionsMenu = false
                },
                onReposition = {
                    onReposition()
                    showMoreOptionsMenu = false
                },
                onSetDueDate = {
                    onSetDueDate()
                    showMoreOptionsMenu = false
                },
                onEditTags = {
                    onEditTags()
                    showMoreOptionsMenu = false
                },
                onGradeNow = {
                    onGradeNow()
                    showMoreOptionsMenu = false
                },
                onResetProgress = {
                    onResetProgress()
                    showMoreOptionsMenu = false
                },
                onExportCard = {
                    onExportCard()
                    showMoreOptionsMenu = false
                },
                onUndoDeleteNote = {
                    scope.launch {
                        viewModel.undo()
                        showMoreOptionsMenu = false
                    }
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

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
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
                    TooltipBox(
                        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
                            positioning = TooltipAnchorPosition.Above,
                        ),
                        tooltip = {
                            PlainTooltip { Text(stringResource(R.string.card_browser_deselect_all)) }
                        },
                        state = rememberTooltipState()
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.deselect_24px),
                            contentDescription = stringResource(R.string.card_browser_deselect_all)
                        )
                    }

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
            TooltipBox(
                positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
                    positioning = TooltipAnchorPosition.Above,
                ), tooltip = {
                    PlainTooltip { Text(stringResource(R.string.card_editor_preview_card)) }
                }, state = rememberTooltipState()
            ) {
                IconButton(onClick = onPreview) {
                    Icon(
                        painter = painterResource(R.drawable.preview_24px),
                        contentDescription = stringResource(R.string.card_editor_preview_card)
                    )
                }
            }
            TooltipBox(
                positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
                    positioning = TooltipAnchorPosition.Above,
                ), tooltip = {
                    PlainTooltip { Text(stringResource(R.string.card_browser_select_all)) }
                }, state = rememberTooltipState()
            ) {
                IconButton(onClick = onSelectAll) {
                    Icon(
                        painter = painterResource(R.drawable.select_all_24px),
                        contentDescription = stringResource(R.string.card_browser_select_all)
                    )
                }
            }
            if (hasSelection) {
                TooltipBox(
                    positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
                        positioning = TooltipAnchorPosition.Above,
                    ), tooltip = {
                        PlainTooltip { Text(stringResource(R.string.menu_mark_note)) }
                    }, state = rememberTooltipState()
                ) {
                    IconButton(onClick = onMark) {
                        Icon(
                            painter = painterResource(R.drawable.star_24px),
                            contentDescription = stringResource(R.string.menu_mark_note)
                        )
                    }
                }
                TooltipBox(
                    positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
                        positioning = TooltipAnchorPosition.Above,
                    ), tooltip = {
                        PlainTooltip { Text(stringResource(R.string.menu_flag)) }
                    }, state = rememberTooltipState()
                ) {
                    IconButton(onClick = onSetFlag) {
                        Icon(
                            painter = painterResource(R.drawable.flag_24px),
                            contentDescription = stringResource(R.string.menu_flag)
                        )
                    }
                }
            } else {
                TooltipBox(
                    positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
                        positioning = TooltipAnchorPosition.Above,
                    ), tooltip = {
                        PlainTooltip { Text(stringResource(R.string.filter)) }
                    }, state = rememberTooltipState()
                ) {
                    IconButton(onClick = onFilter) {
                        Icon(
                            painter = painterResource(R.drawable.filter_alt_24px),
                            contentDescription = stringResource(R.string.filter)
                        )
                    }
                }
                TooltipBox(
                    positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
                        positioning = TooltipAnchorPosition.Above,
                    ), tooltip = {
                        PlainTooltip { Text(stringResource(R.string.browser_options_dialog_heading)) }
                    }, state = rememberTooltipState()
                ) {
                    IconButton(onClick = onOptions) {
                        Icon(
                            painter = painterResource(R.drawable.tune_24px),
                            contentDescription = stringResource(R.string.browser_options_dialog_heading)
                        )
                    }
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
    onFlagFilter: () -> Unit,
    onFilterByTag: () -> Unit
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
            headlineContent = { Text(stringResource(R.string.card_browser_show_marked)) },
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
            headlineContent = { Text(stringResource(R.string.card_browser_show_suspended)) },
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
            headlineContent = { Text(stringResource(R.string.filter_by_tag)) },
            modifier = Modifier.clickable {
                onFilterByTag()
                scope.launch { sheetState.hide() }.invokeOnCompletion {
                    if (!sheetState.isVisible) {
                        onDismissRequest()
                    }
                }
            }
        )
        ListItem(
            headlineContent = { Text(stringResource(R.string.card_browser_search_by_flag)) },
            modifier = Modifier.clickable { onFlagFilter() }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreOptionsBottomSheet(
    onDismissRequest: () -> Unit,
    onChangeDisplayOrder: () -> Unit,
    onCreateFilteredDeck: () -> Unit,
    hasSelection: Boolean,
    onEditNote: () -> Unit,
    onDeleteNote: () -> Unit,
    onCardInfo: () -> Unit,
    onToggleSuspend: () -> Unit,
    onToggleBury: () -> Unit,
    onChangeDeck: () -> Unit,
    onReposition: () -> Unit,
    onSetDueDate: () -> Unit,
    onEditTags: () -> Unit,
    onGradeNow: () -> Unit,
    onResetProgress: () -> Unit,
    onExportCard: () -> Unit,
    onUndoDeleteNote: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        if (hasSelection) {
            ListItem(
                headlineContent = { Text(stringResource(R.string.cardeditor_title_edit_card)) },
                modifier = Modifier.clickable { onEditNote() }
            )
            ListItem(
                headlineContent = { Text(stringResource(R.string.menu_delete_note)) },
                modifier = Modifier.clickable { onDeleteNote() }
            )
            ListItem(
                headlineContent = { Text(stringResource(R.string.card_info_title)) },
                modifier = Modifier.clickable { onCardInfo() }
            )
            ListItem(
                headlineContent = { Text(stringResource(R.string.sentence_toggle_suspend)) },
                modifier = Modifier.clickable { onToggleSuspend() }
            )
            ListItem(
                headlineContent = { Text(stringResource(R.string.sentence_toggle_bury)) },
                modifier = Modifier.clickable { onToggleBury() }
            )
            ListItem(
                headlineContent = { Text(stringResource(R.string.card_browser_change_deck)) },
                modifier = Modifier.clickable { onChangeDeck() }
            )
            ListItem(
                headlineContent = { Text(stringResource(R.string.card_editor_reposition_card)) },
                modifier = Modifier.clickable { onReposition() }
            )
            ListItem(
                headlineContent = { Text(stringResource(R.string.sentence_set_due_date)) },
                modifier = Modifier.clickable { onSetDueDate() }
            )
            ListItem(
                headlineContent = { Text(stringResource(R.string.menu_edit_tags)) },
                modifier = Modifier.clickable { onEditTags() }
            )
            ListItem(
                headlineContent = { Text(stringResource(R.string.sentence_grade_now)) },
                modifier = Modifier.clickable { onGradeNow() }
            )
            ListItem(
                headlineContent = { Text(stringResource(R.string.reset_progress)) },
                modifier = Modifier.clickable { onResetProgress() }
            )
            ListItem(
                headlineContent = { Text(stringResource(R.string.export_card)) },
                modifier = Modifier.clickable { onExportCard() }
            )
        } else {
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
            ListItem(
                headlineContent = { Text(stringResource(R.string.undo_delete_note)) },
                modifier = Modifier.clickable { onUndoDeleteNote() }
            )
        }
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
                            painter = painterResource(id = R.drawable.flag_24px),
                            contentDescription = stringResource(R.string.card_browser_search_by_flag),
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
                            painter = painterResource(id = R.drawable.flag_24px),
                            contentDescription = stringResource(R.string.menu_flag),
                            tint = if (flag == Flag.NONE) {
                                MaterialTheme.colorScheme.onSurface
                            } else {
                                colorResource(
                                    id = flag.browserColorRes
                                        ?: R.color.transparent
                                )
                            }
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
fun CardBrowserErrorState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CardBrowserError()
    }
}

@Composable
fun CardBrowserError(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(id = R.string.card_browser_no_cards_in_deck), modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CardBrowserLoading(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LoadingIndicator(
            color = LoadingIndicatorDefaults.indicatorColor,
            polygons = LoadingIndicatorDefaults.IndeterminateIndicatorPolygons
        )
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
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        columns.forEach { column ->
            Text(
                text = column.label,
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
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
                BrowserRow.Color.COLOR_FLAG_RED -> colorResource(Flag.RED.browserColorRes!!)
                BrowserRow.Color.COLOR_FLAG_ORANGE -> colorResource(Flag.ORANGE.browserColorRes!!)
                BrowserRow.Color.COLOR_FLAG_GREEN -> colorResource(Flag.GREEN.browserColorRes!!)
                BrowserRow.Color.COLOR_FLAG_BLUE -> colorResource(Flag.BLUE.browserColorRes!!)
                BrowserRow.Color.COLOR_FLAG_PINK -> colorResource(Flag.PINK.browserColorRes!!)
                BrowserRow.Color.COLOR_FLAG_TURQUOISE -> colorResource(Flag.TURQUOISE.browserColorRes!!)
                BrowserRow.Color.COLOR_FLAG_PURPLE -> colorResource(Flag.PURPLE.browserColorRes!!)
                else -> MaterialTheme.colorScheme.surface
            }
        }

        else -> MaterialTheme.colorScheme.surface
    }

    val contentColor: Color = when (backgroundColor) {
        MaterialTheme.colorScheme.primaryContainer -> MaterialTheme.colorScheme.onPrimaryContainer
        MaterialTheme.colorScheme.tertiaryContainer -> MaterialTheme.colorScheme.onTertiaryContainer
        colorResource(Flag.RED.browserColorRes!!),
        colorResource(Flag.ORANGE.browserColorRes!!),
        colorResource(Flag.GREEN.browserColorRes!!),
        colorResource(Flag.BLUE.browserColorRes!!),
        colorResource(Flag.PINK.browserColorRes!!),
        colorResource(Flag.TURQUOISE.browserColorRes!!),
        colorResource(Flag.PURPLE.browserColorRes!!)
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
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            row.cellsList.forEach { cell ->
                Text(
                    text = cell.text,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    modifier = Modifier.weight(1f),
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
