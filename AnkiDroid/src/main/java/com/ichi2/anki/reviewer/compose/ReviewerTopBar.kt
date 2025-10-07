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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ichi2.anki.R
import com.ichi2.anki.ui.compose.theme.AnkiDroidTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewerTopBar(
    newCount: Int,
    learnCount: Int,
    reviewCount: Int,
    chosenAnswer: String,
    isMarked: Boolean,
    flag: Int,
    onToggleMark: () -> Unit,
    onSetFlag: (Int) -> Unit,
    modifier: Modifier = Modifier,
    isAnswerShown: Boolean,
    onUnanswerCard: () -> Unit
) {
    CenterAlignedTopAppBar(
        modifier = modifier, title = { Text(chosenAnswer) }, navigationIcon = {
        Counts(
            newCount = newCount,
            learnCount = learnCount,
            reviewCount = reviewCount,
            modifier = Modifier.padding(start = 8.dp)
        )
    }, actions = {
        MarkIcon(isMarked = isMarked, onToggleMark = onToggleMark)
        FlagIcon(currentFlag = flag, onSetFlag = onSetFlag)
        AnimatedVisibility(visible = isAnswerShown) {
            IconButton(onClick = onUnanswerCard) {
                Icon(
                    painterResource(R.drawable.undo_24px),
                    contentDescription = stringResource(id = R.string.unanswer_card),
                )
            }
        }
    }, colors = TopAppBarDefaults.topAppBarColors(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        navigationIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
    )
    )
}

@Composable
fun MarkIcon(isMarked: Boolean, onToggleMark: () -> Unit) {
    IconButton(onClick = onToggleMark) {
        Icon(
            painter = if (isMarked) painterResource(R.drawable.star_shine_24px) else painterResource(
                R.drawable.star_24px
            ),
            contentDescription = stringResource(if (isMarked) R.string.menu_unmark_note else R.string.menu_mark_note),
            tint = if (isMarked) MaterialTheme.colorScheme.tertiary else LocalContentColor.current
        )
    }
}

@Composable
fun FlagIcon(currentFlag: Int, onSetFlag: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val flagColors = listOf(
        Color.Transparent, // 0: no flag
        Color.Red,         // 1: Red
        Color(0xFFFFA500), // 2: Orange
        Color.Green,       // 3: Green
        Color.Blue,        // 4: Blue
        Color.Magenta,     // 5: Pink
        Color.Cyan,        // 6: Turquoise
        Color(0xFF9400D3)  // 7: Purple
    )

    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(
                painter = painterResource(R.drawable.flag_24px),
                contentDescription = "Set Flag",
                tint = if (currentFlag in flagColors.indices && currentFlag != 0) flagColors[currentFlag] else LocalContentColor.current // Use LocalContentColor.current
            )
        }
        DropdownMenu(
            expanded = expanded, onDismissRequest = { expanded = false }) {
            (0..7).forEach { flag ->
                DropdownMenuItem(text = { Text("Flag $flag") }, onClick = {
                    onSetFlag(flag)
                    expanded = false
                })
            }
        }
    }
}

@Composable
fun Counts(newCount: Int, learnCount: Int, reviewCount: Int, modifier: Modifier = Modifier) {
    Row(modifier = modifier) {
        Text(
            buildAnnotatedString {
                withStyle(
                    style = SpanStyle(
                        color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold
                    )
                ) {
                    append("$newCount")
                }
                append(" ")
                withStyle(
                    style = SpanStyle(
                        color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold
                    )
                ) {
                    append("$learnCount")
                }
                append(" ")
                withStyle(
                    style = SpanStyle(
                        color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold
                    )
                ) {
                    append("$reviewCount")
                }
            }, fontSize = 14.sp
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ReviewerTopBarPreview() {
    AnkiDroidTheme {
        ReviewerTopBar(
            newCount = 13,
            learnCount = 3,
            reviewCount = 7,
            chosenAnswer = "Answer",
            isMarked = true,
            flag = 1,
            onToggleMark = {},
            onSetFlag = {},
            isAnswerShown = true,
            onUnanswerCard = {})
    }
}