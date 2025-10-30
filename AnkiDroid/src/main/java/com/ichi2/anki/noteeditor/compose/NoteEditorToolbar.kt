/* **************************************************************************************
 * Copyright (c) 2025 Colby Cabrera <colbycabrera@gmail.com>                            *
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
package com.ichi2.anki.noteeditor.compose

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatUnderlined
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ichi2.anki.R
import com.ichi2.anki.noteeditor.ToolbarButtonModel

/**
 * Formatting toolbar for the note editor
 */
@Composable
fun NoteEditorToolbar(
    modifier: Modifier = Modifier,
    isClozeType: Boolean,
    onBoldClick: () -> Unit,
    onItalicClick: () -> Unit,
    onUnderlineClick: () -> Unit,
    onHorizontalRuleClick: () -> Unit = {},
    onHeadingClick: () -> Unit = {},
    onFontSizeClick: () -> Unit = {},
    onMathjaxClick: () -> Unit = {},
    onMathjaxLongClick: (() -> Unit)? = null,
    onClozeClick: () -> Unit = {},
    onClozeIncrementClick: () -> Unit = {},
    onCustomButtonClick: (ToolbarButtonModel) -> Unit = {},
    onCustomButtonLongClick: (ToolbarButtonModel) -> Unit = {},
    onAddCustomButtonClick: () -> Unit = {},
    customButtons: List<ToolbarButtonModel> = emptyList(),
    isVisible: Boolean = true,
) {
    if (!isVisible) return

    BottomAppBar(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Basic formatting buttons
            ToolbarIconButton(
                icon = Icons.Default.FormatBold,
                contentDescription = stringResource(R.string.format_insert_bold),
                onClick = onBoldClick
            )
            ToolbarIconButton(
                icon = Icons.Default.FormatItalic,
                contentDescription = stringResource(R.string.format_insert_italic),
                onClick = onItalicClick
            )
            ToolbarIconButton(
                icon = Icons.Default.FormatUnderlined,
                contentDescription = stringResource(R.string.format_insert_underline),
                onClick = onUnderlineClick
            )

            ToolbarIconButton(
                painter = painterResource(R.drawable.ic_horizontal_rule_black_24dp),
                contentDescription = stringResource(R.string.insert_horizontal_line),
                onClick = onHorizontalRuleClick
            )

            ToolbarIconButton(
                painter = painterResource(R.drawable.ic_format_title_black_24dp),
                contentDescription = stringResource(R.string.insert_heading),
                onClick = onHeadingClick
            )

            ToolbarIconButton(
                painter = painterResource(R.drawable.ic_format_font_size_24dp),
                contentDescription = stringResource(R.string.format_font_size),
                onClick = onFontSizeClick
            )

            ToolbarIconButton(
                painter = painterResource(R.drawable.ic_add_equation_black_24dp),
                contentDescription = stringResource(R.string.insert_mathjax),
                onClick = onMathjaxClick,
                onLongClick = onMathjaxLongClick
            )

            // Cloze buttons (if cloze note type)
            if (isClozeType) {
                ToolbarIconButton(
                    painter = painterResource(R.drawable.ic_cloze_new_card),
                    contentDescription = stringResource(R.string.multimedia_editor_popup_cloze),
                    onClick = onClozeIncrementClick
                )
                ToolbarIconButton(
                    painter = painterResource(R.drawable.ic_cloze_same_card),
                    contentDescription = stringResource(R.string.multimedia_editor_popup_cloze),
                    onClick = onClozeClick
                )
            }

            // Custom toolbar buttons
            customButtons.forEach { button ->
                val displayText = button.text.ifEmpty { (button.index + 1).toString() }
                ToolbarTextButton(
                    text = displayText,
                    contentDescription = displayText,
                    onClick = { onCustomButtonClick(button) },
                    onLongClick = { onCustomButtonLongClick(button) }
                )
            }

            // Add custom button
            ToolbarIconButton(
                icon = Icons.Default.Add,
                contentDescription = stringResource(R.string.add_toolbar_item),
                onClick = onAddCustomButtonClick
            )
        }
    }
}

/**
 * Icon button for toolbar using Material3 IconButton
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalFoundationApi::class)
@Composable
private fun ToolbarIconButton(
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    painter: androidx.compose.ui.graphics.painter.Painter? = null,
    contentDescription: String,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
) {
    if (onLongClick != null) {
        // For buttons with long click, we still need combinedClickable
        val interactionSource = remember { MutableInteractionSource() }
        IconButton(
            onClick = onClick,
            shapes = IconButtonDefaults.shapes(),
            modifier = modifier.combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
                onLongClick = onLongClick
            ),
            colors = IconButtonDefaults.iconButtonColors(
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        ) {
            when {
                icon != null -> Icon(
                    imageVector = icon,
                    contentDescription = contentDescription
                )
                painter != null -> Icon(
                    painter = painter,
                    contentDescription = contentDescription
                )
            }
        }
    } else {
        // Standard IconButton for buttons without long click
        IconButton(
            onClick = onClick,
            shapes = IconButtonDefaults.shapes(),
            modifier = modifier,
            colors = IconButtonDefaults.iconButtonColors(
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        ) {
            when {
                icon != null -> Icon(
                    imageVector = icon,
                    contentDescription = contentDescription
                )
                painter != null -> Icon(
                    painter = painter,
                    contentDescription = contentDescription
                )
            }
        }
    }
}

/**
 * Text button for custom toolbar buttons (still needs combinedClickable for long press)
 */
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ToolbarTextButton(
    modifier: Modifier = Modifier,
    text: String,
    contentDescription: String,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    IconButton(
        onClick = onClick,
        shapes = IconButtonDefaults.shapes(),
        modifier = modifier.combinedClickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick,
            onLongClick = onLongClick
        ),
        colors = IconButtonDefaults.iconButtonColors(
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge.copy(fontSize = 16.sp)
        )
    }
}

@Preview
@Composable
fun NoteEditorToolbarPreview() {
    MaterialTheme {
        Surface {
            NoteEditorToolbar(
                isClozeType = true,
                onBoldClick = {},
                onItalicClick = {},
                onUnderlineClick = {},
                onHorizontalRuleClick = {},
                onHeadingClick = {},
                onFontSizeClick = {},
                onMathjaxClick = {},
                onMathjaxLongClick = {},
                onClozeClick = {},
                onClozeIncrementClick = {},
                customButtons = listOf(
                    ToolbarButtonModel(index = 0, text = "1", prefix = "<b>", suffix = "</b>"),
                    ToolbarButtonModel(index = 1, text = "2", prefix = "<i>", suffix = "</i>")
                )
            )
        }
    }
}
