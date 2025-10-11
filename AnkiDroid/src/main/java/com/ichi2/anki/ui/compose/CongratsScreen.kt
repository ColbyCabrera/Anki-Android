package com.ichi2.anki.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ichi2.anki.R
import com.ichi2.anki.ui.compose.theme.AnkiDroidTheme
import com.ichi2.anki.ui.compose.theme.RobotoMono
import kotlinx.coroutines.delay
import java.util.Locale
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CongratsScreen(onDeckOptions: () -> Unit, onBack: () -> Unit, timeUntilNextDay: Long) {
    AnkiDroidTheme {
        Scaffold(topBar = {
            TopAppBar(
                title = {
                Text(
                    text = stringResource(id = R.string.app_name),
                    style = MaterialTheme.typography.displayMediumEmphasized
                )
            },
                subtitle = {},
                titleHorizontalAlignment = Alignment.CenterHorizontally,
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(id = R.drawable.arrow_back_24px),
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onDeckOptions) {
                        Icon(
                            painter = painterResource(id = R.drawable.tune_24px),
                            contentDescription = stringResource(R.string.deck_options)
                        )
                    }
                })
        }, content = { contentPadding ->
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding)
                    .verticalScroll(scrollState)
                    .padding(top = 48.dp, start = 16.dp, end = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),

            ) {
                Text(
                    text = stringResource(id = R.string.studyoptions_congrats_finished),
                    style = MaterialTheme.typography.headlineMedium,

                )
                Text(
                    text = stringResource(R.string.daily_limit_reached),
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = stringResource(R.string.study_more),
                    style = MaterialTheme.typography.bodyLarge
                )

                Column(
                    modifier = Modifier
                        .padding(top = 32.dp)
                        .clip(MaterialTheme.shapes.medium)
                        .background(MaterialTheme.colorScheme.tertiaryContainer)
                        .height(340.dp),
                ) {
                    var remainingTime by remember(timeUntilNextDay) {
                        mutableLongStateOf(
                            timeUntilNextDay.coerceAtLeast(0L)
                        )
                    }

                    LaunchedEffect(timeUntilNextDay) {
                        while (remainingTime > 0) {
                            delay(1000)
                            remainingTime = (remainingTime - 1000).coerceAtLeast(0L)
                        }
                    }

                    val hours = TimeUnit.MILLISECONDS.toHours(remainingTime)
                    val minutes = TimeUnit.MILLISECONDS.toMinutes(remainingTime) % 60
                    val seconds = TimeUnit.MILLISECONDS.toSeconds(remainingTime) % 60

                    Text(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        text = stringResource(R.string.next_review_in),
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        fontFamily = RobotoMono,
                        fontSize = MaterialTheme.typography.displayMedium.fontSize,
                        lineHeight = MaterialTheme.typography.displayLarge.lineHeight,
                        fontWeight = FontWeight.SemiBold,
                    )

                    Row(
                        Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = String.format(
                                Locale.US,
                                "%02d:%02d:%02d",
                                hours,
                                minutes,
                                seconds
                            ),
                            modifier = Modifier.scale(1F, 3F),
                            fontFamily = RobotoMono,
                            fontSize = MaterialTheme.typography.displayLarge.fontSize * 1.2,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            softWrap = false
                        )
                    }
                }
            }
        })
    }
}
