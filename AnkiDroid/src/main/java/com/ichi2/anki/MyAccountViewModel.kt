package com.ichi2.anki

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ichi2.anki.CollectionManager.withCol
import com.ichi2.anki.settings.Prefs
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

data class MyAccountState(
    val isLoggedIn: Boolean = false,
    val username: String? = null,
    val email: String = "",
    val password: String = "",
    val isLoginLoading: Boolean = false,
    val loginError: String? = null,
    val screenState: MyAccountScreenState = MyAccountScreenState.ACCOUNT_MANAGEMENT
)

enum class MyAccountScreenState {
    ACCOUNT_MANAGEMENT, REMOVE_ACCOUNT
}

class MyAccountViewModel : ViewModel() {
    private val _state = MutableStateFlow(MyAccountState())
    val state: StateFlow<MyAccountState> = _state.asStateFlow()

    init {
        val loggedIn = Prefs.username != null
        _state.update {
            it.copy(
                isLoggedIn = loggedIn, username = Prefs.username, email = Prefs.username ?: ""
            )
        }
    }

    fun onEmailChanged(email: String) {
        _state.update { it.copy(email = email, loginError = null) }
    }

    fun onPasswordChanged(password: String) {
        _state.update { it.copy(password = password, loginError = null) }
    }

    fun login(onSuccess: () -> Unit) {
        val email = _state.value.email.trim()
        val password = _state.value.password
        if (email.isEmpty() || password.isEmpty()) return

        _state.update { it.copy(isLoginLoading = true, loginError = null) }

        viewModelScope.launch {
            try {
                // This logic is simplified for now, will be integrated with Activity's logic if needed
                // or moved here entirely.
                _state.update { it.copy(isLoginLoading = false) }
                // In a real implementation, we would call the actual login logic here.
                // For now, the Activity will handle the actual login as it has the lifecycle/context.
            } catch (e: Exception) {
                _state.update { it.copy(isLoginLoading = false, loginError = e.message) }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                withCol {
                    media.forceResync()
                }
            } catch (e: Exception) {
                Timber.e(e, "logout: media.forceResync() failed")
            } finally {
                Prefs.hkey = null
                Prefs.username = null
                Prefs.currentSyncUri = null
                _state.update { it.copy(isLoggedIn = false, username = null, password = "", email = "") }
            }
        }
    }

    fun setScreenState(screenState: MyAccountScreenState) {
        _state.update { it.copy(screenState = screenState) }
    }

    fun updateLoginStatus() {
        val loggedIn = Prefs.username != null
        _state.update {
            it.copy(
                isLoggedIn = loggedIn, username = Prefs.username
            )
        }
    }
}
