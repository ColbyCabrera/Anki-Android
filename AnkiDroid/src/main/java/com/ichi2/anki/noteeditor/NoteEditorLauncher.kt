
package com.ichi2.anki.noteeditor

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import androidx.core.os.bundleOf
import com.ichi2.anim.ActivityTransitionAnimation
import com.ichi2.anki.AnkiActivity
import com.ichi2.anki.browser.CardBrowserViewModel
import com.ichi2.anki.libanki.CardId
import com.ichi2.anki.libanki.DeckId
import com.ichi2.anki.utils.Destination

sealed interface NoteEditorLauncher : Destination {
    override fun toIntent(context: Context): Intent = toIntent(context, action = null)

    fun toIntent(
        context: Context,
        action: String? = null,
    ) = Intent(context, NoteEditorActivity::class.java).apply {
        putExtras(toBundle())
        action?.let { this.action = it }
    }

    fun toBundle(): Bundle

    data class ImageOcclusion(
        val imageUri: Uri?,
    ) : NoteEditorLauncher {
        override fun toBundle(): Bundle =
            bundleOf(
                NoteEditorActivity.EXTRA_CALLER to NoteEditorCaller.IMG_OCCLUSION.value,
                NoteEditorActivity.EXTRA_IMG_OCCLUSION to imageUri,
            )
    }

    data class PassArguments(
        val arguments: Bundle,
    ) : NoteEditorLauncher {
        override fun toBundle(): Bundle = arguments
    }

    data class AddNote(
        val deckId: DeckId? = null,
    ) : NoteEditorLauncher {
        override fun toBundle(): Bundle =
            bundleOf(
                NoteEditorActivity.EXTRA_CALLER to NoteEditorCaller.DECKPICKER.value,
            ).also { bundle ->
                deckId?.let { deckId -> bundle.putLong(NoteEditorActivity.EXTRA_DID, deckId) }
            }
    }

    data class AddNoteFromCardBrowser(
        val viewModel: CardBrowserViewModel,
        val inCardBrowserActivity: Boolean = false,
    ) : NoteEditorLauncher {
        override fun toBundle(): Bundle {
            val fragmentArgs =
                bundleOf(
                    NoteEditorActivity.EXTRA_CALLER to NoteEditorCaller.CARDBROWSER_ADD.value,
                    NoteEditorActivity.EXTRA_TEXT_FROM_SEARCH_VIEW to viewModel.searchTerms,
                    NoteEditorActivity.IN_CARD_BROWSER_ACTIVITY to inCardBrowserActivity,
                )
            if (viewModel.lastDeckId?.let { id -> id > 0 } == true) {
                fragmentArgs.putLong(NoteEditorActivity.EXTRA_DID, viewModel.lastDeckId!!)
            }
            return bundleOf(
                NoteEditorActivity.FRAGMENT_ARGS_EXTRA to fragmentArgs,
            )
        }
    }

    data class AddNoteFromReviewer(
        val animation: ActivityTransitionAnimation.Direction? = null,
    ) : NoteEditorLauncher {
        override fun toBundle(): Bundle {
            val fragmentArgs =
                bundleOf(
                    NoteEditorActivity.EXTRA_CALLER to NoteEditorCaller.REVIEWER_ADD.value,
                ).also { bundle ->
                    animation?.let { animation ->
                        bundle.putParcelable(
                            AnkiActivity.FINISH_ANIMATION_EXTRA,
                            animation as Parcelable,
                        )
                    }
                }

            return bundleOf(
                NoteEditorActivity.FRAGMENT_ARGS_EXTRA to fragmentArgs,
            )
        }
    }

    data class AddInstantNote(
        val sharedText: String,
    ) : NoteEditorLauncher {
        override fun toBundle(): Bundle =
            bundleOf(
                NoteEditorActivity.EXTRA_CALLER to NoteEditorCaller.INSTANT_NOTE_EDITOR.value,
                Intent.EXTRA_TEXT to sharedText,
            )
    }

    data class EditCard(
        val cardId: CardId,
        val animation: ActivityTransitionAnimation.Direction,
        val inCardBrowserActivity: Boolean = false,
    ) : NoteEditorLauncher {
        override fun toBundle(): Bundle =
            bundleOf(
                NoteEditorActivity.EXTRA_CALLER to NoteEditorCaller.EDIT.value,
                NoteEditorActivity.EXTRA_CARD_ID to cardId,
                AnkiActivity.FINISH_ANIMATION_EXTRA to animation as Parcelable,
                NoteEditorActivity.IN_CARD_BROWSER_ACTIVITY to inCardBrowserActivity,
            )
    }

    data class EditNoteFromPreviewer(
        val cardId: CardId,
    ) : NoteEditorLauncher {
        override fun toBundle(): Bundle =
            bundleOf(
                NoteEditorActivity.EXTRA_CALLER to NoteEditorCaller.PREVIEWER_EDIT.value,
                NoteEditorActivity.EXTRA_EDIT_FROM_CARD_ID to cardId,
            )
    }

    data class CopyNote(
        val deckId: DeckId,
        val fieldsText: String,
        val tags: List<String>? = null,
    ) : NoteEditorLauncher {
        override fun toBundle(): Bundle =
            bundleOf(
                NoteEditorActivity.EXTRA_CALLER to NoteEditorCaller.NOTEEDITOR.value,
                NoteEditorActivity.EXTRA_DID to deckId,
                NoteEditorActivity.EXTRA_CONTENTS to fieldsText,
            ).also { bundle ->
                tags?.let { tags -> bundle.putStringArray(NoteEditorActivity.EXTRA_TAGS, tags.toTypedArray()) }
            }
    }
}
