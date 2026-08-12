package com.zoya.sudoku.ui.inprogress

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.zoya.sudoku.data.repository.InProgressPuzzle
import com.zoya.sudoku.ui.components.RegionThumbnail
import com.zoya.sudoku.ui.components.ScreenHeader
import com.zoya.sudoku.ui.displayName
import com.zoya.sudoku.ui.formatUpdatedAt

@Composable
fun InProgressScreen(viewModel: InProgressViewModel, onContinue: (Long) -> Unit, onHome: () -> Unit) {
    val puzzles by viewModel.puzzles.collectAsState()

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            ScreenHeader("Продолжить", onHome)

            if (puzzles.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Незавершённых партий нет.", style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(puzzles, key = { it.id }) { puzzle ->
                        InProgressRow(
                            puzzle = puzzle,
                            onContinue = { onContinue(puzzle.id) },
                            onAbandon = { viewModel.abandon(puzzle.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InProgressRow(puzzle: InProgressPuzzle, onContinue: () -> Unit, onAbandon: () -> Unit) {
    var menuExpanded by remember { mutableStateOf(false) }
    var confirmingAbandon by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onContinue)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        RegionThumbnail(layout = puzzle.layout, modifier = Modifier.size(64.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                puzzle.layoutName,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                "${puzzle.difficulty.displayName()} · ${formatUpdatedAt(puzzle.updatedAt)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Box {
            IconButton(onClick = { menuExpanded = true }) {
                Text("⋮", style = MaterialTheme.typography.titleLarge)
            }
            DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                DropdownMenuItem(
                    text = { Text("Бросить") },
                    onClick = { menuExpanded = false; confirmingAbandon = true }
                )
            }
        }
    }

    if (confirmingAbandon) {
        AlertDialog(
            onDismissRequest = { confirmingAbandon = false },
            confirmButton = {
                TextButton(onClick = { confirmingAbandon = false; onAbandon() }) { Text("Бросить") }
            },
            dismissButton = {
                TextButton(onClick = { confirmingAbandon = false }) { Text("Отмена") }
            },
            title = { Text("Бросить партию?") },
            text = { Text("Прогресс в «${puzzle.layoutName}» будет потерян.") }
        )
    }
}
