
package com.ichi2.anki.reviewer.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ichi2.anki.reviewer.ReviewerEvent
import com.ichi2.anki.reviewer.ReviewerViewModel

@Composable
fun ReviewerScreen(
    viewModel: ReviewerViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    Column {
        ReviewerTopBar(
            newCount = state.newCount,
            learnCount = state.learnCount,
            reviewCount = state.reviewCount,
            timer = state.timer,
            chosenAnswer = state.chosenAnswer,
            isMarked = state.isMarked,
            flag = state.flag,
            onToggleMark = { viewModel.onEvent(ReviewerEvent.ToggleMark) },
            onSetFlag = { flag -> viewModel.onEvent(ReviewerEvent.SetFlag(flag)) }
        )
        Flashcard(
            html = state.html,
            onTap = { viewModel.onEvent(ReviewerEvent.ShowAnswer) },
            onLinkClick = { url -> viewModel.onEvent(ReviewerEvent.LinkClicked(url)) },
            imageLoader = AnkiImageLoader(context, state.mediaDirectory),
            mediaDirectory = state.mediaDirectory
        )
    }
}