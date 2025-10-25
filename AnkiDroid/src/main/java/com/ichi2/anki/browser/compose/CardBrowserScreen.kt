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

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.FloatingToolbarDefaults.ScreenOffset
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.motionScheme
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleFloatingActionButton
import androidx.compose.material3.ToggleFloatingActionButtonDefaults.animateIcon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import anki.search.BrowserRow
import com.ichi2.anki.R
import com.ichi2.anki.browser.BrowserRowWithId
import com.ichi2.anki.browser.CardBrowserViewModel
import com.ichi2.anki.browser.ColumnHeading

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CardBrowserScreen(
    viewModel: CardBrowserViewModel,
    onCardClicked: (BrowserRowWithId) -> Unit,
    modifier: Modifier = Modifier
) {
    val browserRows by viewModel.browserRows.collectAsStateWithLifecycle()
    val columnHeadings by viewModel.flowOfColumnHeadings.collectAsStateWithLifecycle(initialValue = emptyList())
    var fabExpanded by remember { mutableStateOf(false) }

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
                FloatingActionButtonMenu(
                    expanded = fabExpanded, button = {
                        ToggleFloatingActionButton(
                            checked = fabExpanded, onCheckedChange = { fabExpanded = it }) {
                            val imageVector by remember {
                                derivedStateOf {
                                    if (checkedProgress > 0.5f) Icons.Filled.Close else Icons.Filled.Add
                                }
                            }
                            Icon(
                                painter = rememberVectorPainter(imageVector),
                                contentDescription = null, // decorative
                                modifier = Modifier.animateIcon({ checkedProgress })
                            )
                        }
                    }) {
                    FloatingActionButtonMenuItem(
                        onClick = { /* TODO */ },
                        icon = { Icon(Icons.Filled.Download, contentDescription = null) },
                        text = { Text(text = stringResource(R.string.get_shared)) })
                    FloatingActionButtonMenuItem(onClick = { /* TODO */ }, icon = {
                        Icon(
                            painterResource(id = R.drawable.ic_add_filtered_deck),
                            contentDescription = null
                        )
                    }, text = { Text(text = stringResource(R.string.new_dynamic_deck)) })
                    FloatingActionButtonMenuItem(onClick = { /* TODO */ }, icon = {
                        Icon(
                            painterResource(id = R.drawable.ic_add_deck_filled),
                            contentDescription = null
                        )
                    }, text = { Text(text = stringResource(R.string.new_deck)) })
                    FloatingActionButtonMenuItem(onClick = { /* TODO */ }, icon = {
                        Icon(
                            painterResource(id = R.drawable.ic_add_note), contentDescription = null
                        )
                    }, text = { Text(text = stringResource(R.string.menu_add_note)) })
                }
            },
            colors = FloatingToolbarDefaults.vibrantFloatingToolbarColors(),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { },
                    modifier = Modifier.height(48.dp),
                ) {
                    Icon(
                        Icons.Filled.MoreVert,
                        contentDescription = stringResource(R.string.more_options)
                    )
                }
                Box(
                    modifier = Modifier.animateContentSize(motionScheme.fastSpatialSpec())
                ) {

                    ButtonGroup(
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        overflowIndicator = { }) {

                        customItem(
                            buttonGroupContent = {
                                val interactionSource = remember { MutableInteractionSource() }
                                Button(
                                    onClick = {

                                    },
                                    modifier = Modifier
                                        .animateWidth(interactionSource)
                                        .height(56.dp),
                                    contentPadding = ButtonDefaults.ExtraSmallContentPadding,
                                    shape = ButtonGroupDefaults.connectedMiddleButtonShapes().shape,
                                    interactionSource = interactionSource,
                                    colors = ButtonDefaults.buttonColors(
                                        MaterialTheme.colorScheme.secondaryContainer,
                                        MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                ) {
                                    Text(
                                        text = "temp",
                                        softWrap = false,
                                        overflow = TextOverflow.Visible
                                    )
                                }
                            },
                            menuContent = {},
                        )
                    }
                }
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
