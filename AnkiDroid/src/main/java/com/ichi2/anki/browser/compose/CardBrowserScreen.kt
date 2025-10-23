package com.ichi2.anki.browser.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import anki.search.BrowserRow
import com.ichi2.anki.browser.CardBrowserViewModel
import com.ichi2.anki.browser.ColumnHeading

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardBrowserScreen(
    viewModel: CardBrowserViewModel,
    onNavigateUp: () -> Unit,
    onCardClicked: (BrowserRow) -> Unit
) {
    val browserRows by viewModel.browserRows.collectAsStateWithLifecycle()
    val columnHeadings by viewModel.flowOfColumnHeadings.collectAsStateWithLifecycle(initialValue = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Card Browser") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Navigate Up")
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: Implement search */ }) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                    // TODO: Add other menu actions
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            CardBrowserHeader(columns = columnHeadings)
            Divider()
            if (browserRows.isEmpty()) {
                // TODO: Show a loading indicator or empty state
                Text(text = "Loading cards...", modifier = Modifier.padding(16.dp))
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(
                        items = browserRows,
                        key = { it.id }
                    ) { row ->
                        CardBrowserRow(
                            row = row,
                            columns = columnHeadings,
                            modifier = Modifier.clickable { onCardClicked(row) }
                        )
                        Divider()
                    }
                }
            }
        }
    }
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
        row.cells.forEach { cell ->
            Text(
                text = cell.text,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                modifier = Modifier.weight(1f) // Basic weighting
            )
        }
    }
}
