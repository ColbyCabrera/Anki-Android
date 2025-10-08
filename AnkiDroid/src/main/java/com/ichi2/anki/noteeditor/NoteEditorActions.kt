package com.ichi2.anki.noteeditor

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ichi2.anki.R

@Composable
fun NoteEditorActions(
    tags: String,
    onTagsClicked: () -> Unit,
    cards: String,
    onCardsClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = onTagsClicked,
            modifier = Modifier.weight(1f)
        ) {
            Text(stringResource(R.string.CardEditorTags, tags))
        }
        Button(
            onClick = onCardsClicked,
            modifier = Modifier.weight(1f)
        ) {
            Text(stringResource(R.string.CardEditorCards, cards))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NoteEditorActionsPreview() {
    NoteEditorActions(
        tags = "tag1, tag2",
        onTagsClicked = {},
        cards = "Card 1, Card 2",
        onCardsClicked = {}
    )
}