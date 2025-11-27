package com.ichi2.anki.noteeditor

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
object NoteEditorRoute : NavKey

@Serializable
data class PreviewerRoute(val cardId: Long) : NavKey
