package com.zoya.sudoku.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.zoya.sudoku.ui.components.DifficultyDialog
import com.zoya.sudoku.ui.components.RegionThumbnail
import com.zoya.sudoku.ui.displayName
import com.zoya.sudoku.ui.formatUpdatedAt

/** How many in-progress puzzles Home shows before pointing to the full "Все" list. */
private const val VISIBLE_IN_PROGRESS = 3

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onConstructor: () -> Unit,
    onLibrary: () -> Unit,
    onStats: () -> Unit,
    onSettings: () -> Unit,
    onAllInProgress: () -> Unit,
    onContinue: (Long) -> Unit,
    onPlay: (Long) -> Unit
) {
    val inProgress by viewModel.inProgress.collectAsState()
    val isStartingRandom by viewModel.isStartingRandom.collectAsState()
    var showDifficultyDialog by remember { mutableStateOf(false) }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Судоку из регионов",
                    style = MaterialTheme.typography.headlineSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onSettings) {
                    Text("⚙", style = MaterialTheme.typography.headlineSmall)
                }
            }
            Spacer(Modifier.height(32.dp))

            Text(
                "НАЧАТЬ",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = { showDifficultyDialog = true },
                enabled = !isStartingRandom,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isStartingRandom) "Готовим игру..." else "Случайная игра")
            }
            Spacer(Modifier.height(12.dp))
            Button(onClick = onLibrary, modifier = Modifier.fillMaxWidth()) {
                Text("Играть по раскладке")
            }

            if (inProgress.isNotEmpty()) {
                Spacer(Modifier.height(24.dp))
                Text(
                    "ПРОДОЛЖИТЬ",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(4.dp))
                for (puzzle in inProgress.take(VISIBLE_IN_PROGRESS)) {
                    InProgressRow(puzzle = puzzle, onClick = { onContinue(puzzle.id) })
                }
                if (inProgress.size > VISIBLE_IN_PROGRESS) {
                    TextButton(onClick = onAllInProgress, modifier = Modifier.fillMaxWidth()) {
                        Text("Все (${inProgress.size})")
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(Modifier.height(24.dp))

            OutlinedButton(onClick = onConstructor, modifier = Modifier.fillMaxWidth()) {
                Text("Конструктор регионов")
            }
            Spacer(Modifier.height(12.dp))
            OutlinedButton(onClick = onStats, modifier = Modifier.fillMaxWidth()) {
                Text("Статистика")
            }
        }
    }

    if (showDifficultyDialog) {
        DifficultyDialog(
            onDismiss = { showDifficultyDialog = false },
            onStart = { difficulty ->
                showDifficultyDialog = false
                viewModel.playRandom(difficulty, onPlay)
            }
        )
    }
}

@Composable
private fun InProgressRow(puzzle: InProgressPuzzle, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        RegionThumbnail(layout = puzzle.layout, modifier = Modifier.size(40.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                puzzle.layoutName,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                "${puzzle.difficulty.displayName()} · ${formatUpdatedAt(puzzle.updatedAt)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
