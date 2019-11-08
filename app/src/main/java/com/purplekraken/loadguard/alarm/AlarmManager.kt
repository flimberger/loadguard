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
import com.purplekraken.loadguard.compat.VibrationEffectCompat
import com.purplekraken.loadguard.compat.VibratorCompat

class AlarmManager(private val ctx: Context) {
    companion object {
        private const val TAG = "AlarmManager"
        private val VIBRATE_PATTERN = longArrayOf(500, 500)
    }

    fun triggerAlarm() {
        Log.d(TAG, "alarm triggered")
        // TODO: acquire a CPU wakeLock if necessary
        RingtonePlayer.play(ctx)
        VibratorCompat.vibrate(
            getVibrator(),
            VibrationEffectCompat.createWaveForm(VIBRATE_PATTERN, 0)
        )
    }

    fun dismiss() {
        Log.d(TAG, "alarm dismissed")
        RingtonePlayer.stop(ctx)
        getVibrator().cancel()
    }

    private fun getVibrator(): Vibrator {
        return ctx.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

}
