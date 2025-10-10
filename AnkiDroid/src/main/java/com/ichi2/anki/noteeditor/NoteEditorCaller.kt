
package com.ichi2.anki.noteeditor

enum class NoteEditorCaller(val value: Int) {
    IMG_OCCLUSION(0),
    DECKPICKER(1),
    CARDBROWSER_ADD(2),
    REVIEWER_ADD(3),
    INSTANT_NOTE_EDITOR(4),
    EDIT(5),
    PREVIEWER_EDIT(6),
    NOTEEDITOR(7);

    companion object {
        fun fromInt(value: Int) = entries.first { it.value == value }
    }
}
