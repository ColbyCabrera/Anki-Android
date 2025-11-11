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
import android.media.MediaPlayer
import android.net.Uri
import androidx.core.text.htmlEncode
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import anki.scheduler.CardAnswer
import com.ichi2.anki.CollectionManager
import com.ichi2.anki.cardviewer.CardMediaPlayer
import com.ichi2.anki.cardviewer.MediaErrorBehavior
import com.ichi2.anki.cardviewer.MediaErrorListener
import com.ichi2.anki.cardviewer.TypeAnswer
import com.ichi2.anki.libanki.Card
import com.ichi2.anki.libanki.CardId
import com.ichi2.anki.libanki.Sound
import com.ichi2.anki.libanki.SoundOrVideoTag
import com.ichi2.anki.libanki.TemplateManager
import com.ichi2.anki.libanki.TtsPlayer
import com.ichi2.anki.libanki.sched.CurrentQueueState
import com.ichi2.anki.preferences.sharedPrefs
import com.ichi2.anki.servicelayer.NoteService
import java.io.File
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

data class ReviewerState(
    val newCount: Int = 0,
    val learnCount: Int = 0,
    val reviewCount: Int = 0,
    val chosenAnswer: String = "",
    val isAnswerShown: Boolean = false,
    val html: String = "<html><body></body></html>",
    val nextTimes: List<String> = List(4) { "" },
    val showTypeInAnswer: Boolean = false,
    val typedAnswer: String = "",
    val isMarked: Boolean = false,
    val flag: Int = 0,
    val mediaDirectory: File? = null,
    val isFinished: Boolean = false
)

sealed class ReviewerEvent {
    object ShowAnswer : ReviewerEvent()
    data class RateCard(val rating: CardAnswer.Rating) : ReviewerEvent()
    object LoadInitialCard : ReviewerEvent()
    data class OnTypedAnswerChanged(val newText: String) : ReviewerEvent()
    object ToggleMark : ReviewerEvent()
    data class SetFlag(val flag: Int) : ReviewerEvent()
    data class LinkClicked(val url: String) : ReviewerEvent()
    data class PlayAudio(val side: String, val index: Int) : ReviewerEvent()
    object EditCard : ReviewerEvent()
    object BuryCard : ReviewerEvent()
    object SuspendCard : ReviewerEvent()
    object UnanswerCard : ReviewerEvent()
    object ReloadCard : ReviewerEvent()
}

sealed class ReviewerEffect {
    data class NavigateToEditCard(val cardId: CardId) : ReviewerEffect()
    object NavigateToDeckPicker : ReviewerEffect()
    data class ShowSnackbar(val message: String) : ReviewerEffect()
}

class ReviewerViewModel(app: Application) : AndroidViewModel(app) {

    companion object {
        private const val PLAY_BUTTON_TEMPLATE =
            """
                <a href="%s" class="replay-button" title="%s" aria-label="Play %s" role="button">
                    <svg xmlns="http://www.w3.org/2000/svg" height="56px" width="56px" class="play-action" viewBox="0 -960 960 960" width="24px">
                        <path d="M320-273v-414q0-17 12-28.5t28-11.5q5 0 10.5 1.5T381-721l326 207q9 6 13.5 15t4.5 19q0 10-4.5 19T707-446L381-239q-5 3-10.5 4.5T360-233q-16 0-28-11.5T320-273Z"/>
                    </svg>
                </a>
            """
    }

    private val _state = MutableStateFlow(ReviewerState())
    val state: StateFlow<ReviewerState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<ReviewerEffect>()
    val effect: SharedFlow<ReviewerEffect> = _effect.asSharedFlow()

    private var currentCard: Card? = null
    private var queueState: CurrentQueueState? = null
    private val typeAnswer = TypeAnswer.createInstance(app.sharedPrefs())
    private val cardMediaPlayer: CardMediaPlayer =
        CardMediaPlayer({ }, object : MediaErrorListener {
            override fun onError(uri: Uri): MediaErrorBehavior {
                Timber.w("Error playing media: %s", uri)
                return MediaErrorBehavior.CONTINUE_MEDIA
        }

        override fun onMediaPlayerError(mp: MediaPlayer?, which: Int, extra: Int, uri: Uri): MediaErrorBehavior {
            Timber.w("Error playing media: %s", uri)
            return MediaErrorBehavior.CONTINUE_MEDIA
        }

        override fun onTtsError(error: TtsPlayer.TtsError, isAutomaticPlayback: Boolean) {
            Timber.w("TTS error: %s", error)
        }
    })

