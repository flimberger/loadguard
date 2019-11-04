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
