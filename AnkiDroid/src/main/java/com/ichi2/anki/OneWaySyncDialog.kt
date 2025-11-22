package com.ichi2.anki

import android.os.Message
import androidx.core.os.bundleOf
import com.ichi2.anki.CollectionManager.withCol
import com.ichi2.anki.dialogs.ConfirmationDialog
import com.ichi2.anki.dialogs.DialogHandlerMessage
import com.ichi2.anki.utils.ext.showDialogFragment

class OneWaySyncDialog(
    val message: String?,
) : DialogHandlerMessage(
    which = WhichDialogHandler.MSG_SHOW_ONE_WAY_SYNC_DIALOG,
    analyticName = "OneWaySyncDialog",
) {
    override fun handleAsyncMessage(activity: AnkiActivity) {
        // Confirmation dialog for one-way sync
        val dialog = ConfirmationDialog()
        val confirm = Runnable {
            // Bypass the check once the user confirms
            activity.launchCatchingTask {
                withCol { modSchemaNoCheck() }
            }
        }
        dialog.setConfirm(confirm)
        dialog.setArgs(message)
        activity.showDialogFragment(dialog)
    }

    override fun toMessage(): Message = Message.obtain().apply {
        what = this@OneWaySyncDialog.what
        data = bundleOf("message" to message)
    }

    companion object {
        fun fromMessage(message: Message): DialogHandlerMessage =
            OneWaySyncDialog(message.data.getString("message"))
    }
}
