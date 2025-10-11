package com.ichi2.anki.ui.compose

import android.os.Bundle
import androidx.activity.compose.setContent
import com.ichi2.anki.AnkiActivity
import com.ichi2.anki.CollectionManager
import com.ichi2.anki.pages.DeckOptions

class CongratsActivity : AnkiActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CongratsScreen(onDeckOptions = {
                val col = CollectionManager.getColUnsafe()
                val intent = DeckOptions.getIntent(this, col.decks.current().id)
                startActivity(intent)
            }, onBack = {
                finish()
            })
        }
    }
}