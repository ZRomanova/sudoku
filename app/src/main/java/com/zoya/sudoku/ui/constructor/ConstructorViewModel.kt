package com.zoya.sudoku.ui.constructor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoya.sudoku.data.repository.RegionLayoutRepository
import com.zoya.sudoku.engine.BOARD_SIZE
import com.zoya.sudoku.engine.RegionLayout
import com.zoya.sudoku.engine.SudokuSolver
import com.zoya.sudoku.engine.Units
import com.zoya.sudoku.engine.regenerateWitness
import com.zoya.sudoku.ui.capitalizeFirst
import com.zoya.sudoku.ui.components.ERASER
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

private const val REJECT_MESSAGE =
    "Эта клетка не подходит выбранному цвету — раскраску нельзя будет завершить корректным судоку."

class ConstructorViewModel(private val repository: RegionLayoutRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(ConstructorUiState())
    val uiState: StateFlow<ConstructorUiState> = _uiState

    private var regenerateJob: Job? = null

    init {
        seedWitness()
        seedName()
    }

    private fun seedName() {
        viewModelScope.launch {
            val nextNumber = repository.count() + 1
            _uiState.update { state -> if (state.name.isBlank()) state.copy(name = "Раскладка #$nextNumber") else state }
        }
    }

    /**
     * (Re)seeds the cached witness with a fresh random full classic-box solution - region shape is
     * irrelevant here, only the row/column Latin constraints matter before anything is painted.
     * Runs off the main thread like every other solver call in this screen.
     */
    private fun seedWitness() {
        viewModelScope.launch(Dispatchers.Default) {
            val seed = SudokuSolver(Units(RegionLayout.classicBoxes())).solve(IntArray(BOARD_SIZE), randomize = true)
            _uiState.update { it.copy(witness = seed?.toList()) }
        }
    }

    fun selectTool(tool: Int) {
        regenerateJob?.cancel() // a pending conflict-resolution for the old tool is no longer relevant
        _uiState.update { it.copy(activeTool = tool, errorMessage = null) }
    }

    fun setName(name: String) {
        _uiState.update { it.copy(name = name.capitalizeFirst()) }
    }

    fun tapCell(cell: Int) {
        regenerateJob?.cancel() // any prior in-flight conflict resolution is now stale
        val state = _uiState.value
        val tool = state.activeTool
        if (tool == NO_TOOL) return // deselected after filling a color; pick a new tool first
        val witness = state.witness ?: return // not seeded yet (first-frame edge case); ignore the tap
        val current = state.cellColors[cell]

        if (tool == ERASER) {
            if (current == UNCOLORED) return
            _uiState.update { s -> s.copy(cellColors = s.cellColors.toMutableList().also { it[cell] = UNCOLORED }) }
            return
        }

        if (current == tool) return
        if (state.counts[tool] >= 9) return // tool is disabled in the UI; defensive no-op

        if (cell !in state.hintedCells) {
            // Fast path: the cached witness already proves this paint keeps a valid solution
            // reachable - commit instantly, no solver call.
            _uiState.value = applyPaint(state, cell, tool, witness)
            return
        }

        // Rare path: the cached witness conflicts here. Try to find a DIFFERENT witness that
        // makes this exact paint work before giving up. Off the main thread so a fast drag never
        // stalls; guarded against staleness so a late result never lands on a board the user has
        // since moved on from (erased, repainted elsewhere, or switched tools away from).
        val snapshotColors = state.cellColors
        regenerateJob = viewModelScope.launch(Dispatchers.Default) {
            val newWitness = regenerateWitness(snapshotColors, cell, tool)
            if (!isActive) return@launch
            _uiState.update { latest ->
                if (latest.cellColors != snapshotColors || latest.activeTool != tool) {
                    latest // board moved on while solving; silently drop this stale result
                } else if (newWitness == null) {
                    latest.copy(errorMessage = REJECT_MESSAGE)
                } else {
                    applyPaint(latest, cell, tool, newWitness.toList())
                }
            }
        }
    }

    private fun applyPaint(state: ConstructorUiState, cell: Int, tool: Int, witness: List<Int>): ConstructorUiState {
        val newColors = state.cellColors.toMutableList().also { it[cell] = tool }
        val newCounts = IntArray(9).also { arr -> newColors.forEach { c -> if (c in 0..8) arr[c]++ } }
        // The color never switches itself: once it hits 9/9 the tool is deselected entirely, so
        // further dragging has no effect until the user explicitly taps another color or the
        // eraser.
        val newTool = if (newCounts[tool] >= 9) NO_TOOL else tool
        return state.copy(cellColors = newColors, witness = witness, activeTool = newTool, errorMessage = null)
    }

    fun generateAndSave(onSaved: () -> Unit) {
        val state = _uiState.value
        if (!state.canSave) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            // No expensive solvability search needed here: every committed paint (see tapCell /
            // applyPaint above) was only ever accepted once it was backed by a witness kept
            // consistent with the ENTIRE board, so by construction this layout is always solvable
            // - state.witness itself already is a valid solution for it.
            val layout = RegionLayout(state.cellColors.toIntArray())
            repository.save(layout, state.name.trim())
            _uiState.value = ConstructorUiState()
            seedWitness()
            seedName()
            onSaved()
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
