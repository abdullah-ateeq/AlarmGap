package com.example.alarmgap.history

import com.example.alarmgap.data.model.AlarmGapHistory
import com.example.alarmgap.data.repository.IAlarmGapPreferencesRepository
import java.util.UUID

class HistoryManager(private val repository: IAlarmGapPreferencesRepository) {

    fun getHistory(): List<AlarmGapHistory> {
        return repository.loadHistory()
    }

    /**
     * Adds an entry to history.
     * Prevents consecutive duplicates and enforces a maximum of 10 items.
     * Returns true if entry was saved, false if skipped as duplicate.
     */
    fun saveEntry(
        startTime: String,
        endTime: String,
        startDate: String?,
        endDate: String?,
        calculationMode: String,
        durationSeconds: Long,
        dayRelationship: String,
        includeSeconds: Boolean
    ): Boolean {
        val currentHistory = repository.loadHistory().toMutableList()

        // Check consecutive duplicate (compare with the most recently saved entry)
        if (currentHistory.isNotEmpty()) {
            val latest = currentHistory.first()
            val isDuplicate = latest.startTime == startTime &&
                    latest.endTime == endTime &&
                    latest.startDate == startDate &&
                    latest.endDate == endDate &&
                    latest.calculationMode == calculationMode &&
                    latest.dayRelationship == dayRelationship &&
                    latest.includeSeconds == includeSeconds

            if (isDuplicate) {
                return false
            }
        }

        val newEntry = AlarmGapHistory(
            id = UUID.randomUUID().toString(),
            startTime = startTime,
            endTime = endTime,
            startDate = startDate,
            endDate = endDate,
            calculationMode = calculationMode,
            durationSeconds = durationSeconds,
            savedAt = System.currentTimeMillis(),
            dayRelationship = dayRelationship,
            includeSeconds = includeSeconds
        )

        // Add to the front
        currentHistory.add(0, newEntry)

        // Enforce maximum of 10 entries (removes oldest)
        val cappedList = currentHistory.take(10)
        repository.saveHistory(cappedList)
        return true
    }

    fun deleteEntry(id: String) {
        val currentHistory = repository.loadHistory().toMutableList()
        currentHistory.removeAll { it.id == id }
        repository.saveHistory(currentHistory)
    }

    fun clearHistory() {
        repository.clearHistory()
    }
}
