package com.ichi2.anki.ui.compose

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.ichi2.anki.R
import kotlinx.coroutines.launch

@Composable
fun SyncIcon(isSyncing: Boolean, onRefresh: () -> Unit) {
    val rotation = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    IconButton(
        onClick = {
            onRefresh()
            scope.launch {
                rotation.animateTo(
                    targetValue = rotation.value + 360f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
            }
        },
        enabled = !isSyncing
    ) {
        Icon(
            painter = painterResource(R.drawable.sync_24px),
            contentDescription = stringResource(R.string.sync_now),
            modifier = Modifier.rotate(rotation.value)
        )
    }
}
