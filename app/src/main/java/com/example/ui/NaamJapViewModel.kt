package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.NaamJapDayEntity
import com.example.data.NaamJapGoalEntity
import com.example.data.NaamJapRepository
import com.example.data.todayDateKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

enum class NaamJapOverlayMode {
    NONE,
    FULLSCREEN_FOCUS,
    RADHA_GAME,
    NAAM_SHAKTI
}

data class NaamJapUiState(
    val allTimeCount: Int = 0,
    val todayCount: Int = 0,
    val goal: NaamJapGoalEntity = NaamJapGoalEntity(),
    val history: List<NaamJapDayEntity> = emptyList(),
    val overlayMode: NaamJapOverlayMode = NaamJapOverlayMode.NONE,
    val showGoalSheet: Boolean = false,
    val snackbarMessage: String? = null
) {
    val allTimeMalas: Int
        get() = allTimeCount / 108

    val todayMalas: Int
        get() = todayCount / 108

    val currentMalaBead: Int
        get() = todayCount % 108

    val activeDaysCount: Int
        get() = history.count { it.count > 0 }

    val goalProgressFraction: Float
        get() = (todayCount.toFloat() / goal.dailyGoal.coerceAtLeast(1)).coerceIn(0f, 1f)

    val goalProgressPercent: Int
        get() = (goalProgressFraction * 100f).roundToInt()

    val isTodayGoalMet: Boolean
        get() = todayCount >= goal.dailyGoal && goal.dailyGoal > 0
}

class NaamJapViewModel(
    private val repository: NaamJapRepository
) : ViewModel() {

    private val overlayModeFlow = MutableStateFlow(NaamJapOverlayMode.NONE)
    private val showGoalSheetFlow = MutableStateFlow(false)
    private val snackbarFlow = MutableStateFlow<String?>(null)

    private data class ModeFlags(
        val overlayMode: NaamJapOverlayMode,
        val showGoalSheet: Boolean,
        val snackbarMessage: String?
    )

    private val flagsFlow = combine(
        overlayModeFlow,
        showGoalSheetFlow,
        snackbarFlow
    ) { mode, goalSheet, snackbar ->
        ModeFlags(mode, goalSheet, snackbar)
    }

    val uiState: StateFlow<NaamJapUiState> = combine(
        repository.allTimeTotal,
        repository.allHistory,
        repository.goal,
        flagsFlow
    ) { total, history, goal, flags ->
        val todayKey = todayDateKey()
        val todayCount = history.firstOrNull { it.dateKey == todayKey }?.count ?: 0
        NaamJapUiState(
            allTimeCount = total,
            todayCount = todayCount,
            goal = goal,
            history = history,
            overlayMode = flags.overlayMode,
            showGoalSheet = flags.showGoalSheet,
            snackbarMessage = flags.snackbarMessage
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = NaamJapUiState()
    )

    init {
        viewModelScope.launch {
            repository.ensureGoalInitialized()
        }
    }

    fun incrementCount() {
        viewModelScope.launch {
            repository.incrementToday(delta = 1)
        }
    }

    fun decrementCount() {
        viewModelScope.launch {
            repository.incrementToday(delta = -1)
        }
    }

    fun openFullScreenFocusMode() {
        overlayModeFlow.value = NaamJapOverlayMode.FULLSCREEN_FOCUS
    }

    fun openRadhaGameMode() {
        overlayModeFlow.value = NaamJapOverlayMode.RADHA_GAME
    }

    fun openNaamShaktiMode() {
        overlayModeFlow.value = NaamJapOverlayMode.NAAM_SHAKTI
    }

    fun exitOverlayMode() {
        overlayModeFlow.value = NaamJapOverlayMode.NONE
    }

    fun openGoalSheet() {
        showGoalSheetFlow.value = true
    }

    fun dismissGoalSheet() {
        showGoalSheetFlow.value = false
    }

    fun saveGoal(dailyGoal: Int, hapticEnabled: Boolean) {
        viewModelScope.launch {
            repository.saveGoal(dailyGoal, hapticEnabled)
            showGoalSheetFlow.value = false
            snackbarFlow.value = "Daily Naam Jap goal set to $dailyGoal (${dailyGoal / 108} Malas)"
        }
    }

    fun clearSnackbar() {
        snackbarFlow.value = null
    }

    class Factory(private val repository: NaamJapRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return NaamJapViewModel(repository) as T
        }
    }
}
