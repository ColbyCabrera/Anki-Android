package com.ichi2.anki.ui.compose.help

import android.content.Intent
import android.net.Uri
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ichi2.anki.R
import com.ichi2.anki.ui.compose.theme.AnkiDroidTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen() {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    val context = LocalContext.current

    AnkiDroidTheme {
        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                LargeTopAppBar(
                    title = {
                        Text(
                            text = stringResource(id = R.string.help_screen_title)
                        )
                    },
                    scrollBehavior = scrollBehavior
                )
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
            ) {
                item {
                    HelpItem(
                        titleRes = R.string.help_manual_title,
                        subtitleRes = R.string.help_manual_subtitle,
                        icon = Icons.AutoMirrored.Filled.Help,
                        onClick = {
                            // TODO: Add actual link
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://docs.ankidroid.org"))
                            context.startActivity(intent)
                        }
                    )
                }
                item {
                    Spacer(modifier = Modifier.padding(8.dp))
                }
                item {
                    HelpItem(
                        titleRes = R.string.help_forum_title,
                        subtitleRes = R.string.help_forum_subtitle,
                        icon = Icons.Default.Forum,
                        onClick = {
                             // TODO: Add actual link
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://forums.ankiweb.net"))
                            context.startActivity(intent)
                        }
                    )
                }
                item {
                    Spacer(modifier = Modifier.padding(8.dp))
                }
                item {
                    HelpItem(
                        titleRes = R.string.help_issue_tracker_title,
                        subtitleRes = R.string.help_issue_tracker_subtitle,
                        icon = Icons.Default.BugReport,
                        onClick = {
                             // TODO: Add actual link
                            // Placeholder
                        }
                    )
                }
                item {
                    Spacer(modifier = Modifier.padding(8.dp))
                }
                item {
                    HelpItem(
                        titleRes = R.string.help_donate_title,
                        subtitleRes = R.string.help_donate_subtitle,
                        icon = Icons.Default.VolunteerActivism,
                        onClick = {
                             // TODO: Add actual link
                            // Placeholder
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun HelpItem(
    @StringRes titleRes: Int,
    @StringRes subtitleRes: Int,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium
    ) {
        ListItem(
            headlineContent = { Text(text = stringResource(id = titleRes)) },
            supportingContent = { Text(text = stringResource(id = subtitleRes)) },
            leadingContent = {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            trailingContent = {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                    contentDescription = null,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        )
    }
}
