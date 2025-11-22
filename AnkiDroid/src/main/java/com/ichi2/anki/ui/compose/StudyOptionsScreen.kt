/* **************************************************************************************
 * Copyright (c) 2009 Andrew Dubya <andrewdubya@gmail.com>                              *
 * Copyright (c) 2009 Nicolas Raoul <nicolas.raoul@gmail.com>                           *
 * Copyright (c) 2009 Edu Zamora <edu.zasu@gmail.com>                                   *
 * Copyright (c) 2009 Daniel Svard <daniel.svard@gmail.com>                             *
 * Copyright (c) 2010 Norbert Nagold <norbert.nagold@gmail.com>                         *
 * Copyright (c) 2014 Timothy Rae <perceptualchaos2@gmail.com>
 *                                                                                      *
 * This program is free software; you can redistribute it and/or modify it under        *
 * the terms of the GNU General Public License as published by the Free Software        *
 * Foundation; either version 3 of the License, or (at your option) any later           *
 * version.                                                                             *
 *                                                                                      *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY      *
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A      *
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.             *
 *                                                                                      *
 * You should have received a copy of the GNU General Public License along with         *
 * this program.  If not, see <http://www.gnu.org/licenses/>.                           *
 ****************************************************************************************/
package com.ichi2.anki.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ichi2.anki.R
import com.ichi2.anki.ui.compose.theme.RobotoMono

data class StudyOptionsData(
    val deckId: Long,
    val deckName: String,
    val deckDescription: String,
    val newCount: Int,
    val lrnCount: Int,
    val revCount: Int,
    val buriedNew: Int,
    val buriedLrn: Int,
    val buriedRev: Int,
    val totalNewCards: Int,
    val totalCards: Int,
    val isFiltered: Boolean,
    val haveBuried: Boolean,
)

@Composable
fun StudyOptionsScreen(
    studyOptionsData: StudyOptionsData?,
    modifier: Modifier = Modifier,
    onStartStudy: () -> Unit,
    onCustomStudy: (Long) -> Unit,
) {
    Surface {
        if (studyOptionsData == null) {
            // Show a loading indicator or an empty state
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }

        } else {
            when {
                studyOptionsData.totalCards == 0 && !studyOptionsData.isFiltered -> {
                    EmptyDeckView(studyOptionsData, modifier)
                }

                studyOptionsData.newCount + studyOptionsData.lrnCount + studyOptionsData.revCount == 0 -> {
                    CongratsView(studyOptionsData, onCustomStudy, modifier)
                }

                else -> {
                    StudyOptionsView(studyOptionsData, onStartStudy, modifier)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun StudyOptionsView(
    studyOptionsData: StudyOptionsData,
    onStartStudy: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        Text(
            text = studyOptionsData.deckName,
            style = MaterialTheme.typography.displaySmallEmphasized,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(8.dp))
        if (studyOptionsData.deckDescription.isNotEmpty()) {
            Text(
                text = studyOptionsData.deckDescription,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            StudyOptionCardCount(
                label = "New", // TODO: Use string resource
                count = studyOptionsData.newCount,
                shape = RoundedPolygonShape(MaterialShapes.SoftBurst),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
            StudyOptionCardCount(
                label = "Learning", // TODO: Use string resource
                count = studyOptionsData.lrnCount,
                shape = RoundedPolygonShape(MaterialShapes.Square),
                containerColor = MaterialTheme.colorScheme.tertiary,
                contentColor = MaterialTheme.colorScheme.onTertiary
            )
            StudyOptionCardCount(
                label = "Review", // TODO: Use string resource
                count = studyOptionsData.revCount,
                shape = RoundedPolygonShape(MaterialShapes.Ghostish),
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary
            )
        }

        if (studyOptionsData.haveBuried) {
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "Buried cards are not included in counts above.", // TODO: Use string resource
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                StudyOptionCardCount(
                    label = "New", // TODO: Use string resource
                    count = studyOptionsData.buriedNew,
                    shape = RoundedPolygonShape(MaterialShapes.Sunny),
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                    contentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                    small = true
                )
                StudyOptionCardCount(
                    label = "Learning", // TODO: Use string resource
                    count = studyOptionsData.buriedLrn,
                    shape = RoundedPolygonShape(MaterialShapes.Cookie4Sided),
                    containerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f),
                    contentColor = MaterialTheme.colorScheme.onTertiary.copy(alpha = 0.8f),
                    small = true
                )
                StudyOptionCardCount(
                    label = "Review", // TODO: Use string resource
                    count = studyOptionsData.buriedRev,
                    shape = RoundedPolygonShape(MaterialShapes.Cookie7Sided),
                    containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
                    contentColor = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.8f),
                    small = true
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.tertiaryContainer,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Total New", style = MaterialTheme.typography.labelMedium
                    )
                    Text(
                        text = studyOptionsData.totalNewCards.toString(),
                        fontSize = 48.sp,
                        fontFamily = RobotoMono
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Total Cards", style = MaterialTheme.typography.labelMedium
                    )
                    Text(
                        text = studyOptionsData.totalCards.toString(),
                        fontSize = 48.sp,
                        fontFamily = RobotoMono
                    )
                }
            }
        }


        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onStartStudy,
            modifier = Modifier
                .fillMaxWidth()
                .height(96.dp),
        ) {
            Text(text = stringResource(R.string.studyoptions_start), fontSize = 24.sp)
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun EmptyDeckView(
    studyOptionsData: StudyOptionsData,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = studyOptionsData.deckName,
            style = MaterialTheme.typography.displaySmallEmphasized,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.empty_deck),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CongratsView(
    studyOptionsData: StudyOptionsData,
    onCustomStudy: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = stringResource(R.string.studyoptions_congrats_finished),
            style = MaterialTheme.typography.displaySmallEmphasized,
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            text = stringResource(R.string.daily_limit_reached),
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = stringResource(R.string.study_more),
            style = MaterialTheme.typography.bodyLarge
        )
        if (!studyOptionsData.isFiltered) {
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp)
                    .padding(top = 12.dp),
                onClick = { onCustomStudy(studyOptionsData.deckId) }
            ) {
                Text(text = "Custom Study", fontSize = 24.sp) // TODO: Use string resource
            }
        }
    }
}

