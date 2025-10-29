/* **************************************************************************************
 * Copyright (c) 2009 Andrew Dubya <andrewdubya@gmail.com>                              *
 * Copyright (c) 2009 Nicolas Raoul <nicolas.raoul@gmail.com>                           *
 * Copyright (c) 2009 Edu Zamora <edu.zasu@gmail.com>                                   *
 * Copyright (c) 2009 Daniel Svard <daniel.svard@gmail.com>                             *
 * Copyright (c) 2010 Norbert Nagold <norbert.nagold@gmail.com>                         *
 * Copyright (c) 2014 Timothy Rae <perceptualchaos2@gmail.com>
 *                                                                                      *
 * This program is free software; you can redistribute it and/or modify it under        *
 * the terms of the GNU General Public License as published by the Free Software        *
 * Foundation; either version 3 of the License, or (at your option) any later           *
 * version.                                                                             *
 *                                                                                      *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY      *
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A      *
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.             *
 *                                                                                      *
 * You should have received a copy of the GNU General Public License along with         *
 * this program.  If not, see <http://www.gnu.org/licenses/>.                           *
 ****************************************************************************************/
package com.ichi2.anki

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.ichi2.anki.CollectionManager.withCol
import com.ichi2.anki.dialogs.customstudy.CustomStudyDialog
import com.ichi2.anki.ui.compose.StudyOptionsData
import com.ichi2.anki.ui.compose.StudyOptionsScreen
import com.ichi2.anki.utils.ext.showDialogFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class StudyOptionsComposeActivity : AnkiActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var studyOptionsData by remember { mutableStateOf<StudyOptionsData?>(null) }

            LaunchedEffect(Unit) {
                studyOptionsData =
                    withContext(Dispatchers.IO) {
                        withCol {
                            val deckId = intent.getLongExtra(DECK_ID, decks.current().id)
                            decks.select(deckId)
                            val deck = decks.current()
                            val counts = sched.counts()
                            var buriedNew = 0
                            var buriedLearning = 0
                            var buriedReview = 0
                            val tree = sched.deckDueTree(deck.id)
                            if (tree != null) {
                                buriedNew = tree.newCount - counts.new
                                buriedLearning = tree.learnCount - counts.lrn
                                buriedReview = tree.reviewCount - counts.rev
                            }
                            StudyOptionsData(
                                deckId = deck.id,
                                deckName = deck.getString("name"),
                                deckDescription = deck.description,
                                newCount = counts.new,
                                lrnCount = counts.lrn,
                                revCount = counts.rev,
                                buriedNew = buriedNew,
                                buriedLrn = buriedLearning,
                                buriedRev = buriedReview,
                                totalNewCards = sched.totalNewForCurrentDeck(),
                                totalCards = decks.cardCount(deck.id, includeSubdecks = true),
                                isFiltered = deck.isFiltered,
                                haveBuried = sched.haveBuried(),
                            )
                        }
                    }
            }

            StudyOptionsScreen(
                studyOptionsData = studyOptionsData,
                onStartStudy = {
                    startActivity(Reviewer.getIntent(this))
                },
                onCustomStudy = { deckId ->
                    showDialogFragment(CustomStudyDialog.createInstance(deckId))
                },
            )
        }
    }
    companion object {
        const val DECK_ID = "deck_id"
    }
}
