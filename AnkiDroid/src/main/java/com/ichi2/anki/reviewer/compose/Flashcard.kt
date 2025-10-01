package com.ichi2.anki.reviewer.compose

import android.annotation.SuppressLint
import android.view.GestureDetector
import android.view.MotionEvent
import android.webkit.WebView
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView

@SuppressLint("SetJavaScriptEnabled", "ClickableViewAccessibility")
@Composable
fun Flashcard(
    html: String,
    onTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                loadDataWithBaseURL(null, html, "text/html", "utf-8", null)

                val gestureDetector = GestureDetector(
                    context,
                    object : GestureDetector.SimpleOnGestureListener() {
                        override fun onSingleTapUp(e: MotionEvent): Boolean {
                            val hitTestResult = hitTestResult
                            if (hitTestResult.type == WebView.HitTestResult.UNKNOWN_TYPE) {
                                onTap()
                                return true
                            }
                            return super.onSingleTapUp(e)
                        }
                    }
                )

                setOnTouchListener { _, event ->
                    gestureDetector.onTouchEvent(event)
                }
            }
        },
        update = { webView ->
            webView.loadDataWithBaseURL(null, html, "text/html", "utf-8", null)
        }
    )
}

@Preview(showBackground = true)
@Composable
fun FlashcardPreview() {
    Flashcard(html = "<html><body><h1>Hello, World!</h1><a href=\"#\">A link</a></body></html>", onTap = {})
}