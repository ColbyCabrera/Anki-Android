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

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.FloatingToolbarDefaults.ScreenOffset
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import anki.scheduler.CardAnswer
import com.ichi2.anim.ActivityTransitionAnimation
import com.ichi2.anki.R
import com.ichi2.anki.noteeditor.NoteEditorLauncher
import com.ichi2.anki.reviewer.ReviewerEffect
import com.ichi2.anki.reviewer.ReviewerEvent
import com.ichi2.anki.reviewer.ReviewerViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

private val ratings = listOf(
    "Again" to CardAnswer.Rating.AGAIN,
    "Hard" to CardAnswer.Rating.HARD,
    "Good" to CardAnswer.Rating.GOOD,
    "Easy" to CardAnswer.Rating.EASY
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ReviewerContent(viewModel: ReviewerViewModel) {
    val state by viewModel.state.collectAsState()
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var showBottomSheet by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val editCardLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        // We probably want to reload the card here
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is ReviewerEffect.NavigateToEditCard -> {
                    val intent = NoteEditorLauncher.EditCard(
                        effect.cardId,
                        ActivityTransitionAnimation.Direction.FADE
                    ).toIntent(context)
                    editCardLauncher.launch(intent)
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(topBar = {
            ReviewerTopBar(
                newCount = state.newCount,
                learnCount = state.learnCount,
                reviewCount = state.reviewCount,
                chosenAnswer = state.chosenAnswer,
                isMarked = state.isMarked,
                flag = state.flag,
                onToggleMark = { viewModel.onEvent(ReviewerEvent.ToggleMark) },
                onSetFlag = { viewModel.onEvent(ReviewerEvent.SetFlag(it)) },
                isAnswerShown = state.isAnswerShown
            ) { viewModel.onEvent(ReviewerEvent.UnanswerCard) }
        }) { paddingValues ->
            Flashcard(
                modifier = Modifier
                    .consumeWindowInsets(paddingValues)
                    .padding(top = paddingValues.calculateTopPadding()),
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
                isAnswerShown = state.isAnswerShown,
            )
        }
        HorizontalFloatingToolbar(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = -ScreenOffset - 16.dp),
            expanded = true,
            colors = FloatingToolbarDefaults.vibrantFloatingToolbarColors(),
        ) {
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
                            contentDescription = stringResource(R.string.more_options),
                        )
                    }
                }) {
                customItem(buttonGroupContent = {
                    val interactionSource = remember { MutableInteractionSource() }
                    IconButton(
                        onClick = { showBottomSheet = true },
                        modifier = Modifier
                            .animateWidth(interactionSource)
                            .height(48.dp),
                    ) {
                        Icon(
                            Icons.Filled.MoreVert,
                            contentDescription = stringResource(R.string.more_options)
                        )
                    }
                }, menuContent = {})

                if (!state.isAnswerShown) {
                    customItem(
                        buttonGroupContent = {
                            val interactionSource = remember { MutableInteractionSource() }
                            Button(
                                onClick = { viewModel.onEvent(ReviewerEvent.ShowAnswer) },
                                modifier = Modifier
                                    .animateWidth(interactionSource)
                                    .height(48.dp),
                                interactionSource = interactionSource,
                                colors = ButtonDefaults.buttonColors(
                                    MaterialTheme.colorScheme.secondaryContainer,
                                    MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            ) {
                                Text(
                                    text = stringResource(R.string.show_answer),
                                    softWrap = false,
                                    overflow = TextOverflow.Clip
                                )
                            }
                        },
                        menuContent = {},
                    )
                } else {
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
                                        softWrap = false,
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
        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = {
                    showBottomSheet = false
                },
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
            ) {
                val menuOptions = remember {
                    listOf(
                        Triple(R.string.redo, Icons.AutoMirrored.Filled.Undo) {
                        // TODO
                    }, Triple(R.string.enable_whiteboard, Icons.Filled.Edit) {
                        // TODO
                    }, Triple(R.string.cardeditor_title_edit_card, Icons.Filled.EditNote) {
                        viewModel.onEvent(ReviewerEvent.EditCard)
                    }, Triple(R.string.menu_edit_tags, Icons.AutoMirrored.Filled.Label) {
                        // TODO
                    }, Triple(R.string.menu_bury_card, Icons.Filled.VisibilityOff) {
                        viewModel.onEvent(ReviewerEvent.BuryCard)
                    }, Triple(R.string.menu_suspend_card, Icons.Filled.Pause) {
                        viewModel.onEvent(ReviewerEvent.SuspendCard)
                    }, Triple(R.string.menu_delete_note, Icons.Filled.Delete) {
                        // TODO
                    }, Triple(R.string.menu_mark_note, Icons.Filled.Star) {
                        viewModel.onEvent(ReviewerEvent.ToggleMark)
                    }, Triple(R.string.card_editor_reschedule_card, Icons.Filled.Schedule) {
                        // TODO
                    }, Triple(R.string.replay_media, Icons.Filled.Replay) {
                        // TODO
                    }, Triple(
                        R.string.menu_enable_voice_playback, Icons.Filled.RecordVoiceOver
                    ) {
                        // TODO
                    }, Triple(R.string.deck_options, Icons.Filled.Tune) {
                        // TODO
                    })
                }
                menuOptions.forEach { (textRes, icon, action) ->
                    ListItem(
                        headlineContent = { Text(stringResource(textRes)) },
                        leadingContent = { Icon(icon, contentDescription = null) },
                        modifier = Modifier.clickable {
                            scope.launch { sheetState.hide() }.invokeOnCompletion {
                                if (!sheetState.isVisible) {
                                    showBottomSheet = false
                                 }
                            }
                            action()
                        })
                }
            }
        }
    }
}