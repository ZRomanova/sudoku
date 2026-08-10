package com.zoya.sudoku.ui.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoya.sudoku.data.decodeDigits
import com.zoya.sudoku.data.repository.PuzzleRepository
import com.zoya.sudoku.data.repository.RegionLayoutRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GameViewModel(
    private val puzzleRepository: PuzzleRepository,
    layoutRepository: RegionLayoutRepository
) : ViewModel() {

    private val _selectedCell = MutableStateFlow<Int?>(null)
    private val _showErrors = MutableStateFlow(false)

    val uiState: StateFlow<GameUiState> = combine(
        puzzleRepository.observeCurrent(),
        layoutRepository.getAll(),
        _selectedCell,
        _showErrors
    ) { entity, layouts, selected, showErrors ->
        val layout = entity?.let { e -> layouts.find { it.id == e.layoutId }?.layout }
        if (entity == null || layout == null) {
            GameUiState.Loading
        } else {
            GameUiState.Loaded(
                cellRegion = layout.cellRegion,
                board = entity.board.decodeDigits(),
                solution = entity.solution.decodeDigits(),
                givens = entity.givens.decodeDigits(),
                selectedCell = selected,
                showErrors = showErrors
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), GameUiState.Loading)

    fun selectCell(cell: Int) {
        _selectedCell.value = cell
    }

    fun inputDigit(digit: Int) {
        val cell = _selectedCell.value ?: return
        viewModelScope.launch { puzzleRepository.applyMove(cell, digit) }
    }

    fun erase() {
        val cell = _selectedCell.value ?: return
        viewModelScope.launch { puzzleRepository.eraseMove(cell) }
    }

    fun toggleCheckErrors() {
        _showErrors.value = !_showErrors.value
    }

    fun finish(onFinished: () -> Unit) {
        viewModelScope.launch {
            puzzleRepository.finishCurrent()
            onFinished()
        }
    }
}
