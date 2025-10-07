package com.ichi2.anki.noteeditor

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector

sealed class ToolbarIcon {
    data class VectorIcon(val imageVector: ImageVector) : ToolbarIcon()
    data class BitmapIcon(val imageBitmap: ImageBitmap) : ToolbarIcon()
}

/**
 * A data class to represent a toolbar button.
 */
data class ToolbarButtonData(
    val id: String,
    val icon: ToolbarIcon,
    val contentDescription: String,
    val onClick: () -> Unit,
    val onLongClick: (() -> Unit)? = null
)

@Composable
fun NoteEditorToolbar(
    visible: Boolean,
    buttons: List<ToolbarButtonData>
) {
    AnimatedVisibility(
        visible = visible,
        enter = expandVertically(),
        exit = shrinkVertically()
    ) {
        LazyRow {
            items(buttons, key = { it.id }) { buttonData ->
                ToolbarButton(
                    icon = buttonData.icon,
                    contentDescription = buttonData.contentDescription,
                    onClick = buttonData.onClick,
                    onLongClick = buttonData.onLongClick
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ToolbarButton(icon: ToolbarIcon, contentDescription: String, onClick: () -> Unit, onLongClick: (() -> Unit)?) {
    IconButton(
        onClick = {}, // onClick is handled by combinedClickable
        modifier = Modifier.combinedClickable(
            onClick = onClick,
            onLongClick = onLongClick
        )
    ) {
        when (icon) {
            is ToolbarIcon.BitmapIcon -> Icon(bitmap = icon.imageBitmap, contentDescription = contentDescription)
            is ToolbarIcon.VectorIcon -> Icon(icon.imageVector, contentDescription = contentDescription)
        }
    }
}