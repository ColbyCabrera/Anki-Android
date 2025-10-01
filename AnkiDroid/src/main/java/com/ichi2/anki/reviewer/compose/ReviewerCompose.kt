package com.ichi2.anki.reviewer.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
                }
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