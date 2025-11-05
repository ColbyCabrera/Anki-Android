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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ichi2.anki.R
import com.ichi2.anki.browser.CardBrowserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterByTagsDialog(
    onDismissRequest: () -> Unit,
    onConfirm: (Set<String>) -> Unit,
    allTags: CardBrowserViewModel.TagsState,
    initialSelection: Set<String>
) {
    var selection by remember(initialSelection) { mutableStateOf(initialSelection.toSet()) }
    var searchQuery by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(text = stringResource(id = R.string.card_browser_search_by_tag)) },
        text = {
            LazyColumn {
                item {
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
                                OutlinedTextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    label = { Text(text = stringResource(id = R.string.card_browser_search_tags_hint)) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceContainer,
                                    shape = MaterialTheme.shapes.large
                                ) {
                                    FlowRow(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        allTags.tags.filter {
                                            it.contains(
                                                other = searchQuery, ignoreCase = true
                                            )
                                        }.forEach { tag ->
                                            FilterChip(
                                                modifier = Modifier.height(
                                                    FilterChipDefaults.Height
                                                ), selected = tag in selection, onClick = {
                                                    selection = if (tag in selection) {
                                                        selection - tag
                                                    } else {
                                                        selection + tag
                                                    }
                                                }, label = { Text(text = tag) }, leadingIcon = {
                                                    if (tag in selection) {
                                                        Icon(
                                                            painter = painterResource(R.drawable.check_24px),
                                                            contentDescription = stringResource(R.string.done_icon),
                                                            modifier = Modifier.size(FilterChipDefaults.IconSize)
                                                        )
                                                    } else {
                                                        Spacer(Modifier.size(FilterChipDefaults.IconSize / 2))
                                                    }
                                                }, trailingIcon = {
                                                    if (tag in selection) {
                                                        Spacer(Modifier.size(0.dp))
                                                    } else {
                                                        Spacer(Modifier.size(FilterChipDefaults.IconSize / 2))
                                                    }
                                                })
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
