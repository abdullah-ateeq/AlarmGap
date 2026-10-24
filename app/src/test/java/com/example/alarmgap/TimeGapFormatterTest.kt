package com.example.alarmgap

import com.example.alarmgap.formatter.TimeGapFormatter
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Duration

class TimeGapFormatterTest {

    @Test
    fun testHumanReadableStandard() {
        val duration = Duration.ofHours(6).plusMinutes(15)
        assertEquals("6 hours 15 minutes", TimeGapFormatter.formatHumanReadable(duration))
    }

    @Test
    fun testHumanReadableMinutesOnly() {
        val duration = Duration.ofMinutes(45)
        assertEquals("45 minutes", TimeGapFormatter.formatHumanReadable(duration))
    }

    @Test
    fun testHumanReadableWithSeconds() {
        val duration = Duration.ofHours(2).plusMinutes(5).plusSeconds(30)
        assertEquals("2 hours 5 minutes 30 seconds", TimeGapFormatter.formatHumanReadable(duration, includeSeconds = true))
    }

    @Test
    fun testHumanReadableZero() {
        val duration = Duration.ZERO
        assertEquals("0 minutes", TimeGapFormatter.formatHumanReadable(duration))
    }

    @Test
    fun testDigitalDurationStandard() {
        val duration = Duration.ofHours(6).plusMinutes(15)
        assertEquals("06:15:00", TimeGapFormatter.formatDigital(duration))
    }

    @Test
    fun testDigitalDurationOver24Hours() {
        val duration = Duration.ofHours(27).plusMinutes(15)
        // Must not wrap to 03:15:00
        assertEquals("27:15:00", TimeGapFormatter.formatDigital(duration))
    }

    @Test
    fun testTotalMinutes() {
        val duration = Duration.ofMinutes(375)
        assertEquals("375 minutes", TimeGapFormatter.formatTotalMinutes(duration))
    }

    @Test
    fun testTotalSeconds() {
        val duration = Duration.ofSeconds(22500)
        assertEquals("22,500 seconds", TimeGapFormatter.formatTotalSeconds(duration))
    }

    @Test
    fun testDecimalHours() {
        assertEquals("6.25", TimeGapFormatter.formatDecimalHours(6.25))
        assertEquals("1.5", TimeGapFormatter.formatDecimalHours(1.5))
        assertEquals("0.75", TimeGapFormatter.formatDecimalHours(0.75))
    }
}
