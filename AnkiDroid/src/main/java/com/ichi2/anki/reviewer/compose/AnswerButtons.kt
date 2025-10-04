/*
 * Copyright (c) 2024 Brayan Oliveira <brayandso.dev@gmail.com>
 *
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
package com.ichi2.anki.reviewer.compose

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ichi2.anki.ui.compose.theme.AnkiDroidTheme

@Composable
fun AnswerButtons(
    isAnswerShown: Boolean,
    showTypeInAnswer: Boolean,
    typedAnswer: String,
    onTypedAnswerChanged: (String) -> Unit,
    onShowAnswer: () -> Unit,
    onAgain: () -> Unit,
    onHard: () -> Unit,
    onGood: () -> Unit,
    onEasy: () -> Unit,
    nextTimes: List<String>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (showTypeInAnswer) {
            TextField(
                value = typedAnswer,
                onValueChange = onTypedAnswerChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Type in the answer") },
                readOnly = isAnswerShown
            )
        }

        if (isAnswerShown) {
            EaseButtons(
                onAgain = onAgain,
                onHard = onHard,
                onGood = onGood,
                onEasy = onEasy,
                nextTimes = nextTimes
            )
        } else {
            ShowAnswerButton(onShowAnswer)
        }
    }
}

import androidx.compose.material3.MaterialTheme

@Composable
fun ShowAnswerButton(
    onShowAnswer: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onShowAnswer,
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    ) {
        Text("Show Answer")
    }
}

@Composable
fun EaseButtons(
    onAgain: () -> Unit,
    onHard: () -> Unit,
    onGood: () -> Unit,
    onEasy: () -> Unit,
    nextTimes: List<String>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        EaseButton(
            label = "Again",
            nextTime = nextTimes.getOrNull(0) ?: "",
            onClick = onAgain,
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
            modifier = Modifier.weight(1f)
        )
        EaseButton(
            label = "Hard",
            nextTime = nextTimes.getOrNull(1) ?: "",
            onClick = onHard,
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.weight(1f)
        )
        EaseButton(
            label = "Good",
            nextTime = nextTimes.getOrNull(2) ?: "",
            onClick = onGood,
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.weight(1f)
        )
        EaseButton(
            label = "Easy",
            nextTime = nextTimes.getOrNull(3) ?: "",
            onClick = onEasy,
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun EaseButton(
    label: String,
    nextTime: String,
    onClick: () -> Unit,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .height(64.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        )
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = nextTime)
            Text(text = label)
        }
    }
}

@Preview(name = "Show Answer Button", showBackground = true)
@Composable
fun AnswerButtonsPreview_ShowAnswer() {
    AnkiDroidTheme {
        AnswerButtons(
            isAnswerShown = false,
            showTypeInAnswer = true,
            onShowAnswer = {},
            onAgain = {},
            onHard = {},
            onGood = {},
            onEasy = {},
            nextTimes = emptyList(),
            typedAnswer = "Some answer",
            onTypedAnswerChanged = {}
        )
    }
}

@Preview(name = "Ease Buttons", showBackground = true)
@Composable
fun AnswerButtonsPreview_EaseButtons() {
    AnkiDroidTheme {
        AnswerButtons(
            isAnswerShown = true,
            showTypeInAnswer = false,
            onShowAnswer = {},
            onAgain = {},
            onHard = {},
            onGood = {},
            onEasy = {},
            nextTimes = listOf("<10m", "2d", "3d", "4d"),
            typedAnswer = "",
            onTypedAnswerChanged = {}
        )
    }
}