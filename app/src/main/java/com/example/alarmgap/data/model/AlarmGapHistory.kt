package com.example.alarmgap.data.model

data class AlarmGapHistory(
    val id: String,
    val startTime: String,
    val endTime: String,
    val startDate: String?,
    val endDate: String?,
    val calculationMode: String,
    val durationSeconds: Long,
    val savedAt: Long,
    val dayRelationship: String = DayRelationship.AUTOMATIC.name,
    val includeSeconds: Boolean = false
)
