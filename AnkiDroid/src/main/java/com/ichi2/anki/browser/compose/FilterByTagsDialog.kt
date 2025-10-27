package com.ichi2.anki.browser.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ichi2.anki.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterByTagsDialog(
    onDismissRequest: () -> Unit,
    onConfirm: (Set<String>) -> Unit,
    allTags: List<String>,
    initialSelection: Set<String>
) {
    var selection by remember(initialSelection) { mutableStateOf(initialSelection.toSet()) }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(text = stringResource(id = R.string.card_browser_search_by_tag)) },
        text = {
            LazyColumn {
                item {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        allTags.forEach { tag ->
                            FilterChip(
                                modifier = Modifier.height(
                                    FilterChipDefaults.Height
                                ),
                                selected = tag in selection,
                                onClick = {
                                    selection = if (tag in selection) {
                                        selection - tag
                                    } else {
                                        selection + tag
                                    }
                                },
                                label = { Text(text = tag) },
                                    leadingIcon = {
                                    if (tag in selection) {

                                        Icon(
                                            painter = painterResource(R.drawable.check_24px),
                                            contentDescription = stringResource(R.string.done_icon),
                                            modifier = Modifier.size(FilterChipDefaults.IconSize)
                                        )

                                    } else {
                                        Spacer(Modifier.size(FilterChipDefaults.IconSize / 2))
                                    }
                                },
                                trailingIcon = {
                                    if (tag in selection) {
                                        Spacer(Modifier.size(0.dp))
                                    } else {
                                        Spacer(Modifier.size(FilterChipDefaults.IconSize / 2))
                                    }
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selection) }) {
                Text(text = stringResource(id = R.string.dialog_ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(text = stringResource(id = R.string.dialog_cancel))
            }
        })
}
