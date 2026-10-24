package com.example.alarmgap.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.alarmgap.data.model.AlarmGapHistory
import com.example.alarmgap.data.model.CalculationMode
import com.example.alarmgap.data.model.DayRelationship
import com.example.alarmgap.data.model.ThemePreference
import com.example.alarmgap.data.model.TimeFormatPreference
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.LocalTime

interface IAlarmGapPreferencesRepository {
    fun loadHistory(): List<AlarmGapHistory>
    fun saveHistory(historyList: List<AlarmGapHistory>)
    fun clearHistory()
}

class AlarmGapPreferencesRepository(context: Context) : IAlarmGapPreferencesRepository {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "alarm_gap_preferences"

        private const val KEY_THEME = "key_theme"
        private const val KEY_DYNAMIC_COLOR = "key_dynamic_color"
        private const val KEY_TIME_FORMAT = "key_time_format"
        private const val KEY_SHOW_SECONDS = "key_show_seconds"
        private const val KEY_CALCULATION_MODE = "key_calculation_mode"
        private const val KEY_DAY_RELATIONSHIP = "key_day_relationship"
        private const val KEY_LIVE_CALCULATION = "key_live_calculation"
        private const val KEY_SHOW_DATE_CONTEXT = "key_show_date_context"

        private const val KEY_SHOW_BREAKDOWN = "key_show_breakdown"
        private const val KEY_SHOW_DIGITAL = "key_show_digital"
        private const val KEY_SHOW_TOTAL_MINUTES = "key_show_total_minutes"
        private const val KEY_SHOW_TOTAL_SECONDS = "key_show_total_seconds"
        private const val KEY_SHOW_DECIMAL_HOURS = "key_show_decimal_hours"

        private const val KEY_HAPTICS = "key_haptics"
        private const val KEY_ANIMATIONS = "key_animations"
        private const val KEY_CONFIRM_DELETE = "key_confirm_delete"
        private const val KEY_RESTORE_LAST_CONFIG = "key_restore_last_config"

        private const val KEY_LAST_START_TIME = "key_last_start_time"
        private const val KEY_LAST_END_TIME = "key_last_end_time"
        private const val KEY_LAST_START_DATE = "key_last_start_date"

