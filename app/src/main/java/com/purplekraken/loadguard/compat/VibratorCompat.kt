package com.purplekraken.loadguard.compat

import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator

class VibratorCompat {
    companion object {
        fun vibrate(vibrator: Vibrator, effect: VibrationEffectCompat) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(effect.timings, effect.repeat))
            } else {
                vibrator.vibrate(effect.timings, effect.repeat)
            }
        }
    }
}
