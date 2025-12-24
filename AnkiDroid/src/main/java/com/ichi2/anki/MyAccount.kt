/***************************************************************************************
 * This program is free software; you can redistribute it and/or modify it under        *
 * the terms of the GNU General Public License as published by the Free Software        *
 * Foundation; either version 3 of the License, or (at your option) any later           *
 * version.                                                                             *
 *                                                                                      *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY      *
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A      *
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.             *
 *                                                                                      *
 * You should have received a copy of the GNU General Public License along with         *
 * this program.  If not, see <http://www.gnu.org/licenses/>.                           *
 ****************************************************************************************/
package com.ichi2.anki

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import com.ichi2.anki.CollectionManager.withCol
import com.ichi2.anki.common.utils.annotation.KotlinCleanup
import com.ichi2.anki.settings.Prefs
import com.ichi2.anki.ui.compose.MyAccountScreen
import com.ichi2.anki.ui.compose.theme.AnkiDroidTheme
import com.ichi2.anki.utils.ext.showDialogFragment
import com.ichi2.utils.AdaptionUtil.isUserATestClient
import com.ichi2.utils.Permissions
import net.ankiweb.rsdroid.exceptions.BackendSyncException
import timber.log.Timber

/**
 * Note: [LoginActivity] extends this and should not handle account creation
 */
open class MyAccount : AnkiActivity() {
    protected val viewModel: MyAccountViewModel by viewModels()

    protected open val showSignUpButton: Boolean = true
    protected open val showNoAccountText: Boolean = true

    open fun switchToState(newState: Int) {
        // Compose handles state switching via viewModel
        viewModel.updateLoginStatus()
    }

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            Timber.i("notification permission: %b", it)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        if (showedActivityFailedScreen(savedInstanceState)) {
            return
        }
        super.onCreate(savedInstanceState)
        if (isUserATestClient) {
            finish()
            return
        }
        mayOpenUrl(R.string.register_url)

        val onBackPressedCallback = object : OnBackPressedCallback(false) {
            override fun handleOnBackPressed() {
                val state = viewModel.state.value
                if (state.screenState == MyAccountScreenState.REMOVE_ACCOUNT) {
                    closeRemoveAccountScreen()
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        }
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        setContent {
            AnkiDroidTheme {
                val state by viewModel.state.collectAsState()

                MyAccountScreen(
                    viewModel = viewModel,
                    onLoginClick = { email, password ->
                        handleNewLogin(
                            email,
                            password,
                            notificationPermissionLauncher
                        )
                    },
                    onResetPasswordClick = { resetPassword() },
                    onSignUpClick = { openUrl(R.string.register_url) },
                    onPrivacyPolicyClick = { openAnkiDroidPrivacyPolicy() },
                    onLostEmailClick = { openUrl(R.string.link_ankiweb_lost_email_instructions) },
                    onRemoveAccountClick = { openRemoveAccountScreen() },
                    onLogoutClick = { logout() },
                    showSignUp = showSignUpButton,
                    showNoAccountText = showNoAccountText
                )

                // Update back button callback enabled state
                androidx.compose.runtime.LaunchedEffect(state.screenState) {
                    onBackPressedCallback.isEnabled =
                        state.screenState == MyAccountScreenState.REMOVE_ACCOUNT
                }
            }
        }
    }


    private fun logout() {
        launchCatchingTask {
            Prefs.hkey = null
            Prefs.username = null
            Prefs.currentSyncUri = null
            withCol {
                this.media.forceResync()
            }
            viewModel.updateLoginStatus()
        }
    }

    /**
     * Opens the AnkiWeb 'remove account' screen
     * @see R.string.remove_account_url
     */
    private fun openRemoveAccountScreen() {
        Timber.i("opening 'remove account'")
        viewModel.setScreenState(MyAccountScreenState.REMOVE_ACCOUNT)
    }

    private fun closeRemoveAccountScreen() {
        Timber.i("closing 'remove account'")
        viewModel.setScreenState(MyAccountScreenState.ACCOUNT_MANAGEMENT)
    }

    private fun resetPassword() {
        super.openUrl(R.string.resetpw_url)
    }

    private fun openAnkiDroidPrivacyPolicy() {
        Timber.i("Opening 'Privacy policy'")
        showDialogFragment(com.ichi2.anki.dialogs.help.HelpDialog.newPrivacyPolicyInstance())
    }

    private fun handleNewLogin(
        username: String,
        password: String,
        resultLauncher: ActivityResultLauncher<String>,
    ) {
        val endpoint = getEndpoint()
        launchCatchingTask {
            val auth = try {
                withProgress(
                    extractProgress = {
                        text = getString(R.string.sign_in)
                    },
                    onCancel = ::cancelSync,
                ) {
                    withCol {
                        this.syncLogin(username, password, endpoint)
                    }
                }
            } catch (exc: BackendSyncException.BackendSyncAuthFailedException) {
                // auth failed; clear out login details
                updateLogin("", "")
                throw exc
            }
            updateLogin(username, auth.hkey)
            setResult(RESULT_OK)
            checkNotificationPermission(this@MyAccount, resultLauncher)
            finish()
        }
    }

    companion object {
        @KotlinCleanup("change to enum")
        internal const val STATE_LOG_IN = 1
        internal const val STATE_LOGGED_IN = 2

        /**
         * Displays a system prompt: "Allow AnkiDroid to send you notifications"
         *
         * [launcher] receives a callback result (`boolean`) unless:
         *  * Permissions were already granted
         *  * We are < API 33
         *
         * Permissions may permanently be denied, in which case [launcher] immediately
         * receives a failure result
         */
        fun checkNotificationPermission(
            context: Context,
            launcher: ActivityResultLauncher<String>,
        ) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                return
            }
            val permission = Permissions.postNotification
            if (permission != null && ContextCompat.checkSelfPermission(
                    context,
                    permission,
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                launcher.launch(permission)
            }
        }
    }
}