        private const val KEY_HISTORY_JSON = "key_history_json"
    }

    var theme: ThemePreference
        get() = ThemePreference.valueOf(
            prefs.getString(KEY_THEME, ThemePreference.SYSTEM.name) ?: ThemePreference.SYSTEM.name
        )
        set(value) = prefs.edit().putString(KEY_THEME, value.name).apply()

    var dynamicColor: Boolean
        get() = prefs.getBoolean(KEY_DYNAMIC_COLOR, true)
        set(value) = prefs.edit().putBoolean(KEY_DYNAMIC_COLOR, value).apply()

    var timeFormat: TimeFormatPreference
        get() = TimeFormatPreference.valueOf(
            prefs.getString(KEY_TIME_FORMAT, TimeFormatPreference.SYSTEM.name)
                ?: TimeFormatPreference.SYSTEM.name
        )
        set(value) = prefs.edit().putString(KEY_TIME_FORMAT, value.name).apply()

    var showSeconds: Boolean
        get() = prefs.getBoolean(KEY_SHOW_SECONDS, false)
        set(value) = prefs.edit().putBoolean(KEY_SHOW_SECONDS, value).apply()

    var calculationMode: CalculationMode
        get() = CalculationMode.valueOf(
            prefs.getString(KEY_CALCULATION_MODE, CalculationMode.TIME_DIFFERENCE.name)
                ?: CalculationMode.TIME_DIFFERENCE.name
        )
        set(value) = prefs.edit().putString(KEY_CALCULATION_MODE, value.name).apply()

    var dayRelationship: DayRelationship
        get() = DayRelationship.valueOf(
            prefs.getString(KEY_DAY_RELATIONSHIP, DayRelationship.AUTOMATIC.name)
                ?: DayRelationship.AUTOMATIC.name
        )
        set(value) = prefs.edit().putString(KEY_DAY_RELATIONSHIP, value.name).apply()

    var liveCalculation: Boolean
        get() = prefs.getBoolean(KEY_LIVE_CALCULATION, true)
        set(value) = prefs.edit().putBoolean(KEY_LIVE_CALCULATION, value).apply()

    var showDateContext: Boolean
        get() = prefs.getBoolean(KEY_SHOW_DATE_CONTEXT, true)
        set(value) = prefs.edit().putBoolean(KEY_SHOW_DATE_CONTEXT, value).apply()

    var showDurationBreakdown: Boolean
        get() = prefs.getBoolean(KEY_SHOW_BREAKDOWN, true)
        set(value) = prefs.edit().putBoolean(KEY_SHOW_BREAKDOWN, value).apply()

    var showDigitalDuration: Boolean
        get() = prefs.getBoolean(KEY_SHOW_DIGITAL, true)
        set(value) = prefs.edit().putBoolean(KEY_SHOW_DIGITAL, value).apply()

    var showTotalMinutes: Boolean
        get() = prefs.getBoolean(KEY_SHOW_TOTAL_MINUTES, true)
        set(value) = prefs.edit().putBoolean(KEY_SHOW_TOTAL_MINUTES, value).apply()

    var showTotalSeconds: Boolean
        get() = prefs.getBoolean(KEY_SHOW_TOTAL_SECONDS, true)
        set(value) = prefs.edit().putBoolean(KEY_SHOW_TOTAL_SECONDS, value).apply()

    var showDecimalHours: Boolean
        get() = prefs.getBoolean(KEY_SHOW_DECIMAL_HOURS, true)
        set(value) = prefs.edit().putBoolean(KEY_SHOW_DECIMAL_HOURS, value).apply()

    var hapticFeedback: Boolean
        get() = prefs.getBoolean(KEY_HAPTICS, true)
        set(value) = prefs.edit().putBoolean(KEY_HAPTICS, value).apply()

    var animations: Boolean
        get() = prefs.getBoolean(KEY_ANIMATIONS, true)
        set(value) = prefs.edit().putBoolean(KEY_ANIMATIONS, value).apply()

    var confirmDelete: Boolean
        get() = prefs.getBoolean(KEY_CONFIRM_DELETE, true)
        set(value) = prefs.edit().putBoolean(KEY_CONFIRM_DELETE, value).apply()

    var restoreLastConfiguration: Boolean
        get() = prefs.getBoolean(KEY_RESTORE_LAST_CONFIG, true)
        set(value) = prefs.edit().putBoolean(KEY_RESTORE_LAST_CONFIG, value).apply()

    var lastStartTime: LocalTime
        get() {
            val str = prefs.getString(KEY_LAST_START_TIME, null)
            return if (str != null) runCatching { LocalTime.parse(str) }.getOrDefault(LocalTime.of(8, 0, 0))
            else LocalTime.of(8, 0, 0)
        }
        set(value) = prefs.edit().putString(KEY_LAST_START_TIME, value.toString()).apply()

    var lastEndTime: LocalTime
        get() {
            val str = prefs.getString(KEY_LAST_END_TIME, null)
            return if (str != null) runCatching { LocalTime.parse(str) }.getOrDefault(LocalTime.of(17, 0, 0))
            else LocalTime.of(17, 0, 0)
        }
        set(value) = prefs.edit().putString(KEY_LAST_END_TIME, value.toString()).apply()

    var lastStartDate: LocalDate
        get() {
            val str = prefs.getString(KEY_LAST_START_DATE, null)
            return if (str != null) runCatching { LocalDate.parse(str) }.getOrDefault(LocalDate.now())
            else LocalDate.now()
        }
        set(value) = prefs.edit().putString(KEY_LAST_START_DATE, value.toString()).apply()

    fun resetSettingsToDefaults() {
        prefs.edit()
            .putString(KEY_THEME, ThemePreference.SYSTEM.name)
            .putBoolean(KEY_DYNAMIC_COLOR, true)
            .putString(KEY_TIME_FORMAT, TimeFormatPreference.SYSTEM.name)
            .putBoolean(KEY_SHOW_SECONDS, false)
            .putString(KEY_CALCULATION_MODE, CalculationMode.TIME_DIFFERENCE.name)
            .putString(KEY_DAY_RELATIONSHIP, DayRelationship.AUTOMATIC.name)
            .putBoolean(KEY_LIVE_CALCULATION, true)
            .putBoolean(KEY_SHOW_DATE_CONTEXT, true)
            .putBoolean(KEY_SHOW_BREAKDOWN, true)
            .putBoolean(KEY_SHOW_DIGITAL, true)
            .putBoolean(KEY_SHOW_TOTAL_MINUTES, true)
            .putBoolean(KEY_SHOW_TOTAL_SECONDS, true)
            .putBoolean(KEY_SHOW_DECIMAL_HOURS, true)
            .putBoolean(KEY_HAPTICS, true)
            .putBoolean(KEY_ANIMATIONS, true)
            .putBoolean(KEY_CONFIRM_DELETE, true)
            .putBoolean(KEY_RESTORE_LAST_CONFIG, true)
            .apply()
    }

    override fun loadHistory(): List<AlarmGapHistory> {
        val jsonStr = prefs.getString(KEY_HISTORY_JSON, null) ?: return emptyList()
        return try {
            val jsonArray = JSONArray(jsonStr)
            val list = mutableListOf<AlarmGapHistory>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    AlarmGapHistory(
                        id = obj.optString("id", System.currentTimeMillis().toString()),
                        startTime = obj.getString("startTime"),
                        endTime = obj.getString("endTime"),
                        startDate = if (obj.has("startDate") && !obj.isNull("startDate")) obj.getString("startDate") else null,
                        endDate = if (obj.has("endDate") && !obj.isNull("endDate")) obj.getString("endDate") else null,
                        calculationMode = obj.optString("calculationMode", CalculationMode.TIME_DIFFERENCE.name),
                        durationSeconds = obj.getLong("durationSeconds"),
                        savedAt = obj.getLong("savedAt"),
                        dayRelationship = obj.optString("dayRelationship", DayRelationship.AUTOMATIC.name),
                        includeSeconds = obj.optBoolean("includeSeconds", false)
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    override fun saveHistory(historyList: List<AlarmGapHistory>) {
        val jsonArray = JSONArray()
        // Save max 10
        val limitedList = historyList.take(10)
        for (item in limitedList) {
            val obj = JSONObject().apply {
                put("id", item.id)
                put("startTime", item.startTime)
                put("endTime", item.endTime)
                put("startDate", item.startDate)
                put("endDate", item.endDate)
                put("calculationMode", item.calculationMode)
                put("durationSeconds", item.durationSeconds)
                put("savedAt", item.savedAt)
                put("dayRelationship", item.dayRelationship)
                put("includeSeconds", item.includeSeconds)
            }
            jsonArray.put(obj)
        }
        prefs.edit().putString(KEY_HISTORY_JSON, jsonArray.toString()).apply()
    }

    override fun clearHistory() {
        prefs.edit().remove(KEY_HISTORY_JSON).apply()
    }
}
