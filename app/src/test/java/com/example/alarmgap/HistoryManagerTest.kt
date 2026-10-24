package com.example.alarmgap

import com.example.alarmgap.data.model.AlarmGapHistory
import com.example.alarmgap.data.repository.IAlarmGapPreferencesRepository
import com.example.alarmgap.history.HistoryManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FakePreferencesRepository : IAlarmGapPreferencesRepository {
    private var historyList: List<AlarmGapHistory> = emptyList()

    override fun loadHistory(): List<AlarmGapHistory> = historyList

    override fun saveHistory(historyList: List<AlarmGapHistory>) {
        this.historyList = historyList
    }

    override fun clearHistory() {
        this.historyList = emptyList()
    }
}

class HistoryManagerTest {

    private lateinit var fakeRepo: FakePreferencesRepository
    private lateinit var historyManager: HistoryManager

    @Before
    fun setUp() {
        fakeRepo = FakePreferencesRepository()
        historyManager = HistoryManager(fakeRepo)
    }

    @Test
    fun testHistoryLimitMax10Entries() {
        // Save 10 distinct calculations
        for (i in 1..10) {
            val saved = historyManager.saveEntry(
                startTime = "0$i:00",
                endTime = "1$i:00",
                startDate = "2026-10-24",
                endDate = "2026-10-24",
                calculationMode = "TIME_DIFFERENCE",
                durationSeconds = (i * 3600).toLong(),
                dayRelationship = "AUTOMATIC",
                includeSeconds = false
            )
            assertTrue(saved)
        }

        assertEquals(10, historyManager.getHistory().size)
        // Most recent is "010:00"
        assertEquals("010:00", historyManager.getHistory().first().startTime)

        // Save 11th entry
        val saved11 = historyManager.saveEntry(
            startTime = "11:00",
            endTime = "12:00",
            startDate = "2026-10-24",
            endDate = "2026-10-24",
            calculationMode = "TIME_DIFFERENCE",
            durationSeconds = 3600L,
            dayRelationship = "AUTOMATIC",
            includeSeconds = false
        )
        assertTrue(saved11)

        val historyAfter11 = historyManager.getHistory()
        // Must still be capped at 10 entries
        assertEquals(10, historyAfter11.size)
        // Newest entry is at the top
        assertEquals("11:00", historyAfter11.first().startTime)
        // Oldest entry "01:00" should have been removed
        assertFalse(historyAfter11.any { it.startTime == "01:00" })
    }

    @Test
    fun testConsecutiveDuplicatePrevention() {
        // First save
        val firstSave = historyManager.saveEntry(
            startTime = "08:30",
            endTime = "14:45",
            startDate = "2026-10-24",
            endDate = "2026-10-24",
            calculationMode = "TIME_DIFFERENCE",
            durationSeconds = 22500L,
            dayRelationship = "AUTOMATIC",
            includeSeconds = false
        )
        assertTrue(firstSave)
        assertEquals(1, historyManager.getHistory().size)

        // Duplicate consecutive save with exact same parameters
        val duplicateSave = historyManager.saveEntry(
            startTime = "08:30",
            endTime = "14:45",
            startDate = "2026-10-24",
            endDate = "2026-10-24",
            calculationMode = "TIME_DIFFERENCE",
            durationSeconds = 22500L,
            dayRelationship = "AUTOMATIC",
            includeSeconds = false
        )
        assertFalse(duplicateSave)
        assertEquals(1, historyManager.getHistory().size)
    }

    @Test
    fun testDeleteEntry() {
        historyManager.saveEntry(
            startTime = "08:00",
            endTime = "10:00",
            startDate = "2026-10-24",
            endDate = "2026-10-24",
            calculationMode = "TIME_DIFFERENCE",
            durationSeconds = 7200L,
            dayRelationship = "AUTOMATIC",
            includeSeconds = false
        )
        val entry = historyManager.getHistory().first()
        historyManager.deleteEntry(entry.id)
        assertTrue(historyManager.getHistory().isEmpty())
    }

    @Test
    fun testClearHistory() {
        historyManager.saveEntry(
            startTime = "08:00",
            endTime = "10:00",
            startDate = "2026-10-24",
            endDate = "2026-10-24",
            calculationMode = "TIME_DIFFERENCE",
            durationSeconds = 7200L,
            dayRelationship = "AUTOMATIC",
            includeSeconds = false
        )
        historyManager.saveEntry(
            startTime = "10:00",
            endTime = "12:00",
            startDate = "2026-10-24",
            endDate = "2026-10-24",
            calculationMode = "TIME_DIFFERENCE",
            durationSeconds = 7200L,
            dayRelationship = "AUTOMATIC",
            includeSeconds = false
        )
        assertEquals(2, historyManager.getHistory().size)
        historyManager.clearHistory()
        assertEquals(0, historyManager.getHistory().size)
    }
}
