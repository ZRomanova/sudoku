package com.zoya.sudoku.ui.constructor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoya.sudoku.data.repository.RegionLayoutRepository
import com.zoya.sudoku.engine.RegionLayout
import com.zoya.sudoku.engine.isSolvable
import com.zoya.sudoku.ui.components.ERASER
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ConstructorViewModel(private val repository: RegionLayoutRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(ConstructorUiState())
    val uiState: StateFlow<ConstructorUiState> = _uiState

    init {
        viewModelScope.launch {
            val nextNumber = repository.count() + 1
            _uiState.update { state -> if (state.name.isBlank()) state.copy(name = "Раскладка #$nextNumber") else state }
        }
    }

    fun selectTool(tool: Int) {
        _uiState.update { it.copy(activeTool = tool, errorMessage = null) }
    }

    fun setName(name: String) {
        _uiState.update { it.copy(name = name) }
    }

    fun tapCell(cell: Int) {
        _uiState.update { state ->
            val tool = state.activeTool
            val current = state.cellColors[cell]
            if (tool == ERASER) {
                if (current == UNCOLORED) return@update state
                state.copy(cellColors = state.cellColors.toMutableList().also { it[cell] = UNCOLORED })
            } else {
                if (current == tool) return@update state
                if (state.counts[tool] >= 9) return@update state // tool is disabled in the UI; defensive no-op
                val newColors = state.cellColors.toMutableList().also { it[cell] = tool }
                val newCounts = IntArray(9).also { arr -> newColors.forEach { c -> if (c in 0..8) arr[c]++ } }
                // The just-used color becomes unusable the instant it hits 9 - hand the active
                // tool straight to the next available color so painting stays uninterrupted.
                val newTool = if (newCounts[tool] >= 9) {
                    (0..8).firstOrNull { newCounts[it] < 9 } ?: ERASER
                } else {
                    tool
                }
                state.copy(cellColors = newColors, activeTool = newTool)
            }
        }
    }

    fun generateAndSave(onSaved: () -> Unit) {
        val state = _uiState.value
        if (!state.canSave) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            val layout = RegionLayout(state.cellColors.toIntArray())
            val valid = withContext(Dispatchers.Default) { layout.isSolvable() }
            if (!valid) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = "На такой раскраске нельзя построить корректное судоку. Измените форму регионов и попробуйте снова."
                    )
                }
                return@launch
            }
            repository.save(layout, state.name.trim())
            _uiState.update { ConstructorUiState() } // reset for next time
            onSaved()
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
