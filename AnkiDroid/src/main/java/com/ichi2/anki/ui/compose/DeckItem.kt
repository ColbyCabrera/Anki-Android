/****************************************************************************************
 * Copyright (c) 2009 Edu Zamora <edu.zasu@gmail.com>                                   *
 * Copyright (c) 2009 Casey Link <unnamedrambler@gmail.com>                             *
 * Copyright (c) 2014 Timothy Rae <perceptualchaos2@gmail.com>                          *
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
package com.ichi2.anki.ui.compose

import android.graphics.Matrix
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.toPath
import com.ichi2.anki.R
import com.ichi2.anki.deckpicker.DisplayDeckNode

private val SubDeckCardRadius = 14.dp

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
internal class RoundedPolygonShape(private val polygon: RoundedPolygon) : Shape {
    override fun createOutline(
        size: Size, layoutDirection: LayoutDirection, density: Density
    ): Outline {
        val matrix = Matrix()
        matrix.setScale(size.width, size.height)
        val path = polygon.toPath()
        path.transform(matrix)
        return Outline.Generic(path.asComposePath())
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DeckItem(
    deck: DisplayDeckNode,
    modifier: Modifier = Modifier,
    onDeckClick: () -> Unit,
    onExpandClick: () -> Unit,
    onDeckOptions: () -> Unit,
    onRename: () -> Unit,
    onExport: () -> Unit,
    onDelete: () -> Unit,
    onRebuild: () -> Unit,
    onEmpty: () -> Unit,
) {
    var isContextMenuOpen by remember { mutableStateOf(false) }

    val content = @Composable {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { onDeckClick() },
                        onLongPress = { isContextMenuOpen = true },
                    )
                },
            verticalAlignment = Alignment.CenterVertically,
        ) {

            Text(
                text = deck.lastDeckNameComponent,
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 12.dp, horizontal = 8.dp),
                style = if (deck.depth == 0) MaterialTheme.typography.titleLargeEmphasized else MaterialTheme.typography.titleMedium,
            )
            Row(
                modifier = Modifier.height(70.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CardCountsContainer(
                    cardCount = deck.newCount,
                    labelText = "New",
                    shape = RoundedPolygonShape(MaterialShapes.Clover4Leaf)
                )
                CardCountsContainer(
                    cardCount = deck.lrnCount,
                    labelText = "Learn",
                    shape = RoundedPolygonShape(MaterialShapes.Ghostish)
                )
                CardCountsContainer(
                    cardCount = deck.revCount,
                    labelText = "Review",
                    shape = RoundedPolygonShape(MaterialShapes.Flower)
                )
            }


            if (deck.canCollapse) {
                Surface(
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .size(36.dp)
                        .clipToBounds()
                        .pointerInput(Unit) {
                            detectTapGestures(onTap = { onExpandClick() })
                        },
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    color = MaterialTheme.colorScheme.surfaceDim,
                    shape = MaterialTheme.shapes.extraLarge,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(
                                if (deck.collapsed) R.drawable.ic_expand_more_black_24dp else R.drawable.ic_expand_less_black_24dp,
                            ),
                            contentDescription = if (deck.collapsed) stringResource(R.string.expand) else stringResource(
                                R.string.collapse
                            ),
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.size(44.dp))
            }
            DropdownMenu(
                expanded = isContextMenuOpen,
                onDismissRequest = { isContextMenuOpen = false },
            ) {
                if (deck.filtered) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.rebuild_cram_label)) },
                        onClick = {
                            onRebuild()
                            isContextMenuOpen = false
                        },
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.empty_cram_label)) },
                        onClick = {
                            onEmpty()
                            isContextMenuOpen = false
                        },
                    )
                } else {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.rename_deck)) },
                        onClick = {
                            onRename()
                            isContextMenuOpen = false
                        },
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.export_deck)) },
                        onClick = {
                            onExport()
                            isContextMenuOpen = false
                        },
                    )
                }
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.deck_options)) },
                    onClick = {
                        onDeckOptions()
                        isContextMenuOpen = false
                    },
                )
                DropdownMenuItem(
                    text = { Text("Delete") },
                    onClick = {
                        onDelete()
                        isContextMenuOpen = false
                    },
                )
            }
        }
    }


    when (deck.depth) {
        0 -> {
            content()
        }

        1 -> {
            Card(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(horizontal = 2.dp, vertical = 2.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ),
                shape = RoundedCornerShape(SubDeckCardRadius),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                content()
            }
        }

        else -> {
            Box(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(start = ((deck.depth - 1) * 16 + 8).dp, top = 2.dp, bottom = 2.dp)
                    .clip(RoundedCornerShape(SubDeckCardRadius))
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh),
            ) {
                content()
            }
        }
    }
}


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CardCountsContainer(
    modifier: Modifier = Modifier, cardCount: Int, labelText: String, shape: Shape
) {
    Box(
        modifier = Modifier
            .size(30.dp)
            .clip(shape)
            .background(MaterialTheme.colorScheme.primaryContainer)
            .zIndex(1F),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = cardCount.toString(),
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(0.dp)
        )
    }
}


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Preview
@Composable
fun CardCountsContainerPreview() {
    CardCountsContainer(
        cardCount = 10, labelText = "New", shape = RoundedPolygonShape(MaterialShapes.Clover4Leaf)
    )
}
