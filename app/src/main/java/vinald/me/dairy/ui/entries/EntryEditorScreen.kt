package vinald.me.dairy.ui.entries

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AssistChip
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import vinald.me.dairy.data.Mood
import vinald.me.dairy.ui.PhotoStrip
import vinald.me.dairy.ui.formatFull
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntryEditorScreen(
    entryId: Long?,
    onDone: () -> Unit,
    viewModel: EntryEditorViewModel = viewModel(factory = EntryEditorViewModel.factory(entryId)),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showDatePicker by remember { mutableStateOf(false) }

    val photoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(),
    ) { uris -> viewModel.onPhotosPicked(uris) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (entryId == null) "New entry" else "Edit entry") },
                navigationIcon = {
                    IconButton(onClick = onDone) {
                        Icon(Icons.Default.Close, contentDescription = "Discard")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.save(onSaved = { onDone() }) },
                        enabled = state.canSave,
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Save")
                    }
                },
            )
        },
    ) { padding ->
        if (!state.loaded) return@Scaffold
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(Modifier.height(8.dp))
            AssistChip(
                onClick = { showDatePicker = true },
                label = { Text(state.date.formatFull()) },
                leadingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
            )

            Spacer(Modifier.height(16.dp))
            Text("Mood", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(8.dp))
            MoodPicker(selected = state.mood, onSelect = viewModel::onMoodChange)

            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = state.title,
                onValueChange = viewModel::onTitleChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Title") },
                singleLine = true,
            )

            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = state.body,
                onValueChange = viewModel::onBodyChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp),
                label = { Text("What happened today?") },
            )

            Spacer(Modifier.height(16.dp))
            val photoModels: List<Any> = state.existingPhotos.map {
                viewModel.photoFile(it.fileName)
            } + state.newPhotos
            if (photoModels.isNotEmpty()) {
                PhotoStrip(
                    photos = photoModels,
                    modifier = Modifier.fillMaxWidth(),
                    onRemove = { model ->
                        when (model) {
                            is Uri -> viewModel.onRemoveNewPhoto(model)
                            is File -> state.existingPhotos
                                .firstOrNull { viewModel.photoFile(it.fileName) == model }
                                ?.let(viewModel::onRemoveExistingPhoto)
                        }
                    },
                )
                Spacer(Modifier.height(8.dp))
            }
            OutlinedButton(
                onClick = {
                    photoPicker.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                    )
                },
            ) {
                Icon(Icons.Default.AddPhotoAlternate, contentDescription = null)
                Text("  Add photos")
            }

            Spacer(Modifier.height(24.dp))
        }
    }

    if (showDatePicker) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = state.date
                .atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { millis ->
                        viewModel.onDateChange(
                            LocalDate.ofInstant(Instant.ofEpochMilli(millis), ZoneOffset.UTC),
                        )
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            },
        ) {
            DatePicker(state = pickerState)
        }
    }
}

@Composable
private fun MoodPicker(selected: Mood, onSelect: (Mood) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Mood.entries.forEach { mood ->
            val isSelected = mood == selected
            Surface(
                shape = CircleShape,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                },
                border = if (isSelected) {
                    BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                } else {
                    null
                },
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
                    .selectable(
                        selected = isSelected,
                        role = Role.RadioButton,
                        onClick = { onSelect(mood) },
                    ),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        mood.emoji,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.headlineSmall,
                    )
                }
            }
        }
    }
}
