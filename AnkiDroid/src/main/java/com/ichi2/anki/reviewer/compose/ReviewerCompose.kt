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

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FloatingToolbarHorizontalFabPosition
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import anki.scheduler.CardAnswer
import com.ichi2.anki.reviewer.ReviewerEvent
import com.ichi2.anki.reviewer.ReviewerViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ReviewerContent(viewModel: ReviewerViewModel) {
    val state by viewModel.state.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                ReviewerTopBar(
                    newCount = state.newCount,
                    learnCount = state.learnCount,
                    reviewCount = state.reviewCount,
                    timer = state.timer,
                    chosenAnswer = state.chosenAnswer,
                    isMarked = state.isMarked,
                    flag = state.flag,
                    onToggleMark = { viewModel.onEvent(ReviewerEvent.ToggleMark) },
                    onSetFlag = { viewModel.onEvent(ReviewerEvent.SetFlag(it)) })
            }) { paddingValues ->
            Flashcard(
                html = state.html, onTap = {
                if (!state.isAnswerShown) {
                    viewModel.onEvent(ReviewerEvent.ShowAnswer)
                }
            }, onLinkClick = {
                viewModel.onEvent(ReviewerEvent.LinkClicked(it))
            }, mediaDirectory = state.mediaDirectory, modifier = Modifier.padding(paddingValues)
            )
        }
        HorizontalFloatingToolbar(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = (-16).dp),
            expanded = true, // Always expanded to show content
            floatingActionButton = {
                ExpandableReviewerFab(
                    onEdit = { viewModel.onEvent(ReviewerEvent.EditCard) },
                    onBury = { viewModel.onEvent(ReviewerEvent.BuryCard) },
                    onSuspend = { viewModel.onEvent(ReviewerEvent.SuspendCard) })
            },
            floatingActionButtonPosition = FloatingToolbarHorizontalFabPosition.Start,
        ) {
            if (!state.isAnswerShown) {
                Button(onClick = { viewModel.onEvent(ReviewerEvent.ShowAnswer) }) {
                    Text("Show Answer")
                }
            } else {
                ButtonGroup(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    overflowIndicator = { menuState ->
                        FilledIconButton(
                            onClick = {
                                if (menuState.isExpanded) {
                                    menuState.dismiss()
                                } else {
                                    menuState.show()
                                }
                            }) {
                            Icon(
                                imageVector = Icons.Filled.MoreVert,
                                contentDescription = "Localized description",
                            )
                        }
                    }) {
                    val ratings = listOf(
                        "Again" to CardAnswer.Rating.AGAIN,
                        "Hard" to CardAnswer.Rating.HARD,
                        "Good" to CardAnswer.Rating.GOOD,
                        "Easy" to CardAnswer.Rating.EASY
                    )
                    ratings.forEachIndexed { index, (_, rating) ->
                        customItem(
                            buttonGroupContent = {
                                val interactionSource = remember { MutableInteractionSource() }
                                Button(
                                    onClick = { viewModel.onEvent(ReviewerEvent.RateCard(rating)) },
                                    modifier = Modifier.animateWidth(interactionSource),
                                    contentPadding = ButtonDefaults.SmallContentPadding,
                                    shape = ButtonDefaults.squareShape,
                                    interactionSource = interactionSource
                                ) {
                                    Text(state.nextTimes[index])
                                }
                            },
                            menuContent = {},
                        )
                    }
                }
            }
        }
    }
}
