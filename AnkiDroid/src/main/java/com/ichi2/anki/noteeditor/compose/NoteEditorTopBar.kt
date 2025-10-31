package com.ichi2.anki.noteeditor.compose

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * Represents an item displayed in the overflow menu of the note editor top app bar.
 */
sealed interface NoteEditorOverflowItem {
    val id: String
    val title: String
    val enabled: Boolean
    val visible: Boolean
}

@Immutable
data class NoteEditorSimpleOverflowItem(
    override val id: String,
    override val title: String,
    override val enabled: Boolean = true,
    override val visible: Boolean = true,
    val onClick: () -> Unit,
) : NoteEditorOverflowItem

@Immutable
data class NoteEditorToggleOverflowItem(
    override val id: String,
    override val title: String,
    val checked: Boolean,
    val onCheckedChange: (Boolean) -> Unit,
    override val enabled: Boolean = true,
    override val visible: Boolean = true,
) : NoteEditorOverflowItem

/**
 * Top app bar for the Compose note editor screen.
 * Provides navigation, primary actions (save / preview) and an overflow menu for secondary actions.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorTopAppBar(
    title: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    showSaveAction: Boolean = true,
    saveEnabled: Boolean = true,
    onSaveClick: (() -> Unit)? = null,
    showPreviewAction: Boolean = true,
    previewEnabled: Boolean = true,
    onPreviewClick: (() -> Unit)? = null,
    primaryActions: @Composable RowScope.() -> Unit = {},
    overflowItems: List<NoteEditorOverflowItem> = emptyList(),
) {
    var overflowExpanded by remember { mutableStateOf(false) }
    val visibleOverflowItems = remember(overflowItems) { overflowItems.filter { it.visible } }

    TopAppBar(
        modifier = modifier,
        windowInsets = WindowInsets.statusBars,
        title = {
            Text(
                text = title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleLarge,
            )
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                )
            }
        },
        actions = {
            if (showSaveAction && onSaveClick != null) {
                IconButton(
                    onClick = onSaveClick,
                    enabled = saveEnabled,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Save,
                        contentDescription = null,
                    )
                }
            }
            if (showPreviewAction && onPreviewClick != null) {
                IconButton(
                    onClick = onPreviewClick,
                    enabled = previewEnabled,
                ) {
                    Icon(
                        imageVector = Icons.Filled.RemoveRedEye,
                        contentDescription = null,
                    )
                }
            }
            primaryActions()
            if (visibleOverflowItems.isNotEmpty()) {
                IconButton(onClick = { overflowExpanded = true }) {
                    Icon(
                        imageVector = Icons.Filled.MoreVert,
                        contentDescription = null,
                    )
                }
                DropdownMenu(
                    expanded = overflowExpanded,
                    onDismissRequest = { overflowExpanded = false },
                ) {
                    visibleOverflowItems.forEach { item ->
                        when (item) {
                            is NoteEditorSimpleOverflowItem -> {
                                DropdownMenuItem(
                                    text = { Text(item.title) },
                                    enabled = item.enabled,
                                    onClick = {
                                        overflowExpanded = false
                                        item.onClick()
                                    },
                                )
                            }
                            is NoteEditorToggleOverflowItem -> {
                                DropdownMenuItem(
                                    text = { Text(item.title) },
                                    enabled = item.enabled,
                                    onClick = {
                                        overflowExpanded = false
                                        item.onCheckedChange(!item.checked)
                                    },
                                    trailingIcon = {
                                        Checkbox(
                                            checked = item.checked,
                                            enabled = item.enabled,
                                            onCheckedChange = { isChecked ->
                                                overflowExpanded = false
                                                item.onCheckedChange(isChecked)
                                            },
                                            modifier = Modifier.semantics {
                                                contentDescription = item.title
                                            },
                                        )
                                    },
                                )
                            }
                        }
                    }
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            actionIconContentColor = MaterialTheme.colorScheme.onSurface,
        ),
    )
}
