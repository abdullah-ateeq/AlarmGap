package com.example.alarmgap.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Animation
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alarmgap.data.model.ThemePreference
import com.example.alarmgap.data.model.TimeFormatPreference
import com.example.alarmgap.ui.components.AppTopBar
import com.example.alarmgap.ui.home.AlarmGapViewModel
import com.example.alarmgap.util.HapticUtil

@Composable
fun SettingsScreen(
    viewModel: AlarmGapViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val haptic = LocalHapticFeedback.current
    val snackbarHostState = remember { SnackbarHostState() }

    var showThemeDialog by remember { mutableStateOf(false) }
    var showTimeFormatDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }
    var showClearHistoryDialog by remember { mutableStateOf(false) }

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
                title = "Settings",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack
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
            // APPEARANCE SECTION
            SettingsSectionHeader(title = "APPEARANCE", icon = Icons.Default.Palette)

            SettingsClickableRow(
                title = "Theme",
                subtitle = uiState.themePreference.displayName,
                onClick = {
                    HapticUtil.performClickFeedback(haptic, uiState.hapticFeedback)
                    showThemeDialog = true
                }
            )

            SettingsSwitchRow(
                title = "Dynamic Color",
                subtitle = "Match system wallpaper colors on Android 12+",
                checked = uiState.dynamicColor,
                onCheckedChange = {
                    HapticUtil.performClickFeedback(haptic, uiState.hapticFeedback)
                    viewModel.updateDynamicColor(it)
                }
            )

            Spacer(modifier = Modifier.height(18.dp))

            // TIME SETTINGS SECTION
            SettingsSectionHeader(title = "TIME SETTINGS", icon = Icons.Default.Schedule)

            SettingsClickableRow(
                title = "Time Format",
                subtitle = uiState.timeFormat.displayName,
                onClick = {
                    HapticUtil.performClickFeedback(haptic, uiState.hapticFeedback)
                    showTimeFormatDialog = true
                }
            )

            SettingsSwitchRow(
                title = "Show Seconds",
                subtitle = "Include exact seconds in picker, calculation, and copy",
                checked = uiState.showSeconds,
                onCheckedChange = {
                    HapticUtil.performClickFeedback(haptic, uiState.hapticFeedback)
                    viewModel.updateShowSeconds(it)
                }
            )

            Spacer(modifier = Modifier.height(18.dp))

            // CALCULATION SETTINGS SECTION
            SettingsSectionHeader(title = "CALCULATION SETTINGS", icon = Icons.Default.Calculate)

            SettingsSwitchRow(
                title = "Live Calculation",
                subtitle = "Recalculate automatically when times or modes change",
                checked = uiState.liveCalculation,
                onCheckedChange = {
                    HapticUtil.performClickFeedback(haptic, uiState.hapticFeedback)
                    viewModel.updateLiveCalculation(it)
                }
            )

            SettingsSwitchRow(
                title = "Show Date Context",
                subtitle = "Display calendar dates for midnight and day offsets",
                checked = uiState.showDateContext,
                onCheckedChange = {
                    HapticUtil.performClickFeedback(haptic, uiState.hapticFeedback)
                    viewModel.updateShowDateContext(it)
                }
            )

            Spacer(modifier = Modifier.height(18.dp))

            // RESULT VISIBILITY SETTINGS SECTION
            SettingsSectionHeader(title = "RESULT PREFERENCES", icon = Icons.Default.Visibility)

            SettingsSwitchRow(
                title = "Duration Breakdown",
                subtitle = "Show separate hours, minutes, and seconds units",
                checked = uiState.showDurationBreakdown,
                onCheckedChange = {
                    HapticUtil.performClickFeedback(haptic, uiState.hapticFeedback)
                    viewModel.updateShowDurationBreakdown(it)
                }
            )

            SettingsSwitchRow(
                title = "Digital Duration",
                subtitle = "Display digital clock format (HH:mm:ss)",
                checked = uiState.showDigitalDuration,
                onCheckedChange = {
                    HapticUtil.performClickFeedback(haptic, uiState.hapticFeedback)
                    viewModel.updateShowDigitalDuration(it)
                }
            )

            SettingsSwitchRow(
                title = "Total Minutes",
                subtitle = "Display full duration expressed in minutes",
                checked = uiState.showTotalMinutes,
                onCheckedChange = {
                    HapticUtil.performClickFeedback(haptic, uiState.hapticFeedback)
                    viewModel.updateShowTotalMinutes(it)
                }
            )

            SettingsSwitchRow(
                title = "Total Seconds",
                subtitle = "Display full duration expressed in seconds",
                checked = uiState.showTotalSeconds,
                onCheckedChange = {
                    HapticUtil.performClickFeedback(haptic, uiState.hapticFeedback)
                    viewModel.updateShowTotalSeconds(it)
                }
            )

            SettingsSwitchRow(
                title = "Decimal Hours",
                subtitle = "Display duration in decimal hours (e.g., 6.25 hrs)",
                checked = uiState.showDecimalHours,
                onCheckedChange = {
                    HapticUtil.performClickFeedback(haptic, uiState.hapticFeedback)
                    viewModel.updateShowDecimalHours(it)
                }
            )

            Spacer(modifier = Modifier.height(18.dp))

            // BEHAVIOR SETTINGS SECTION
            SettingsSectionHeader(title = "BEHAVIOR & FEEDBACK", icon = Icons.Default.TouchApp)

            SettingsSwitchRow(
                title = "Haptic Feedback",
                subtitle = "Vibration ticks on button presses and time changes",
                checked = uiState.hapticFeedback,
                onCheckedChange = {
                    HapticUtil.performClickFeedback(haptic, uiState.hapticFeedback)
                    viewModel.updateHapticFeedback(it)
                }
            )

            SettingsSwitchRow(
                title = "Animations",
                subtitle = "Smooth UI transitions between calculation states",
                checked = uiState.animations,
                onCheckedChange = {
                    HapticUtil.performClickFeedback(haptic, uiState.hapticFeedback)
                    viewModel.updateAnimations(it)
                }
            )

            SettingsSwitchRow(
                title = "Confirm Delete",
                subtitle = "Ask for confirmation before deleting saved calculations",
                checked = uiState.confirmDelete,
                onCheckedChange = {
                    HapticUtil.performClickFeedback(haptic, uiState.hapticFeedback)
                    viewModel.updateConfirmDelete(it)
                }
            )

            SettingsSwitchRow(
                title = "Restore Last Configuration",
                subtitle = "Remember previous times and modes when opening app",
                checked = uiState.restoreLastConfig,
                onCheckedChange = {
                    HapticUtil.performClickFeedback(haptic, uiState.hapticFeedback)
                    viewModel.updateRestoreLastConfiguration(it)
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ACTIONS
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        HapticUtil.performClickFeedback(haptic, uiState.hapticFeedback)
                        showClearHistoryDialog = true
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.size(4.dp))
                    Text("Clear History", style = MaterialTheme.typography.labelMedium)
                }

                OutlinedButton(
                    onClick = {
                        HapticUtil.performClickFeedback(haptic, uiState.hapticFeedback)
                        showResetDialog = true
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.size(4.dp))
                    Text("Reset Settings", style = MaterialTheme.typography.labelMedium)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Theme Dialog
        if (showThemeDialog) {
            AlertDialog(
                onDismissRequest = { showThemeDialog = false },
                title = { Text("Choose Theme") },
                text = {
                    Column {
                        ThemePreference.values().forEach { pref ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.updateTheme(pref)
                                        showThemeDialog = false
                                    }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = uiState.themePreference == pref,
                                    onClick = {
                                        viewModel.updateTheme(pref)
                                        showThemeDialog = false
                                    }
                                )
                                Text(
                                    text = pref.displayName,
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showThemeDialog = false }) {
                        Text("Close")
                    }
                }
            )
        }

        // Time Format Dialog
        if (showTimeFormatDialog) {
            AlertDialog(
                onDismissRequest = { showTimeFormatDialog = false },
                title = { Text("Choose Time Format") },
                text = {
                    Column {
                        TimeFormatPreference.values().forEach { pref ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.updateTimeFormat(pref)
                                        showTimeFormatDialog = false
                                    }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = uiState.timeFormat == pref,
                                    onClick = {
                                        viewModel.updateTimeFormat(pref)
                                        showTimeFormatDialog = false
                                    }
                                )
                                Text(
                                    text = pref.displayName,
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showTimeFormatDialog = false }) {
                        Text("Close")
                    }
                }
            )
        }

        // Reset Settings Dialog
        if (showResetDialog) {
            AlertDialog(
                onDismissRequest = { showResetDialog = false },
                title = { Text("Reset settings?") },
                text = {
                    Text("This will restore all default preferences. Your saved history will not be deleted.")
                },
                confirmButton = {
                    TextButton(onClick = {
                        HapticUtil.performImpactFeedback(haptic, uiState.hapticFeedback)
                        viewModel.resetSettings()
                        showResetDialog = false
                    }) {
                        Text("Reset", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showResetDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Clear History Dialog
        if (showClearHistoryDialog) {
            AlertDialog(
                onDismissRequest = { showClearHistoryDialog = false },
                title = { Text("Clear all saved calculations?") },
                text = {
                    Text("Are you sure you want to clear your saved calculation history?")
                },
                confirmButton = {
                    TextButton(onClick = {
                        HapticUtil.performImpactFeedback(haptic, uiState.hapticFeedback)
                        viewModel.clearAllHistory()
                        showClearHistoryDialog = false
                    }) {
                        Text("Clear", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showClearHistoryDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
private fun SettingsSectionHeader(title: String, icon: ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.size(6.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            ),
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun SettingsClickableRow(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SettingsSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    }
}
