package com.example.alarmgap.data.model

import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

data class AlarmGapUiState(
    val startTime: LocalTime = LocalTime.of(8, 0, 0),
    val endTime: LocalTime = LocalTime.of(17, 0, 0),
    val startDate: LocalDate = LocalDate.now(),
    val calculationMode: CalculationMode = CalculationMode.TIME_DIFFERENCE,
    val dayRelationship: DayRelationship = DayRelationship.AUTOMATIC,
    val durationHours: Int = 0,
    val durationMinutes: Int = 0,
    val durationSeconds: Int = 0,
    val totalSeconds: Long = 0,
    val totalMinutes: Long = 0,
    val decimalHours: Double = 0.0,
    val result: Duration? = null,
    val errorMessage: String? = null,
    val history: List<AlarmGapHistory> = emptyList(),

    // Detailed context and resolved timestamps
    val resolvedStartDateTime: LocalDateTime? = null,
    val resolvedEndDateTime: LocalDateTime? = null,
    val isAmbiguousSameTime: Boolean = false,

    // Duration-to-End / Duration-to-Start inputs
    val inputDurationHours: Long = 2,
    val inputDurationMinutes: Int = 30,
    val inputDurationSeconds: Int = 0,

    // Preferences
    val timeFormat: TimeFormatPreference = TimeFormatPreference.SYSTEM,
    val showSeconds: Boolean = false,
    val liveCalculation: Boolean = true,
    val showDateContext: Boolean = true,
    val showDurationBreakdown: Boolean = true,
    val showDigitalDuration: Boolean = true,
    val showTotalMinutes: Boolean = true,
    val showTotalSeconds: Boolean = true,
    val showDecimalHours: Boolean = true,
    val hapticFeedback: Boolean = true,
    val animations: Boolean = true,
    val confirmDelete: Boolean = true,
    val restoreLastConfig: Boolean = true,
    val themePreference: ThemePreference = ThemePreference.SYSTEM,
    val dynamicColor: Boolean = true,

    // Feedback
    val userNotification: String? = null
)
