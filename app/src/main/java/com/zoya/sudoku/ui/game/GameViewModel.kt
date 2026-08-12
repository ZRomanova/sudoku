package com.zoya.sudoku.ui.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoya.sudoku.data.decodeDigits
import com.zoya.sudoku.data.decodeNoteMasks
import com.zoya.sudoku.data.repository.NoteClearMode
import com.zoya.sudoku.data.repository.PuzzleRepository
import com.zoya.sudoku.data.repository.RegionLayoutRepository
import com.zoya.sudoku.data.repository.SettingsRepository
import com.zoya.sudoku.data.repository.StatsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GameViewModel(
    private val puzzleId: Long,
    private val puzzleRepository: PuzzleRepository,
    layoutRepository: RegionLayoutRepository,
    private val statsRepository: StatsRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _selectedCell = MutableStateFlow<Int?>(null)
    private val _showErrors = MutableStateFlow(false)
    private val _notesMode = MutableStateFlow(false)

    val uiState: StateFlow<GameUiState> = combine(
        puzzleRepository.observe(puzzleId),
        layoutRepository.getAll(),
        _selectedCell,
        _showErrors,
        _notesMode
    ) { entity, layouts, selected, showErrors, notesMode ->
        val savedLayout = entity?.let { e -> layouts.find { it.id == e.layoutId } }
        if (entity == null || savedLayout == null) {
            GameUiState.Loading
        } else {
            val layout = savedLayout.layout
            GameUiState.Loaded(
                layoutId = entity.layoutId,
                layoutName = savedLayout.name,
                difficulty = entity.difficulty,
                cellRegion = layout.cellRegion,
                board = entity.board.decodeDigits(),
                solution = entity.solution.decodeDigits(),
                givens = entity.givens.decodeDigits(),
                notes = entity.notes.decodeNoteMasks(),
                notesMode = notesMode,
                selectedCell = selected,
                showErrors = showErrors
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), GameUiState.Loading)

    fun selectCell(cell: Int) {
        _selectedCell.value = cell
    }

    fun toggleNotesMode() {
        _notesMode.value = !_notesMode.value
    }

    fun inputDigit(digit: Int) {
        val cell = _selectedCell.value ?: return
        viewModelScope.launch {
            if (_notesMode.value) {
                puzzleRepository.toggleNote(puzzleId, cell, digit)
            } else {
                val clearMode = settingsRepository.noteClearMode.first()
                puzzleRepository.applyMove(puzzleId, cell, digit, clearMode)
            }
        }
    }

    fun erase() {
        val cell = _selectedCell.value ?: return
        viewModelScope.launch {
            if (_notesMode.value) {
                puzzleRepository.clearNotes(puzzleId, cell)
            } else {
                puzzleRepository.eraseMove(puzzleId, cell)
            }
        }
    }

    fun toggleCheckErrors() {
        _showErrors.value = !_showErrors.value
    }

    /** Wipes all entered digits and notes, leaving only the given clues. Confirmed via alert in
     *  GameScreen before this is called, since it can't be undone. */
    fun resetProgress() {
        viewModelScope.launch {
            puzzleRepository.resetProgress(puzzleId)
        }
    }

    /** Only reachable from a full board (see GameScreen), so this is always a real finish - never
     *  called for a puzzle the player merely navigated away from. */
    fun finish(onFinished: () -> Unit) {
        val current = uiState.value as? GameUiState.Loaded ?: return
        viewModelScope.launch {
            statsRepository.recordResult(current.layoutId, current.difficulty, current.isCorrect)
            puzzleRepository.finishCurrent(puzzleId)
            onFinished()
        }
    }
}
