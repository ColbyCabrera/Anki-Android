package com.ichi2.anki.browser.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import anki.search.BrowserRow
import com.ichi2.anki.R
import com.ichi2.anki.browser.BrowserRowWithId
import com.ichi2.anki.browser.CardBrowserViewModel
import com.ichi2.anki.browser.ColumnHeading

@Composable
fun CardBrowserScreen(
    viewModel: CardBrowserViewModel,
    onCardClicked: (BrowserRowWithId) -> Unit,
    modifier: Modifier = Modifier
) {
    val browserRows by viewModel.browserRows.collectAsStateWithLifecycle()
    val columnHeadings by viewModel.flowOfColumnHeadings.collectAsStateWithLifecycle(initialValue = emptyList())

    Column(modifier = modifier) {
        CardBrowserHeader(columns = columnHeadings)
        HorizontalDivider()
        if (browserRows.isEmpty()) {
            EmptyCardBrowser()
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(
                    items = browserRows,
                    key = { it.id }
                ) { row ->
                    CardBrowserRow(
                        row = row.browserRow,
                        columns = columnHeadings,
                        modifier = Modifier.clickable { onCardClicked(row) }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
fun EmptyCardBrowser(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CardBrowserEmpty()
    }
}

@Composable
fun CardBrowserEmpty(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(id = R.string.card_browser_no_cards_in_deck),
        modifier = modifier
    )
}

@Composable
fun CardBrowserHeader(columns: List<ColumnHeading>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        columns.forEach { column ->
            Text(
                text = column.label,
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.weight(1f) // Basic weighting
            )
        }
    }
}

@Composable
fun CardBrowserRow(
    row: BrowserRow,
    columns: List<ColumnHeading>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // This is a simplified representation. The original adapter handles HTML.
        // For a full conversion, a composable that can render HTML would be needed.
        row.getCellsList().forEach { cell ->
            Text(
                text = cell.getText(),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                modifier = Modifier.weight(1f) // Basic weighting
            )
        }
    }
}
