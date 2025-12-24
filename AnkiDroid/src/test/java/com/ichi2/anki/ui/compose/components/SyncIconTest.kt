package com.ichi2.anki.ui.compose.components

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.ichi2.anki.R
import com.ichi2.anki.RobolectricTest
import com.ichi2.anki.SyncIconState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

import android.app.Application
import android.content.ComponentName
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.core.app.ApplicationProvider
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import org.robolectric.Shadows

@RunWith(AndroidJUnit4::class)
class SyncIconTest : RobolectricTest() {

    @get:org.junit.Rule(order = 1)
    val addActivityToRobolectricRule = object : TestWatcher() {
        override fun starting(description: Description?) {
            super.starting(description)
            val appContext: Application = ApplicationProvider.getApplicationContext()
            Shadows.shadowOf(appContext.packageManager).addActivityIfNotPresent(
                ComponentName(
                    appContext.packageName,
                    ComponentActivity::class.java.name,
                )
            )
        }
    }

    @get:org.junit.Rule(order = 2)
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun syncIcon_isSyncing_showsSyncingDescription() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val syncingLabel = context.getString(R.string.syncing)

        composeTestRule.setContent {
            SyncIcon(
                isSyncing = true,
                syncState = SyncIconState.Normal,
                onRefresh = {}
            )
        }

        // Verify the content description matches the syncing state
        composeTestRule.onNodeWithContentDescription(syncingLabel).assertExists()
    }

    @Test
    fun syncIcon_notSyncing_showsStateDescription() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val syncNowLabel = context.getString(R.string.sync_now)

        composeTestRule.setContent {
            SyncIcon(
                isSyncing = false,
                syncState = SyncIconState.Normal,
                onRefresh = {}
            )
        }

        // Verify the content description matches the normal state
        composeTestRule.onNodeWithContentDescription(syncNowLabel).assertExists()
    }
}
