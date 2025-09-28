package com.ichi2.anki.ui.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ichi2.anki.R
import com.ichi2.anki.deckpicker.DisplayDeckNode

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun DeckPickerContent(
    decks: List<DisplayDeckNode>,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    backgroundImage: Painter?,
    modifier: Modifier = Modifier,
    onDeckClick: (DisplayDeckNode) -> Unit,
    onExpandClick: (DisplayDeckNode) -> Unit,
    onDeckOptions: (DisplayDeckNode) -> Unit,
    onRename: (DisplayDeckNode) -> Unit,
    onExport: (DisplayDeckNode) -> Unit,
    onDelete: (DisplayDeckNode) -> Unit,
    onRebuild: (DisplayDeckNode) -> Unit,
    onEmpty: (DisplayDeckNode) -> Unit,
) {
    val pullRefreshState = rememberPullRefreshState(refreshing = isRefreshing, onRefresh = onRefresh)

    Box(
        modifier =
        modifier
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
        Box(modifier = Modifier.fillMaxSize().pullRefresh(pullRefreshState)) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
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
                    Card(
                        modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),

                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Column (modifier = Modifier.padding(8.dp)) {
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
                            // Render the sub-decks
                            groupedDecks[rootDeck]?.forEach { subDeck ->
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
            PullRefreshIndicator(
                refreshing = isRefreshing,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
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
    searchFocusRequester: androidx.compose.ui.focus.FocusRequester =
        androidx.compose.ui.focus
            .FocusRequester(),
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    var isFabMenuOpen by remember { mutableStateOf(false) }
    var isSearchOpen by remember { mutableStateOf(false) }
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            LargeTopAppBar(
                title = {
                    if (!isSearchOpen) Text(stringResource(R.string.app_name), style = MaterialTheme.typography.titleLarge)
                }, // Expressive TopAppBar Title
                navigationIcon = {
                    IconButton(onClick = onNavigationIconClick) {
                        Icon(Icons.Default.Menu, contentDescription = stringResource(R.string.navigation_drawer_open))
                    }
                },
                actions = {
                    if (isSearchOpen) {
                        TextField(
                            value = searchQuery,
                            onValueChange = onSearchQueryChanged,
                            modifier = Modifier.weight(1f).focusRequester(searchFocusRequester),
                            placeholder = { Text(stringResource(R.string.search_decks)) },
                            trailingIcon = {
                                IconButton(onClick = {
                                    onSearchQueryChanged("")
                                    isSearchOpen = false
                                }) {
                                    Icon(Icons.Default.Close, contentDescription = stringResource(R.string.close))
                                }
                            },
                        )
                    } else {
                        IconButton(onClick = { isSearchOpen = true }) {
                            Icon(Icons.Default.Search, contentDescription = stringResource(R.string.search_decks))
                        }
                    }
                },
                colors =
                TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface,
                ),
                scrollBehavior = scrollBehavior,
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { isFabMenuOpen = !isFabMenuOpen },
                shape = MaterialTheme.shapes.extraLarge, // Apply expressive shape
            ) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add))
                DropdownMenu(
                    expanded = isFabMenuOpen,
                    onDismissRequest = { isFabMenuOpen = false },
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.add_note)) },
                        onClick = {
                            onAddNote()
                            isFabMenuOpen = false
                        },
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.new_deck)) },
                        onClick = {
                            onAddDeck()
                            isFabMenuOpen = false
                        },
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.get_shared)) },
                        onClick = {
                            onAddSharedDeck()
                            isFabMenuOpen = false
                        },
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.new_dynamic_deck)) },
                        onClick = {
                            onAddFilteredDeck()
                            isFabMenuOpen = false
                        },
                    )
                }
            }
        },
    ) { paddingValues ->
        DeckPickerContent(
            decks = decks,
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            backgroundImage = backgroundImage,
            modifier = Modifier.padding(paddingValues),
            onDeckClick = onDeckClick,
            onExpandClick = onExpandClick,
            onDeckOptions = onDeckOptions,
            onRename = onRename,
            onExport = onExport,
            onDelete = onDelete,
            onRebuild = onRebuild,
            onEmpty = onEmpty,
        )
    }
}
