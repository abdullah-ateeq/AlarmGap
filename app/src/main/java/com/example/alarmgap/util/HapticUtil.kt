package com.example.alarmgap.util

import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType

object HapticUtil {

    fun performClickFeedback(haptic: HapticFeedback?, isEnabled: Boolean) {
        if (!isEnabled || haptic == null) return
        try {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        } catch (_: Exception) {}
    }

    fun performImpactFeedback(haptic: HapticFeedback?, isEnabled: Boolean) {
        if (!isEnabled || haptic == null) return
        try {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        } catch (_: Exception) {}
    }

    fun performViewTick(view: View?, isEnabled: Boolean) {
        if (!isEnabled || view == null) return
        try {
            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
        } catch (_: Exception) {}
    }
}
