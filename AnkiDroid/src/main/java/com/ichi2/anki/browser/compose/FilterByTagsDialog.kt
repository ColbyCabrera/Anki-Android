/* **************************************************************************************
 * Copyright (c) 2009 Andrew Dubya <andrewdubya@gmail.com>                              *
 * Copyright (c) 2009 Nicolas Raoul <nicolas.raoul@gmail.com>                           *
 * Copyright (c) 2009 Edu Zamora <edu.zasu@gmail.com>                                   *
 * Copyright (c) 2009 Daniel Svard <daniel.svard@gmail.com>                             *
 * Copyright (c) 2010 Norbert Nagold <norbert.nagold@gmail.com>                         *
 * Copyright (c) 2014 Timothy Rae <perceptualchaos2@gmail.com>
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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconToggleButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ichi2.anki.R
import com.ichi2.anki.browser.CardBrowserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterByTagsDialog(
    onDismissRequest: () -> Unit,
    onConfirm: (Set<String>) -> Unit,
    allTags: CardBrowserViewModel.TagsState,
    initialSelection: Set<String>,
    deckTags: Set<String> = emptySet(),
    initialFilterByDeck: Boolean = false,
    onFilterByDeckChanged: (Boolean) -> Unit = {}
) {
    var selection by remember(initialSelection) { mutableStateOf(initialSelection.toSet()) }
    var searchQuery by remember { mutableStateOf("") }
    var isToggleChecked by remember(initialFilterByDeck) { mutableStateOf(initialFilterByDeck) }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(text = stringResource(id = R.string.card_browser_search_by_tag)) },
        text = {
            when (allTags) {
                is CardBrowserViewModel.TagsState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is CardBrowserViewModel.TagsState.Loaded -> {
                    Column {
                        SearchBarRow(
                            searchQuery = searchQuery,
                            onSearchQueryChange = { searchQuery = it },
                            isToggleChecked = isToggleChecked,
                            onToggleCheckedChange = {
                                isToggleChecked = it
                                onFilterByDeckChanged(it)
                            }
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceContainer,
                            shape = MaterialTheme.shapes.large
                        ) {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                contentPadding = PaddingValues(vertical = 16.dp)
                            ) {
                                item {
                                    val filteredTags = allTags.tags
                                        .filter { it.contains(other = searchQuery, ignoreCase = true) }
                                        .filter { !isToggleChecked || it in deckTags }

                                    if (filteredTags.isEmpty()) {
                                        Box(
                                            modifier = Modifier.fillMaxWidth(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = stringResource(R.string.card_browser_no_tags_found),
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    } else {
                                        FlowRow(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            filteredTags.forEach { tag ->
                                                FilterChip(
                                                    modifier = Modifier.height(
                                                        FilterChipDefaults.Height
                                                    ),
                                                    selected = tag in selection,
                                                    onClick = {
                                                        selection = if (tag in selection) {
                                                            selection - tag
                                                        } else {
                                                            selection + tag
                                                        }
                                                    },
                                                    label = { Text(text = tag) },
                                                    leadingIcon = {
                                                        if (tag in selection) {
                                                            Icon(
                                                                painter = painterResource(R.drawable.check_24px),
                                                                contentDescription = stringResource(
                                                                    R.string.done_icon
                                                                ),
                                                                modifier = Modifier.size(
                                                                    FilterChipDefaults.IconSize
                                                                )
                                                            )
                                                        } else {
                                                            Spacer(Modifier.size(FilterChipDefaults.IconSize / 2))
                                                        }
                                                    },
                                                    trailingIcon = {
                                                        if (tag in selection) {
                                                            Spacer(Modifier.size(0.dp))
                                                        } else {
                                                            Spacer(Modifier.size(FilterChipDefaults.IconSize / 2))
                                                        }
                                                    },
                                                    colors = FilterChipDefaults.filterChipColors(
                                                        selectedContainerColor = MaterialTheme.colorScheme.tertiary,
                                                        selectedLabelColor = MaterialTheme.colorScheme.onTertiary,
                                                        selectedLeadingIconColor = MaterialTheme.colorScheme.onTertiary,
                                                    ),
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selection) }) {
                Text(text = stringResource(id = R.string.dialog_ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(text = stringResource(id = R.string.dialog_cancel))
            }
        })
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SearchBarRow(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    isToggleChecked: Boolean,
    onToggleCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = MaterialTheme.shapes.extraLargeIncreased,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = { Text(text = stringResource(id = R.string.card_browser_search_tags_hint)) },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = stringResource(R.string.card_browser_search_hint)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = stringResource(R.string.close)
                            )
                        }
                    }
                },
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent
                ),
                modifier = Modifier.weight(1f)
            )

            val tooltipContent = if (isToggleChecked) {
                stringResource(R.string.card_browser_filter_tags_by_deck_on_description)
            } else {
                stringResource(R.string.card_browser_filter_tags_by_deck_off_description)
            }
            TooltipBox(
                positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
                    TooltipAnchorPosition.Above
                ),
                tooltip = {
                    PlainTooltip {
                        Text(tooltipContent)
                    }
                },
                state = rememberTooltipState()
            ) {
                FilledTonalIconToggleButton(
                    checked = isToggleChecked,
                    onCheckedChange = onToggleCheckedChange,
                    shapes = IconButtonDefaults.toggleableShapes(),
                    //colors = IconButtonDefaults.filledTonalIconToggleButtonColors()
                ) {
                    Icon(
                        painter = painterResource(
                            if (isToggleChecked) R.drawable.filter_alt_24px else R.drawable.filter_alt_off_24px
                        ),
                        contentDescription = tooltipContent,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun SearchBarRowPreview() {
    var searchQuery by remember { mutableStateOf("") }
    var isToggleChecked by remember { mutableStateOf(false) }

    MaterialTheme {
        SearchBarRow(
            searchQuery = searchQuery,
            onSearchQueryChange = { searchQuery = it },
            isToggleChecked = isToggleChecked,
            onToggleCheckedChange = { isToggleChecked = it },
            modifier = Modifier.padding(16.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun SearchBarRowPreviewWithText() {
    var searchQuery by remember { mutableStateOf("kotlin") }
    var isToggleChecked by remember { mutableStateOf(true) }

    MaterialTheme {
        SearchBarRow(
            searchQuery = searchQuery,
            onSearchQueryChange = { searchQuery = it },
            isToggleChecked = isToggleChecked,
            onToggleCheckedChange = { isToggleChecked = it },
            modifier = Modifier.padding(16.dp)
        )
    }
}
