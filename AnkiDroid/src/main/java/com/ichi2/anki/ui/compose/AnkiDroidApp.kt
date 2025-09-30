/*
 Copyright (c) 2011 Norbert Nagold <norbert.nagold@gmail.com>
 Copyright (c) 2015 Timothy Rae <perceptualchaos2@gmail.com>
 Copyright (c) 2021 Akshay Jadhav <jadhavakshay0701@gmail.com>

 This program is free software; you can redistribute it and/or modify it under
 the terms of the GNU General Public License as published by the Free Software
 Foundation; either version 3 of the License, or (at your option) any later
 version.

 This program is distributed in the hope that it is useful, but WITHOUT ANY
 WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 PARTICULAR PURPOSE. See the GNU General Public License for more details.

 You should have received a copy of the GNU General Public License along with
 this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.ichi2.anki.ui.compose

import android.os.Build
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Shapes
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ichi2.anki.R
import com.ichi2.anki.deckpicker.DisplayDeckNode
import com.ichi2.themes.Themes

// Define Expressive Typography
val AppTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp,
    ),
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 24.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp,
    ),
    // Add other text styles as needed, inheriting from M3 defaults or customizing
)

// Define Expressive Shapes
val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp), // Default M3
    small = RoundedCornerShape(8.dp), // Expressive: Slightly more rounded
    medium = RoundedCornerShape(16.dp), // Expressive: More pronounced rounding for cards/buttons
    large = RoundedCornerShape(24.dp), // Expressive: Very rounded for larger elements like dialogs
    extraLarge = RoundedCornerShape(32.dp), // Expressive: For prominent elements like FABs or hero containers
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AnkiDroidApp(
    fragmented: Boolean,
    decks: List<DisplayDeckNode>,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
    backgroundImage: Painter?,
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
    studyOptionsData: StudyOptionsData?,
    onStartStudy: () -> Unit,
    onRebuildDeck: (Long) -> Unit,
    onEmptyDeck: (Long) -> Unit,
    onCustomStudy: (Long) -> Unit,
    onDeckOptionsItemSelected: (Long) -> Unit,
    onUnbury: (Long) -> Unit,
    requestSearchFocus: Boolean,
    onSearchFocusRequested: () -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    val searchFocusRequester = remember {
        androidx.compose.ui.focus.FocusRequester()
    }

    LaunchedEffect(requestSearchFocus) {
        if (requestSearchFocus) {
            searchFocusRequester.requestFocus()
            onSearchFocusRequested()
        }
    }

    val context = LocalContext.current
    val currentAnkiTheme = Themes.currentTheme
    val colorScheme = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (currentAnkiTheme.isNightMode) {
            dynamicDarkColorScheme(context)
        } else {
            dynamicLightColorScheme(context)
        }
    } else {
        if (currentAnkiTheme.isNightMode) {
            darkColorScheme()
        } else {
            lightColorScheme()
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography, // Apply expressive typography
        shapes = AppShapes, // Apply expressive shapes
        motionScheme = MotionScheme.expressive()
    ) {
        if (fragmented) {
            var isSearchOpen by remember { mutableStateOf(false) }
            var isStudyOptionsMenuOpen by remember { mutableStateOf(false) }
            var isFabMenuOpen by remember { mutableStateOf(false) }
            val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
            val listState = rememberLazyListState()
            // Tablet layout
            Scaffold(
                modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
                snackbarHost = {
                    SnackbarHost(snackbarHostState) { snackbarData ->
                        Snackbar(
                            snackbarData = snackbarData,
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                        )
                    }
                },
                topBar = {
                    LargeTopAppBar(
                        title = {
                            if (!isSearchOpen) Text(
                                stringResource(R.string.app_name),
                                style = MaterialTheme.typography.titleLarge
                            )
                        }, // Expressive TopAppBar Title
                        navigationIcon = {
                            IconButton(onClick = onNavigationIconClick) {
                                Icon(
                                    Icons.Default.Menu,
                                    contentDescription = stringResource(R.string.navigation_drawer_open),
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
                                                contentDescription = stringResource(R.string.close),
                                            )
                                        }

                                    },
                                )
                            } else {
                                IconButton(onClick = { isSearchOpen = true }) {
                                    Icon(
                                        Icons.Default.Search,
                                        contentDescription = stringResource(R.string.search_decks),
                                    )
                                }
                            }
                            if (studyOptionsData != null) {
                                IconButton(onClick = { isStudyOptionsMenuOpen = true }) {
                                    Icon(
                                        Icons.Default.MoreVert,
                                        contentDescription = stringResource(R.string.more_options),
                                    )
                                }
                                DropdownMenu(
                                    expanded = isStudyOptionsMenuOpen,
                                    onDismissRequest = { isStudyOptionsMenuOpen = false },
                                ) {
                                    if (studyOptionsData.isFiltered) {
                                        DropdownMenuItem(
                                            text = { Text(stringResource(R.string.rebuild)) },
                                            onClick = {
                                                onRebuildDeck(studyOptionsData.deckId)
                                                isStudyOptionsMenuOpen = false
                                            },
                                            leadingIcon = {
                                                Icon(
                                                    Icons.Default.Refresh,
                                                    contentDescription = null,
                                                )
                                            },
                                        )
                                        DropdownMenuItem(
                                            text = { Text(stringResource(R.string.empty_cards_action)) },
                                            onClick = {
                                                onEmptyDeck(studyOptionsData.deckId)
                                                isStudyOptionsMenuOpen = false
                                            },
                                            leadingIcon = {
                                                Icon(
                                                    Icons.Outlined.Delete,
                                                    contentDescription = null,
                                                )
                                            },
                                        )
                                    } else {
                                        DropdownMenuItem(
                                            text = { Text(stringResource(R.string.custom_study)) },
                                            onClick = {
                                                onCustomStudy(studyOptionsData.deckId)
                                                isStudyOptionsMenuOpen = false
                                            },
                                            leadingIcon = {
                                                Icon(
                                                    Icons.Default.Star,
                                                    contentDescription = null,
                                                )
                                            },
                                        )
                                    }
                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.deck_options)) },
                                        onClick = {
                                            onDeckOptionsItemSelected(studyOptionsData.deckId)
                                            isStudyOptionsMenuOpen = false
                                        },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Default.Settings,
                                                contentDescription = null,
                                            )
                                        },
                                    )
                                    if (studyOptionsData.haveBuried) {
                                        DropdownMenuItem(
                                            text = { Text(stringResource(R.string.unbury)) },
                                            onClick = {
                                                onUnbury(studyOptionsData.deckId)
                                                isStudyOptionsMenuOpen = false
                                            },
                                            leadingIcon = {
                                                Icon(
                                                    painter = painterResource(R.drawable.undo_24px),
                                                    contentDescription = null,
                                                )
                                            },
                                        )
                                    }
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
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
                        Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add))
                        DropdownMenu(
                            expanded = isFabMenuOpen,
                            onDismissRequest = { isFabMenuOpen = false },
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.add_card)) },
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
                Row(
                    Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                ) {
                    Box(modifier = Modifier.weight(1f)) {
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
                            listState = listState
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        StudyOptionsScreen(
                            studyOptionsData = studyOptionsData,
                            onStartStudy = onStartStudy,
                            onCustomStudy = onCustomStudy,
                        )
                    }
                }
            }
        } else {
            // Phone layout
            DeckPickerScreen(
                decks = decks,
                isRefreshing = isRefreshing,
                searchFocusRequester = searchFocusRequester,
                snackbarHostState = snackbarHostState,
                onRefresh = onRefresh,
                searchQuery = searchQuery,
                onSearchQueryChanged = onSearchQueryChanged,
                backgroundImage = backgroundImage,
                onDeckClick = onDeckClick,
                onExpandClick = onExpandClick,
                onAddNote = onAddNote,
                onAddDeck = onAddDeck,
                onAddSharedDeck = onAddSharedDeck,
                onAddFilteredDeck = onAddFilteredDeck,
                onDeckOptions = onDeckOptions,
                onRename = onRename,
                onExport = onExport,
                onDelete = onDelete,
                onRebuild = onRebuild,
                onEmpty = onEmpty,
                onNavigationIconClick = onNavigationIconClick,
            )
        }
    }
}