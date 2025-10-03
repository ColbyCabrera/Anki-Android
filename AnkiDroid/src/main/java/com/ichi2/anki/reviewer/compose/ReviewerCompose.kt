/****************************************************************************************
 * Copyright (c) 2009 Edu Zamora <edu.zasu@gmail.com>                                   *
 * Copyright (c) 2009 Casey Link <unnamedrambler@gmail.com>                             *
 * Copyright (c) 2014 Timothy Rae <perceptualchaos2@gmail.com>                          *
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
package com.ichi2.anki.reviewer.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import anki.scheduler.CardAnswer.Rating
import com.ichi2.anki.reviewer.ReviewerEvent
import com.ichi2.anki.reviewer.ReviewerViewModel

@Composable
fun ReviewerContent(viewModel: ReviewerViewModel) {
    val state by viewModel.state.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        ReviewerTopBar(
            newCount = state.newCount,
            learnCount = state.learnCount,
            reviewCount = state.reviewCount,
            timer = state.timer,
            chosenAnswer = state.chosenAnswer,
            isMarked = state.isMarked,
            flag = state.flag,
            onToggleMark = { viewModel.onEvent(ReviewerEvent.ToggleMark) },
            onSetFlag = { viewModel.onEvent(ReviewerEvent.SetFlag(it)) }
        )
        Box(modifier = Modifier.weight(1f)) {
            Flashcard(
                html = state.html,
                onTap = {
                    if (!state.isAnswerShown) {
                        viewModel.onEvent(ReviewerEvent.ShowAnswer)
                    }
                },
                onLinkClick = {
                    viewModel.onEvent(ReviewerEvent.LinkClicked(it))
                },
                mediaDirectory = state.mediaDirectory
            )
        }
        AnswerButtons(
            isAnswerShown = state.isAnswerShown,
            showTypeInAnswer = state.showTypeInAnswer,
            onShowAnswer = { viewModel.onEvent(ReviewerEvent.ShowAnswer) },
            onAgain = { viewModel.onEvent(ReviewerEvent.RateCard(Rating.AGAIN)) },
            onHard = { viewModel.onEvent(ReviewerEvent.RateCard(Rating.HARD)) },
            onGood = { viewModel.onEvent(ReviewerEvent.RateCard(Rating.GOOD)) },
            onEasy = { viewModel.onEvent(ReviewerEvent.RateCard(Rating.EASY)) },
            nextTimes = state.nextTimes,
            typedAnswer = state.typedAnswer,
            onTypedAnswerChanged = { viewModel.onEvent(ReviewerEvent.OnTypedAnswerChanged(it)) }
        )
    }
}