package com.ichi2.anki.reviewer.compose

import android.annotation.SuppressLint
import android.view.MotionEvent
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import coil.ImageLoader
import java.io.File

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun Flashcard(
    html: String,
    onTap: () -> Unit,
    onLinkClick: (String) -> Unit,
    imageLoader: ImageLoader,
    modifier: Modifier = Modifier,
    mediaDirectory: File?
) {
    AndroidView(
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.allowFileAccess = true
                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                        if (url != null) {
                            onLinkClick(url)
                            return true
                        }
                        return false
                    }
                }
                setOnTouchListener { _, event ->
                    if (event.action == MotionEvent.ACTION_UP) {
                        onTap()
                    }
                    true
                }
            }
        },
        update = { webView ->
            webView.loadDataWithBaseURL(
                "file:///$mediaDirectory/",
                html,
                "text/html",
                "UTF-8",
                null
            )
        },
        modifier = modifier.fillMaxSize()
    )
}

@Preview(showBackground = true)
@Composable
fun FlashcardPreview() {
    Flashcard(
        html = "<html><body><h1>Hello, World!</h1><a href=\"https://example.com\">A link</a></body></html>",
        onTap = {},
        onLinkClick = {},
        imageLoader = ImageLoader.Builder(androidx.compose.ui.platform.LocalContext.current).build(),
        mediaDirectory = null
    )
}