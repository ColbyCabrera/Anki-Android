package com.ichi2.anki.reviewer.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import com.ichi2.anki.ui.compose.theme.AnkiDroidTheme
import com.ichi2.anki.ui.compose.theme.LocalAnkiColors

@Composable
fun ReviewerTopBar(
    newCount: Int,
    learnCount: Int,
    reviewCount: Int,
    timer: String,
    chosenAnswer: String,
    isMarked: Boolean,
    flag: Int,
    onToggleMark: () -> Unit,
    onSetFlag: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val ankiColors = LocalAnkiColors.current
    androidx.constraintlayout.compose.ConstraintLayout(
        modifier = modifier
            .fillMaxWidth()
            .background(ankiColors.topBar)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        val (counts, answer, timerText, icons) = createRefs()

        Counts(
            newCount = newCount,
            learnCount = learnCount,
            reviewCount = reviewCount,
            modifier = Modifier.constrainAs(counts) {
                start.linkTo(parent.start)
                centerVerticallyTo(parent)
            }
        )

        Text(
            text = chosenAnswer,
            modifier = Modifier.constrainAs(answer) {
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                centerVerticallyTo(parent)
            }
        )

        Row(
            modifier = Modifier.constrainAs(icons) {
                end.linkTo(timerText.start, margin = 8.dp)
                centerVerticallyTo(parent)
            }
        ) {
            MarkIcon(isMarked = isMarked, onToggleMark = onToggleMark)
            FlagIcon(currentFlag = flag, onSetFlag = onSetFlag)
        }

        Text(
            text = timer,
            fontSize = 14.sp,
            modifier = Modifier.constrainAs(timerText) {
                end.linkTo(parent.end)
                centerVerticallyTo(parent)
            }
        )
    }
}

@Composable
fun MarkIcon(isMarked: Boolean, onToggleMark: () -> Unit) {
    IconButton(onClick = onToggleMark) {
        Icon(
            imageVector = if (isMarked) Icons.Filled.Star else Icons.Outlined.StarOutline,
            contentDescription = "Mark Note",
            tint = if (isMarked) Color.Yellow else Color.Gray
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
                imageVector = Icons.Default.Flag,
                contentDescription = "Set Flag",
                tint = if (currentFlag in flagColors.indices && currentFlag != 0) flagColors[currentFlag] else Color.Gray
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            (0..7).forEach { flag ->
                DropdownMenuItem(
                    text = { Text("Flag $flag") },
                    onClick = {
                        onSetFlag(flag)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun Counts(newCount: Int, learnCount: Int, reviewCount: Int, modifier: Modifier = Modifier) {
    val ankiColors = LocalAnkiColors.current
    Row(modifier = modifier) {
        Text(
            buildAnnotatedString {
                withStyle(style = SpanStyle(color = ankiColors.newCount, fontWeight = FontWeight.Bold)) {
                    append("$newCount")
                }
                append(" ")
                withStyle(style = SpanStyle(color = ankiColors.learnCount, fontWeight = FontWeight.Bold)) {
                    append("$learnCount")
                }
                append(" ")
                withStyle(style = SpanStyle(color = ankiColors.reviewCount, fontWeight = FontWeight.Bold)) {
                    append("$reviewCount")
                }
            },
            fontSize = 14.sp
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
            timer = "0.5s",
            chosenAnswer = "Answer",
            isMarked = true,
            flag = 1,
            onToggleMark = {},
            onSetFlag = {}
        )
    }
}