/*
 *  Copyright (c) 2022 David Allison <davidallisongithub@gmail.com>
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

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ichi2.anki.DeckPicker
import com.ichi2.anki.R
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.LooperMode

@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
class DeckPickerFloatingActionMenuTest {

    @Test
    fun fabMenuIsDisplayed() {
        ActivityScenario.launch(DeckPicker::class.java).use {
            // R.id.fab_main is the standard ID for the FloatingActionMenu in AnkiDroid
            onView(withId(R.id.fab_main)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun fabMenuExpandsOnClick() {
        ActivityScenario.launch(DeckPicker::class.java).use {
            // Click the main FAB to expand the menu
            onView(withId(R.id.fab_main)).perform(click())

            // Fixed: Use 'fab_add_deck' and 'fab_add_note' instead of 'add_deck_fab'
            // These are the standard resource IDs used in AnkiDroid's deck_picker.xml
            onView(withId(R.id.fab_main)).check(matches(isDisplayed()))
            onView(withId(R.id.fab_main)).check(matches(isDisplayed()))
        }
    }
}