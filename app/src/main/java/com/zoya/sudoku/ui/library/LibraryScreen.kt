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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import com.zoya.sudoku.data.repository.SavedLayout
import com.zoya.sudoku.engine.Difficulty
import com.zoya.sudoku.ui.components.ScreenHeader
import com.zoya.sudoku.ui.displayName

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
    onDelete: () -> Unit
) {
    var showPlayDialog by remember { mutableStateOf(false) }
    var confirmingDelete by remember { mutableStateOf(false) }

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
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = { confirmingDelete = true }, enabled = !isGenerating) {
            Text("✕", style = MaterialTheme.typography.titleLarge)
        }
    }

    if (showPlayDialog) {
        PlayDialog(
            onDismiss = { showPlayDialog = false },
            onStart = { difficulty ->
                showPlayDialog = false
                onPlay(difficulty)
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
private fun PlayDialog(onDismiss: () -> Unit, onStart: (Difficulty) -> Unit) {
    var selected by remember { mutableStateOf(Difficulty.MEDIUM) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Сложность") },
        text = {
            Column {
                for (difficulty in Difficulty.entries) {
                    FilterChip(
                        selected = selected == difficulty,
                        onClick = { selected = difficulty },
                        label = { Text(difficulty.displayName()) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    )
                }
            }
        },
        confirmButton = { TextButton(onClick = { onStart(selected) }) { Text("Старт") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } }
    )
}
