package com.ichi2.anki.reviewer.compose

import android.content.Context
import coil.ImageLoader
import java.io.File

fun AnkiImageLoader(
    context: Context,
    mediaDirectory: File?
): ImageLoader {
    return ImageLoader.Builder(context)
        .build()
}