    /** A job that is running for the current card. This is used to prevent multiple actions from running at the same time. */
    private var cardActionJob: Job? = null

    init {
        onEvent(ReviewerEvent.LoadInitialCard)
    }

    override fun onCleared() {
        cardMediaPlayer.close()
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
            is ReviewerEvent.PlayAudio -> playAudio(event.side, event.index)
            is ReviewerEvent.UnanswerCard -> unanswerCard()
            is ReviewerEvent.EditCard -> editCard()
            is ReviewerEvent.BuryCard -> buryCard()
            is ReviewerEvent.SuspendCard -> suspendCard()
            is ReviewerEvent.ReloadCard -> reloadCard()
        }
    }

    private suspend fun reloadCardSuspend() {
        val card = currentCard ?: return

        cardMediaPlayer.loadCardAvTags(card)
        CollectionManager.withCol {
            val note = card.note(this)
            typeAnswer.updateInfo(this, card, getApplication<Application>().resources)
            val renderOutput = card.renderOutput(this, reload = true)

            _state.update {
                it.copy(
                    html = processHtml(renderOutput.questionText, renderOutput),
                    isAnswerShown = false,
                    showTypeInAnswer = typeAnswer.correct != null,
                    nextTimes = List(4) { "" },
                    chosenAnswer = "",
                    typedAnswer = "",
                    isMarked = note.hasTag(this, "marked"),
                    flag = card.userFlag(),
                    mediaDirectory = this.media.dir,
                    isFinished = false
                )
            }
        }
    }

    private fun editCard() {
        val card = currentCard ?: return
        viewModelScope.launch {
            _effect.emit(ReviewerEffect.NavigateToEditCard(card.id))
        }
    }

    private fun linkClicked(url: String) {
        val match = Sound.AV_PLAYLINK_RE.find(url)
        if (match != null) {
            val (side, indexString) = match.destructured
            val index = indexString.toInt()
            onEvent(ReviewerEvent.PlayAudio(side, index))
            return
        }

        val intent = Intent(Intent.ACTION_VIEW, url.toUri())
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        getApplication<Application>().startActivity(intent)
    }

    private fun playAudio(side: String, index: Int) {
        viewModelScope.launch {
            val card = currentCard ?: return@launch
            val avTag = CollectionManager.withCol {
                val renderOutput = card.renderOutput(this)
                when (side) {
                    "q" -> renderOutput.questionAvTags.getOrNull(index)
                    "a" -> renderOutput.answerAvTags.getOrNull(index)
                    else -> null
                }
            }
            if (avTag is SoundOrVideoTag) {
                cardMediaPlayer.playOne(avTag)
            }
        }
    }

    private fun reloadCard() {
        if (cardActionJob?.isActive == true) {
            return
        }
        cardActionJob = viewModelScope.launch {
            reloadCardSuspend()
        }.also {
            it.invokeOnCompletion { cardActionJob = null }
        }
    }

    private fun onTypedAnswerChanged(newText: String) {
        _state.update { it.copy(typedAnswer = newText) }
    }

    private fun loadCard() {
        if (cardActionJob?.isActive == true) {
            return
        }
        cardActionJob = viewModelScope.launch {
            loadCardSuspend()
        }.also {
            it.invokeOnCompletion { cardActionJob = null }
        }
    }

    private suspend fun loadCardSuspend() {
        val cardAndQueueState = getNextCard()
        if (cardAndQueueState == null) {
            _state.update {
                it.copy(
                    isFinished = true,
                    newCount = 0,
                    learnCount = 0,
                    reviewCount = 0
                )
            }
            _effect.emit(ReviewerEffect.NavigateToDeckPicker)
            currentCard = null
            queueState = null
            return
        }
        val (card, queue) = cardAndQueueState
        currentCard = card
        queueState = queue
        cardMediaPlayer.loadCardAvTags(card)
        CollectionManager.withCol {
            val note = card.note(this)
            typeAnswer.updateInfo(this, card, getApplication<Application>().resources)
            val renderOutput = card.renderOutput(this)
            _state.update {
                it.copy(
                    newCount = queue.counts.new,
                    learnCount = queue.counts.lrn,
                    reviewCount = queue.counts.rev,
                    html = processHtml(renderOutput.questionText, renderOutput),
                    isAnswerShown = false,
                    showTypeInAnswer = typeAnswer.correct != null,
                    nextTimes = List(4) { "" },
                    chosenAnswer = "",
                    typedAnswer = "",
                    isMarked = note.hasTag(this, "marked"),
                    flag = card.userFlag(),
                    mediaDirectory = this.media.dir,
                    isFinished = false
                )
            }
        }
    }

    private suspend fun getNextCard(): Pair<Card, CurrentQueueState>? = CollectionManager.withCol {
        this.sched.currentQueueState()?.let {
            it.topCard.renderOutput(this, reload = true)
            Pair(it.topCard, it)
        }
    }

    private fun showAnswer() {
        if (cardActionJob?.isActive == true || _state.value.isFinished) {
            return
        }
        val card = currentCard ?: return
        val queue = queueState ?: return

        cardActionJob = viewModelScope.launch {
            CollectionManager.withCol {
                val labels = this.sched.describeNextStates(queue.states)
                typeAnswer.input = _state.value.typedAnswer
                val renderOutput = card.renderOutput(this)
                val answerHtml = typeAnswer.filterAnswer(renderOutput.answerText)

                val paddedLabels = (labels + List(4) { "" }).take(4)

                _state.update {
                    it.copy(
                        html = processHtml(answerHtml, renderOutput),
                        isAnswerShown = true,
                        nextTimes = paddedLabels
                    )
                }
            }
        }.also {
            it.invokeOnCompletion { cardActionJob = null }
        }
    }

    private fun rateCard(rating: CardAnswer.Rating) {
        if (cardActionJob?.isActive == true || _state.value.isFinished) {
            return
        }
        val queue = queueState ?: return
        cardActionJob = viewModelScope.launch {
            var wasLeech = false
            CollectionManager.withCol {
                this.sched.answerCard(queue, rating).also {
                    wasLeech = this.sched.stateIsLeech(queue.states.again)
                }
            }

            if (rating == CardAnswer.Rating.AGAIN && wasLeech) {
                val leechMessage: String = if (queue.topCard.queue.buriedOrSuspended()) {
                    getApplication<Application>().resources.getString(com.ichi2.anki.R.string.leech_suspend_notification)
                } else {
                    getApplication<Application>().resources.getString(com.ichi2.anki.R.string.leech_notification)
                }
                _effect.emit(ReviewerEffect.ShowSnackbar(leechMessage))
            }

            loadCardSuspend()
        }.also {
            it.invokeOnCompletion { cardActionJob = null }
        }
    }

    private fun unanswerCard() {
        val card = currentCard ?: return
        viewModelScope.launch {
            CollectionManager.withCol {
                val renderOutput = card.renderOutput(this)
                _state.update {
                    it.copy(
                        html = processHtml(renderOutput.questionText, renderOutput),
                        isAnswerShown = false,
                        nextTimes = List(4) { "" },
                        chosenAnswer = ""
                    )
                }
            }
        }
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
                this.setUserFlagForCards(listOf(card.id), flag)
            }
            _state.update { it.copy(flag = flag) }
        }
    }

    private fun performCardAction(action: suspend (Card) -> Unit) {
        if (cardActionJob?.isActive == true || _state.value.isFinished) {
            return
        }
        val card = currentCard ?: return
        cardActionJob = viewModelScope.launch {
            action(card)
            loadCardSuspend()
        }.also {
            it.invokeOnCompletion { cardActionJob = null }
        }
    }

    private fun buryCard() {
        performCardAction { card ->
            CollectionManager.withCol {
                this.sched.buryCards(listOf(card.id))
            }
        }
    }

    private fun suspendCard() {
        performCardAction { card ->
            CollectionManager.withCol {
                this.sched.suspendCards(listOf(card.id))
            }
        }
    }

    private fun processHtml(
        html: String,
        renderOutput: TemplateManager.TemplateRenderContext.TemplateRenderOutput
    ): String {
        val processedHtml = Sound.replaceAvRefsWith(html, renderOutput) { avTag, avRef ->
            when (avTag) {
                is SoundOrVideoTag -> {
                    val url = "playsound:${avRef.side}:${avRef.index}"
                    val content = avTag.filename.htmlEncode()
                    PLAY_BUTTON_TEMPLATE.format(url, content, content)
                }

                else -> null
            }
        }
        return "<style>${renderOutput.css}</style>$processedHtml"
    }
}
