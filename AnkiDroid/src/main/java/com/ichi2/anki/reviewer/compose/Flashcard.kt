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
package com.ichi2.anki.reviewer.compose

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.GestureDetector
import android.view.MotionEvent
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import timber.log.Timber
import java.io.File

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun Flashcard(
    html: String,
    onTap: () -> Unit,
    onLinkClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    mediaDirectory: File?,
    isAnswerShown: Boolean
) {
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val onSurfaceColorHex = String.format("#%06X", (0xFFFFFF and onSurfaceColor.toArgb()))
    val typography = MaterialTheme.typography
    val displayLargeStyle = typography.displayMedium
    val bodyLargeStyle = typography.titleLarge

    Crossfade(
        targetState = Pair(isAnswerShown, html),
        animationSpec = tween(300)
    ) { (shown, currentHtml) ->
        val currentStyle = if (shown) bodyLargeStyle else displayLargeStyle
        val currentPadding = if (shown) 40 else 36
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
                    val gestureDetector = GestureDetector(
                        context,
                        object : GestureDetector.SimpleOnGestureListener() {
                            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                                onTap()
                                return true
                            }
                        }
                    )
                    setOnTouchListener { _, event ->
                        gestureDetector.onTouchEvent(event)
                        false
                    }
                    setBackgroundColor(Color.TRANSPARENT)
                }
            },
            update = { webView ->
                val styledHtml = """
                <style>
                    @import url('https://fonts.googleapis.com/css2?family=Roboto&display=swap');
                    html {
                        color: ${onSurfaceColorHex}EF;
                        text-align: center;
                        font-family: 'Roboto', sans-serif;
                        font-size: ${currentStyle.fontSize.value}px;
                        font-weight: ${currentStyle.fontWeight?.weight ?: 400};
                        line-height: ${currentStyle.lineHeight.value}px;
                        letter-spacing: ${currentStyle.letterSpacing.value}px;
                        padding-top: ${currentPadding}px;
                    }
                    hr {
                        opacity: 0.1;
                         margin-bottom: 12px;
                    }
                        
                </style>
                $currentHtml
                """.trimIndent()
                Timber.tag("Flashcard").d("styledHtml: $styledHtml")
                if (webView.tag != styledHtml) {
                    webView.tag = styledHtml
                    webView.loadDataWithBaseURL(
                        "file:///$mediaDirectory/",
                        styledHtml,
                        "text/html",
                        "UTF-8",
                        null
                    )
                }
            },
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun FlashcardPreview() {
    Flashcard(
        html = "<html><body><h1>Hello, World!</h1><a href=\"https://example.com\">A link</a></body></html>",
        onTap = {},
        onLinkClick = {},
        mediaDirectory = null,
        isAnswerShown = false
    )
}

@Preview(showBackground = true)
@Composable
fun FlashcardPreviewAnswerShown() {
    Flashcard(
        html = "<html><body><h1>Hello, World!</h1><a href=\"https://example.com\">A link</a></body></html>",
        onTap = {},
        onLinkClick = {},
        mediaDirectory = null,
        isAnswerShown = true
    )
}
