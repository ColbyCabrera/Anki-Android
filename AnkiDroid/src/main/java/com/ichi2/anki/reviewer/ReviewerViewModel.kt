/*
 * Copyright (c) 2024 Brayan Oliveira <brayandso.dev@gmail.com>
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.ichi2.anki.reviewer

import android.app.Application
import android.content.Intent
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import anki.scheduler.CardAnswer
import com.ichi2.anki.CollectionManager
import com.ichi2.anki.cardviewer.TypeAnswer
import com.ichi2.anki.libanki.Card
import com.ichi2.anki.libanki.sched.CurrentQueueState
import com.ichi2.anki.preferences.sharedPrefs
import com.ichi2.anki.servicelayer.NoteService
import java.io.File
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ReviewerState(
    val newCount: Int = 0,
    val learnCount: Int = 0,
    val reviewCount: Int = 0,
    val timer: String = "0.0s",
    val chosenAnswer: String = "",
    val isAnswerShown: Boolean = false,
    val html: String = "<html><body><h1>Loading...</h1></body></html>",
    val nextTimes: List<String> = List(4) { "" },
    val showTypeInAnswer: Boolean = false,
    val typedAnswer: String = "",
    val isMarked: Boolean = false,
    val flag: Int = 0,
    val mediaDirectory: File? = null
)

sealed class ReviewerEvent {
    object ShowAnswer : ReviewerEvent()
    data class RateCard(val rating: CardAnswer.Rating) : ReviewerEvent()
    object LoadInitialCard : ReviewerEvent()
    data class OnTypedAnswerChanged(val newText: String) : ReviewerEvent()
    object ToggleMark : ReviewerEvent()
    data class SetFlag(val flag: Int) : ReviewerEvent()
    data class LinkClicked(val url: String) : ReviewerEvent()
}

class ReviewerViewModel(app: Application) : AndroidViewModel(app) {
    private val _state = MutableStateFlow(ReviewerState())
    val state: StateFlow<ReviewerState> = _state.asStateFlow()

    private var currentCard: Card? = null
    private var queueState: CurrentQueueState? = null
    private var timerJob: Job? = null
    private val typeAnswer = TypeAnswer.createInstance(app.sharedPrefs())

    init {
        onEvent(ReviewerEvent.LoadInitialCard)
    }

    fun onEvent(event: ReviewerEvent) {
        when (event) {
            is ReviewerEvent.ShowAnswer -> showAnswer()
            is ReviewerEvent.RateCard -> rateCard(event.rating)
            is ReviewerEvent.LoadInitialCard -> loadCard()
            is ReviewerEvent.OnTypedAnswerChanged -> onTypedAnswerChanged(event.newText)
            is ReviewerEvent.ToggleMark -> toggleMark()
            is ReviewerEvent.SetFlag -> setFlag(event.flag)
            is ReviewerEvent.LinkClicked -> linkClicked(event.url)
        }
    }

    private fun linkClicked(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, url.toUri())
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        getApplication<Application>().startActivity(intent)
    }

    private fun onTypedAnswerChanged(newText: String) {
        _state.update { it.copy(typedAnswer = newText) }
    }

    private fun loadCard() {
        viewModelScope.launch {
            val cardAndQueueState = getNextCard()
            if (cardAndQueueState == null) {
                _state.update { it.copy(html = "<html><body><h1>Finished!</h1></body></html>") }
                return@launch
            }
            val (card, queue) = cardAndQueueState
            currentCard = card
            queueState = queue
            CollectionManager.withCol {
                val note = card.note(this)
                typeAnswer.updateInfo(this, card, getApplication<Application>().resources)
                _state.update {
                    it.copy(
                        newCount = queue.counts.new,
                        learnCount = queue.counts.lrn,
                        reviewCount = queue.counts.rev,
                        html = card.question(this),
                        isAnswerShown = false,
                        showTypeInAnswer = typeAnswer.correct != null,
                        nextTimes = List(4) { "" },
                        chosenAnswer = "",
                        typedAnswer = "",
                        timer = "0.0s",
                        isMarked = note.hasTag(this, "marked"),
                        flag = card.userFlag(),
                        mediaDirectory = media.dir
                    )
                }
            }
            startTimer()
        }
    }

    private suspend fun getNextCard(): Pair<Card, CurrentQueueState>? = CollectionManager.withCol {
        sched.currentQueueState()?.let {
            it.topCard.renderOutput(this, reload = true)
            Pair(it.topCard, it)
        }
    }

    private fun showAnswer() {
        stopTimer()
        val card = currentCard ?: return
        val queue = queueState ?: return

        viewModelScope.launch {
            CollectionManager.withCol {
                val labels = sched.describeNextStates(queue.states)
                typeAnswer.input = _state.value.typedAnswer
                val answerHtml = typeAnswer.filterAnswer(card.answer(this))

                val paddedLabels = (labels + List(4) { "" }).take(4)

                _state.update {
                    it.copy(
                        html = answerHtml,
                        isAnswerShown = true,
                        nextTimes = paddedLabels
                    )
                }
            }
        }
    }

    private fun rateCard(rating: CardAnswer.Rating) {
        stopTimer()
        val queue = queueState ?: return
        viewModelScope.launch {
            CollectionManager.withCol {
                sched.answerCard(queue, rating)
            }
            loadCard()
        }
    }

    private fun startTimer() {
        stopTimer()
        timerJob = viewModelScope.launch {
            val startTime = System.currentTimeMillis()
            while (true) {
                delay(100)
                val elapsedTime = System.currentTimeMillis() - startTime
                _state.update { it.copy(timer = "${elapsedTime / 1000.0}s") }
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    private fun toggleMark() {
        viewModelScope.launch {
            val card = currentCard ?: return@launch
            val note = CollectionManager.withCol {
                card.note(this)
            }
            NoteService.toggleMark(note)
            _state.update { it.copy(isMarked = !_state.value.isMarked) }
        }
    }

    private fun setFlag(flag: Int) {
        viewModelScope.launch {
            val card = currentCard ?: return@launch
            CollectionManager.withCol {
                setUserFlagForCards(listOf(card.id), flag)
            }
            _state.update { it.copy(flag = flag) }
        }
    }
}
