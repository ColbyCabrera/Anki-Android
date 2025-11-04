/* **************************************************************************************
 * Copyright (c) 2025 Colby Cabrera <colbycabrera.wd@gmail.com>                            *
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
package com.ichi2.anki.noteeditor

/**
 * Represents a button in the note editor toolbar.
 *
 * When the button is clicked, it inserts [prefix] before the current selection
 * and [suffix] after the current selection in the text field.
 */
data class ToolbarButtonModel(
    /** The position/order of the button in the toolbar */
    val index: Int,
    /** The label displayed on the button */
    val text: String,
    /** The text to insert before the current selection. Defaults to empty string. */
    val prefix: String = "",
    /** The text to insert after the current selection. Defaults to empty string. */
    val suffix: String = "",
)
