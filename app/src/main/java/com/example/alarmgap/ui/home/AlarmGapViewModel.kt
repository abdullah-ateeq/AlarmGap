package com.example.alarmgap.ui.home

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.alarmgap.calculator.AlarmGapCalculator
import com.example.alarmgap.calculator.IdenticalTimesException
import com.example.alarmgap.data.model.AlarmGapHistory
import com.example.alarmgap.data.model.AlarmGapUiState
import com.example.alarmgap.data.model.CalculationMode
import com.example.alarmgap.data.model.DayRelationship
import com.example.alarmgap.data.model.ThemePreference
import com.example.alarmgap.data.model.TimeFormatPreference
import com.example.alarmgap.data.repository.AlarmGapPreferencesRepository
import com.example.alarmgap.formatter.TimeGapFormatter
import com.example.alarmgap.history.HistoryManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class AlarmGapViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AlarmGapPreferencesRepository(application)
    private val historyManager = HistoryManager(repository)
    private val calculator = AlarmGapCalculator()

    private val _uiState = MutableStateFlow(AlarmGapUiState())
    val uiState: StateFlow<AlarmGapUiState> = _uiState.asStateFlow()

    init {
        loadInitialState()
    }

    private fun loadInitialState() {
        val restoreConfig = repository.restoreLastConfiguration
        val startTime = if (restoreConfig) repository.lastStartTime else LocalTime.of(8, 0, 0)
        val endTime = if (restoreConfig) repository.lastEndTime else LocalTime.of(17, 0, 0)
        val startDate = if (restoreConfig) repository.lastStartDate else LocalDate.now()
        val calcMode = if (restoreConfig) repository.calculationMode else CalculationMode.TIME_DIFFERENCE
        val dayRel = if (restoreConfig) repository.dayRelationship else DayRelationship.AUTOMATIC

        _uiState.update { current ->
            current.copy(
                startTime = startTime,
                endTime = endTime,
                startDate = startDate,
                calculationMode = calcMode,
                dayRelationship = dayRel,
                timeFormat = repository.timeFormat,
                showSeconds = repository.showSeconds,
                liveCalculation = repository.liveCalculation,
                showDateContext = repository.showDateContext,
                showDurationBreakdown = repository.showDurationBreakdown,
                showDigitalDuration = repository.showDigitalDuration,
                showTotalMinutes = repository.showTotalMinutes,
                showTotalSeconds = repository.showTotalSeconds,
                showDecimalHours = repository.showDecimalHours,
                hapticFeedback = repository.hapticFeedback,
                animations = repository.animations,
                confirmDelete = repository.confirmDelete,
                restoreLastConfig = repository.restoreLastConfiguration,
                themePreference = repository.theme,
                dynamicColor = repository.dynamicColor,
                history = historyManager.getHistory()
            )
        }

        recalculate()
    }

    fun onStartTimeChanged(time: LocalTime) {
        val updatedTime = if (_uiState.value.showSeconds) time else time.withSecond(0).withNano(0)
        _uiState.update { it.copy(startTime = updatedTime) }
        repository.lastStartTime = updatedTime
        if (_uiState.value.liveCalculation) {
            recalculate()
        }
    }

    fun onEndTimeChanged(time: LocalTime) {
        val updatedTime = if (_uiState.value.showSeconds) time else time.withSecond(0).withNano(0)
        _uiState.update { it.copy(endTime = updatedTime) }
        repository.lastEndTime = updatedTime
        if (_uiState.value.liveCalculation) {
            recalculate()
        }
    }

    fun onStartSecondsChanged(seconds: Int) {
        val validSeconds = seconds.coerceIn(0, 59)
        val updatedTime = _uiState.value.startTime.withSecond(validSeconds)
        _uiState.update { it.copy(startTime = updatedTime) }
        repository.lastStartTime = updatedTime
        if (_uiState.value.liveCalculation) {
            recalculate()
        }
    }

    fun onEndSecondsChanged(seconds: Int) {
        val validSeconds = seconds.coerceIn(0, 59)
        val updatedTime = _uiState.value.endTime.withSecond(validSeconds)
        _uiState.update { it.copy(endTime = updatedTime) }
        repository.lastEndTime = updatedTime
        if (_uiState.value.liveCalculation) {
            recalculate()
        }
    }

    fun onStartDateChanged(date: LocalDate) {
        _uiState.update { it.copy(startDate = date) }
        repository.lastStartDate = date
        if (_uiState.value.liveCalculation) {
            recalculate()
        }
    }

    fun onCalculationModeChanged(mode: CalculationMode) {
        _uiState.update { it.copy(calculationMode = mode) }
        repository.calculationMode = mode
        if (_uiState.value.liveCalculation) {
            recalculate()
        }
    }

    fun onDayRelationshipChanged(relationship: DayRelationship) {
        _uiState.update { it.copy(dayRelationship = relationship) }
        repository.dayRelationship = relationship
        if (_uiState.value.liveCalculation) {
            recalculate()
        }
    }

    fun onInputDurationChanged(hours: Long, minutes: Int, seconds: Int) {
        _uiState.update {
            it.copy(
                inputDurationHours = hours.coerceAtLeast(0),
                inputDurationMinutes = minutes.coerceIn(0, 59),
                inputDurationSeconds = seconds.coerceIn(0, 59)
            )
        }
        if (_uiState.value.liveCalculation) {
            recalculate()
        }
    }

    fun swapTimes() {
        val oldStart = _uiState.value.startTime
        val oldEnd = _uiState.value.endTime
        _uiState.update { it.copy(startTime = oldEnd, endTime = oldStart) }
        repository.lastStartTime = oldEnd
        repository.lastEndTime = oldStart
        if (_uiState.value.liveCalculation) {
            recalculate()
        }
    }

    /**
     * Applies a quick preset: End Time = Start Time + preset duration
     */
    fun applyPresetDuration(duration: Duration) {
        val startDateTime = LocalDateTime.of(_uiState.value.startDate, _uiState.value.startTime)
        val calculatedEndDateTime = startDateTime.plus(duration)
        val newEndTime = calculatedEndDateTime.toLocalTime()
        _uiState.update { it.copy(endTime = newEndTime) }
        repository.lastEndTime = newEndTime
        if (_uiState.value.liveCalculation) {
            recalculate()
        }
    }

    /**
     * Applies common schedule presets (Workday, Half day, Overnight, Full day)
     */
    fun applyCommonPreset(start: LocalTime, end: LocalTime, dayRel: DayRelationship) {
        _uiState.update {
            it.copy(
                startTime = start,
                endTime = end,
                dayRelationship = dayRel,
                calculationMode = CalculationMode.TIME_DIFFERENCE
            )
        }
        repository.lastStartTime = start
        repository.lastEndTime = end
        repository.dayRelationship = dayRel
        recalculate()
    }

    /**
     * Reverse calculation: Takes current duration and Start Time to enter Duration -> End Time mode
     */
    fun triggerReverseCalculation() {
        val currentDuration = _uiState.value.result ?: return
        val hours = currentDuration.toHours()
        val minutes = (currentDuration.toMinutes() % 60).toInt()
        val seconds = (currentDuration.seconds % 60).toInt()

        _uiState.update {
            it.copy(
                calculationMode = CalculationMode.DURATION_TO_END,
                inputDurationHours = hours,
                inputDurationMinutes = minutes,
                inputDurationSeconds = seconds
            )
        }
        recalculate()
    }

    fun recalculate() {
        val state = _uiState.value
        val startDate = state.startDate

        when (state.calculationMode) {
            CalculationMode.TIME_DIFFERENCE -> {
                try {
                    val calcResult = calculator.calculate(
                        startDate = startDate,
                        startTime = state.startTime,
                        endTime = state.endTime,
                        relationship = state.dayRelationship
                    )

                    val duration = calcResult.duration
                    val totalMinutes = calculator.calculateTotalMinutes(duration)
                    val totalSeconds = calculator.calculateTotalSeconds(duration)
                    val decimalHours = calculator.calculateDecimalHours(duration)

                    _uiState.update {
                        it.copy(
                            result = duration,
                            resolvedStartDateTime = calcResult.startDateTime,
                            resolvedEndDateTime = calcResult.endDateTime,
                            durationHours = duration.toHours().toInt(),
                            durationMinutes = (duration.toMinutes() % 60).toInt(),
                            durationSeconds = (duration.seconds % 60).toInt(),
                            totalMinutes = totalMinutes,
                            totalSeconds = totalSeconds,
                            decimalHours = decimalHours,
                            errorMessage = null,
                            isAmbiguousSameTime = false
                        )
                    }
                } catch (e: IdenticalTimesException) {
                    _uiState.update {
                        it.copy(
                            result = null,
                            errorMessage = e.message,
                            isAmbiguousSameTime = true
                        )
                    }
                } catch (e: Exception) {
                    _uiState.update {
                        it.copy(
                            result = null,
                            errorMessage = e.message ?: "Calculation error",
                            isAmbiguousSameTime = false
                        )
                    }
                }
            }

            CalculationMode.DURATION_TO_END -> {
                try {
                    val duration = Duration.ofHours(state.inputDurationHours)
                        .plusMinutes(state.inputDurationMinutes.toLong())
                        .plusSeconds(state.inputDurationSeconds.toLong())

                    val startDateTime = LocalDateTime.of(startDate, state.startTime)
                    val endDateTime = calculator.calculateEndTime(startDateTime, duration)

                    val totalMinutes = calculator.calculateTotalMinutes(duration)
                    val totalSeconds = calculator.calculateTotalSeconds(duration)
                    val decimalHours = calculator.calculateDecimalHours(duration)

                    _uiState.update {
                        it.copy(
                            endTime = endDateTime.toLocalTime(),
                            result = duration,
                            resolvedStartDateTime = startDateTime,
                            resolvedEndDateTime = endDateTime,
                            durationHours = duration.toHours().toInt(),
                            durationMinutes = (duration.toMinutes() % 60).toInt(),
                            durationSeconds = (duration.seconds % 60).toInt(),
                            totalMinutes = totalMinutes,
                            totalSeconds = totalSeconds,
                            decimalHours = decimalHours,
                            errorMessage = null,
                            isAmbiguousSameTime = false
                        )
                    }
                } catch (e: Exception) {
                    _uiState.update {
                        it.copy(
                            result = null,
                            errorMessage = e.message ?: "Invalid duration input",
                            isAmbiguousSameTime = false
                        )
                    }
                }
            }

            CalculationMode.DURATION_TO_START -> {
                try {
                    val duration = Duration.ofHours(state.inputDurationHours)
                        .plusMinutes(state.inputDurationMinutes.toLong())
                        .plusSeconds(state.inputDurationSeconds.toLong())

                    val endDateTime = LocalDateTime.of(startDate, state.endTime)
                    val startDateTime = calculator.calculateStartTime(endDateTime, duration)

                    val totalMinutes = calculator.calculateTotalMinutes(duration)
                    val totalSeconds = calculator.calculateTotalSeconds(duration)
                    val decimalHours = calculator.calculateDecimalHours(duration)

                    _uiState.update {
                        it.copy(
                            startTime = startDateTime.toLocalTime(),
                            result = duration,
                            resolvedStartDateTime = startDateTime,
                            resolvedEndDateTime = endDateTime,
                            durationHours = duration.toHours().toInt(),
                            durationMinutes = (duration.toMinutes() % 60).toInt(),
                            durationSeconds = (duration.seconds % 60).toInt(),
                            totalMinutes = totalMinutes,
                            totalSeconds = totalSeconds,
                            decimalHours = decimalHours,
                            errorMessage = null,
                            isAmbiguousSameTime = false
                        )
                    }
                } catch (e: Exception) {
                    _uiState.update {
                        it.copy(
                            result = null,
                            errorMessage = e.message ?: "Invalid duration input",
                            isAmbiguousSameTime = false
                        )
                    }
                }
            }
        }
    }

    fun saveCurrentCalculation(): Boolean {
        val duration = _uiState.value.result ?: return false
        val state = _uiState.value

        val saved = historyManager.saveEntry(
            startTime = state.startTime.toString(),
            endTime = state.endTime.toString(),
            startDate = state.resolvedStartDateTime?.toLocalDate()?.toString() ?: state.startDate.toString(),
            endDate = state.resolvedEndDateTime?.toLocalDate()?.toString(),
            calculationMode = state.calculationMode.name,
            durationSeconds = duration.seconds,
            dayRelationship = state.dayRelationship.name,
            includeSeconds = state.showSeconds
        )

        _uiState.update { it.copy(history = historyManager.getHistory()) }
        if (saved) {
            showNotification("Saved to history")
        } else {
            showNotification("Calculation already saved in history")
        }
        return saved
    }

    fun restoreHistory(entry: AlarmGapHistory) {
        val restoredStartTime = runCatching { LocalTime.parse(entry.startTime) }.getOrDefault(LocalTime.of(8, 0, 0))
        val restoredEndTime = runCatching { LocalTime.parse(entry.endTime) }.getOrDefault(LocalTime.of(17, 0, 0))
        val restoredStartDate = entry.startDate?.let { runCatching { LocalDate.parse(it) }.getOrNull() } ?: LocalDate.now()
        val restoredCalcMode = runCatching { CalculationMode.valueOf(entry.calculationMode) }.getOrDefault(CalculationMode.TIME_DIFFERENCE)
        val restoredDayRel = runCatching { DayRelationship.valueOf(entry.dayRelationship) }.getOrDefault(DayRelationship.AUTOMATIC)

        _uiState.update {
            it.copy(
                startTime = restoredStartTime,
                endTime = restoredEndTime,
                startDate = restoredStartDate,
                calculationMode = restoredCalcMode,
                dayRelationship = restoredDayRel,
                showSeconds = entry.includeSeconds || it.showSeconds
            )
        }
        repository.lastStartTime = restoredStartTime
        repository.lastEndTime = restoredEndTime
        repository.lastStartDate = restoredStartDate
        repository.calculationMode = restoredCalcMode
        repository.dayRelationship = restoredDayRel

        recalculate()
        showNotification("Restored from history")
    }

    fun deleteHistoryEntry(id: String) {
        historyManager.deleteEntry(id)
        _uiState.update { it.copy(history = historyManager.getHistory()) }
        showNotification("Calculation deleted")
    }

    fun clearAllHistory() {
        historyManager.clearHistory()
        _uiState.update { it.copy(history = emptyList()) }
        showNotification("All history cleared")
    }

    fun copyResult(context: Context) {
        val duration = _uiState.value.result ?: return
        val state = _uiState.value

        val startFormatted = TimeGapFormatter.formatTime(state.startTime, state.timeFormat, state.showSeconds, context)
        val endFormatted = TimeGapFormatter.formatTime(state.endTime, state.timeFormat, state.showSeconds, context)
        val humanDuration = TimeGapFormatter.formatHumanReadable(duration, state.showSeconds)
        val digitalDuration = TimeGapFormatter.formatDigital(duration)
        val decimalStr = TimeGapFormatter.formatDecimalHours(state.decimalHours)

        val startDateFormatted = if (state.showDateContext && state.resolvedStartDateTime != null) {
            TimeGapFormatter.formatDateShort(state.resolvedStartDateTime.toLocalDate())
        } else null
        val endDateFormatted = if (state.showDateContext && state.resolvedEndDateTime != null) {
            TimeGapFormatter.formatDateShort(state.resolvedEndDateTime.toLocalDate())
        } else null

        val text = TimeGapFormatter.buildCopyResultText(
            startTimeFormatted = startFormatted,
            endTimeFormatted = endFormatted,
            humanDuration = humanDuration,
            digitalDuration = digitalDuration,
            totalMinutes = state.totalMinutes,
            totalSeconds = state.totalSeconds,
            decimalHoursStr = decimalStr,
            startDateFormatted = startDateFormatted,
            endDateFormatted = endDateFormatted
        )

        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("AlarmGap Result", text)
        clipboard.setPrimaryClip(clip)
        showNotification("Full result copied to clipboard")
    }

    fun copyDurationOnly(context: Context) {
        val duration = _uiState.value.result ?: return
        val state = _uiState.value
        val humanDuration = TimeGapFormatter.formatHumanReadable(duration, state.showSeconds)

        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("AlarmGap Duration", humanDuration)
        clipboard.setPrimaryClip(clip)
        showNotification("Duration copied: $humanDuration")
    }

    fun shareResult(context: Context) {
        val duration = _uiState.value.result ?: return
        val state = _uiState.value

        val startFormatted = TimeGapFormatter.formatTime(state.startTime, state.timeFormat, state.showSeconds, context)
        val endFormatted = TimeGapFormatter.formatTime(state.endTime, state.timeFormat, state.showSeconds, context)
        val humanDuration = TimeGapFormatter.formatHumanReadable(duration, state.showSeconds)

        val shareText = TimeGapFormatter.buildShareText(
            startTimeFormatted = startFormatted,
            endTimeFormatted = endFormatted,
            humanDuration = humanDuration,
            totalMinutes = state.totalMinutes
        )

        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Share Time Gap")
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(shareIntent)
    }

    // Setting update handlers
    fun updateTheme(theme: ThemePreference) {
        repository.theme = theme
        _uiState.update { it.copy(themePreference = theme) }
    }

    fun updateDynamicColor(enabled: Boolean) {
        repository.dynamicColor = enabled
        _uiState.update { it.copy(dynamicColor = enabled) }
    }

    fun updateTimeFormat(format: TimeFormatPreference) {
        repository.timeFormat = format
        _uiState.update { it.copy(timeFormat = format) }
    }

    fun updateShowSeconds(enabled: Boolean) {
        repository.showSeconds = enabled
        _uiState.update { it.copy(showSeconds = enabled) }
        recalculate()
    }

    fun updateLiveCalculation(enabled: Boolean) {
        repository.liveCalculation = enabled
        _uiState.update { it.copy(liveCalculation = enabled) }
        if (enabled) {
            recalculate()
        }
    }

    fun updateShowDateContext(enabled: Boolean) {
        repository.showDateContext = enabled
        _uiState.update { it.copy(showDateContext = enabled) }
    }

    fun updateShowDurationBreakdown(enabled: Boolean) {
        repository.showDurationBreakdown = enabled
        _uiState.update { it.copy(showDurationBreakdown = enabled) }
    }

    fun updateShowDigitalDuration(enabled: Boolean) {
        repository.showDigitalDuration = enabled
        _uiState.update { it.copy(showDigitalDuration = enabled) }
    }

    fun updateShowTotalMinutes(enabled: Boolean) {
        repository.showTotalMinutes = enabled
        _uiState.update { it.copy(showTotalMinutes = enabled) }
    }

    fun updateShowTotalSeconds(enabled: Boolean) {
        repository.showTotalSeconds = enabled
        _uiState.update { it.copy(showTotalSeconds = enabled) }
    }

    fun updateShowDecimalHours(enabled: Boolean) {
        repository.showDecimalHours = enabled
        _uiState.update { it.copy(showDecimalHours = enabled) }
    }

    fun updateHapticFeedback(enabled: Boolean) {
        repository.hapticFeedback = enabled
        _uiState.update { it.copy(hapticFeedback = enabled) }
    }

    fun updateAnimations(enabled: Boolean) {
        repository.animations = enabled
        _uiState.update { it.copy(animations = enabled) }
    }

    fun updateConfirmDelete(enabled: Boolean) {
        repository.confirmDelete = enabled
        _uiState.update { it.copy(confirmDelete = enabled) }
    }

    fun updateRestoreLastConfiguration(enabled: Boolean) {
        repository.restoreLastConfiguration = enabled
        _uiState.update { it.copy(restoreLastConfig = enabled) }
    }

    fun resetSettings() {
        repository.resetSettingsToDefaults()
        loadInitialState()
        showNotification("Settings reset to defaults")
    }

    fun dismissNotification() {
        _uiState.update { it.copy(userNotification = null) }
    }

    private fun showNotification(message: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(userNotification = message) }
        }
    }
}
