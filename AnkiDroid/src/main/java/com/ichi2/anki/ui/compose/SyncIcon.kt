package com.ichi2.anki.ui.compose

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.stringResource
import com.ichi2.anki.R

@Composable
fun SyncIcon(isSyncing: Boolean, onRefresh: () -> Unit) {
    val rotation by rememberInfiniteTransition(label = "sync-rotation").animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1000,
                easing = LinearEasing
            )
        ),
        label = "sync-rotation-animation"
    )

    IconButton(onClick = onRefresh) {
        Icon(
            Icons.Default.Refresh,
            contentDescription = stringResource(R.string.sync_now),
            modifier = if (isSyncing) Modifier.rotate(rotation) else Modifier
        )
    }
}