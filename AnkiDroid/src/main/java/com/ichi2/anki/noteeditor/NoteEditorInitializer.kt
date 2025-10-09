package com.ichi2.anki.noteeditor

import android.content.Intent
import com.ichi2.anki.AnkiActivity
import com.ichi2.anki.libanki.Card
import com.ichi2.anki.libanki.Collection
import com.ichi2.anki.libanki.Note
import timber.log.Timber

data class InitialNoteEditorState(
    val note: Note,
    val currentCard: Card?,
    val caller: AnkiActivity.NoteEditorCaller
)

object NoteEditorInitializer {

    fun loadInitialState(
        activity: AnkiActivity,
        col: Collection,
        intent: Intent,
    ): InitialNoteEditorState? {
        var caller = AnkiActivity.NoteEditorCaller.fromValue(intent.getIntExtra(AnkiActivity.EXTRA_CALLER, AnkiActivity.NoteEditorCaller.NO_CALLER.value))
        if (caller == null) {
            val action = intent.action
            if (AnkiActivity.ACTION_CREATE_FLASHCARD == action || AnkiActivity.ACTION_CREATE_FLASHCARD_SEND == action || Intent.ACTION_PROCESS_TEXT == action) {
                caller = AnkiActivity.NoteEditorCaller.NOTEEDITOR_INTENT_ADD
            } else {
                Timber.e("Could not determine caller, closing")
                activity.finish()
                return null
            }
        }

        var note: Note? = null
        var card: Card? = null

        when (caller) {
            AnkiActivity.NoteEditorCaller.NO_CALLER -> {
                Timber.e("no caller could be identified, closing")
                activity.finish()
                return null
            }
            AnkiActivity.NoteEditorCaller.EDIT -> {
                val cardId = intent.getLongExtra(AnkiActivity.EXTRA_CARD_ID, -1)
                card = col.getCard(cardId)
                note = card?.note(col)
                if (note == null) {
                    Timber.e("Error loading note for editing, closing.")
                    activity.finish()
                    return null
                }
            }
            AnkiActivity.NoteEditorCaller.PREVIEWER_EDIT -> {
                val id = intent.getLongExtra(AnkiActivity.EXTRA_EDIT_FROM_CARD_ID, -1)
                card = col.getCard(id)
                note = card?.note(col)
                if (note == null) {
                    Timber.e("Error loading note for editing from previewer, closing.")
                    activity.finish()
                    return null
                }
            }
            AnkiActivity.NoteEditorCaller.STUDYOPTIONS,
            AnkiActivity.NoteEditorCaller.DECKPICKER,
            AnkiActivity.NoteEditorCaller.REVIEWER_ADD,
            AnkiActivity.NoteEditorCaller.CARDBROWSER_ADD,
            AnkiActivity.NoteEditorCaller.NOTEEDITOR,
            AnkiActivity.NoteEditorCaller.NOTEEDITOR_INTENT_ADD,
            AnkiActivity.NoteEditorCaller.INSTANT_NOTE_EDITOR,
            AnkiActivity.NoteEditorCaller.IMG_OCCLUSION,
            AnkiActivity.NoteEditorCaller.ADD_IMAGE -> {
                // Handled by the null check below
            }
        }

        if (note == null) {
            val notetype = col.notetypes.current()
            note = Note.fromNotetypeId(col, notetype.id)
        }

        return InitialNoteEditorState(note, card, caller)
    }
}