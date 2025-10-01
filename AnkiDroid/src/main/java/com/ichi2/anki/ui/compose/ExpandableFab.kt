package com.ichi2.anki.ui.compose

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.material3.ToggleFloatingActionButtonDefaults.animateIcon
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.unit.dp
import com.ichi2.anki.R

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ExpandableFab(
    onAddNote: () -> Unit,
    onAddDeck: () -> Unit,
    onAddSharedDeck: () -> Unit,
    onAddFilteredDeck: () -> Unit,
) {
    var fabMenuExpanded by rememberSaveable { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val scrimColor by animateColorAsState(
        targetValue = if (fabMenuExpanded) MaterialTheme.colorScheme.scrim.copy(alpha = 0.4f) else Color.Transparent,
        animationSpec = tween(500),
        label = "Scrim"
    )

    if (fabMenuExpanded) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(scrimColor)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { fabMenuExpanded = false })
        )
    }
    BackHandler(fabMenuExpanded) { fabMenuExpanded = false }

    val onMenuItemClick = { action: () -> Unit ->
        {
            action()
            fabMenuExpanded = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(end = 8.dp, bottom = 32.dp),
        contentAlignment = Alignment.BottomEnd
    ) {
        FloatingActionButtonMenu(
            expanded = fabMenuExpanded,
            button = {
                val fabMenuExpandedStateDescription = stringResource(R.string.fab_menu_expanded)
                val fabMenuCollapsedStateDescription =
                    stringResource(R.string.fab_menu_collapsed)
                val fabMenuToggleContentDescription = stringResource(R.string.fab_menu_toggle)
                ToggleFloatingActionButton(modifier = Modifier
                    .semantics {
                        traversalIndex = -1f
                        stateDescription =
                            if (fabMenuExpanded) fabMenuExpandedStateDescription else fabMenuCollapsedStateDescription
                        contentDescription = fabMenuToggleContentDescription
                    }
                    .focusRequester(focusRequester),
                    checked = fabMenuExpanded,
                    onCheckedChange = { fabMenuExpanded = !fabMenuExpanded }) {
                    val imageVector by remember {
                        derivedStateOf {
                            if (checkedProgress > 0.5f) Icons.Filled.Close else Icons.Filled.Add
                        }
                    }
                    Icon(
                        painter = rememberVectorPainter(imageVector),
                        contentDescription = null,
                        modifier = Modifier.animateIcon({ checkedProgress }),
                    )
                }
            },
        ) {
            FloatingActionButtonMenuItem(
                onClick = onMenuItemClick(onAddSharedDeck),
                icon = { Icon(Icons.Filled.Download, contentDescription = null) },
                text = { Text(text = stringResource(R.string.get_shared)) },
            )
            FloatingActionButtonMenuItem(
                onClick = onMenuItemClick(onAddFilteredDeck),
                icon = {
                    Icon(
                        painterResource(id = R.drawable.ic_add_filtered_deck),
                        contentDescription = null
                    )
                },
                text = { Text(text = stringResource(R.string.new_dynamic_deck)) },
            )
            FloatingActionButtonMenuItem(
                onClick = onMenuItemClick(onAddDeck),
                icon = {
                    Icon(
                        painterResource(id = R.drawable.ic_add_deck_filled),
                        contentDescription = null
                    )
                },
                text = { Text(text = stringResource(R.string.new_deck)) },
            )
            FloatingActionButtonMenuItem(
                onClick = onMenuItemClick(onAddNote),
                icon = {
                    Icon(
                        painterResource(id = R.drawable.ic_add_note),
                        contentDescription = null
                    )
                },
                text = { Text(text = stringResource(R.string.add_card)) },
            )
        }
    }
}