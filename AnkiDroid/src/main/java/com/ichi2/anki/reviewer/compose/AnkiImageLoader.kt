package com.ichi2.anki.reviewer.compose

import android.content.Context
import android.graphics.drawable.Drawable
import coil.Coil
import coil.request.ImageRequest
import com.malik.nativehtml.image.ImageLoader
import java.io.File

class AnkiImageLoader(
    private val context: Context,
    private val mediaDirectory: File?
) : ImageLoader {
    override fun loadImage(url: String, callback: (Drawable?) -> Unit) {
        val imagePath = if (mediaDirectory != null && !url.startsWith("http")) {
            File(mediaDirectory, url).absolutePath
        } else {
            url
        }

        val request = ImageRequest.Builder(context)
            .data(imagePath)
            .target {
                callback(it)
            }
            .build()
        Coil.imageLoader(context).enqueue(request)
    }

    override fun preLoadImage(url: String) {
        // Not implemented
    }
}