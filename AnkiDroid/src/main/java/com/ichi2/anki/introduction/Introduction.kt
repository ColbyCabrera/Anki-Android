/*
* Copyright (c) 2022 David Allison <davidallisongithub@gmail.com> 2025 Colby Cabrera <colbycabrera.wd@gmail.com>

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

package com.ichi2.anki.introduction


import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ichi2.anki.R
import com.ichi2.anki.ui.compose.theme.AnkiDroidTheme

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun IntroductionScreen(
    acknowledgedState: MutableState<Boolean>, onGetStarted: () -> Unit, onSync: () -> Unit
) {
    val acknowledged by acknowledgedState
    val uriHandler = LocalUriHandler.current

    if (acknowledged) {
        BackHandler {
            acknowledgedState.value = false
        }
    }

    AnkiDroidTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
            ) {
                if (!acknowledged) {
                    Text(
                        text = "Before continuing!",
                        style = MaterialTheme.typography.displayMediumEmphasized,
                        modifier = Modifier.semantics { contentDescription = "intro_title" }
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "This is app is fork of AnkiDroid so please consider donating to the AnkiDroid team to support their work. The creator of Anki has also kindly allowed the use of AnkiWeb sync. If you'd like to support him, please consider buying the iPhone version of Anki.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "If you have any issues with this version, please contact me and not the AnkiDroid team. Happy memorizing!",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Button(
                            onClick = { uriHandler.openUri("https://opencollective.com/ankidroid") },
                            modifier = Modifier.weight(1F),
                            colors = ButtonDefaults.filledTonalButtonColors()
                        ) {
                            Text("Donate to AnkiDroid")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { acknowledgedState.value = true },
                            modifier = Modifier.semantics { contentDescription = "ok_button" }
                        ) {
                            Text(stringResource(R.string.dialog_ok))
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Button(
                            onClick = onGetStarted, modifier = Modifier.fillMaxWidth(0.8f)
                        ) {
                            Text(stringResource(R.string.intro_get_started))
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = onSync, modifier = Modifier.fillMaxWidth(0.8f)
                        ) {
                            Text(stringResource(R.string.intro_sync_from_ankiweb))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun IntroductionScreen(
    onGetStarted: () -> Unit, onSync: () -> Unit
) {
    val acknowledgedState = remember { mutableStateOf(false) }
    IntroductionScreen(
        acknowledgedState = acknowledgedState, onGetStarted = onGetStarted, onSync = onSync
    )
}

@Preview
@Composable
fun IntroductionScreenPreview() {
    IntroductionScreen(onGetStarted = { }, onSync = { })
}

