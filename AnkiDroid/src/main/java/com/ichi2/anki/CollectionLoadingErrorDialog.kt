package com.ichi2.anki

import android.os.Message
import com.ichi2.anki.dialogs.DatabaseErrorDialog.DatabaseErrorDialogType
import com.ichi2.anki.dialogs.DialogHandlerMessage

class CollectionLoadingErrorDialog : DialogHandlerMessage(
    WhichDialogHandler.MSG_SHOW_COLLECTION_LOADING_ERROR_DIALOG,
    "CollectionLoadingErrorDialog",
) {
    override fun handleAsyncMessage(activity: AnkiActivity) {
        // Collection could not be opened
        activity.showDatabaseErrorDialog(DatabaseErrorDialogType.DIALOG_LOAD_FAILED)
    }

    override fun toMessage() = emptyMessage(this.what)
}