@Composable
fun StudyOptionCardCount(
    label: String,
    count: Int,
    shape: Shape,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
    small: Boolean = false
) {
    val size = if (small) 40.dp else 64.dp
    val textStyle =
        if (small) MaterialTheme.typography.titleMedium else MaterialTheme.typography.headlineSmall

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        Box(
            modifier = Modifier
                .size(size)
                .clip(shape)
                .background(containerColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = count.toString(),
                color = contentColor,
                style = textStyle,
                modifier = Modifier
                    .padding(4.dp)
                    .basicMarquee()
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = label, style = MaterialTheme.typography.labelMedium)
    }
}

@Preview(showBackground = true)
@Composable
fun StudyOptionsScreenPreview() {
    val sampleData = StudyOptionsData(
        deckId = 1,
        deckName = "My Awesome Deck",
        deckDescription = "This is a great deck for learning Compose.",
        newCount = 10,
        lrnCount = 5,
        revCount = 20,
        buriedNew = 2,
        buriedLrn = 1,
        buriedRev = 3,
        totalNewCards = 50,
        totalCards = 200,
        isFiltered = false,
        haveBuried = true,
    )
    StudyOptionsScreen(studyOptionsData = sampleData, onStartStudy = {}, onCustomStudy = {})
}

@Preview(showBackground = true)
@Composable
fun CongratsViewPreview() {
    val sampleData = StudyOptionsData(
        deckId = 1,
        deckName = "My Awesome Deck",
        deckDescription = "",
        newCount = 0,
        lrnCount = 0,
        revCount = 0,
        buriedNew = 0,
        buriedLrn = 0,
        buriedRev = 0,
        totalNewCards = 0,
        totalCards = 100,
        isFiltered = false,
        haveBuried = false,
    )
    CongratsView(studyOptionsData = sampleData, onCustomStudy = {})
}

@Preview(showBackground = true)
@Composable
fun EmptyDeckViewPreview() {
    val sampleData = StudyOptionsData(
        deckId = 1,
        deckName = "My Awesome Deck",
        deckDescription = "",
        newCount = 0,
        lrnCount = 0,
        revCount = 0,
        buriedNew = 0,
        buriedLrn = 0,
        buriedRev = 0,
        totalNewCards = 0,
        totalCards = 0,
        isFiltered = false,
        haveBuried = false,
    )
    EmptyDeckView(studyOptionsData = sampleData)
}
