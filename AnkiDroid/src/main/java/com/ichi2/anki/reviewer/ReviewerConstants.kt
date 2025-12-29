package com.ichi2.anki.reviewer

/**
 * Constants used across the Reviewer component.
 */
object ReviewerConstants {
    /** Default duration (ms) for action snackbars (undo, bury, suspend) */
    const val ACTION_SNACKBAR_DURATION_MS = 500

    /** Request code for audio recording permission */
    const val REQUEST_AUDIO_PERMISSION = 0

    /** Delay (ms) before retrying displayCardAnswer when waiting for state mutation */
    const val STATE_MUTATION_RETRY_DELAY_MS = 50L
}
