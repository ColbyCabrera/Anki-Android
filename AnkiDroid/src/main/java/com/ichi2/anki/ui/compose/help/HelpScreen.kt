package com.ichi2.anki.ui.compose.help

import android.content.Intent
import android.net.Uri
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ichi2.anki.R
import com.ichi2.anki.ui.compose.theme.AnkiDroidTheme

private data class HelpLink(
    @StringRes val titleRes: Int,
    @StringRes val subtitleRes: Int,
    val icon: ImageVector,
    val url: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen() {
    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    val context = LocalContext.current

    val helpLinks = listOf(
        HelpLink(
            R.string.help_manual_title,
            R.string.help_manual_subtitle,
            Icons.AutoMirrored.Filled.Help,
            "https://docs.ankidroid.org"
        ),
        HelpLink(
            R.string.help_forum_title,
            R.string.help_forum_subtitle,
            Icons.Default.Forum,
            "https://forums.ankiweb.net"
        ),
        HelpLink(
            R.string.help_issue_tracker_title,
            R.string.help_issue_tracker_subtitle,
            Icons.Default.BugReport,
            "https://github.com/ColbyCabrera/Anki-Android"
        ),
        HelpLink(
            R.string.help_donate_title,
            R.string.help_donate_subtitle,
            Icons.Default.VolunteerActivism,
            "https://ankidroid.org/#donations"
        )
    )

    val cardColors = listOf(
        MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer,
        MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer,
        MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer,
        MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant,
    )

    AnkiDroidTheme {
        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection), topBar = {
                LargeTopAppBar(
                    title = {
                        Text(
                            text = stringResource(id = R.string.help_screen_title)
                        )
                    }, scrollBehavior = scrollBehavior
                )
            }) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                itemsIndexed(helpLinks) { index, helpLink ->
                    val (containerColor, contentColor) = cardColors[index % cardColors.size]
                    HelpItem(
                        titleRes = helpLink.titleRes,
                        subtitleRes = helpLink.subtitleRes,
                        icon = helpLink.icon,
                        containerColor = containerColor,
                        contentColor = contentColor,
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(helpLink.url))
                            context.startActivity(intent)
                        })
                }
            }
        }
    }
}

@Composable
private fun HelpItem(
    @StringRes titleRes: Int,
    @StringRes subtitleRes: Int,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = containerColor, contentColor = contentColor
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = icon, contentDescription = null, modifier = Modifier.size(48.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(id = titleRes),
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = stringResource(id = subtitleRes),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                contentDescription = null,
            )
        }
    }
}
