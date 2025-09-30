package com.ichi2.anki.deckpicker

import android.content.Intent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ichi2.anki.CardBrowser
import com.ichi2.anki.DeckPicker
import com.ichi2.anki.R
import com.ichi2.anki.dialogs.help.HelpDialog
import com.ichi2.anki.preferences.PreferencesActivity

@Composable
fun AppNavigationDrawer(
    drawerState: DrawerState,
    onNavigate: () -> Unit
) {
    val context = LocalContext.current

    ModalDrawerSheet {
        Text(
            stringResource(R.string.app_name),
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.titleLarge
        )
        Divider()
        NavigationDrawerItem(
            icon = { Icon(Icons.Filled.Home, contentDescription = null) },
            label = { Text(stringResource(R.string.deck_picker_title)) },
            selected = true,
            onClick = {
                context.startActivity(Intent(context, DeckPicker::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP))
                onNavigate()
            }
        )
        NavigationDrawerItem(
            icon = { Icon(Icons.Filled.Search, contentDescription = null) },
            label = { Text(stringResource(R.string.card_browser)) },
            selected = false,
            onClick = {
                context.startActivity(Intent(context, CardBrowser::class.java))
                onNavigate()
            }
        )
        NavigationDrawerItem(
            icon = { Icon(Icons.Filled.BarChart, contentDescription = null) },
            label = { Text(stringResource(R.string.statistics_title)) },
            selected = false,
            onClick = {
                context.startActivity(com.ichi2.anki.pages.Statistics.getIntent(context))
                onNavigate()
            }
        )
        Divider()
        NavigationDrawerItem(
            icon = { Icon(Icons.Filled.Settings, contentDescription = null) },
            label = { Text(stringResource(R.string.settings)) },
            selected = false,
            onClick = {
                context.startActivity(PreferencesActivity.getIntent(context))
                onNavigate()
            }
        )
        NavigationDrawerItem(
            icon = { Icon(Icons.Filled.Help, contentDescription = null) },
            label = { Text(stringResource(R.string.help)) },
            selected = false,
            onClick = {
                (context as? DeckPicker)?.showDialogFragment(HelpDialog.newHelpInstance())
                onNavigate()
            }
        )
        NavigationDrawerItem(
            icon = { Icon(Icons.Filled.Favorite, contentDescription = null) },
            label = { Text(stringResource(R.string.support_ankidroid)) },
            selected = false,
            onClick = {
                 (context as? DeckPicker)?.showDialogFragment(HelpDialog.newSupportInstance(false))
                onNavigate()
            }
        )
    }
}