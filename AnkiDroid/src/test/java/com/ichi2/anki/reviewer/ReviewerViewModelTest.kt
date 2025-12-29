/*
 *  Copyright (c) 2024 the Anki-Android contributors
 *
 *  This program is free software; you can redistribute it and/or modify it under
 *  the terms of the GNU General Public License as published by the Free Software
 *  Foundation; either version 3 of the License, or (at your option) any later
 *  version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY
 *  WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 *  PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with
 *  this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.ichi2.anki.reviewer

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ichi2.anki.RobolectricTest
import kotlinx.coroutines.flow.first
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Tests for [ReviewerViewModel].
 *
 * These tests require [CollectionStorageMode.IN_MEMORY_WITH_MEDIA] because the ViewModel
 * accesses the media directory during card loading.
 */
@RunWith(AndroidJUnit4::class)
class ReviewerViewModelTest : RobolectricTest() {

    // Must use IN_MEMORY_WITH_MEDIA to provide media directory access
    override fun getCollectionStorageMode() = CollectionStorageMode.IN_MEMORY_WITH_MEDIA

    @Test
    fun `initial state has empty counts`() = runTest {
        val viewModel = ReviewerViewModel(ApplicationProvider.getApplicationContext())
        advanceRobolectricLooper()

        // When there are no cards, the review should finish
        val state = viewModel.state.first()
        assertThat("No cards means review is finished", state.isFinished, equalTo(true))
        assertThat("New count should be 0", state.newCount, equalTo(0))
        assertThat("Learn count should be 0", state.learnCount, equalTo(0))
        assertThat("Review count should be 0", state.reviewCount, equalTo(0))
    }

    @Test
    fun `card loads successfully when cards exist`() = runTest {
        // Add a card so there's something to review
        addBasicNote("Front", "Back")

        val viewModel = ReviewerViewModel(ApplicationProvider.getApplicationContext())
        advanceRobolectricLooper()

        val state = viewModel.state.first()
        assertThat("Review should not be finished when cards exist", state.isFinished, equalTo(false))
        assertThat("New count should be 1", state.newCount, equalTo(1))
    }

    @Test
    fun `typed answer is updated via event`() = runTest {
        addBasicNote()
        val viewModel = ReviewerViewModel(ApplicationProvider.getApplicationContext())
        advanceRobolectricLooper()

        viewModel.onEvent(ReviewerEvent.OnTypedAnswerChanged("test answer"))

        val state = viewModel.state.first()
        assertThat("Typed answer should be updated", state.typedAnswer, equalTo("test answer"))
    }

    @Test
    fun `whiteboard state is updated via event`() = runTest {
        addBasicNote()
        val viewModel = ReviewerViewModel(ApplicationProvider.getApplicationContext())
        advanceRobolectricLooper()

        viewModel.onEvent(ReviewerEvent.OnWhiteboardStateChanged(true))

        val state = viewModel.state.first()
        assertThat("Whiteboard should be enabled", state.isWhiteboardEnabled, equalTo(true))

        viewModel.onEvent(ReviewerEvent.OnWhiteboardStateChanged(false))

        val state2 = viewModel.state.first()
        assertThat("Whiteboard should be disabled", state2.isWhiteboardEnabled, equalTo(false))
    }

    @Test
    fun `voice playback state is updated via event`() = runTest {
        addBasicNote()
        val viewModel = ReviewerViewModel(ApplicationProvider.getApplicationContext())
        advanceRobolectricLooper()

        viewModel.onEvent(ReviewerEvent.OnVoicePlaybackStateChanged(true))

        val state = viewModel.state.first()
        assertThat("Voice playback should be enabled", state.isVoicePlaybackEnabled, equalTo(true))
    }
}
