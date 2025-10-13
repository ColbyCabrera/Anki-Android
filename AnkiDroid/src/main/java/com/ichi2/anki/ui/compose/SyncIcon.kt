package com.ichi2.anki.ui.compose

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.ichi2.anki.R

@Composable
fun SyncIcon(isSyncing: Boolean, onRefresh: () -> Unit) {
    val rotation = remember { Animatable(0f) }

    LaunchedEffect(isSyncing) {
        if (isSyncing) {
            rotation.animateTo(
                targetValue = 360f, animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = 100, easing = LinearEasing
                    )
                )
            )
        } else {
            rotation.snapTo(0f)
        }
    }

    IconButton(onClick = onRefresh) {
        Icon(
            painter = painterResource(R.drawable.sync_24px),
            contentDescription = stringResource(R.string.sync_now),
            modifier = Modifier.rotate(rotation.value)
        )
    }
}