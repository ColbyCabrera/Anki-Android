package com.ichi2.anki.reviewer

import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import anki.scheduler.CardAnswer.Rating
import com.ichi2.anki.CollectionManager
import com.ichi2.anki.cardviewer.TypeAnswer
import com.ichi2.anki.libanki.Card
import com.ichi2.anki.libanki.sched.CurrentQueueState
import com.ichi2.anki.preferences.sharedPrefs
import com.ichi2.anki.servicelayer.NoteService
import com.ichi2.anki.utils.ext.setFlagForCards
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

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
    data class RateCard(val rating: Rating) : ReviewerEvent()
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
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
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
            withContext(Dispatchers.IO) {
                val col = CollectionManager.getColUnsafe()
                val note = card.note(col)
                typeAnswer.updateInfo(col, card, getApplication<Application>().resources)
                val isMarked = note.hasTag("marked")
                val showTypeInAnswer = typeAnswer.correct != null
                val questionHtml = card.question()
                val flag = card.flag()
                val mediaDirectory = col.media.dir()

                withContext(Dispatchers.Main) {
                    _state.update {
                        it.copy(
                            newCount = queue.counts.new,
                            learnCount = queue.counts.lrn,
                            reviewCount = queue.counts.rev,
                            html = questionHtml,
                            isAnswerShown = false,
                            showTypeInAnswer = showTypeInAnswer,
                            nextTimes = List(4) { "" },
                            chosenAnswer = "",
                            typedAnswer = "",
                            timer = "0.0s",
                            isMarked = isMarked,
                            flag = flag,
                            mediaDirectory = mediaDirectory
                        )
                    }
                    startTimer()
                }
            }
        }
    }

    private suspend fun getNextCard(): Pair<Card, CurrentQueueState>? = withContext(Dispatchers.IO) {
        val col = CollectionManager.getCol() ?: return@withContext null
        val state = col.sched.currentQueueState()?.apply {
            topCard.renderOutput(col, reload = true)
        }
        state?.let {
            Pair(it.topCard, it)
        }
    }

    private fun showAnswer() {
        stopTimer()
        val card = currentCard ?: return
        val queue = queueState ?: return

        viewModelScope.launch {
            val labels = withContext(Dispatchers.IO) {
                CollectionManager.getCol()?.sched?.describeNextStates(queue.states) ?: emptyList()
            }
            typeAnswer.input = _state.value.typedAnswer
            val answerHtml = withContext(Dispatchers.IO) {
                typeAnswer.filterAnswer(card.answer())
            }

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

    private fun rateCard(rating: Rating) {
        stopTimer()
        val queue = queueState ?: return
        viewModelScope.launch(Dispatchers.IO) {
            val col = CollectionManager.getCol() ?: return@launch
            col.sched.answerCard(queue, rating)
            withContext(Dispatchers.Main) {
                loadCard()
            }
        }
    }

    private fun startTimer() {
        stopTimer()
        timerJob = viewModelScope.launch {
            var seconds = 0
            while (true) {
                delay(100)
                seconds++
                _state.update { it.copy(timer = "${seconds / 10.0}s") }
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    private fun toggleMark() {
        viewModelScope.launch(Dispatchers.IO) {
            val card = currentCard ?: return@launch
            val col = CollectionManager.getColUnsafe()
            val note = card.note(col)
            NoteService.toggleMark(note)
            withContext(Dispatchers.Main) {
                _state.update { it.copy(isMarked = !_state.value.isMarked) }
            }
        }
    }

    private fun setFlag(flag: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val card = currentCard ?: return@launch
            val col = CollectionManager.getColUnsafe()
            col.setFlagForCards(listOf(card.id), flag)
            withContext(Dispatchers.Main) {
                _state.update { it.copy(flag = flag) }
            }
        }
    }
}