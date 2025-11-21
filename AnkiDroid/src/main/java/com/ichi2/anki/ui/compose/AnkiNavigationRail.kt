package com.ichi2.anki.ui.compose

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.ichi2.anki.R

enum class AppNavigationItem(
    @DrawableRes val icon: Int,
    @StringRes val labelResId: Int
) {
    Decks(R.drawable.ic_list_black, R.string.decks),
    CardBrowser(R.drawable.ic_flashcard_black, R.string.card_browser),
    Statistics(R.drawable.ic_bar_chart_black, R.string.statistics),
    Settings(R.drawable.ic_settings_black, R.string.settings),
    Help(R.drawable.ic_help_black, R.string.help),
    Support(R.drawable.ic_support_ankidroid, R.string.help_title_support_ankidroid)
}

@Composable
fun AnkiNavigationRail(
    selectedItem: AppNavigationItem,
    onNavigate: (AppNavigationItem) -> Unit
) {
    NavigationRail {
        AppNavigationItem.values().forEach { item ->
            NavigationRailItem(
                icon = {
                    Icon(
                        painter = painterResource(item.icon),
                        contentDescription = stringResource(item.labelResId),
                    )
                },
                label = { Text(stringResource(item.labelResId)) },
                selected = selectedItem == item,
                onClick = {
                    onNavigate(item)
                },
            )
        }
    }
}
