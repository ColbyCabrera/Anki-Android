package com.ichi2.anki.reviewer.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.malik.nativehtml.NativeHtml
import com.malik.nativehtml.image.CoilImageLoader
import com.malik.nativehtml.image.ImageLoader

@Composable
fun Flashcard(
    html: String,
    onTap: () -> Unit,
    onLinkClick: (String) -> Unit,
    imageLoader: ImageLoader,
    modifier: Modifier = Modifier
) {
    NativeHtml(
        html = html,
        modifier = modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onTap
            ),
        imageLoader = imageLoader,
        onLinkClick = onLinkClick
    )
}

@Preview(showBackground = true)
@Composable
fun FlashcardPreview() {
    Flashcard(
        html = "<html><body><h1>Hello, World!</h1><a href=\"https://example.com\">A link</a></body></html>",
        onTap = {},
        onLinkClick = {},
        imageLoader = CoilImageLoader()
    )
}