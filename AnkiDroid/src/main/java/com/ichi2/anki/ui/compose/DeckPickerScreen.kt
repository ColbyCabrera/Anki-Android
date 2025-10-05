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

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.motionScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.toPath
import com.ichi2.anki.R
import com.ichi2.anki.deckpicker.DisplayDeckNode

private val expandedDeckCardRadius = 24.dp
private val collapsedDeckCardRadius = 70.dp

private class MorphShape(
    private val morph: Morph, private val percentage: Float
) : Shape {
    override fun createOutline(
        size: Size, layoutDirection: LayoutDirection, density: Density
    ): Outline {
        // To draw the morph, we need to scale the path to the component size.
        // We could also do that in a Modifier, but doing it here makes it more reusable.
        val matrix = android.graphics.Matrix()
        matrix.setScale(size.width, size.height)
        val androidPath = morph.toPath(progress = percentage.coerceIn(0f, 1f))
        androidPath.transform(matrix)
        return Outline.Generic(androidPath.asComposePath())
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DeckPickerContent(
    decks: List<DisplayDeckNode>,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    backgroundImage: Painter?,
    listState: LazyListState,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    onDeckClick: (DisplayDeckNode) -> Unit,
    onExpandClick: (DisplayDeckNode) -> Unit,
    onDeckOptions: (DisplayDeckNode) -> Unit,
    onRename: (DisplayDeckNode) -> Unit,
    onExport: (DisplayDeckNode) -> Unit,
    onDelete: (DisplayDeckNode) -> Unit,
    onRebuild: (DisplayDeckNode) -> Unit,
    onEmpty: (DisplayDeckNode) -> Unit,
) {
    val state = rememberPullToRefreshState()
    val morphingShape = remember(state.distanceFraction) {
        MorphShape(
            morph = Morph(
                start = MaterialShapes.Pentagon,
                end = MaterialShapes.Cookie12Sided,
            ), percentage = state.distanceFraction
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        if (backgroundImage != null) {
            Image(
                painter = backgroundImage,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        }
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            state = state,
            modifier = Modifier.fillMaxSize(),
            indicator = {
                Box(
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .align(Alignment.TopCenter)
                        .width(42.dp)
                        .height(42.dp)
                        .graphicsLayer {
                            alpha = state.distanceFraction
                            rotationZ = state.distanceFraction * 360
                            translationY = (state.distanceFraction * 140) - 60
                        }
                        .clip(morphingShape)
                        .background(MaterialTheme.colorScheme.primary)) {
                    Box(modifier = Modifier.padding(16.dp))
                }
            }) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = contentPadding,
                state = listState
            ) {
                // Group decks by their parent
                val groupedDecks = mutableMapOf<DisplayDeckNode, MutableList<DisplayDeckNode>>()
                val rootDecks = mutableListOf<DisplayDeckNode>()
                var currentParent: DisplayDeckNode? = null

                for (deck in decks) {
                    if (deck.depth == 0) {
                        currentParent = deck
                        rootDecks.add(deck)
                        groupedDecks[deck] = mutableListOf()
                    } else if (currentParent != null) {
                        groupedDecks[currentParent]?.add(deck)
                    }
                }

                items(rootDecks) { rootDeck ->
                    val cornerRadius by animateDpAsState(
                        targetValue = if (!rootDeck.collapsed && rootDeck.canCollapse) expandedDeckCardRadius else collapsedDeckCardRadius,
                        animationSpec = motionScheme.defaultEffectsSpec()
                    )
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                        shape = RoundedCornerShape(cornerRadius),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            // Render the parent deck
                            DeckItem(
                                deck = rootDeck,
                                onDeckClick = { onDeckClick(rootDeck) },
                                onExpandClick = { onExpandClick(rootDeck) },
                                onDeckOptions = { onDeckOptions(rootDeck) },
                                onRename = { onRename(rootDeck) },
                                onExport = { onExport(rootDeck) },
                                onDelete = { onDelete(rootDeck) },
                                onRebuild = { onRebuild(rootDeck) },
                                onEmpty = { onEmpty(rootDeck) },
                            )

                            // Create a remembered state for sub-decks to handle exit animation correctly
                            val subDecks = groupedDecks[rootDeck]
                            var rememberedSubDecks by remember {
                                mutableStateOf<List<DisplayDeckNode>?>(
                                    null
                                )
                            }
                            if (!rootDeck.collapsed) {
                                rememberedSubDecks = subDecks
                            }

                            // Render the sub-decks
                            AnimatedVisibility(
                                visible = !rootDeck.collapsed,
                                enter = expandVertically(motionScheme.defaultSpatialSpec()) + fadeIn(
                                    motionScheme.defaultEffectsSpec()
                                ),
                                exit = shrinkVertically(motionScheme.fastSpatialSpec()) + fadeOut(
                                    motionScheme.defaultEffectsSpec()
                                ),
                            ) {
                                Column {
                                    (rememberedSubDecks ?: emptyList()).forEach { subDeck ->
                                        DeckItem(
                                            deck = subDeck,
                                            onDeckClick = { onDeckClick(subDeck) },
                                            onExpandClick = { onExpandClick(subDeck) },
                                            onDeckOptions = { onDeckOptions(subDeck) },
                                            onRename = { onRename(subDeck) },
                                            onExport = { onExport(subDeck) },
                                            onDelete = { onDelete(subDeck) },
                                            onRebuild = { onRebuild(subDeck) },
                                            onEmpty = { onEmpty(subDeck) },
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
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DeckPickerScreen(
    decks: List<DisplayDeckNode>,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
    backgroundImage: Painter?,
    modifier: Modifier = Modifier,
    onDeckClick: (DisplayDeckNode) -> Unit,
    onExpandClick: (DisplayDeckNode) -> Unit,
    onAddNote: () -> Unit,
    onAddDeck: () -> Unit,
    onAddSharedDeck: () -> Unit,
    onAddFilteredDeck: () -> Unit,
    onDeckOptions: (DisplayDeckNode) -> Unit,
    onRename: (DisplayDeckNode) -> Unit,
    onExport: (DisplayDeckNode) -> Unit,
    onDelete: (DisplayDeckNode) -> Unit,
    onRebuild: (DisplayDeckNode) -> Unit,
    onEmpty: (DisplayDeckNode) -> Unit,
    onNavigationIconClick: () -> Unit,
    searchFocusRequester: FocusRequester = FocusRequester(),
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    fabMenuExpanded: Boolean,
    onFabMenuExpandedChange: (Boolean) -> Unit
) {
    var isSearchOpen by remember { mutableStateOf(false) }
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val listState = rememberLazyListState()

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            snackbarHost = {
                SnackbarHost(snackbarHostState) { data ->
                    Snackbar(
                        snackbarData = data,
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary,
                        actionColor = MaterialTheme.colorScheme.primary,
                        dismissActionContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                }
            },
            topBar = {
                LargeTopAppBar(
                    title = {
                        if (!isSearchOpen) Text(
                            stringResource(R.string.app_name),
                            style = MaterialTheme.typography.displayMediumEmphasized
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigationIconClick) {
                            Icon(
                                Icons.Default.Menu,
                                contentDescription = stringResource(R.string.navigation_drawer_open)
                            )
                        }
                    },
                    actions = {
                        if (isSearchOpen) {
                            TextField(
                                value = searchQuery,
                                onValueChange = onSearchQueryChanged,
                                modifier = Modifier
                                    .weight(1f)
                                    .focusRequester(searchFocusRequester),
                                placeholder = { Text(stringResource(R.string.search_decks)) },
                                trailingIcon = {
                                    IconButton(onClick = {
                                        onSearchQueryChanged("")
                                        isSearchOpen = false
                                    }) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = stringResource(R.string.close)
                                        )
                                    }
                                },
                            )
                        } else {
                            IconButton(onClick = { isSearchOpen = true }) {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = stringResource(R.string.search_decks)
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        scrolledContainerColor = MaterialTheme.colorScheme.surface,
                        navigationIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                    scrollBehavior = scrollBehavior,
                )
            },
        ) { paddingValues ->
            DeckPickerContent(
                decks = decks,
                isRefreshing = isRefreshing,
                onRefresh = onRefresh,
                backgroundImage = backgroundImage,
                onDeckClick = onDeckClick,
                onExpandClick = onExpandClick,
                onDeckOptions = onDeckOptions,
                onRename = onRename,
                onExport = onExport,
                onDelete = onDelete,
                onRebuild = onRebuild,
                onEmpty = onEmpty,
                listState = listState,
                modifier = Modifier.padding(paddingValues)
            )
        }
        Scrim(
            visible = fabMenuExpanded, onDismiss = { onFabMenuExpandedChange(false) })
        ExpandableFabContainer {
            ExpandableFab(
                expanded = fabMenuExpanded,
                onExpandedChange = onFabMenuExpandedChange,
                onAddNote = onAddNote,
                onAddDeck = onAddDeck,
                onAddSharedDeck = onAddSharedDeck,
                onAddFilteredDeck = onAddFilteredDeck
            )
        }
        BackHandler(fabMenuExpanded) { onFabMenuExpandedChange(false) }
    }
}

@Preview
@Composable
fun DeckPickerContentPreview() {
    DeckPickerContent(
        decks = emptyList(),
        isRefreshing = false,
        onRefresh = {},
        backgroundImage = null,
        onDeckClick = {},
        onExpandClick = {},
        onDeckOptions = {},
        onRename = {},
        onExport = {},
        onDelete = {},
        onRebuild = {},
        onEmpty = {},
        listState = rememberLazyListState()
    )
}

@Preview
@Composable
fun DeckPickerScreenPreview() {
    DeckPickerScreen(
        decks = emptyList(),
        isRefreshing = false,
        onRefresh = {},
        searchQuery = "",
        onSearchQueryChanged = {},
        backgroundImage = null,
        onDeckClick = {},
        onExpandClick = {},
        onAddNote = {},
        onAddDeck = {},
        onAddSharedDeck = {},
        onAddFilteredDeck = {},
        onDeckOptions = {},
        onRename = {},
        onExport = {},
        onDelete = {},
        onRebuild = {},
        onEmpty = {},
        onNavigationIconClick = {},
        fabMenuExpanded = false,
        onFabMenuExpandedChange = {})
}