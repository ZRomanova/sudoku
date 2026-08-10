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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onConstructor: () -> Unit,
    onLibrary: () -> Unit,
    onContinue: () -> Unit
) {
    val hasActivePuzzle by viewModel.hasActivePuzzle.collectAsState()

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

            Button(onClick = onConstructor, modifier = Modifier.fillMaxWidth()) {
                Text("Конструктор регионов")
            }
            Spacer(Modifier.height(16.dp))
            Button(onClick = onLibrary, modifier = Modifier.fillMaxWidth()) {
                Text("Мои раскраски")
            }
        }
    }
}
