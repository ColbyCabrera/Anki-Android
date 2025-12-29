/*
 *  Copyright (c) 2020 David Allison <davidallisongithub@gmail.com>
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
package com.ichi2.anki

import android.content.Intent
import android.graphics.Color
import androidx.annotation.CheckResult
import androidx.core.content.edit
import androidx.test.ext.junit.runners.AndroidJUnit4
import anki.scheduler.CardAnswer.Rating
import com.ichi2.anki.cardviewer.Gesture
import com.ichi2.anki.cardviewer.Gesture.SWIPE_DOWN
import com.ichi2.anki.cardviewer.Gesture.SWIPE_RIGHT
import com.ichi2.anki.cardviewer.Gesture.SWIPE_UP
import com.ichi2.anki.cardviewer.GestureProcessor
import com.ichi2.anki.cardviewer.ViewerCommand
import com.ichi2.anki.libanki.Consts
import com.ichi2.anki.libanki.DeckId
import com.ichi2.anki.model.WhiteboardPenColor
import com.ichi2.anki.preferences.sharedPrefs
import com.ichi2.anki.reviewer.Binding
import com.ichi2.anki.reviewer.FullScreenMode
import com.ichi2.anki.reviewer.FullScreenMode.Companion.setPreference
import com.ichi2.anki.reviewer.MappableBinding.Companion.toPreferenceString
import com.ichi2.anki.reviewer.ReviewerBinding
import com.ichi2.anki.utils.ext.addBinding
import com.ichi2.testutils.common.Flaky
import com.ichi2.testutils.common.OS
import com.ichi2.themes.Theme
import com.ichi2.themes.Themes.currentTheme
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.greaterThan
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric

/** A non-parameterized ReviewerTest - we should probably rename ReviewerTest in future  */
@RunWith(AndroidJUnit4::class)
class ReviewerNoParamTest : RobolectricTest() {
    override fun getCollectionStorageMode() = CollectionStorageMode.IN_MEMORY_WITH_MEDIA

    @Before
    override fun setUp() {
        super.setUp()
        // This doesn't do an upgrade in the correct place
        MetaDB.resetDB(targetContext)
    }

    @Test
    fun defaultWhiteboardColorIsUsedOnFirstRun() {
        // When no color is stored, MetaDB returns null (UI applies default)
        val retrievedColor = MetaDB.getWhiteboardPenColor(targetContext, Consts.DEFAULT_DECK_ID)
        assertThat("No color stored initially - null means use UI default", retrievedColor.lightPenColor, equalTo(null))
    }

    @Test
    fun whiteboardLightModeColorIsUsed() {
        storeLightModeColor(ARBITRARY_PEN_COLOR_VALUE)

        val retrievedColor = MetaDB.getWhiteboardPenColor(targetContext, Consts.DEFAULT_DECK_ID)
        assertThat("Light mode color is stored", retrievedColor.lightPenColor, equalTo(555))
    }

    @Test
    fun whiteboardDarkModeColorIsUsed() {
        storeDarkModeColor(555)

        val retrievedColor = MetaDB.getWhiteboardPenColor(targetContext, Consts.DEFAULT_DECK_ID)
        assertThat("Dark mode color is stored", retrievedColor.darkPenColor, equalTo(555))
    }

    @Test
    fun whiteboardPenColorChangeChangesDatabaseLight() {
        storeLightModeColor(ARBITRARY_PEN_COLOR_VALUE)

        val penColorResult = penColor
        assertThat("Light pen color is changed", penColorResult.lightPenColor, equalTo(ARBITRARY_PEN_COLOR_VALUE))
    }

    @Test
    fun whiteboardPenColorChangeChangesDatabaseDark() {
        storeDarkModeColor(ARBITRARY_PEN_COLOR_VALUE)

        val penColorResult = penColor
        assertThat("Dark pen color is changed", penColorResult.darkPenColor, equalTo(ARBITRARY_PEN_COLOR_VALUE))
    }

    @Test
    fun whiteboardDarkPenColorIsNotUsedInLightMode() {
        storeDarkModeColor(555)

        val retrievedColor = MetaDB.getWhiteboardPenColor(targetContext, Consts.DEFAULT_DECK_ID)
        // Dark mode color is 555, light mode color is null (use default)
        assertThat("Light pen color is null (use UI default) when only dark is set", retrievedColor.lightPenColor, equalTo(null))
        assertThat("Dark pen color is stored correctly", retrievedColor.darkPenColor, equalTo(555))
    }

