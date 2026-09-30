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

    /** Frozen final board once solved correctly - the DB row is deleted at that point, so the
     *  "Решено верно" view can't come from the live puzzle flow anymore. */
    private val _solvedSnapshot = MutableStateFlow<GameUiState.Loaded?>(null)

    /** Non-null while the "Есть ошибки" dialog is up: how many cells are wrong. */
    private val _mistakeCount = MutableStateFlow<Int?>(null)
    val mistakeCount: StateFlow<Int?> = _mistakeCount

    private var finishing = false

    private val liveState = combine(
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
                showErrors = showErrors,
                attemptFailed = entity.attemptFailed
            )
        }
    }

    val uiState: StateFlow<GameUiState> = combine(liveState, _solvedSnapshot) { live, solved -> solved ?: live }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), GameUiState.Loading)

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

    /**
     * Only reachable from a full board (see GameScreen). A correct board ends the game with a
     * win (unless a wrong finish already logged a loss for it) and shows the solved view. A wrong
     * board logs a loss right away - once per game - then highlights the mistakes and lets the
     * player either fix them or leave.
     */
    fun finish() {
        val current = uiState.value as? GameUiState.Loaded ?: return
        if (finishing || current.isSolved) return
        finishing = true
        viewModelScope.launch {
            try {
                if (current.isCorrect) {
                    _solvedSnapshot.value = current.copy(isSolved = true, selectedCell = null, showErrors = false)
                    if (!current.attemptFailed) {
                        statsRepository.recordResult(current.layoutId, current.difficulty, correct = true)
                    }
                    puzzleRepository.finishCurrent(puzzleId)
                } else {
                    if (!current.attemptFailed) {
                        statsRepository.recordResult(current.layoutId, current.difficulty, correct = false)
                        puzzleRepository.markAttemptFailed(puzzleId)
                    }
                    _showErrors.value = true
                    _mistakeCount.value = current.wrongCells.size
                }
            } finally {
                finishing = false
            }
        }
    }

    /** "Исправить": close the dialog, keep playing with the mistakes highlighted. */
    fun keepFixing() {
        _mistakeCount.value = null
    }

    /** "Выйти" after a wrong finish: the loss is already on record, so the game just ends. */
    fun giveUp(onDone: () -> Unit) {
        _mistakeCount.value = null
        viewModelScope.launch {
            puzzleRepository.finishCurrent(puzzleId)
            onDone()
        }
    }
}
