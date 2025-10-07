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
    onFormat: (TextFormatter) -> Unit,
    onShowFontSizeDialog: () -> Unit,
    onShowHeadingsDialog: () -> Unit,
    showClozeButtons: Boolean,
    onClozeIncrement: () -> Unit,
    onClozeSame: () -> Unit,
    onAddCustomButtonClicked: () -> Unit,
    customButtons: List<ToolbarButtonData>
) {
    AnimatedVisibility(
        visible = visible,
        enter = expandVertically(),
        exit = shrinkVertically()
    ) {
        val defaultButtons = listOf(
            ToolbarButtonData("bold", ToolbarIcon.VectorIcon(Icons.Default.FormatBold), "Bold", onClick = { onFormat(TextWrapper("<b>", "</b>")) }),
            ToolbarButtonData("italic", ToolbarIcon.VectorIcon(Icons.Default.FormatItalic), "Italic", onClick = { onFormat(TextWrapper("<i>", "</i>")) }),
            ToolbarButtonData("underline", ToolbarIcon.VectorIcon(Icons.Default.FormatUnderlined), "Underline", onClick = { onFormat(TextWrapper("<u>", "</u>")) }),
            ToolbarButtonData("mathjax", ToolbarIcon.VectorIcon(Icons.Default.Code), "MathJax", onClick = { onFormat(TextWrapper("\\(", "\\)")) }),
            ToolbarButtonData("hr", ToolbarIcon.VectorIcon(Icons.Default.HorizontalRule), "Horizontal Rule", onClick = { onFormat(TextWrapper("<hr>", "")) }),
            ToolbarButtonData("font-size", ToolbarIcon.VectorIcon(Icons.Default.FormatSize), "Font Size", onClick = onShowFontSizeDialog),
            ToolbarButtonData("headings", ToolbarIcon.VectorIcon(Icons.Default.Title), "Headings", onClick = onShowHeadingsDialog)
        )

        val clozeButtons = if (showClozeButtons) {
            listOf(
                ToolbarButtonData("cloze_increment", ToolbarIcon.VectorIcon(Icons.Default.LooksOne), "Cloze (New)", onClick = onClozeIncrement),
                ToolbarButtonData("cloze_same", ToolbarIcon.VectorIcon(Icons.Default.LooksTwo), "Cloze (Same)", onClick = onClozeSame)
            )
        } else {
            emptyList()
        }

        val allButtons = customButtons.reversed() + clozeButtons + defaultButtons

        LazyRow {
            items(allButtons, key = { it.id }) { buttonData ->
                ToolbarButton(
                    icon = buttonData.icon,
                    contentDescription = buttonData.contentDescription,
                    onClick = buttonData.onClick,
                    onLongClick = buttonData.onLongClick
                )
            }
            item {
                ToolbarButton(
                    icon = ToolbarIcon.VectorIcon(Icons.Default.Add),
                    contentDescription = "Add Custom Button",
                    onClick = onAddCustomButtonClicked,
                    onLongClick = null
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