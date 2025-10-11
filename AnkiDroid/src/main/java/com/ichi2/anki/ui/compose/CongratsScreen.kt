package com.ichi2.anki.ui.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ichi2.anki.R
import com.ichi2.anki.ui.compose.theme.AnkiDroidTheme
import com.ichi2.anki.ui.compose.theme.RobotoMono
import kotlinx.coroutines.delay
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding)
                    .padding(vertical = 48.dp, horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(id = R.string.studyoptions_congrats_finished),
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    text = stringResource(R.string.daily_limit_reached),
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = stringResource(R.string.study_more),
                    style = MaterialTheme.typography.bodyLarge
                )

                var remainingTime by remember { mutableLongStateOf(timeUntilNextDay) }

                LaunchedEffect(Unit) {
                    while (remainingTime > 0) {
                        delay(1000)
                        remainingTime -= 1000
                    }
                }

                val hours = TimeUnit.MILLISECONDS.toHours(remainingTime)
                val minutes = TimeUnit.MILLISECONDS.toMinutes(remainingTime) % 60
                val seconds = TimeUnit.MILLISECONDS.toSeconds(remainingTime) % 60

                Text()

                Text(
                    text = "Next review in: ${
                        hours.toString().padStart(2, '0')
                    }:${minutes.toString().padStart(2, '0')}:${
                        seconds.toString().padStart(2, '0')
                    }",
                    fontFamily = RobotoMono,
                    fontSize = MaterialTheme.typography.displayLarge.fontSize,
                )
            }
        })
    }
}
