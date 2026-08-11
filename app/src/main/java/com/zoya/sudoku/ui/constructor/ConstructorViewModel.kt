package com.zoya.sudoku.ui.constructor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoya.sudoku.data.repository.RegionLayoutRepository
import com.zoya.sudoku.engine.RegionLayout
import com.zoya.sudoku.engine.isSolvable
import com.zoya.sudoku.engine.wouldRemainFeasible
import com.zoya.sudoku.ui.capitalizeFirst
import com.zoya.sudoku.ui.components.ERASER
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ConstructorViewModel(private val repository: RegionLayoutRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(ConstructorUiState())
    val uiState: StateFlow<ConstructorUiState> = _uiState

    private var validationJob: Job? = null

    init {
        viewModelScope.launch {
            val nextNumber = repository.count() + 1
            _uiState.update { state -> if (state.name.isBlank()) state.copy(name = "Раскладка #$nextNumber") else state }
        }
    }

    fun selectTool(tool: Int) {
        _uiState.update { it.copy(activeTool = tool, blockedCells = emptySet(), errorMessage = null) }
        scheduleValidation()
    }

    fun setName(name: String) {
        _uiState.update { it.copy(name = name.capitalizeFirst()) }
    }

    fun tapCell(cell: Int) {
        _uiState.update { state ->
            val tool = state.activeTool
            if (tool == NO_TOOL) return@update state // deselected after filling a color; pick a new tool first
            val current = state.cellColors[cell]
            if (tool == ERASER) {
                if (current == UNCOLORED) return@update state
                state.copy(
                    cellColors = state.cellColors.toMutableList().also { it[cell] = UNCOLORED },
                    blockedCells = emptySet()
                )
            } else {
                if (current == tool) return@update state
                if (state.counts[tool] >= 9) return@update state // tool is disabled in the UI; defensive no-op
                if (cell in state.blockedCells) return@update state // proven dead end for this color; no-op
                val newColors = state.cellColors.toMutableList().also { it[cell] = tool }
                val newCounts = IntArray(9).also { arr -> newColors.forEach { c -> if (c in 0..8) arr[c]++ } }
                // The color never switches itself: once it hits 9/9 the tool is deselected
                // entirely, so further dragging has no effect until the user explicitly taps
                // another color or the eraser.
                val newTool = if (newCounts[tool] >= 9) NO_TOOL else tool
                state.copy(cellColors = newColors, activeTool = newTool, blockedCells = emptySet())
            }
        }
        scheduleValidation()
    }

    /**
     * Recomputes which uncolored cells would be a proven dead end for the active color, so the
     * grid can grey them out before the user wastes a paint on them. Runs off the main thread
     * since it's up to 81 solver calls; cancels any still-running pass so a fast drag doesn't
     * pile up stale work fighting over the final state.
     */
    private fun scheduleValidation() {
        validationJob?.cancel()
        validationJob = viewModelScope.launch(Dispatchers.Default) {
            val state = _uiState.value
            val tool = state.activeTool
            if (tool == ERASER || tool == NO_TOOL) return@launch
            val colors = state.cellColors
            val blocked = mutableSetOf<Int>()
            for (cell in colors.indices) {
                if (!isActive) return@launch
                if (colors[cell] != UNCOLORED) continue
                if (!wouldRemainFeasible(colors, cell, tool)) blocked.add(cell)
            }
            if (isActive) _uiState.update { it.copy(blockedCells = blocked) }
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
