package com.example.alarmgap.calculator

import com.example.alarmgap.data.model.DayRelationship
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class IdenticalTimesException(message: String = "Both times are identical. Choose Same Day or Next Day.") :
    IllegalArgumentException(message)

data class CalculationResult(
    val duration: Duration,
    val startDateTime: LocalDateTime,
    val endDateTime: LocalDateTime,
    val resolvedRelationship: DayRelationship
)

class AlarmGapCalculator {

    /**
     * Primary calculation function as specified in requirements.
     */
    fun calculateDuration(
        startDate: LocalDate,
        startTime: LocalTime,
        endTime: LocalTime,
        relationship: DayRelationship
    ): Duration {
        return calculate(startDate, startTime, endTime, relationship).duration
    }

    /**
     * Detailed calculation returning start, end timestamps and the duration.
     */
    fun calculate(
        startDate: LocalDate,
        startTime: LocalTime,
        endTime: LocalTime,
        relationship: DayRelationship
    ): CalculationResult {
        return when (relationship) {
            DayRelationship.AUTOMATIC -> {
                when {
                    endTime.isAfter(startTime) -> {
                        val startDateTime = LocalDateTime.of(startDate, startTime)
                        val endDateTime = LocalDateTime.of(startDate, endTime)
                        CalculationResult(
                            duration = Duration.between(startDateTime, endDateTime),
                            startDateTime = startDateTime,
                            endDateTime = endDateTime,
                            resolvedRelationship = DayRelationship.SAME_DAY
                        )
                    }
                    endTime.isBefore(startTime) -> {
                        val startDateTime = LocalDateTime.of(startDate, startTime)
                        val endDateTime = LocalDateTime.of(startDate.plusDays(1), endTime)
                        CalculationResult(
                            duration = Duration.between(startDateTime, endDateTime),
                            startDateTime = startDateTime,
                            endDateTime = endDateTime,
                            resolvedRelationship = DayRelationship.NEXT_DAY
                        )
                    }
                    else -> {
                        // Identical times in automatic mode: do NOT silently assume 0 or 24 hours without user intent.
                        throw IdenticalTimesException(
                            "Both times are identical.\n\nChoose:\nSame Day\nor\nNext Day"
                        )
                    }
                }
            }

            DayRelationship.SAME_DAY -> {
                if (endTime.isBefore(startTime)) {
                    throw IllegalArgumentException(
                        "End time is earlier than start time.\n\nUse Next Day if the time crosses midnight."
                    )
                }
                val startDateTime = LocalDateTime.of(startDate, startTime)
                val endDateTime = LocalDateTime.of(startDate, endTime)
                CalculationResult(
                    duration = Duration.between(startDateTime, endDateTime),
                    startDateTime = startDateTime,
                    endDateTime = endDateTime,
                    resolvedRelationship = DayRelationship.SAME_DAY
                )
            }

            DayRelationship.NEXT_DAY -> {
                val startDateTime = LocalDateTime.of(startDate, startTime)
                val endDateTime = LocalDateTime.of(startDate.plusDays(1), endTime)
                CalculationResult(
                    duration = Duration.between(startDateTime, endDateTime),
                    startDateTime = startDateTime,
                    endDateTime = endDateTime,
                    resolvedRelationship = DayRelationship.NEXT_DAY
                )
            }

            DayRelationship.PREVIOUS_DAY -> {
                // End time belongs to the previous day
                val startDateTime = LocalDateTime.of(startDate, startTime)
                val endDateTime = LocalDateTime.of(startDate.minusDays(1), endTime)
                // The exact positive duration between the previous-day end moment and the start moment
                val duration = Duration.between(endDateTime, startDateTime)
                CalculationResult(
                    duration = duration,
                    startDateTime = startDateTime,
                    endDateTime = endDateTime,
                    resolvedRelationship = DayRelationship.PREVIOUS_DAY
                )
            }
        }
    }

    /**
     * Calculates the End Time given a Start Time and a Duration.
     * Supports durations exceeding 24 hours (e.g. 27 hours).
     */
    fun calculateEndTime(
        startDateTime: LocalDateTime,
        duration: Duration
    ): LocalDateTime {
        validateDurationInput(duration)
        return startDateTime.plus(duration)
    }

    /**
     * Calculates the Start Time given an End Time and a Duration.
     */
    fun calculateStartTime(
        endDateTime: LocalDateTime,
        duration: Duration
    ): LocalDateTime {
        validateDurationInput(duration)
        return endDateTime.minus(duration)
    }

    /**
     * Validates that duration is between 1 second and 365 days.
     */
    fun validateDurationInput(duration: Duration) {
        val totalSeconds = duration.seconds
        val maxSeconds = 365L * 24 * 3600
        require(totalSeconds >= 1) { "Duration must be at least 1 second." }
        require(totalSeconds <= maxSeconds) { "Duration must not exceed 365 days." }
    }

    fun calculateTotalMinutes(duration: Duration): Long {
        return duration.toMinutes()
    }

    fun calculateTotalSeconds(duration: Duration): Long {
        return duration.seconds
    }

    fun calculateDecimalHours(duration: Duration): Double {
        return duration.seconds.toDouble() / 3600.0
    }
}
