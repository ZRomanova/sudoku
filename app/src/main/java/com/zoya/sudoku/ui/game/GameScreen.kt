package com.zoya.sudoku.ui.game

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zoya.sudoku.ui.components.DigitPad
import com.zoya.sudoku.ui.components.ScreenHeader
import com.zoya.sudoku.ui.components.SudokuGridView
import com.zoya.sudoku.ui.theme.ErrorDigitColor
import com.zoya.sudoku.ui.theme.RegionColors
import com.zoya.sudoku.ui.theme.contrastingDigitColor

@Composable
fun GameScreen(viewModel: GameViewModel, onHome: () -> Unit) {
    val state by viewModel.uiState.collectAsState()

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            ScreenHeader("Игра", onHome)
            Spacer(Modifier.height(12.dp))

            when (val s = state) {
                // Blank themed background only while the puzzle loads - no spinner, per the
                // no-progress-indicators requirement; the read is near-instant in practice.
                GameUiState.Loading -> {}
                is GameUiState.Loaded -> GameContent(
                    state = s,
                    onSelect = viewModel::selectCell,
                    onDigit = viewModel::inputDigit,
                    onErase = viewModel::erase,
                    onCheckErrors = viewModel::toggleCheckErrors,
                    onFinish = { viewModel.finish(onHome) }
                )
            }
        }
    }
}

@Composable
private fun GameContent(
    state: GameUiState.Loaded,
    onSelect: (Int) -> Unit,
    onDigit: (Int) -> Unit,
    onErase: () -> Unit,
    onCheckErrors: () -> Unit,
    onFinish: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        SudokuGridView(
            cellRegion = { cell -> state.cellRegion[cell] },
            selectedCell = state.selectedCell,
            onCellTap = onSelect,
            modifier = Modifier.fillMaxWidth()
        ) { cell ->
            val digit = state.board[cell]
            if (digit != 0) {
                val background = RegionColors[state.cellRegion[cell]]
                val isWrong = state.showErrors && digit != state.solution[cell]
                Text(
                    text = digit.toString(),
                    color = if (isWrong) ErrorDigitColor else contrastingDigitColor(background),
                    fontWeight = if (state.givens[cell] != 0) FontWeight.Bold else FontWeight.Normal,
                    style = MaterialTheme.typography.headlineSmall
                )
            }
        }

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

        DigitPad(onDigit = onDigit, onErase = onErase, modifier = Modifier.fillMaxWidth())
    }
}
