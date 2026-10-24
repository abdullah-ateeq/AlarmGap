package com.example.alarmgap.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alarmgap.data.model.AlarmGapUiState
import com.example.alarmgap.data.model.DayRelationship
import com.example.alarmgap.formatter.TimeGapFormatter
import java.time.Duration

@Composable
fun DurationResultCard(
    uiState: AlarmGapUiState,
    formattedStartTime: String,
    formattedEndTime: String,
    onCopyFull: () -> Unit,
    onCopyDuration: () -> Unit,
    onSaveHistory: () -> Unit,
    onShare: () -> Unit,
    onReverse: () -> Unit,
    onSelectDayRelationship: (DayRelationship) -> Unit,
    modifier: Modifier = Modifier
) {
    val resultDuration = uiState.result

    if (uiState.isAmbiguousSameTime) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f)),
            modifier = modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Both times are identical.",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Choose your intended day relationship:",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { onSelectDayRelationship(DayRelationship.SAME_DAY) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Same Day (0m)")
                    }
                    FilledTonalButton(
                        onClick = { onSelectDayRelationship(DayRelationship.NEXT_DAY) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Next Day (24h)")
                    }
                }
            }
        }
        return
    }

    if (uiState.errorMessage != null) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f)
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f)),
            modifier = modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = uiState.errorMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.error
                )
                if (uiState.dayRelationship == DayRelationship.SAME_DAY) {
                    Spacer(modifier = Modifier.height(12.dp))
                    FilledTonalButton(
                        onClick = { onSelectDayRelationship(DayRelationship.NEXT_DAY) }
                    ) {
                        Text("Switch to Next Day")
                    }
                }
            }
        }
        return
    }

    if (resultDuration == null) {
        return
    }

    val humanDuration = TimeGapFormatter.formatHumanReadable(resultDuration, uiState.showSeconds)
    val shortDuration = TimeGapFormatter.formatShortDuration(resultDuration, uiState.showSeconds)
    val digitalDuration = TimeGapFormatter.formatDigital(resultDuration)

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header label
            Text(
                text = "TIME GAP",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                ),
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Large duration summary
            Text(
                text = shortDuration,
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.semantics {
                    contentDescription = "Time gap, $humanDuration."
                }
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Human readable description
            Text(
                text = humanDuration,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            if (uiState.showDigitalDuration) {
                Spacer(modifier = Modifier.height(10.dp))
                // Digital clock pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = digitalDuration,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.semantics {
                            contentDescription = "Duration, ${resultDuration.toHours()} hours, ${(resultDuration.toMinutes() % 60)} minutes, ${(resultDuration.seconds % 60)} seconds."
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Timeline line connecting Start and End
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = formattedStartTime,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(10.dp))
                Box(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .width(48.dp)
                        .height(2.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = formattedEndTime,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Date context line if applicable
            if (uiState.showDateContext && uiState.resolvedStartDateTime != null && uiState.resolvedEndDateTime != null) {
                val startDateStr = TimeGapFormatter.formatDateShort(uiState.resolvedStartDateTime.toLocalDate())
                val endDateStr = TimeGapFormatter.formatDateShort(uiState.resolvedEndDateTime.toLocalDate())
                val isNextDay = uiState.resolvedEndDateTime.toLocalDate().isAfter(uiState.resolvedStartDateTime.toLocalDate())
                val isPrevDay = uiState.resolvedEndDateTime.toLocalDate().isBefore(uiState.resolvedStartDateTime.toLocalDate())

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = when {
                        isNextDay -> "FROM $startDateStr → TO $endDateStr (Next Day)"
                        isPrevDay -> "FROM $startDateStr → TO $endDateStr (Previous Day)"
                        else -> "$startDateStr (Same Day)"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Duration Breakdown Section
            if (uiState.showDurationBreakdown) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "DURATION BREAKDOWN",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        BreakdownItem(value = "${uiState.durationHours}", unit = if (uiState.durationHours == 1) "hour" else "hours")
                        BreakdownItem(value = "${uiState.durationMinutes}", unit = if (uiState.durationMinutes == 1) "minute" else "minutes")
                        BreakdownItem(value = "${uiState.durationSeconds}", unit = if (uiState.durationSeconds == 1) "second" else "seconds")
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            // Stat Cards (Total Minutes, Total Seconds, Decimal Hours)
            if (uiState.showTotalMinutes || uiState.showTotalSeconds || uiState.showDecimalHours) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (uiState.showTotalMinutes) {
                        StatCard(
                            label = "TOTAL MINUTES",
                            value = "${uiState.totalMinutes}",
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (uiState.showTotalSeconds) {
                        StatCard(
                            label = "TOTAL SECONDS",
                            value = TimeGapFormatter.formatTotalSeconds(resultDuration).replace(" seconds", "").replace(" second", ""),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (uiState.showDecimalHours) {
                        StatCard(
                            label = "DECIMAL HOURS",
                            value = TimeGapFormatter.formatDecimalHours(uiState.decimalHours),
                            unit = "hrs",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Action Buttons Row: [ COPY ] [ SAVE ] [ SHARE ] [ REVERSE ]
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onCopyFull,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Copy", style = MaterialTheme.typography.labelMedium)
                }

                FilledTonalButton(
                    onClick = onSaveHistory,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.BookmarkBorder, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Save", style = MaterialTheme.typography.labelMedium)
                }

                OutlinedButton(
                    onClick = onShare,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Share", style = MaterialTheme.typography.labelMedium)
                }

                OutlinedButton(
                    onClick = onReverse,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Replay, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reverse", style = MaterialTheme.typography.labelMedium)
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            OutlinedButton(
                onClick = onCopyDuration,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Copy Duration Only (\"$humanDuration\")", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun BreakdownItem(value: String, unit: String) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ),
        modifier = Modifier.width(96.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(vertical = 8.dp, horizontal = 12.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = unit,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, unit: String? = null, modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 10.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.5.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (unit != null) "$value $unit" else value,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1
            )
        }
    }
}
