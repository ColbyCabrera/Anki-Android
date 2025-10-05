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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import anki.scheduler.CardAnswer
import com.ichi2.anki.reviewer.ReviewerEvent
import com.ichi2.anki.reviewer.ReviewerViewModel

@Composable
fun AppBarRow(
    overflowIndicator: @Composable () -> Unit,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        content()
        overflowIndicator()
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
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
                    onSetFlag = { viewModel.onEvent(ReviewerEvent.SetFlag(it)) }
                )
            }
        ) { paddingValues ->
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
                mediaDirectory = state.mediaDirectory,
                modifier = Modifier.padding(paddingValues)
            )
        }
        HorizontalFloatingToolbar(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = (-16).dp),
            expanded = true, // Always expanded to show content
            leadingContent = {
                ExpandableReviewerFab(
                    onEdit = { viewModel.onEvent(ReviewerEvent.EditCard) },
                    onBury = { viewModel.onEvent(ReviewerEvent.BuryCard) },
                    onSuspend = { viewModel.onEvent(ReviewerEvent.SuspendCard) }
                )
            },
            content = {
                if (!state.isAnswerShown) {
                    Button(onClick = { viewModel.onEvent(ReviewerEvent.ShowAnswer) }) {
                        Text("Show Answer")
                    }
                } else {
                    IconButton(onClick = { /* TODO: Maybe a different action? */ }) {
                        Icon(Icons.Filled.Replay, contentDescription = "Flip Card")
                    }
                }
            },
            trailingContent = {
                AnimatedVisibility(
                    visible = state.isAnswerShown,
                    enter = slideInHorizontally(initialOffsetX = { it / 2 }) + fadeIn(),
                    exit = slideOutHorizontally(targetOffsetX = { it / 2 }) + fadeOut()
                ) {
                    AppBarRow(
                        overflowIndicator = {
                            IconButton(onClick = { /*TODO*/ }) {
                                Icon(
                                    Icons.Default.MoreVert,
                                    contentDescription = "More Actions"
                                )
                            }
                        }
                    ) {
                        Button(onClick = { viewModel.onEvent(ReviewerEvent.RateCard(CardAnswer.Rating.AGAIN)) }) {
                            Text("Again")
                        }
                        Button(onClick = { viewModel.onEvent(ReviewerEvent.RateCard(CardAnswer.Rating.HARD)) }) {
                            Text("Hard")
                        }
                        Button(onClick = { viewModel.onEvent(ReviewerEvent.RateCard(CardAnswer.Rating.GOOD)) }) {
                            Text("Good")
                        }
                        Button(onClick = { viewModel.onEvent(ReviewerEvent.RateCard(CardAnswer.Rating.EASY)) }) {
                            Text("Easy")
                        }
                    }
                }
            }
        )
    }
}