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
package com.ichi2.anki

/**
 * Enum to identify the caller of the NoteEditor.
 */
enum class NoteEditorCaller(val value: Int) {
    DECKPICKER(0),
    REVIEWER_ADD(1),
    REVIEWER_EDIT(2), // Legacy, likely unused or mapped to EDIT
    BROWSER(3), // Legacy
    WIDGET(4), // Legacy
    INTENT(5), // Legacy
    NOTEEDITOR(6), // Copy note
    ADD_IMAGE(7),
    IMG_OCCLUSION(8),
    CARDBROWSER_ADD(9),
    EDIT(10),
    PREVIEWER_EDIT(11),
    INSTANT_NOTE_EDITOR(12);

    companion object {
        fun fromInt(value: Int): NoteEditorCaller? = entries.find { it.value == value }
    }
}
