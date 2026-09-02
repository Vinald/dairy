package vinald.me.dairy.ui.entries

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import vinald.me.dairy.ui.formatMedium

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntryListScreen(
    onCreateEntry: () -> Unit,
    onOpenEntry: (Long) -> Unit,
    viewModel: EntryListViewModel = viewModel(factory = EntryListViewModel.Factory),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Diary") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreateEntry) {
                Icon(Icons.Default.Add, contentDescription = "New entry")
            }
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            OutlinedTextField(
                value = query,
                onValueChange = viewModel::onQueryChange,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Search entries") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onQueryChange("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear search")
                        }
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            )

            when {
                uiState.loading -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                    CircularProgressIndicator()
                }

                uiState.entries.isEmpty() -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                    Text(
                        if (query.isBlank()) "No entries yet. Tap + to write one."
                        else "No entries match \"$query\".",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }

                else -> LazyColumn(contentPadding = PaddingValues(bottom = 88.dp)) {
                    items(uiState.entries, key = { it.id }) { item ->
                        EntryRow(item, onClick = { onOpenEntry(item.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun EntryRow(item: EntryListItem, onClick: () -> Unit) {
    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        headlineContent = {
            Text(item.title.ifBlank { "(untitled)" }, maxLines = 1)
        },
        overlineContent = { Text(item.date.formatMedium()) },
        supportingContent = {
            if (item.snippet.isNotBlank()) Text(item.snippet, maxLines = 2)
        },
        leadingContent = {
            Text(item.mood.emoji, style = MaterialTheme.typography.headlineSmall)
        },
        trailingContent = {
            if (item.photoCount > 0) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text(item.photoCount.toString())
                }
            }
        },
    )
}
