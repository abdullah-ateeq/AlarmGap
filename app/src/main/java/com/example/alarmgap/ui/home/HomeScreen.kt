package com.example.alarmgap.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alarmgap.data.model.CalculationMode
import com.example.alarmgap.data.model.DayRelationship
import com.example.alarmgap.formatter.TimeGapFormatter
import com.example.alarmgap.ui.components.AlarmGapTimePickerDialog
import com.example.alarmgap.ui.components.AppTopBar
import com.example.alarmgap.ui.components.DurationResultCard
import com.example.alarmgap.ui.components.TimeCard
import com.example.alarmgap.ui.components.TimelineConnector
import com.example.alarmgap.util.HapticUtil
import java.time.Duration
import java.time.LocalTime

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    viewModel: AlarmGapViewModel,
    onNavigateHistory: () -> Unit,
    onNavigateSettings: () -> Unit,
    onNavigateAbout: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val snackbarHostState = remember { SnackbarHostState() }

    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.userNotification) {
        val message = uiState.userNotification
        if (message != null) {
            snackbarHostState.showSnackbar(message)
            viewModel.dismissNotification()
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "AlarmGap",
                subtitle = "Calculate the duration between two clock times.",
                onNavigateHistory = onNavigateHistory,
                onNavigateSettings = onNavigateSettings,
                onNavigateAbout = onNavigateAbout
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Calculation Mode Selector (Secondary toggle)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = uiState.calculationMode == CalculationMode.TIME_DIFFERENCE,
                    onClick = {
                        HapticUtil.performClickFeedback(haptic, uiState.hapticFeedback)
                        viewModel.onCalculationModeChanged(CalculationMode.TIME_DIFFERENCE)
                    },
                    label = { Text("Time Difference") }
                )
                FilterChip(
                    selected = uiState.calculationMode == CalculationMode.DURATION_TO_END,
                    onClick = {
                        HapticUtil.performClickFeedback(haptic, uiState.hapticFeedback)
                        viewModel.onCalculationModeChanged(CalculationMode.DURATION_TO_END)
                    },
                    label = { Text("Duration → End Time") }
                )
                FilterChip(
                    selected = uiState.calculationMode == CalculationMode.DURATION_TO_START,
                    onClick = {
                        HapticUtil.performClickFeedback(haptic, uiState.hapticFeedback)
                        viewModel.onCalculationModeChanged(CalculationMode.DURATION_TO_START)
                    },
                    label = { Text("Duration → Start Time") }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            val startFormatted = TimeGapFormatter.formatTime(
                uiState.startTime,
                uiState.timeFormat,
                uiState.showSeconds,
                context
            )
            val endFormatted = TimeGapFormatter.formatTime(
                uiState.endTime,
                uiState.timeFormat,
                uiState.showSeconds,
                context
            )

            val resolvedStart = uiState.resolvedStartDateTime
            val resolvedEnd = uiState.resolvedEndDateTime

            val startDateContext = if (uiState.showDateContext && resolvedStart != null) {
                TimeGapFormatter.formatDateShort(resolvedStart.toLocalDate())
            } else null

            val endDateContext = if (uiState.showDateContext && resolvedEnd != null) {
                TimeGapFormatter.formatDateShort(resolvedEnd.toLocalDate())
            } else null

            if (uiState.calculationMode == CalculationMode.TIME_DIFFERENCE) {
                // START TIME CARD
                TimeCard(
                    label = "Start Time",
                    formattedTime = startFormatted,
                    dateContext = startDateContext,
                    accessibilityDescription = "Start time, $startFormatted.",
                    onClickChange = {
                        HapticUtil.performClickFeedback(haptic, uiState.hapticFeedback)
                        showStartTimePicker = true
                    }
                )

                // TIMELINE CONNECTOR WITH SWAP
                TimelineConnector(
                    onSwap = {
                        HapticUtil.performImpactFeedback(haptic, uiState.hapticFeedback)
                        viewModel.swapTimes()
                    }
                )

                // END TIME CARD
                TimeCard(
                    label = "End Time",
                    formattedTime = endFormatted,
                    dateContext = endDateContext,
                    accessibilityDescription = "End time, $endFormatted.",
                    onClickChange = {
                        HapticUtil.performClickFeedback(haptic, uiState.hapticFeedback)
                        showEndTimePicker = true
                    }
                )

                Spacer(modifier = Modifier.height(14.dp))

                // DAY RELATIONSHIP SELECTOR
                Text(
                    text = "CALCULATION MODE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DayRelationship.values().forEach { relationship ->
                        FilterChip(
                            selected = uiState.dayRelationship == relationship,
                            onClick = {
                                HapticUtil.performClickFeedback(haptic, uiState.hapticFeedback)
                                viewModel.onDayRelationshipChanged(relationship)
                            },
                            label = { Text(relationship.displayName) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // QUICK PRESETS
                Text(
                    text = "QUICK PRESETS (START + DURATION)",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val quickPresets = listOf(
                        "15 min" to Duration.ofMinutes(15),
                        "30 min" to Duration.ofMinutes(30),
                        "1 hour" to Duration.ofHours(1),
                        "2 hours" to Duration.ofHours(2),
                        "4 hours" to Duration.ofHours(4),
                        "8 hours" to Duration.ofHours(8),
                        "12 hours" to Duration.ofHours(12)
                    )

                    quickPresets.forEach { (label, duration) ->
                        SuggestionChip(
                            onClick = {
                                HapticUtil.performClickFeedback(haptic, uiState.hapticFeedback)
                                viewModel.applyPresetDuration(duration)
                            },
                            label = { Text(label) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // COMMON SCHEDULE PRESETS
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SuggestionChip(
                        onClick = {
                            HapticUtil.performClickFeedback(haptic, uiState.hapticFeedback)
                            viewModel.applyCommonPreset(
                                LocalTime.of(9, 0),
                                LocalTime.of(17, 0),
                                DayRelationship.AUTOMATIC
                            )
                        },
                        label = { Text("Workday (9-5)") }
                    )
                    SuggestionChip(
                        onClick = {
                            HapticUtil.performClickFeedback(haptic, uiState.hapticFeedback)
                            viewModel.applyCommonPreset(
                                LocalTime.of(9, 0),
                                LocalTime.of(13, 0),
                                DayRelationship.AUTOMATIC
                            )
                        },
                        label = { Text("Half Day (9-1)") }
                    )
                    SuggestionChip(
                        onClick = {
                            HapticUtil.performClickFeedback(haptic, uiState.hapticFeedback)
                            viewModel.applyCommonPreset(
                                LocalTime.of(22, 0),
                                LocalTime.of(6, 0),
                                DayRelationship.NEXT_DAY
                            )
                        },
                        label = { Text("Overnight (10-6)") }
                    )
                    SuggestionChip(
                        onClick = {
                            HapticUtil.performClickFeedback(haptic, uiState.hapticFeedback)
                            viewModel.applyCommonPreset(
                                LocalTime.of(0, 0),
                                LocalTime.of(0, 0),
                                DayRelationship.NEXT_DAY
                            )
                        },
                        label = { Text("Full Day (24h)") }
                    )
                }

            } else {
                // Duration -> End or Start mode
                if (uiState.calculationMode == CalculationMode.DURATION_TO_END) {
                    TimeCard(
                        label = "Start Time",
                        formattedTime = startFormatted,
                        dateContext = startDateContext,
                        accessibilityDescription = "Start time, $startFormatted.",
                        onClickChange = {
                            HapticUtil.performClickFeedback(haptic, uiState.hapticFeedback)
                            showStartTimePicker = true
                        }
                    )
                } else {
                    TimeCard(
                        label = "End Time",
                        formattedTime = endFormatted,
                        dateContext = endDateContext,
                        accessibilityDescription = "End time, $endFormatted.",
                        onClickChange = {
                            HapticUtil.performClickFeedback(haptic, uiState.hapticFeedback)
                            showEndTimePicker = true
                        }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // DURATION INPUT CARD
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "DURATION INPUT",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = uiState.inputDurationHours.toString(),
                                onValueChange = { str ->
                                    val h = str.filter { it.isDigit() }.toLongOrNull() ?: 0L
                                    viewModel.onInputDurationChanged(
                                        h,
                                        uiState.inputDurationMinutes,
                                        uiState.inputDurationSeconds
                                    )
                                },
                                label = { Text("Hours") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = uiState.inputDurationMinutes.toString(),
                                onValueChange = { str ->
                                    val m = (str.filter { it.isDigit() }.toIntOrNull() ?: 0).coerceIn(0, 59)
                                    viewModel.onInputDurationChanged(
                                        uiState.inputDurationHours,
                                        m,
                                        uiState.inputDurationSeconds
                                    )
                                },
                                label = { Text("Minutes") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                            if (uiState.showSeconds) {
                                OutlinedTextField(
                                    value = uiState.inputDurationSeconds.toString(),
                                    onValueChange = { str ->
                                        val s = (str.filter { it.isDigit() }.toIntOrNull() ?: 0).coerceIn(0, 59)
                                        viewModel.onInputDurationChanged(
                                            uiState.inputDurationHours,
                                            uiState.inputDurationMinutes,
                                            s
                                        )
                                    },
                                    label = { Text("Seconds") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Quick duration presets
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val durationPresets = listOf(
                                "15m" to (0L to 15),
                                "30m" to (0L to 30),
                                "1h" to (1L to 0),
                                "2h" to (2L to 0),
                                "4h" to (4L to 0),
                                "8h" to (8L to 0),
                                "12h" to (12L to 0),
                                "24h" to (24L to 0)
                            )

                            durationPresets.forEach { (label, pair) ->
                                SuggestionChip(
                                    onClick = {
                                        HapticUtil.performClickFeedback(haptic, uiState.hapticFeedback)
                                        viewModel.onInputDurationChanged(pair.first, pair.second, 0)
                                    },
                                    label = { Text(label) }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // CALCULATE BUTTON (Always provided, especially prominent when Live Calculation is off)
            Button(
                onClick = {
                    HapticUtil.performImpactFeedback(haptic, uiState.hapticFeedback)
                    viewModel.recalculate()
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Icon(Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "CALCULATE DURATION",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // MAIN RESULT CARD
            DurationResultCard(
                uiState = uiState,
                formattedStartTime = startFormatted,
                formattedEndTime = endFormatted,
                onCopyFull = {
                    HapticUtil.performClickFeedback(haptic, uiState.hapticFeedback)
                    viewModel.copyResult(context)
                },
                onCopyDuration = {
                    HapticUtil.performClickFeedback(haptic, uiState.hapticFeedback)
                    viewModel.copyDurationOnly(context)
                },
                onSaveHistory = {
                    HapticUtil.performImpactFeedback(haptic, uiState.hapticFeedback)
                    viewModel.saveCurrentCalculation()
                },
                onShare = {
                    HapticUtil.performClickFeedback(haptic, uiState.hapticFeedback)
                    viewModel.shareResult(context)
                },
                onReverse = {
                    HapticUtil.performClickFeedback(haptic, uiState.hapticFeedback)
                    viewModel.triggerReverseCalculation()
                },
                onSelectDayRelationship = { rel ->
                    HapticUtil.performClickFeedback(haptic, uiState.hapticFeedback)
                    viewModel.onDayRelationshipChanged(rel)
                }
            )

            Spacer(modifier = Modifier.height(28.dp))
        }

        // Time Picker Dialogs
        val is24HourMode = when (uiState.timeFormat) {
            com.example.alarmgap.data.model.TimeFormatPreference.SYSTEM ->
                android.text.format.DateFormat.is24HourFormat(context)
            com.example.alarmgap.data.model.TimeFormatPreference.FORMAT_12_HOUR -> false
            com.example.alarmgap.data.model.TimeFormatPreference.FORMAT_24_HOUR -> true
        }

        if (showStartTimePicker) {
            AlarmGapTimePickerDialog(
                title = "Select Start Time",
                initialTime = uiState.startTime,
                is24Hour = is24HourMode,
                allowSeconds = uiState.showSeconds,
                onConfirm = { chosenTime ->
                    HapticUtil.performClickFeedback(haptic, uiState.hapticFeedback)
                    viewModel.onStartTimeChanged(chosenTime)
                    showStartTimePicker = false
                },
                onDismiss = { showStartTimePicker = false }
            )
        }

        if (showEndTimePicker) {
            AlarmGapTimePickerDialog(
                title = "Select End Time",
                initialTime = uiState.endTime,
                is24Hour = is24HourMode,
                allowSeconds = uiState.showSeconds,
                onConfirm = { chosenTime ->
                    HapticUtil.performClickFeedback(haptic, uiState.hapticFeedback)
                    viewModel.onEndTimeChanged(chosenTime)
                    showEndTimePicker = false
                },
                onDismiss = { showEndTimePicker = false }
            )
        }
    }
}
