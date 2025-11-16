/*
 * Test to ensure going back from DeckPicker returns to the introduction screen
 * and the "First things first!" text is visible again.
 */
package com.ichi2.anki

import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ichi2.anki.tests.InstrumentedTest
import com.ichi2.anki.testutil.GrantStoragePermission
import com.ichi2.anki.testutil.grantPermissions
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class IntroductionBackNavigationTest : InstrumentedTest() {
    @get:Rule
    val activityScenarioRule = ActivityScenarioRule(IntroductionActivity::class.java)

    @get:Rule
    val runtimePermissionRule = grantPermissions(GrantStoragePermission.storagePermission)

    @Test
    fun backFromDeckPickerReshowsFirstThingsFirst() {
        // Click the "Get started" button
        onView(withText(R.string.intro_get_started)).perform(click())

        // Press back to return to the IntroductionActivity
        Espresso.pressBack()

        // The "First things first!" title should be visible again
        onView(withText("First things first!")).check(matches(isDisplayed()))
    }
}