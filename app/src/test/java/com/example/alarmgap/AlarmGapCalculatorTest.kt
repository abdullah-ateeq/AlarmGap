package com.example.alarmgap

import com.example.alarmgap.calculator.AlarmGapCalculator
import com.example.alarmgap.calculator.IdenticalTimesException
import com.example.alarmgap.data.model.DayRelationship
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class AlarmGapCalculatorTest {

    private lateinit var calculator: AlarmGapCalculator
    private val testDate: LocalDate = LocalDate.of(2026, 10, 24)

    @Before
    fun setUp() {
        calculator = AlarmGapCalculator()
    }

    @Test
    fun testBasicSameDay() {
        val start = LocalTime.of(8, 0)
        val end = LocalTime.of(10, 0)
        val duration = calculator.calculateDuration(testDate, start, end, DayRelationship.SAME_DAY)
        assertEquals(Duration.ofHours(2), duration)
        assertEquals(2L, duration.toHours())
    }

    @Test
    fun testMinutes() {
        val start = LocalTime.of(8, 15)
        val end = LocalTime.of(9, 45)
        val duration = calculator.calculateDuration(testDate, start, end, DayRelationship.SAME_DAY)
        assertEquals(Duration.ofMinutes(90), duration)
        assertEquals(1L, duration.toHours())
        assertEquals(30L, duration.toMinutes() % 60)
    }

    @Test
    fun testSeconds() {
        val start = LocalTime.of(8, 15, 30)
        val end = LocalTime.of(9, 45, 45)
        val duration = calculator.calculateDuration(testDate, start, end, DayRelationship.SAME_DAY)
        val expected = Duration.ofHours(1).plusMinutes(30).plusSeconds(15)
        assertEquals(expected, duration)
        assertEquals(1L, duration.toHours())
        assertEquals(30L, duration.toMinutes() % 60)
        assertEquals(15L, duration.seconds % 60)
    }

    @Test
    fun test12PmNoon() {
        // 11:00 AM to 12:00 PM (noon)
        val start = LocalTime.of(11, 0)
        val end = LocalTime.of(12, 0)
        val duration = calculator.calculateDuration(testDate, start, end, DayRelationship.SAME_DAY)
        assertEquals(Duration.ofHours(1), duration)
    }

    @Test
    fun test12AmMidnightNextDay() {
        // 11:00 PM to 12:00 AM (midnight next day)
        val start = LocalTime.of(23, 0)
        val end = LocalTime.of(0, 0)
        val duration = calculator.calculateDuration(testDate, start, end, DayRelationship.NEXT_DAY)
        assertEquals(Duration.ofHours(1), duration)
    }

    @Test
    fun testMidnightNextDay() {
        // 10:00 PM to 02:00 AM Next Day
        val start = LocalTime.of(22, 0)
        val end = LocalTime.of(2, 0)
        val duration = calculator.calculateDuration(testDate, start, end, DayRelationship.NEXT_DAY)
        assertEquals(Duration.ofHours(4), duration)
    }

    @Test
    fun testOverMidnightWithMinutes() {
        // 11:45 PM to 01:15 AM Next Day
        val start = LocalTime.of(23, 45)
        val end = LocalTime.of(1, 15)
        val duration = calculator.calculateDuration(testDate, start, end, DayRelationship.NEXT_DAY)
        val expected = Duration.ofHours(1).plusMinutes(30)
        assertEquals(expected, duration)
    }

    @Test
    fun testExactSameTimeSameDay() {
        // 08:00 to 08:00 Same Day -> 0 minutes
        val start = LocalTime.of(8, 0)
        val end = LocalTime.of(8, 0)
        val duration = calculator.calculateDuration(testDate, start, end, DayRelationship.SAME_DAY)
        assertEquals(Duration.ZERO, duration)
    }

    @Test
    fun testExactSameTimeNextDay() {
        // 08:00 to 08:00 Next Day -> 24 hours
        val start = LocalTime.of(8, 0)
        val end = LocalTime.of(8, 0)
        val duration = calculator.calculateDuration(testDate, start, end, DayRelationship.NEXT_DAY)
        assertEquals(Duration.ofHours(24), duration)
    }

    @Test
    fun testAutomaticEarlierEnd() {
        // 10:00 PM to 02:00 AM Automatic -> 4 hours (interpreted as next day)
        val start = LocalTime.of(22, 0)
        val end = LocalTime.of(2, 0)
        val duration = calculator.calculateDuration(testDate, start, end, DayRelationship.AUTOMATIC)
        assertEquals(Duration.ofHours(4), duration)
    }

    @Test
    fun testAutomaticLaterEnd() {
        // 08:00 AM to 05:00 PM Automatic -> 9 hours (interpreted as same day)
        val start = LocalTime.of(8, 0)
        val end = LocalTime.of(17, 0)
        val duration = calculator.calculateDuration(testDate, start, end, DayRelationship.AUTOMATIC)
        assertEquals(Duration.ofHours(9), duration)
    }

    @Test
    fun testAutomaticSameTimeThrows() {
        // 08:00 to 08:00 Automatic -> Require explicit interpretation
        val start = LocalTime.of(8, 0)
        val end = LocalTime.of(8, 0)
        assertThrows(IdenticalTimesException::class.java) {
            calculator.calculateDuration(testDate, start, end, DayRelationship.AUTOMATIC)
        }
    }

    @Test
    fun testFullDay() {
        // 12:00 AM to 12:00 AM Next Day -> 24 hours, 1440 minutes, 86400 seconds
        val start = LocalTime.of(0, 0)
        val end = LocalTime.of(0, 0)
        val duration = calculator.calculateDuration(testDate, start, end, DayRelationship.NEXT_DAY)
        assertEquals(Duration.ofHours(24), duration)
        assertEquals(24L, duration.toHours())
        assertEquals(1440L, calculator.calculateTotalMinutes(duration))
        assertEquals(86400L, calculator.calculateTotalSeconds(duration))
    }

    @Test
    fun testLongDuration() {
        // Start: 08:00 AM, Duration: 27 hours -> End: 11:00 AM next day
        val startDateTime = LocalDateTime.of(testDate, LocalTime.of(8, 0))
        val duration = Duration.ofHours(27)
        val endDateTime = calculator.calculateEndTime(startDateTime, duration)
        assertEquals(LocalDate.of(2026, 10, 25), endDateTime.toLocalDate())
        assertEquals(LocalTime.of(11, 0), endDateTime.toLocalTime())
    }

    @Test
    fun testDecimalHours() {
        // 6 hours 15 minutes -> 6.25
        val duration6h15m = Duration.ofHours(6).plusMinutes(15)
        assertEquals(6.25, calculator.calculateDecimalHours(duration6h15m), 0.0001)

        // 45 minutes -> 0.75
        val duration45m = Duration.ofMinutes(45)
        assertEquals(0.75, calculator.calculateDecimalHours(duration45m), 0.0001)

        // 90 minutes -> 1.5
        val duration90m = Duration.ofMinutes(90)
        assertEquals(1.5, calculator.calculateDecimalHours(duration90m), 0.0001)
    }

    @Test
    fun testTotalMinutes() {
        // 2 hours 30 minutes -> 150 minutes
        val duration = Duration.ofHours(2).plusMinutes(30)
        assertEquals(150L, calculator.calculateTotalMinutes(duration))
    }

    @Test
    fun testTotalSeconds() {
        // 1 hour -> 3600 seconds
        val duration = Duration.ofHours(1)
        assertEquals(3600L, calculator.calculateTotalSeconds(duration))
    }

    @Test
    fun testPreviousDayCalculation() {
        // Start: 02:00 AM, End: 10:00 PM Previous Day -> 4 hours
        val start = LocalTime.of(2, 0)
        val end = LocalTime.of(22, 0)
        val duration = calculator.calculateDuration(testDate, start, end, DayRelationship.PREVIOUS_DAY)
        assertEquals(Duration.ofHours(4), duration)
    }

    @Test
    fun testSameDayEarlierEndThrows() {
        // 10:00 AM to 08:00 AM Same Day -> throws error
        val start = LocalTime.of(10, 0)
        val end = LocalTime.of(8, 0)
        assertThrows(IllegalArgumentException::class.java) {
            calculator.calculateDuration(testDate, start, end, DayRelationship.SAME_DAY)
        }
    }

    @Test
    fun testSwapRecalculation() {
        // Initially 08:00 -> 10:00 Same Day (2 hours)
        var start = LocalTime.of(8, 0)
        var end = LocalTime.of(10, 0)
        var duration = calculator.calculateDuration(testDate, start, end, DayRelationship.AUTOMATIC)
        assertEquals(Duration.ofHours(2), duration)

        // Swap: Start 10:00, End 08:00
        val temp = start
        start = end
        end = temp

        // In Automatic, end < start is interpreted as Next Day (22 hours)
        duration = calculator.calculateDuration(testDate, start, end, DayRelationship.AUTOMATIC)
        assertEquals(Duration.ofHours(22), duration)
    }

    @Test
    fun testDurationToEndCalculation() {
        // Start: 08:30 AM, Duration: 6h 15m -> End: 02:45 PM
        val startDateTime = LocalDateTime.of(testDate, LocalTime.of(8, 30))
        val duration = Duration.ofHours(6).plusMinutes(15)
        val endDateTime = calculator.calculateEndTime(startDateTime, duration)
        assertEquals(LocalTime.of(14, 45), endDateTime.toLocalTime())
    }

    @Test
    fun testDurationToStartCalculation() {
        // End: 02:45 PM, Duration: 6h 15m -> Start: 08:30 AM
        val endDateTime = LocalDateTime.of(testDate, LocalTime.of(14, 45))
        val duration = Duration.ofHours(6).plusMinutes(15)
        val startDateTime = calculator.calculateStartTime(endDateTime, duration)
        assertEquals(LocalTime.of(8, 30), startDateTime.toLocalTime())
    }

    @Test
    fun testDurationBoundsValidation() {
        // Zero duration should fail validation
        assertThrows(IllegalArgumentException::class.java) {
            calculator.validateDurationInput(Duration.ZERO)
        }
        // Duration over 365 days should fail validation
        assertThrows(IllegalArgumentException::class.java) {
            calculator.validateDurationInput(Duration.ofDays(366))
        }
    }
}
