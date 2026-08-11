package com.zoya.sudoku.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.zoya.sudoku.ui.components.DifficultyDialog

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onConstructor: () -> Unit,
    onLibrary: () -> Unit,
    onStats: () -> Unit,
    onContinue: () -> Unit,
    onPlay: () -> Unit
) {
    val hasActivePuzzle by viewModel.hasActivePuzzle.collectAsState()
    val isStartingRandom by viewModel.isStartingRandom.collectAsState()
    var showDifficultyDialog by remember { mutableStateOf(false) }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Судоку из регионов", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(48.dp))

            if (hasActivePuzzle) {
                Button(onClick = onContinue, modifier = Modifier.fillMaxWidth()) {
                    Text("Продолжить")
                }
                Spacer(Modifier.height(16.dp))
            }

            Button(
                onClick = { showDifficultyDialog = true },
                enabled = !isStartingRandom,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isStartingRandom) "Готовим игру..." else "Случайная игра")
            }
            Spacer(Modifier.height(16.dp))
            Button(onClick = onConstructor, modifier = Modifier.fillMaxWidth()) {
                Text("Конструктор регионов")
            }
            Spacer(Modifier.height(16.dp))
            Button(onClick = onLibrary, modifier = Modifier.fillMaxWidth()) {
                Text("Мои раскраски")
            }
            Spacer(Modifier.height(16.dp))
            Button(onClick = onStats, modifier = Modifier.fillMaxWidth()) {
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
