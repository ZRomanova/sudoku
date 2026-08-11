package com.zoya.sudoku.ui.stats

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.zoya.sudoku.data.repository.LayoutStats
import com.zoya.sudoku.data.repository.ResultTally
import com.zoya.sudoku.engine.Difficulty
import com.zoya.sudoku.ui.components.RegionThumbnail
import com.zoya.sudoku.ui.components.ScreenHeader
import com.zoya.sudoku.ui.displayName

@Composable
fun StatsScreen(viewModel: StatsViewModel, onHome: () -> Unit) {
    val layoutStats by viewModel.layoutStats.collectAsState()
    var confirmingReset by remember { mutableStateOf(false) }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            ScreenHeader("Статистика", onHome) {
                IconButton(onClick = { confirmingReset = true }, enabled = layoutStats.isNotEmpty()) {
                    Text("↺", style = MaterialTheme.typography.titleLarge)
                }
            }

            if (layoutStats.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "Пока нет завершённых игр.\nСыграйте хотя бы одну партию до конца, чтобы увидеть статистику.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(layoutStats, key = { it.layoutId }) { stats ->
                        StatsRow(stats)
                    }
                }
            }
        }
    }

    if (confirmingReset) {
        AlertDialog(
            onDismissRequest = { confirmingReset = false },
            confirmButton = {
                TextButton(onClick = { confirmingReset = false; viewModel.reset() }) { Text("Сбросить") }
            },
            dismissButton = {
                TextButton(onClick = { confirmingReset = false }) { Text("Отмена") }
            },
            title = { Text("Сбросить статистику?") },
            text = { Text("Вся история завершённых игр будет удалена без возможности восстановления.") }
        )
    }
}

@Composable
private fun StatsRow(stats: LayoutStats) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .padding(vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            RegionThumbnail(layout = stats.layout, modifier = Modifier.size(56.dp))
            Text(
                stats.layoutName,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Text(
                "${stats.overall.percent}%",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }

        if (expanded) {
            Column(modifier = Modifier.padding(start = 68.dp, top = 8.dp)) {
                for (difficulty in Difficulty.entries) {
                    val tally = stats.byDifficulty[difficulty] ?: ResultTally(correct = 0, total = 0)
                    Text(
                        "${difficulty.displayName()}: ${tally.describe()}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    "Всего: ${stats.overall.correct} из ${stats.overall.total} игр",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

private fun ResultTally.describe(): String =
    if (total == 0) "— не играли" else "$correct/$total ($percent%)"