    @Test
    fun differentDeckPenColorDoesNotAffectCurrentDeck() {
        val did = 2L
        storeLightModeColor(ARBITRARY_PEN_COLOR_VALUE, did)

        val retrievedColor = MetaDB.getWhiteboardPenColor(targetContext, Consts.DEFAULT_DECK_ID)
        assertThat("Pen color for default deck is null (use UI default)", retrievedColor.lightPenColor, equalTo(null))
    }

    @Test
    fun flippingCardHidesFullscreen() {
        addBasicNote("Hello", "World")
        val reviewer = startReviewerFullScreen()

        val hideCount = reviewer.delayedHideCount

        reviewer.displayCardAnswer()

        assertThat("Hide should be called after flipping a card", reviewer.delayedHideCount, greaterThan(hideCount))
    }

    @Test
    @Flaky(
        OS.ALL,
        "Hide should be called after answering a card" +
            "    Expected: a value greater than <2>" +
            "         but: <2> was equal to <2>",
    )
    fun showingCardHidesFullScreen() {
        addBasicNote("Hello", "World")
        val reviewer = startReviewerFullScreen()

        reviewer.displayCardAnswer()
        advanceRobolectricLooper()

        val hideCount = reviewer.delayedHideCount

        reviewer.answerCard(Rating.AGAIN)
        advanceRobolectricLooper()

        assertThat("Hide should be called after answering a card", reviewer.delayedHideCount, greaterThan(hideCount))
    }

    @Test
    @Flaky(OS.ALL, "Expected: a value greater than <2> but: <2> was equal to <2>")
    fun undoingCardHidesFullScreen() =
        runTest {
            addBasicNote("Hello", "World")
            val reviewer = startReviewerFullScreen()

            reviewer.displayCardAnswer()
            advanceRobolectricLooper()
            reviewer.answerCard(Rating.AGAIN)
            advanceRobolectricLooper()

            val hideCount = reviewer.delayedHideCount

            reviewer.undo()

            advanceRobolectricLooper()

            assertThat("Hide should be called after answering a card", reviewer.delayedHideCount, greaterThan(hideCount))
        }

    @Test
    @Flaky(OS.ALL, "hasDrawerSwipeConflicts was false")
    fun defaultDrawerConflictIsTrueIfGesturesEnabled() {
        enableGestureSetting()
        enableGesture(SWIPE_RIGHT)
        val reviewer = startReviewerFullScreen()

        assertThat(reviewer.hasDrawerSwipeConflicts(), equalTo(true))
    }

    @Test
    fun noDrawerConflictsBeforeOnCreate() {
        enableGestureSetting()
        val controller = Robolectric.buildActivity(Reviewer::class.java, Intent())
        try {
            assertThat("no conflicts before onCreate", controller.get().hasDrawerSwipeConflicts(), equalTo(false))
        } finally {
            try {
                enableGesture(SWIPE_UP)
            } catch (e: Exception) {
                // ignore
            }
        }
    }

    @Test
    fun noDrawerConflictsIfGesturesDisabled() {
        disableGestureSetting()
        enableGesture(SWIPE_UP)
        val reviewer = startReviewerFullScreen()
        assertThat("gestures should be disabled", gestureProcessor.isEnabled, equalTo(false))
        assertThat(reviewer.hasDrawerSwipeConflicts(), equalTo(false))
    }

    @Test
    fun noDrawerConflictsIfNoGestures() {
        enableGestureSetting()
        disableConflictGestures()
        val reviewer = startReviewerFullScreen()
        assertThat("gestures should be enabled", gestureProcessor.isEnabled, equalTo(true))
        assertThat("no conflicts, so no conflicts detected", reviewer.hasDrawerSwipeConflicts(), equalTo(false))
    }

    @Test
    @Flaky(os = OS.ALL, "final assertion is false")
    fun drawerConflictsIfUp() {
        enableGestureSetting()
        disableConflictGestures()
        enableGesture(SWIPE_UP)
        val reviewer = startReviewerFullScreen()
        assertThat("gestures should be enabled", gestureProcessor.isEnabled, equalTo(true))
        assertThat(reviewer.hasDrawerSwipeConflicts(), equalTo(true))
    }

