package com.zoya.sudoku.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoya.sudoku.data.repository.NoteClearMode
import com.zoya.sudoku.data.repository.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(private val settingsRepository: SettingsRepository) : ViewModel() {

    val noteClearMode: StateFlow<NoteClearMode> = settingsRepository.noteClearMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), NoteClearMode.KEEP)

    fun setNoteClearMode(mode: NoteClearMode) {
        viewModelScope.launch { settingsRepository.setNoteClearMode(mode) }
    }
}
