package com.ichi2.anki.ui.compose

import android.os.Bundle
import androidx.activity.compose.setContent
import com.ichi2.anki.AnkiActivity
import com.ichi2.anki.CollectionManager
import com.ichi2.anki.pages.DeckOptions
import timber.log.Timber

class CongratsActivity : AnkiActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            val col = CollectionManager.getColUnsafe()
            val timeUntilNextDay =
                (col.sched.dayCutoff * 1000 - System.currentTimeMillis()).coerceAtLeast(0L)

            setContent {
                CongratsScreen(
                    onDeckOptions = {
                    val intent = DeckOptions.getIntent(this, col.decks.current().id)
                    startActivity(intent)
                }, onBack = {
                    finish()
                }, timeUntilNextDay = timeUntilNextDay
                )
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting collection in CongratsActivity")
            finish()
        }
    }
}