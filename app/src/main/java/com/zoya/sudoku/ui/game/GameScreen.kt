package com.zoya.sudoku.ui.game

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zoya.sudoku.ui.components.DigitPad
import com.zoya.sudoku.ui.components.NotesGrid
import com.zoya.sudoku.ui.components.ScreenHeader
import com.zoya.sudoku.ui.components.SudokuGridView
import com.zoya.sudoku.ui.theme.ErrorCellColor
import com.zoya.sudoku.ui.theme.RegionColors
import com.zoya.sudoku.ui.theme.SuccessColor
import com.zoya.sudoku.ui.theme.contrastingDigitColor

@Composable
fun GameScreen(viewModel: GameViewModel, onHome: () -> Unit) {
    val state by viewModel.uiState.collectAsState()
    val mistakeCount by viewModel.mistakeCount.collectAsState()
    var confirmingReset by remember { mutableStateOf(false) }

    // Solving a puzzle often means staring at the board without touching the screen - don't let
    // the system's screen-off timer cut that thinking time short while this screen is open.
    val view = LocalView.current
    DisposableEffect(Unit) {
        view.keepScreenOn = true
        onDispose { view.keepScreenOn = false }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            val title = when (val s = state) {
                is GameUiState.Loaded -> s.layoutName
                GameUiState.Loading -> "Игра"
            }
            ScreenHeader(title, onHome) {
                val canReset = (state as? GameUiState.Loaded)?.isSolved == false
                IconButton(onClick = { confirmingReset = true }, enabled = canReset) {
                    Text("↺", style = MaterialTheme.typography.titleLarge)
                }
            }
            Spacer(Modifier.height(12.dp))

            when (val s = state) {
                // Blank themed background only while the puzzle loads - no spinner, per the
                // no-progress-indicators requirement; the read is near-instant in practice.
                GameUiState.Loading -> {}
                is GameUiState.Loaded -> if (s.isSolved) {
                    SolvedContent(state = s, onHome = onHome)
                } else {
                    GameContent(
                        state = s,
                        onSelect = viewModel::selectCell,
                        onDigit = viewModel::inputDigit,
                        onErase = viewModel::erase,
                        onToggleNotesMode = viewModel::toggleNotesMode,
                        onCheckErrors = viewModel::toggleCheckErrors,
                        onFinish = viewModel::finish
                    )
                }
            }
        }
    }

    if (confirmingReset) {
        AlertDialog(
            onDismissRequest = { confirmingReset = false },
            confirmButton = {
                TextButton(onClick = { confirmingReset = false; viewModel.resetProgress() }) { Text("Очистить") }
            },
            dismissButton = {
                TextButton(onClick = { confirmingReset = false }) { Text("Отмена") }
            },
            title = { Text("Очистить поле?") },
            text = { Text("Все введённые цифры и пометки будут удалены. Начальные цифры головоломки останутся на месте.") }
        )
    }

    mistakeCount?.let { count ->
        AlertDialog(
            onDismissRequest = viewModel::keepFixing,
            confirmButton = {
                TextButton(onClick = viewModel::keepFixing) { Text("Исправить") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.giveUp(onHome) }) { Text("Выйти") }
            },
            title = { Text("✗ Есть ошибки: $count", color = ErrorCellColor) },
            text = {
                Text("Попытка засчитана как неудачная. Неверные клетки подсвечены красным — их можно исправить и дорешать.")
            }
        )
    }
}

@Composable
private fun GameContent(
    state: GameUiState.Loaded,
    onSelect: (Int) -> Unit,
    onDigit: (Int) -> Unit,
    onErase: () -> Unit,
    onToggleNotesMode: () -> Unit,
    onCheckErrors: () -> Unit,
    onFinish: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Board(state = state, onCellTap = onSelect)

        Spacer(Modifier.height(20.dp))

        if (state.isFull) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(onClick = onCheckErrors, modifier = Modifier.weight(1f)) {
                    Text(if (state.showErrors) "Скрыть проверку" else "Проверить")
                }
                Button(onClick = onFinish, modifier = Modifier.weight(1f)) {
                    Text("Завершить")
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (state.notesMode) {
                Button(onClick = onToggleNotesMode, modifier = Modifier.fillMaxWidth()) {
                    Text("Пометки: вкл")
                }
            } else {
                OutlinedButton(onClick = onToggleNotesMode, modifier = Modifier.fillMaxWidth()) {
                    Text("Пометки: выкл")
                }
            }
        }
        Spacer(Modifier.height(8.dp))

        val noteDigitsInSelectedCell = state.selectedCell
            ?.takeIf { state.notesMode }
            ?.let { cell -> (1..9).filter { digit -> state.notes[cell] and (1 shl (digit - 1)) != 0 }.toSet() }
            ?: emptySet()

        DigitPad(
            onDigit = onDigit,
            onErase = onErase,
            activeDigits = noteDigitsInSelectedCell,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/** The finished board, read-only, under a green "Решено верно" banner. */
@Composable
private fun SolvedContent(state: GameUiState.Loaded, onHome: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        Surface(color = SuccessColor, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Text(
                    "✓ Решено верно!",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge
                )
                if (state.attemptFailed) {
                    Text(
                        "В статистике — неудача: ошибки исправлены после «Завершить»",
                        color = Color.White,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
        Spacer(Modifier.height(12.dp))

        Board(state = state, onCellTap = null)

        Spacer(Modifier.height(20.dp))
        Button(onClick = onHome, modifier = Modifier.fillMaxWidth()) {
            Text("На главную")
        }
    }
}

@Composable
private fun Board(state: GameUiState.Loaded, onCellTap: ((Int) -> Unit)?) {
    val wrongCells = if (state.showErrors) state.wrongCells else emptySet()
    SudokuGridView(
        cellRegion = { cell -> state.cellRegion[cell] },
        selectedCell = state.selectedCell,
        onCellTap = onCellTap,
        modifier = Modifier.fillMaxWidth()
    ) { cell ->
        val digit = state.board[cell]
        val background = RegionColors[state.cellRegion[cell]]
        if (digit != 0 && cell in wrongCells) {
            // A solid red tile with a white rim reads as "wrong" on every region color - a red
            // digit alone disappears on the terracotta/rose regions.
            val shape = RoundedCornerShape(6.dp)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(3.dp)
                    .background(ErrorCellColor, shape)
                    .border(2.dp, Color.White, shape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = digit.toString(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.headlineSmall
                )
            }
        } else if (digit != 0) {
            Text(
                text = digit.toString(),
                color = contrastingDigitColor(background),
                fontWeight = if (state.givens[cell] != 0) FontWeight.Bold else FontWeight.Normal,
                style = MaterialTheme.typography.headlineSmall
            )
        } else if (state.notes[cell] != 0) {
            NotesGrid(mask = state.notes[cell], color = contrastingDigitColor(background))
        }
    }
}
