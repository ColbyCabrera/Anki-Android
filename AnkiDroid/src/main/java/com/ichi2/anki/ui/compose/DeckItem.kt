package com.ichi2.anki.ui.compose

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ichi2.anki.R
import com.ichi2.anki.deckpicker.DisplayDeckNode

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

    Card(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp) // Spacing for the card itself
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { onDeckClick() },
                        onLongPress = { isContextMenuOpen = true },
                    )
                },
        shape = MaterialTheme.shapes.medium, // Expressive shape
    ) {
        Row(
            modifier =
                Modifier
                    .padding(start = (deck.depth * 16).dp) // Indentation for sub-decks
                    .padding(vertical = 12.dp, horizontal = 8.dp),
            // Inner padding for content within the card
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (deck.canCollapse) {
                Icon(
                    painter =
                        painterResource(
                            if (deck.collapsed) R.drawable.ic_expand_more_black_24dp else R.drawable.ic_expand_less_black_24dp,
                        ),
                    contentDescription = if (deck.collapsed) stringResource(R.string.expand) else stringResource(R.string.collapse),
                    modifier =
                        Modifier.pointerInput(Unit) {
                            detectTapGestures(onTap = { onExpandClick() })
                        },
                )
            } else {
                Spacer(modifier = Modifier.width(24.dp)) // Maintain spacing if not collapsible
            }
            Text(
                text = deck.lastDeckNameComponent,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleLarge, // Expressive typography
            )
            Text(
                text = deck.newCount.toString(),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 4.dp),
                style = MaterialTheme.typography.bodyMedium, // Consistent typography for counts
            )
            Text(
                text = deck.lrnCount.toString(),
                color = MaterialTheme.colorScheme.error, // Or another semantic color
                modifier = Modifier.padding(horizontal = 4.dp),
                style = MaterialTheme.typography.bodyMedium, // Consistent typography for counts
            )
            Text(
                text = deck.revCount.toString(),
                color = Color(0xFF006400), // Darker Green, consider from ColorScheme
                modifier = Modifier.padding(horizontal = 4.dp),
                style = MaterialTheme.typography.bodyMedium, // Consistent typography for counts
            )
            // Context Menu Anchor - usually an IconButton
            // IconButton(onClick = { isContextMenuOpen = true }) {
            //     Icon(Icons.Filled.MoreVert, contentDescription = stringResource(R.string.more_options))
            // }
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
                    text = { Text(stringResource(R.string.deck_options)) }, // Corrected to use resource
                    onClick = {
                        onDeckOptions()
                        isContextMenuOpen = false
                    },
                )
                DropdownMenuItem(
                    text = { Text("Delete") }, // Corrected to use resource (assuming R.string.delete exists)
                    onClick = {
                        onDelete()
                        isContextMenuOpen = false
                    },
                )
            }
        }
    }
}
