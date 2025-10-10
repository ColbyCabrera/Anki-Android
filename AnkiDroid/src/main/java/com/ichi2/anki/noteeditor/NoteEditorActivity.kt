
package com.ichi2.anki.noteeditor

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import com.ichi2.anki.AnkiDroidApp
import com.ichi2.anki.theme.AnkiTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NoteEditorActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AnkiTheme {
                NoteEditorScreen(
                    onTagsClick = { /*TODO*/ },
                    onCardsClick = { /*TODO*/ },
                    onMediaClick = { /*TODO*/ }
                )
            }
        }
    }

    companion object {
        const val EXTRA_CALLER = "caller"
        const val EXTRA_IMG_OCCLUSION = "image_occlusion"
        const val EXTRA_DID = "deck_id"
        const val EXTRA_TEXT_FROM_SEARCH_VIEW = "text_from_search_view"
        const val IN_CARD_BROWSER_ACTIVITY = "in_card_browser_activity"
        const val FRAGMENT_ARGS_EXTRA = "fragment_args_extra"
        const val EXTRA_CARD_ID = "card_id"
        const val EXTRA_EDIT_FROM_CARD_ID = "edit_from_card_id"
        const val EXTRA_CONTENTS = "contents"
        const val EXTRA_TAGS = "tags"
        const val RELOAD_REQUIRED_EXTRA_KEY = "reload_required"
        const val NOTE_CHANGED_EXTRA_KEY = "note_changed"


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
                fun fromInt(value: Int) = values().first { it.value == value }
            }
        }
    }
}
