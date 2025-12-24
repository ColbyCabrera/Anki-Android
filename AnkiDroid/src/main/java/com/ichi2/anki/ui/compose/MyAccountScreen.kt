package com.ichi2.anki.ui.compose

import android.net.http.SslError
import android.webkit.SslErrorHandler
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import com.ichi2.anki.MyAccountScreenState
import com.ichi2.anki.MyAccountViewModel
import com.ichi2.anki.R
import com.ichi2.anki.ui.compose.theme.AnkiDroidTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyAccountScreen(
    viewModel: MyAccountViewModel,
    onLoginClick: (String, String) -> Unit,
    onResetPasswordClick: () -> Unit,
    onSignUpClick: () -> Unit,
    onPrivacyPolicyClick: () -> Unit,
    onLostEmailClick: () -> Unit,
    onRemoveAccountClick: () -> Unit,
    onLogoutClick: () -> Unit,
    showSignUp: Boolean = true,
    showNoAccountText: Boolean = true
) {
    val state by viewModel.state.collectAsState()

    AnkiDroidTheme {
        when (state.screenState) {
            MyAccountScreenState.ACCOUNT_MANAGEMENT -> {
                Scaffold { padding ->
                    when (state.isLoggedIn) {
                        true -> LoggedInContent(
                            modifier = Modifier.padding(padding),
                            username = state.username ?: "",
                            onLogoutClick = onLogoutClick,
                            onRemoveAccountClick = onRemoveAccountClick,
                            onPrivacyPolicyClick = onPrivacyPolicyClick
                        )

                        false -> {
                            var password by remember { mutableStateOf("") }
                            LoggedOutContent(
                                modifier = Modifier.padding(padding),
                                email = state.email,
                                password = password,
                                isLoading = state.isLoginLoading,
                                onEmailChanged = viewModel::onEmailChanged,
                                onPasswordChanged = { password = it },
                                onLoginClick = { onLoginClick(state.email, password) },
                                onResetPasswordClick = onResetPasswordClick,
                                onSignUpClick = onSignUpClick,
                                onPrivacyPolicyClick = onPrivacyPolicyClick,
                                onLostEmailClick = onLostEmailClick,
                                showSignUp = showSignUp,
                                showNoAccountText = showNoAccountText
                            )
                        }
                    }
                }
            }

            MyAccountScreenState.REMOVE_ACCOUNT -> {
                RemoveAccountContent(
                    onBack = { viewModel.setScreenState(MyAccountScreenState.ACCOUNT_MANAGEMENT) })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemoveAccountContent(
    onBack: () -> Unit
) {
    val removeAccountUrl = stringResource(R.string.remove_account_url)

    // Redirect logic from RemoveAccountFragment
    val urlsToRedirect = listOf(
        "https://ankiweb.net/account/login?afterAuth=1",
        "https://ankiweb.net/decks",
        "https://ankiweb.net/account/verify-email"
    )

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.remove_account)) }, navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(androidx.appcompat.R.string.abc_action_bar_up_description)
                    )
                }
            })
        }) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            AndroidView(
                factory = { ctx ->
                    WebView(ctx).apply {
                        settings.javaScriptEnabled = true
                        settings.displayZoomControls = false
                        settings.builtInZoomControls = true
                        settings.setSupportZoom(true)

                        // Security hardening as requested
                        settings.allowFileAccess = false
                        settings.allowContentAccess = false

                        removeJavascriptInterface("searchBoxJavaBridge_")
                        removeJavascriptInterface("accessibility")
                        removeJavascriptInterface("accessibilityTraversal")

                        var redirectCount = 0

                        webViewClient = object : WebViewClient() {
                            private fun isUrlAllowed(url: String?): Boolean {
                                if (url == null) return false
                                val uri = url.toUri()
                                val host = uri.host ?: return false
                                return host == "ankiweb.net" || host.endsWith(".ankiweb.net")
                            }

                            private fun maybeRedirect(url: String?): Boolean {
                                if (url == null) return false
                                if (urlsToRedirect.any { url.startsWith(it) }) {
                                    redirectCount++
                                    if (redirectCount <= 3) {
                                        loadUrl(removeAccountUrl)
                                        return true
                                    }
                                }
                                return false
                            }

                            override fun shouldInterceptRequest(
                                view: WebView?, request: WebResourceRequest?
                            ): WebResourceResponse? {
                                if (!isUrlAllowed(request?.url?.toString())) {
                                    return WebResourceResponse("text/plain", "utf-8", null)
                                }
                                return super.shouldInterceptRequest(view, request)
                            }

                            override fun onReceivedSslError(
                                view: WebView?, handler: SslErrorHandler?, error: SslError?
                            ) {
                                handler?.cancel()
                            }

                            override fun shouldOverrideUrlLoading(
                                view: WebView?,
                                request: WebResourceRequest?
                            ): Boolean {
                                val url = request?.url?.toString()
                                if (maybeRedirect(url)) return true

                                return !isUrlAllowed(url)
                            }

                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                maybeRedirect(url)
                            }
                        }
                        loadUrl(removeAccountUrl)
                    }
                }, modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
fun LoggedOutContent(
    modifier: Modifier = Modifier,
    email: String,
    password: String,
    isLoading: Boolean,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onLoginClick: () -> Unit,
    onResetPasswordClick: () -> Unit,
    onSignUpClick: () -> Unit,
    onPrivacyPolicyClick: () -> Unit,
    onLostEmailClick: () -> Unit,
    showSignUp: Boolean,
    showNoAccountText: Boolean
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        OutlinedTextField(
            value = email,
            onValueChange = onEmailChanged,
            label = { Text(stringResource(R.string.username)) },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChanged,
            label = { Text(stringResource(R.string.password)) },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onLoginClick,
            modifier = Modifier.widthIn(min = 200.dp),
            enabled = email.isNotEmpty() && password.isNotEmpty() && !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text(stringResource(R.string.log_in))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = onResetPasswordClick) {
            Text(stringResource(R.string.reset_password))
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (showNoAccountText) {
            Text(
                text = stringResource(R.string.sign_up_description),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )

            Text(
                text = stringResource(R.string.ankiweb_is_not_affiliated_with_this_app),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (showSignUp) {
            TextButton(onClick = onSignUpClick) {
                Text(stringResource(R.string.sign_up))
            }
        }

        TextButton(onClick = onPrivacyPolicyClick) {
            Text(stringResource(R.string.help_title_privacy))
        }

        TextButton(onClick = onLostEmailClick) {
            Text(stringResource(R.string.lost_mail_instructions))
        }
    }
}

@Composable
fun LoggedInContent(
    modifier: Modifier = Modifier,
    username: String,
    onLogoutClick: () -> Unit,
    onRemoveAccountClick: () -> Unit,
    onPrivacyPolicyClick: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(R.drawable.ic_link),
            contentDescription = null,
            modifier = Modifier.size(height = 80.dp, width = 60.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.logged_as), style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = username,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onLogoutClick, modifier = Modifier.widthIn(min = 200.dp)
        ) {
            Text(stringResource(R.string.log_out))
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onRemoveAccountClick,
            modifier = Modifier.widthIn(min = 200.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Text(stringResource(R.string.remove_account))
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = onPrivacyPolicyClick) {
            Text(stringResource(R.string.help_title_privacy))
        }
    }
}
