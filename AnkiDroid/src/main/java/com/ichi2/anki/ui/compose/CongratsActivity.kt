
package com.ichi2.anki.ui.compose

import android.os.Bundle
import androidx.activity.compose.setContent
import com.ichi2.anki.AnkiActivity

class CongratsActivity : AnkiActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CongratsScreen()
        }
    }
}
