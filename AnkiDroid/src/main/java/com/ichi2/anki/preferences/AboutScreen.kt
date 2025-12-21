/*
 *  Copyright (c) 2024 David Allison <davidallisongithub@gmail.com>
 *
 *  This program is free software; you can redistribute it and/or modify it under
 *  the terms of the GNU General Public License as published by the Free Software
 *  Foundation; either version 3 of the License, or (at your option) any later
 *  version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY
 *  WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 *  PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with
 *  this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.ichi2.anki.preferences

import android.text.method.LinkMovementMethod
import android.widget.TextView
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.text.parseAsHtml
import com.ichi2.anki.R
import com.ichi2.anki.ui.compose.theme.AnkiDroidTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    versionText: String,
    buildDateText: String,
    backendText: String,
    fsrsText: String,
    contributorsText: String,
    licenseText: String,
    onBackClick: () -> Unit,
    onLogoClick: () -> Unit,
    onCopyDebugClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text(stringResource(R.string.pref_cat_about_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(androidx.appcompat.R.string.abc_action_bar_up_description)
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo
            Image(
                painter = painterResource(R.drawable.ic_dialog_info),
                contentDescription = null,
                modifier = Modifier
                    .size(96.dp)
                    .clickable(onClick = onLogoClick)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.about_fork_of, stringResource(R.string.app_name)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            SectionTitle(stringResource(R.string.about_version))

            // Versions
            VersionText(versionText)
            VersionText(buildDateText)
            VersionText(backendText)

            if (fsrsText.isNotEmpty()) {
                VersionText(fsrsText)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                // Contributors
                SectionTitle(stringResource(R.string.contributors_title))
                HtmlTextView(
                    text = contributorsText
                )

                Spacer(modifier = Modifier.height(20.dp))

                // License
                SectionTitle(stringResource(R.string.license))
                HtmlTextView(
                    text = licenseText
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Buttons
            TextButton(onClick = onCopyDebugClick) {
                Text(stringResource(R.string.feedback_copy_debug))
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun VersionText(text: String) {
    if (text.isNotEmpty()) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 2.dp)
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
private fun HtmlTextView(text: String) {
    val textColor = MaterialTheme.colorScheme.onSurfaceVariant.toArgb()

    AndroidView(
        factory = { context ->
            TextView(context).apply {
                movementMethod = LinkMovementMethod.getInstance()
                setTextColor(textColor)
                textSize = 14f
            }
        },
        update = {
            it.text = text.parseAsHtml()
        },
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

@Preview
@Composable
private fun AboutScreenPreview() {
    AnkiDroidTheme {
        AboutScreen(
            versionText = "2.x",
            buildDateText = "13 Apr 2023",
            backendText = "(anki 23.10.1 / ...)",
            fsrsText = "(FSRS 0.6.4)",
            contributorsText = "Contributors...",
            licenseText = "License...",
            onBackClick = {},
            onLogoClick = {},
            onCopyDebugClick = {}
        )
    }
}
