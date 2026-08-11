package com.zoya.sudoku.ui.library

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.zoya.sudoku.data.repository.SavedLayout
import com.zoya.sudoku.engine.Difficulty
import com.zoya.sudoku.ui.capitalizeFirst
import com.zoya.sudoku.ui.components.DifficultyDialog
import com.zoya.sudoku.ui.components.RegionThumbnail
import com.zoya.sudoku.ui.components.ScreenHeader

@Composable
fun LibraryScreen(viewModel: LibraryViewModel, onPlay: () -> Unit, onHome: () -> Unit) {
    val layouts by viewModel.layouts.collectAsState()
    val generatingLayoutId by viewModel.generatingLayoutId.collectAsState()

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            ScreenHeader("Мои раскраски", onHome)

            if (layouts.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "Пока нет сохранённых раскрасок.\nСоздайте одну в конструкторе.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(layouts, key = { it.id }) { saved ->
                        LayoutRow(
                            saved = saved,
                            isGenerating = generatingLayoutId == saved.id,
                            onPlay = { difficulty -> viewModel.play(saved.id, difficulty, onPlay) },
                            onRename = { name -> viewModel.rename(saved.id, name) },
                            onDelete = { viewModel.delete(saved.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LayoutRow(
    saved: SavedLayout,
    isGenerating: Boolean,
    onPlay: (Difficulty) -> Unit,
    onRename: (String) -> Unit,
    onDelete: () -> Unit
) {
    var showPlayDialog by remember { mutableStateOf(false) }
    var renaming by remember { mutableStateOf(false) }
    var confirmingDelete by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (isGenerating) 0.5f else 1f)
            .clickable(enabled = !isGenerating) { showPlayDialog = true }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        RegionThumbnail(layout = saved.layout, modifier = Modifier.size(80.dp))
        Text(
            saved.name,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Box {
            IconButton(onClick = { menuExpanded = true }, enabled = !isGenerating) {
                Text("⋮", style = MaterialTheme.typography.titleLarge)
            }
            DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                DropdownMenuItem(
                    text = { Text("Переименовать") },
                    onClick = { menuExpanded = false; renaming = true }
                )
                DropdownMenuItem(
                    text = { Text("Удалить") },
                    onClick = { menuExpanded = false; confirmingDelete = true }
                )
            }
        }
    }

    if (showPlayDialog) {
        DifficultyDialog(
            onDismiss = { showPlayDialog = false },
            onStart = { difficulty ->
                showPlayDialog = false
                onPlay(difficulty)
            }
        )
    }

    if (renaming) {
        RenameDialog(
            currentName = saved.name,
            onDismiss = { renaming = false },
            onConfirm = { name ->
                renaming = false
                onRename(name)
            }
        )
    }

    if (confirmingDelete) {
        AlertDialog(
            onDismissRequest = { confirmingDelete = false },
            confirmButton = {
                TextButton(onClick = { confirmingDelete = false; onDelete() }) { Text("Удалить") }
            },
            dismissButton = {
                TextButton(onClick = { confirmingDelete = false }) { Text("Отмена") }
            },
            title = { Text("Удалить раскраску?") },
            text = { Text("«${saved.name}» будет удалена без возможности восстановления.") }
        )
    }
}

@Composable
private fun RenameDialog(currentName: String, onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var name by remember { mutableStateOf(currentName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Переименовать") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it.capitalizeFirst() },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(name) }, enabled = name.isNotBlank()) { Text("Сохранить") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        }
    )
}
