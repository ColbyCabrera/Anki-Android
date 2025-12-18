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
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.FloatingToolbarDefaults.ScreenOffset
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.motionScheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import anki.scheduler.CardAnswer
import com.ichi2.anim.ActivityTransitionAnimation
import com.ichi2.anki.R
import com.ichi2.anki.noteeditor.NoteEditorLauncher
import com.ichi2.anki.reviewer.ReviewerEffect
import com.ichi2.anki.reviewer.ReviewerEvent
import com.ichi2.anki.reviewer.ReviewerViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import com.ichi2.anki.Whiteboard as WhiteboardView

private val ratings = listOf(
    "Again" to CardAnswer.Rating.AGAIN,
    "Hard" to CardAnswer.Rating.HARD,
    "Good" to CardAnswer.Rating.GOOD,
    "Easy" to CardAnswer.Rating.EASY
)

// You can rename this class to be more descriptive
class InvertedTopCornersShape(private val cornerRadius: Dp) : Shape {
    override fun createOutline(
        size: Size, layoutDirection: LayoutDirection, density: Density
    ): Outline {
        val cornerRadiusPx = with(density) { cornerRadius.toPx() }

        val path = Path().apply {
            // --- Top-Left Corner Path ---
            moveTo(0f, 0f) // Start at the top-left point
            lineTo(cornerRadiusPx, 0f) // Line to the start of the arc
            // Arc from (r, 0) down to (0, r)
            arcTo(
                rect = Rect(
                    left = 0f, top = 0f, right = 2 * cornerRadiusPx, bottom = 2 * cornerRadiusPx
                ), startAngleDegrees = 270f,   // Top-center of the rect
                sweepAngleDegrees = -90f, // Sweep counter-clockwise
                forceMoveTo = false
            )
            // lineTo(0f, 0f) is implicitly added by close()
            close() // Close the path, drawing a line from (0, r) back to (0, 0)

            // --- Top-Right Corner Path ---
            moveTo(size.width, 0f) // Start at the top-right point
            lineTo(size.width - cornerRadiusPx, 0f) // Line to the start of the arc
            // Arc from (width - r, 0) down to (width, r)
            arcTo(
                rect = Rect(
                    left = size.width - 2 * cornerRadiusPx,
                    top = 0f,
                    right = size.width,
                    bottom = 2 * cornerRadiusPx
                ), startAngleDegrees = 270f, // Top-center of the rect
                sweepAngleDegrees = 90f,  // Sweep clockwise
                forceMoveTo = false
            )
            // lineTo(size.width, 0f) is implicitly added by close()
            close() // Close the path, drawing a line from (width, r) back to (width, 0)
        }
        return Outline.Generic(path)
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ReviewerContent(viewModel: ReviewerViewModel, whiteboard: WhiteboardView?) {
    val state by viewModel.state.collectAsState()
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var showBottomSheet by remember { mutableStateOf(false) }
    var toolbarHeight by remember { mutableIntStateOf(0) }
    val toolbarHeightDp = with(LocalDensity.current) { toolbarHeight.toDp() }
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val layoutDirection = LocalLayoutDirection.current

    val editCardLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        viewModel.onEvent(ReviewerEvent.ReloadCard)
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is ReviewerEffect.NavigateToEditCard -> {
                    val intent = NoteEditorLauncher.EditCard(
                        effect.cardId, ActivityTransitionAnimation.Direction.FADE
                    ).toIntent(context)
                    editCardLauncher.launch(intent)
                }

                is ReviewerEffect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(effect.message)
                }

                else -> {
                    // All other effects are handled by the Activity
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(snackbarHost = {
            SnackbarHost(
                snackbarHostState, modifier = Modifier.padding(bottom = toolbarHeightDp + 32.dp)
            ) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary,
                    actionColor = MaterialTheme.colorScheme.primary,
                    dismissActionContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }
        }, topBar = {
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
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .padding(
                        top = paddingValues.calculateTopPadding(),
                        start = paddingValues.calculateStartPadding(layoutDirection),
                        end = paddingValues.calculateEndPadding(layoutDirection)
                    ),
            ) {
                Box(
                    modifier = Modifier.background(MaterialTheme.colorScheme.background)
                ) {
                    val invertedTopCornersShape =
                        remember { InvertedTopCornersShape(cornerRadius = 32.dp) }

                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .zIndex(1F)
                            .height(100.dp)
                            .fillMaxWidth(),
                        shape = invertedTopCornersShape,
                        color = MaterialTheme.colorScheme.surfaceContainer
                    ) {}

                    Flashcard(
                        html = state.html,
                        onTap = { },
                        onLinkClick = {
                            viewModel.onEvent(ReviewerEvent.LinkClicked(it))
                        },
                        mediaDirectory = state.mediaDirectory,
                        isAnswerShown = state.isAnswerShown,
                        toolbarHeight = (toolbarHeightDp + 48.dp).value.toInt()
                    )

                    Whiteboard(
                        enabled = state.isWhiteboardEnabled,
                        whiteboard = whiteboard,
                        modifier = Modifier.padding(bottom = toolbarHeightDp + 48.dp)
                    )

                    HorizontalFloatingToolbar(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .offset(y = -ScreenOffset)
                            .padding(bottom = paddingValues.calculateBottomPadding())
                            .onSizeChanged { toolbarHeight = it.height },
                        expanded = true,
                        colors = FloatingToolbarDefaults.vibrantFloatingToolbarColors(),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { showBottomSheet = true },
                                modifier = Modifier.height(48.dp),
                            ) {
                                Icon(
                                    Icons.Filled.MoreVert,
                                    contentDescription = stringResource(R.string.more_options)
                                )
                            }
                            Box(
                                modifier = Modifier.animateContentSize(motionScheme.fastSpatialSpec())
                            ) {
                                if (!state.isAnswerShown) {
                                    val interactionSource = remember { MutableInteractionSource() }
                                    val isPressed by interactionSource.collectIsPressedAsState()
                                    val defaultHorizontalPadding =
                                        ButtonDefaults.MediumContentPadding.calculateLeftPadding(
                                            layoutDirection = LocalLayoutDirection.current
                                        )
                                    val horizontalPadding by animateDpAsState(
                                        if (isPressed) defaultHorizontalPadding + 4.dp else defaultHorizontalPadding,
                                        motionScheme.fastSpatialSpec()
                                    )
                                    Button(
                                        onClick = { viewModel.onEvent(ReviewerEvent.ShowAnswer) },
                                        modifier = Modifier.height(56.dp),
                                        interactionSource = interactionSource,
                                        contentPadding = PaddingValues(horizontal = horizontalPadding),
                                        colors = ButtonDefaults.buttonColors(
                                            MaterialTheme.colorScheme.primary,
                                            MaterialTheme.colorScheme.onPrimary
                                        )
                                    ) {
                                        Text(
                                            text = stringResource(R.string.show_answer),
                                            softWrap = false,
                                            overflow = TextOverflow.Clip
                                        )
                                    }
                                } else {
                                    ButtonGroup(
                                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                                        overflowIndicator = { }) {

                                        ratings.forEachIndexed { index, (_, rating) ->
                                            customItem(
                                                buttonGroupContent = {
                                                    val interactionSource =
                                                        remember { MutableInteractionSource() }
                                                    Button(
                                                        onClick = {
                                                            viewModel.onEvent(
                                                                ReviewerEvent.RateCard(
                                                                    rating
                                                                )
                                                            )
                                                        },
                                                        modifier = Modifier
                                                            .animateWidth(
                                                                interactionSource
                                                            )
                                                            .height(56.dp),
                                                        contentPadding = ButtonDefaults.ExtraSmallContentPadding,
                                                        shape = when (index) {
                                                            0 -> ButtonGroupDefaults.connectedLeadingButtonShape
                                                            3 -> ButtonGroupDefaults.connectedTrailingButtonShape
                                                            else -> ButtonGroupDefaults.connectedMiddleButtonShapes().shape
                                                        },
                                                        interactionSource = interactionSource,
                                                        colors = ButtonDefaults.buttonColors(
                                                            MaterialTheme.colorScheme.primary,
                                                            MaterialTheme.colorScheme.onPrimary
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
                        }
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
                val menuOptions =
                    remember(state.isWhiteboardEnabled, state.isVoicePlaybackEnabled) {
                        listOf(/*
                                THIS DOESN'T WORK AS UNDO ISN'T IMPLEMENTED
                                Triple(R.string.redo, Icons.AutoMirrored.Filled.Undo) {
                                    viewModel.onEvent(ReviewerEvent.Redo)
                                },
                                */
                            Triple(
                                if (state.isWhiteboardEnabled) R.string.disable_whiteboard else R.string.enable_whiteboard,
                                Icons.Filled.Edit
                            ) {
                                viewModel.onEvent(ReviewerEvent.ToggleWhiteboard)
                            }, Triple(R.string.cardeditor_title_edit_card, Icons.Filled.EditNote) {
                                viewModel.onEvent(ReviewerEvent.EditCard)
                            }, Triple(R.string.menu_edit_tags, Icons.AutoMirrored.Filled.Label) {
                                viewModel.onEvent(ReviewerEvent.EditTags)
                            }, Triple(R.string.menu_bury_card, Icons.Filled.VisibilityOff) {
                                viewModel.onEvent(ReviewerEvent.BuryCard)
                            }, Triple(R.string.menu_suspend_card, Icons.Filled.Pause) {
                                viewModel.onEvent(ReviewerEvent.SuspendCard)
                            }, Triple(R.string.menu_delete_note, Icons.Filled.Delete) {
                                viewModel.onEvent(ReviewerEvent.DeleteNote)
                            }, Triple(R.string.card_editor_reschedule_card, Icons.Filled.Schedule) {
                                viewModel.onEvent(ReviewerEvent.RescheduleCard)
                            }, Triple(R.string.replay_media, Icons.Filled.Replay) {
                                viewModel.onEvent(ReviewerEvent.ReplayMedia)
                            }, Triple(
                                if (state.isVoicePlaybackEnabled) R.string.menu_disable_voice_playback else R.string.menu_enable_voice_playback,
                                Icons.Filled.RecordVoiceOver
                            ) {
                                viewModel.onEvent(ReviewerEvent.ToggleVoicePlayback)
                            }, Triple(R.string.deck_options, Icons.Filled.Tune) {
                                viewModel.onEvent(ReviewerEvent.DeckOptions)
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
