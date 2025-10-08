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
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.motionScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.toPath
import com.ichi2.anki.R
import com.ichi2.anki.deckpicker.DisplayDeckNode

private val expandedDeckCardRadius = 14.dp
private val collapsedDeckCardRadius = 70.dp

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

@OptIn(ExperimentalMaterial3ExpressiveApi::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
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

    val cornerRadius by animateDpAsState(
        targetValue = if (!deck.collapsed && deck.canCollapse) expandedDeckCardRadius else collapsedDeckCardRadius,
        animationSpec = motionScheme.defaultEffectsSpec()
    )

    val content = @Composable {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = { onDeckClick() },
                    onLongClick = { isContextMenuOpen = true }
                )
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {

            Text(
                text = deck.lastDeckNameComponent,
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 12.dp, horizontal = 8.dp),
                style = if (deck.depth == 0) MaterialTheme.typography.titleLargeEmphasized else MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Row(
                modifier = Modifier
                    .height(70.dp)
                    .padding(horizontal = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CardCountsContainer(
                    cardCount = deck.newCount,
                    shape = RoundedPolygonShape(MaterialShapes.Clover4Leaf),
                    containerColor = MaterialTheme.colorScheme.secondaryFixedDim,
                )

                CardCountsContainer(
                    cardCount = deck.revCount,
                    shape = RoundedPolygonShape(MaterialShapes.Ghostish),
                    containerColor = MaterialTheme.colorScheme.secondary,
                )
            }


            if (deck.canCollapse) {
                IconButton(
                    onClick = { onExpandClick() },
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .size(36.dp)
                ) {
                    Icon(
                        painter = painterResource(
                            if (deck.collapsed) R.drawable.ic_expand_more_black_24dp else R.drawable.ic_expand_less_black_24dp,
                        ),
                        contentDescription = if (deck.collapsed) stringResource(R.string.expand) else stringResource(
                            R.string.collapse
                        ),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
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
                shape = RoundedCornerShape(cornerRadius),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                content()
            }
        }

        else -> {
            Box(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp, bottom = 2.dp)
                   .clip(RoundedCornerShape(cornerRadius))
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
    cardCount: Int,
    shape: Shape,
    containerColor: Color = MaterialTheme.colorScheme.secondary,
) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(shape)
            .background(containerColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = cardCount.toString(),
            color = MaterialTheme.colorScheme.onSecondary,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier
                .padding(0.dp)
                .basicMarquee()
        )
    }
}


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Preview
@Composable
fun CardCountsContainerPreview() {
    CardCountsContainer(
        cardCount = 10, shape = RoundedPolygonShape(MaterialShapes.Clover4Leaf)
    )
}
