/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2019 Florian Limberger <flo@purplekraken.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.purplekraken.loadguard.alarm

import android.content.Context
import android.os.Vibrator
import android.util.Log
import com.purplekraken.loadguard.LoadGuardApp
import com.purplekraken.loadguard.compat.VibrationEffectCompat
import com.purplekraken.loadguard.compat.VibratorCompat

/**
 * Controller for the alarm state
 *
 * Simple state machine:
 * <pre>
 * Armed -> Triggered -> Dismissed
 *   ^---------------------+
 * </pre>
 */
class AlarmController(private val ctx: Context) {
    companion object {
        private const val TAG = "AlarmController"
        private val VIBRATE_PATTERN = longArrayOf(500, 500)

        private const val ALARM_CHANNEL_ID = "com.purplekraken.loadguard.ALARM_CHANNEL_ID"
        private const val ALARM_NOTIFICATION_ID = 1002
    }

    private val notificationController: AlarmNotificationController

    init {
        notificationController = AlarmNotificationController(
            ctx,
            ALARM_CHANNEL_ID,
            "Charging threshold alarm",
            "Alarm triggered when the charging threshold is reached",
            ALARM_NOTIFICATION_ID
        )
    }

    var state = AlarmState.ARMED

    fun triggerAlarm() {
        when (state) {
            AlarmState.ARMED -> startAlarm()
            AlarmState.TRIGGERED -> Log.w(TAG, "BUG(state): alarm is already triggered")
            AlarmState.DISMISSED -> Log.w(TAG, "BUG(state): alarm is already dismissed")
        }
    }

    private fun startAlarm() {
        // TODO: acquire a CPU wakeLock if necessary
        state = AlarmState.TRIGGERED
        val batmon = (ctx.applicationContext as LoadGuardApp).batteryMonitor
        notificationController.showAlarmNotification(batmon.chargingLevel)
        RingtonePlayer.play(ctx)
        VibratorCompat.vibrate(
            getVibrator(),
            VibrationEffectCompat.createWaveForm(VIBRATE_PATTERN, 0)
        )
        Log.d(TAG, "alarm triggered")
    }

    /**
     * Silence the alarm
     *
     * This function <emph>does not</emph> clear the triggered state, as the next battery change
     * would trigger the alarm instead. To return into the initial state, use {@link #reset()}
     * instead.
     */
    fun dismiss() {
        when (state) {
            AlarmState.ARMED -> Log.d(TAG, "BUG(state): can't mute a not yet triggered alarm")
            AlarmState.TRIGGERED -> stopAlarm()
            AlarmState.DISMISSED -> Log.d(TAG, "BUG(state): alarm is already dismissed")
        }
    }

    private fun stopAlarm() {
        RingtonePlayer.stop(ctx)
        getVibrator().cancel()
        notificationController.hideNotification()
        state = AlarmState.DISMISSED
        Log.d(TAG, "alarm dismissed")
    }

    /**
     * Reset the internal state
     *
     * The internal state is reset to be neither triggered nor muted.
     */
    fun reset() {
        state = AlarmState.ARMED
    }

    private fun getVibrator(): Vibrator {
        return ctx.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    enum class AlarmState {
        ARMED,
        TRIGGERED,
        DISMISSED
    }
}
