package com.zoya.sudoku.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** What happens to pencil marks when the player types a cell's final value. */
enum class NoteClearMode {
    /** Only this cell's own notes are discarded; every other cell's notes are left alone. */
    CLEAR_CELL,
    /** This cell's own notes are discarded, and the entered digit is also stripped as a
     *  candidate from every peer cell (same row/column/region) - like a paper solver's helper. */
    CLEAR_PEERS,
    /** Nothing is discarded - the cell's notes stay stored, just hidden while the value shows,
     *  and reappear if the value is erased. */
    KEEP
}

private val NOTE_CLEAR_MODE_KEY = stringPreferencesKey("note_clear_mode")

class SettingsRepository(private val dataStore: DataStore<Preferences>) {

    val noteClearMode: Flow<NoteClearMode> = dataStore.data.map { prefs ->
        prefs[NOTE_CLEAR_MODE_KEY]?.let { saved ->
            runCatching { NoteClearMode.valueOf(saved) }.getOrNull()
        } ?: NoteClearMode.KEEP
    }

    suspend fun setNoteClearMode(mode: NoteClearMode) {
        dataStore.edit { prefs -> prefs[NOTE_CLEAR_MODE_KEY] = mode.name }
    }
}
