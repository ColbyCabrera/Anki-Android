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
package com.ichi2.anki.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ichi2.anki.R

@Composable
fun BrowserOptions(
    onCardsModeSelected: () -> Unit,
    onNotesModeSelected: () -> Unit,
    initialMode: Int,
    onTruncateChanged: (Boolean) -> Unit,
    initialTruncate: Boolean,
    onIgnoreAccentsChanged: (Boolean) -> Unit,
    initialIgnoreAccents: Boolean,
    onManageColumnsClicked: () -> Unit,
    onRenameFlagClicked: () -> Unit,
) {
    val selectedMode = remember { mutableIntStateOf(initialMode) }
    val truncateChecked = remember { mutableStateOf(initialTruncate) }
    val ignoreAccentsChecked = remember { mutableStateOf(initialIgnoreAccents) }

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState()),
    ) {
        Text(
            text = stringResource(id = R.string.toggle_cards_notes),
            style = MaterialTheme.typography.titleMedium,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RadioButton(
                selected = selectedMode.intValue == 0,
                onClick = {
                    selectedMode.intValue = 0
                    onCardsModeSelected()
                },
            )
            Text(
                text = stringResource(id = R.string.show_cards),
                modifier = Modifier.padding(start = 8.dp),
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RadioButton(
                selected = selectedMode.intValue == 1,
                onClick = {
                    selectedMode.intValue = 1
                    onNotesModeSelected()
                },
            )
            Text(
                text = stringResource(id = R.string.show_notes),
                modifier = Modifier.padding(start = 8.dp),
            )
        }
        HorizontalDivider(
            modifier = Modifier.padding(vertical = 8.dp),
            thickness = DividerDefaults.Thickness,
            color = DividerDefaults.color
        )
        Text(
            text = stringResource(id = R.string.card_browser_truncate),
            style = MaterialTheme.typography.titleMedium,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(
                checked = truncateChecked.value,
                onCheckedChange = {
                    truncateChecked.value = it
                    onTruncateChanged(it)
                },
            )
            Text(
                text = stringResource(id = R.string.card_browser_truncate),
                modifier = Modifier.padding(start = 8.dp),
            )
        }
        Text(
            text = stringResource(id = R.string.truncate_content_help),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(start = 16.dp, top = 4.dp),
        )
        HorizontalDivider(
            modifier = Modifier.padding(vertical = 8.dp),
            thickness = DividerDefaults.Thickness,
            color = DividerDefaults.color
        )
        Text(
            text = stringResource(id = R.string.pref_cat_studying),
            style = MaterialTheme.typography.titleMedium,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(
                checked = ignoreAccentsChecked.value,
                onCheckedChange = {
                    ignoreAccentsChecked.value = it
                    onIgnoreAccentsChanged(it)
                },
            )
            Text(
                text = stringResource(id = R.string.ignore_accents_in_search),
                modifier = Modifier.padding(start = 8.dp),
            )
        }
        HorizontalDivider(
            modifier = Modifier.padding(vertical = 8.dp),
            thickness = DividerDefaults.Thickness,
            color = DividerDefaults.color
        )
        Text(
            text = stringResource(id = R.string.browse_manage_columns_main_heading),
            style = MaterialTheme.typography.titleMedium,
        )
        TextButton(onClick = onManageColumnsClicked) {
            Text(text = stringResource(id = R.string.browse_manage_columns))
        }
        HorizontalDivider(
            modifier = Modifier.padding(vertical = 8.dp),
            thickness = DividerDefaults.Thickness,
            color = DividerDefaults.color
        )
        Text(
            text = stringResource(id = R.string.menu_flag),
            style = MaterialTheme.typography.titleMedium,
        )
        TextButton(onClick = onRenameFlagClicked) {
            Text(text = stringResource(id = R.string.rename_flag))
        }
    }
}

@Preview
@Composable
fun PreviewBrowserOptions() {
    BrowserOptions(
        onCardsModeSelected = {},
        onNotesModeSelected = {},
        initialMode = 0,
        onTruncateChanged = {},
        initialTruncate = false,
        onIgnoreAccentsChanged = {},
        initialIgnoreAccents = false,
        onManageColumnsClicked = {},
        onRenameFlagClicked = {},
    )
}
