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
 * This program is distributed in the hope that in editing this file it will be useful, but WITHOUT ANY      *
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.FloatingToolbarDefaults.ScreenOffset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import anki.scheduler.CardAnswer
import com.ichi2.anki.R
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
                .offset(y = -ScreenOffset - 16.dp),
            expanded = true,
            colors = FloatingToolbarDefaults.vibrantFloatingToolbarColors(),
            ) {

            if (!state.isAnswerShown) {
                Button(onClick = { viewModel.onEvent(ReviewerEvent.ShowAnswer) }) {
                    Text("Show Answer")
                }
            } else {
                ButtonGroup(
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
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

                    customItem(
                        buttonGroupContent = {
                            var showMenu by remember { mutableStateOf(false) }
                            IconButton(onClick = { showMenu = !showMenu }) {
                                Icon(Icons.Filled.MoreVert, contentDescription = "More options")
                            }
                        },
                        menuContent = {
                            var showMenu by remember { mutableStateOf(false) }
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false }
                            ) {
                                val menuOptions = listOf(
                                    "Redo" to Icons.AutoMirrored.Filled.Undo,
                                    "Enable whiteboard" to Icons.Filled.Edit,
                                    "Edit note" to Icons.Filled.EditNote,
                                    "Edit tags" to Icons.AutoMirrored.Filled.Label,
                                    "Bury card" to Icons.Filled.VisibilityOff,
                                    "Suspend card" to Icons.Filled.Pause,
                                    "Delete note" to Icons.Filled.Delete,
                                    "Mark note" to Icons.Filled.Star,
                                    "Reschedule" to Icons.Filled.Schedule,
                                    "Replay media" to Icons.Filled.Replay,
                                    "Enable voice playback" to Icons.Filled.RecordVoiceOver,
                                    "Deck options" to Icons.Filled.Tune
                                )
                                menuOptions.forEach { (text, icon) ->
                                    DropdownMenuItem(
                                        text = { Text(text) },
                                        onClick = { /*TODO*/ },
                                        leadingIcon = { Icon(icon, contentDescription = null) }
                                    )
                                }
                            }
                        }
                    )

                    ratings.forEachIndexed { index, (_, rating) ->
                        customItem(
                            buttonGroupContent = {
                                val interactionSource = remember { MutableInteractionSource() }
                                Button(
                                    onClick = { viewModel.onEvent(ReviewerEvent.RateCard(rating)) },
                                    modifier = Modifier
                                        .animateWidth(interactionSource)
                                        .height(48.dp),
                                    contentPadding = ButtonDefaults.ExtraSmallContentPadding,
                                    shape = when (index) {
                                        0 -> ButtonGroupDefaults.connectedLeadingButtonShape
                                        3 -> ButtonGroupDefaults.connectedTrailingButtonShape
                                        else -> ButtonGroupDefaults.connectedMiddleButtonShapes().shape
                                    },
                                    interactionSource = interactionSource,
                                    colors = ButtonDefaults.buttonColors(
                                        MaterialTheme.colorScheme.secondaryContainer,
                                        MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                ) {
                                    Text(
                                        state.nextTimes[index],
                                        maxLines = 1,
                                        overflow = TextOverflow.Visible
                                    )
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
