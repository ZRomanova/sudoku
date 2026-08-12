package com.zoya.sudoku.ui.constructor

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.zoya.sudoku.ui.components.ColorPalette
import com.zoya.sudoku.ui.components.ScreenHeader
import com.zoya.sudoku.ui.components.SudokuGridView

@Composable
fun ConstructorScreen(viewModel: ConstructorViewModel, onSaved: () -> Unit, onHome: () -> Unit, onTips: () -> Unit) {
    val state by viewModel.uiState.collectAsState()

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            ScreenHeader("Конструктор", onHome) {
                IconButton(onClick = onTips) {
                    Text("?", style = MaterialTheme.typography.titleLarge)
                }
            }
            Spacer(Modifier.height(12.dp))

            SudokuGridView(
                cellRegion = { cell -> state.cellColors[cell] },
                onCellTap = viewModel::tapCell,
                dragPaint = true,
                blockedCells = state.blockedCells,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))
            ColorPalette(
                counts = state.counts,
                activeTool = state.activeTool,
                onSelect = viewModel::selectTool
            )

            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = state.name,
                onValueChange = viewModel::setName,
                label = { Text("Название раскладки") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))
            Button(
                onClick = { viewModel.generateAndSave(onSaved) },
                enabled = state.canSave,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (state.isSaving) "Проверка..." else "Проверить и сохранить")
            }
        }
    }

    if (state.errorMessage != null) {
        AlertDialog(
            onDismissRequest = viewModel::dismissError,
            confirmButton = {
                Button(onClick = viewModel::dismissError) { Text("Понятно") }
            },
            title = { Text("Раскраска не подходит") },
            text = { Text(state.errorMessage!!) }
        )
    }
}