    @Test
    @Flaky(os = OS.ALL, "final assertion is false")
    fun drawerConflictsIfDown() {
        enableGestureSetting()
        disableConflictGestures()
        enableGesture(SWIPE_DOWN)
        val reviewer = startReviewerFullScreen()
        assertThat("gestures should be enabled", gestureProcessor.isEnabled, equalTo(true))
        assertThat(reviewer.hasDrawerSwipeConflicts(), equalTo(true))
    }

    @Test
    @Flaky(os = OS.ALL, "final assertion is false")
    fun drawerConflictsIfRight() {
        enableGestureSetting()
        disableConflictGestures()
        enableGesture(SWIPE_RIGHT)
        val reviewer = startReviewerFullScreen()
        assertThat("gestures should be enabled", gestureProcessor.isEnabled, equalTo(true))
        assertThat(reviewer.hasDrawerSwipeConflicts(), equalTo(true))
    }

    @Test
    fun normalReviewerFitsSystemWindows() {
        val reviewer = startReviewer()
        assertThat(reviewer.fitsSystemWindows(), equalTo(true))
    }

    @Test
    fun fullscreenDoesNotFitSystemWindow() {
        val reviewer = startReviewerFullScreen()
        assertThat(reviewer.fitsSystemWindows(), equalTo(false))
    }

    private val gestureProcessor: GestureProcessor
        get() {
            val gestureProcessor = GestureProcessor(null)
            gestureProcessor.init(targetContext.sharedPrefs())
            return gestureProcessor
        }

    private fun disableConflictGestures() {
        disableGestures(SWIPE_UP, SWIPE_DOWN, SWIPE_RIGHT)
    }

    private fun enableGestureSetting() {
        setGestureSetting(true)
    }

    private fun disableGestureSetting() {
        setGestureSetting(false)
    }

    private fun setGestureSetting(value: Boolean) {
        targetContext.sharedPrefs().edit {
            putBoolean(GestureProcessor.PREF_KEY, value)
        }
    }

    private fun disableGestures(vararg gestures: Gesture) {
        val prefs = targetContext.sharedPrefs()
        for (command in ViewerCommand.entries) {
            for (mappableBinding in ReviewerBinding.fromPreference(prefs, command)) {
                val gestureBinding = mappableBinding.binding as? Binding.GestureInput? ?: continue
                if (gestureBinding.gesture in gestures) {
                    val bindings = ReviewerBinding.fromPreferenceString(command.preferenceKey).toMutableList()
                    bindings.remove(mappableBinding)
                    prefs.edit {
                        putString(command.preferenceKey, bindings.toPreferenceString())
                    }
                }
            }
        }
    }

    /** Enables a gesture (without changing the overall setting of whether gestures are allowed)  */
    private fun enableGesture(gesture: Gesture) {
        val prefs = targetContext.sharedPrefs()
        ViewerCommand.FLIP_OR_ANSWER_EASE1.addBinding(
            prefs,
            ReviewerBinding.fromGesture(gesture),
        )
    }

    private fun startReviewerFullScreen(): ReviewerExt {
        val sharedPrefs = targetContext.sharedPrefs()
        setPreference(sharedPrefs, FullScreenMode.BUTTONS_ONLY)
        return ReviewerTest.startReviewer(this, ReviewerExt::class.java)
    }

    @Suppress("SameParameterValue")
    private fun storeDarkModeColor(value: Int) {
        MetaDB.storeWhiteboardPenColor(targetContext, Consts.DEFAULT_DECK_ID, false, value)
    }

    @Suppress("SameParameterValue")
    private fun storeLightModeColor(
        value: Int,
        did: DeckId?,
    ) {
        MetaDB.storeWhiteboardPenColor(targetContext, did!!, false, value)
    }

    @Suppress("SameParameterValue")
    private fun storeLightModeColor(value: Int) {
        MetaDB.storeWhiteboardPenColor(targetContext, Consts.DEFAULT_DECK_ID, true, value)
    }

    private val penColor: WhiteboardPenColor
        get() = MetaDB.getWhiteboardPenColor(targetContext, Consts.DEFAULT_DECK_ID)



    private fun startReviewer(): Reviewer = ReviewerTest.startReviewer(this)

    private class ReviewerExt : Reviewer() {
        var delayedHideCount = 0

        override fun delayedHide(delayMillis: Int) {
            delayedHideCount++
            super.delayedHide(delayMillis)
        }
    }

    companion object {
        const val DEFAULT_LIGHT_PEN_COLOR = Color.BLACK
        const val ARBITRARY_PEN_COLOR_VALUE = 555
    }
}
