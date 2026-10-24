package com.example.alarmgap.formatter

import android.content.Context
import com.example.alarmgap.data.model.TimeFormatPreference
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object TimeGapFormatter {

    private val DATE_FORMATTER = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.US)
    private val DATE_SHORT_FORMATTER = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.US)

    /**
     * Human-readable primary duration.
     * Examples: "6 hours 15 minutes", "45 minutes", "2 hours 5 minutes 30 seconds", "0 minutes"
     */
    fun formatHumanReadable(duration: Duration, includeSeconds: Boolean = false): String {
        val totalSeconds = duration.seconds
        val hours = duration.toHours()
        val minutes = (duration.toMinutes() % 60).toInt()
        val seconds = (totalSeconds % 60).toInt()

        if (totalSeconds == 0L) {
            return "0 minutes"
        }

        val parts = mutableListOf<String>()

        if (hours > 0) {
            parts.add("$hours " + if (hours == 1L) "hour" else "hours")
        }

        if (minutes > 0) {
            parts.add("$minutes " + if (minutes == 1) "minute" else "minutes")
        }

        if (includeSeconds && seconds > 0) {
            parts.add("$seconds " + if (seconds == 1) "second" else "seconds")
        }

        // If hours and minutes were 0 but seconds were present and includeSeconds is false, show "< 1 minute" or "0 minutes"
        if (parts.isEmpty()) {
            return if (includeSeconds && seconds > 0) {
                "$seconds " + if (seconds == 1) "second" else "seconds"
            } else {
                "0 minutes"
            }
        }

        return parts.joinToString(" ")
    }

    /**
     * Short representation for quick cards e.g. "6h 15m" or "6h 15m 30s"
     */
    fun formatShortDuration(duration: Duration, includeSeconds: Boolean = false): String {
        val hours = duration.toHours()
        val minutes = (duration.toMinutes() % 60).toInt()
        val seconds = (duration.seconds % 60).toInt()

        if (duration.seconds == 0L) return "0m"

        val parts = mutableListOf<String>()
        if (hours > 0) parts.add("${hours}h")
        if (minutes > 0) parts.add("${minutes}m")
        if (includeSeconds && seconds > 0) parts.add("${seconds}s")

        return if (parts.isEmpty()) {
            if (includeSeconds && seconds > 0) "${seconds}s" else "0m"
        } else {
            parts.joinToString(" ")
        }
    }

    /**
     * Digital format: HH:mm:ss.
     * If hours > 24 (e.g. 27 hours), does NOT wrap, e.g. "27:15:00".
     */
    fun formatDigital(duration: Duration): String {
        val hours = duration.toHours()
        val minutes = (duration.toMinutes() % 60).toInt()
        val seconds = (duration.seconds % 60).toInt()
        return String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
    }

    /**
     * Total minutes formatted with unit e.g. "375 minutes".
     */
    fun formatTotalMinutes(duration: Duration): String {
        val totalMinutes = duration.toMinutes()
        return "$totalMinutes " + if (totalMinutes == 1L) "minute" else "minutes"
    }

    /**
     * Total seconds formatted with commas e.g. "22,500 seconds".
     */
    fun formatTotalSeconds(duration: Duration): String {
        val totalSeconds = duration.seconds
        val formatted = NumberFormat.getNumberInstance(Locale.US).format(totalSeconds)
        return "$formatted " + if (totalSeconds == 1L) "second" else "seconds"
    }

    /**
     * Decimal hours formatted sensibly without unnecessary trailing zeros.
     * e.g. 6.25, 1.5, 0.75
     */
    fun formatDecimalHours(decimalHours: Double): String {
        val symbols = DecimalFormatSymbols(Locale.US)
        val df = DecimalFormat("0.####", symbols)
        return df.format(decimalHours)
    }

    /**
     * Formats LocalTime according to preference and seconds toggle.
     */
    fun formatTime(
        time: LocalTime,
        preference: TimeFormatPreference,
        includeSeconds: Boolean,
        context: Context? = null
    ): String {
        val is24Hour = when (preference) {
            TimeFormatPreference.SYSTEM -> {
                if (context != null) {
                    android.text.format.DateFormat.is24HourFormat(context)
                } else false
            }
            TimeFormatPreference.FORMAT_12_HOUR -> false
            TimeFormatPreference.FORMAT_24_HOUR -> true
        }

        val pattern = when {
            is24Hour && includeSeconds -> "HH:mm:ss"
            is24Hour && !includeSeconds -> "HH:mm"
            !is24Hour && includeSeconds -> "hh:mm:ss a"
            else -> "hh:mm a"
        }

        val formatter = DateTimeFormatter.ofPattern(pattern, Locale.US)
        return time.format(formatter)
    }

    fun formatDate(date: LocalDate): String {
        return date.format(DATE_FORMATTER)
    }

    fun formatDateShort(date: LocalDate): String {
        return date.format(DATE_SHORT_FORMATTER)
    }

    fun formatDateTime(
        dateTime: LocalDateTime,
        preference: TimeFormatPreference,
        includeSeconds: Boolean,
        context: Context? = null
    ): String {
        val dateStr = formatDateShort(dateTime.toLocalDate())
        val timeStr = formatTime(dateTime.toLocalTime(), preference, includeSeconds, context)
        return "$dateStr, $timeStr"
    }

    /**
     * Generates complete Copy Result text.
     */
    fun buildCopyResultText(
        startTimeFormatted: String,
        endTimeFormatted: String,
        humanDuration: String,
        digitalDuration: String,
        totalMinutes: Long,
        totalSeconds: Long,
        decimalHoursStr: String,
        startDateFormatted: String? = null,
        endDateFormatted: String? = null
    ): String {
        val sb = StringBuilder()
        sb.appendLine("AlarmGap")
        sb.appendLine()

        if (startDateFormatted != null && endDateFormatted != null && startDateFormatted != endDateFormatted) {
            sb.appendLine("Start:")
            sb.appendLine("$startDateFormatted $startTimeFormatted")
            sb.appendLine()
            sb.appendLine("End:")
            sb.appendLine("$endDateFormatted $endTimeFormatted")
        } else {
            sb.appendLine("Start:")
            sb.appendLine(startTimeFormatted)
            sb.appendLine()
            sb.appendLine("End:")
            sb.appendLine(endTimeFormatted)
        }
        sb.appendLine()
        sb.appendLine("Duration:")
        sb.appendLine(humanDuration)
        sb.appendLine()
        sb.appendLine("Digital:")
        sb.appendLine(digitalDuration)
        sb.appendLine()
        sb.appendLine("Total minutes:")
        sb.appendLine(totalMinutes.toString())
        sb.appendLine()
        val secondsFormatted = NumberFormat.getNumberInstance(Locale.US).format(totalSeconds)
        sb.appendLine("Total seconds:")
        sb.appendLine(secondsFormatted)
        sb.appendLine()
        sb.appendLine("Decimal hours:")
        sb.append(decimalHoursStr)

        return sb.toString()
    }

    /**
     * Generates Share Result text.
     */
    fun buildShareText(
        startTimeFormatted: String,
        endTimeFormatted: String,
        humanDuration: String,
        totalMinutes: Long
    ): String {
        val sb = StringBuilder()
        sb.appendLine("AlarmGap")
        sb.appendLine()
        sb.appendLine("$startTimeFormatted → $endTimeFormatted")
        sb.appendLine()
        sb.appendLine("Duration:")
        sb.appendLine(humanDuration)
        sb.appendLine()
        sb.append("Total:\n$totalMinutes minutes")
        return sb.toString()
    }
}
