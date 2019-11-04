package com.purplekraken.loadguard

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.util.Log

class BatteryMonitor(private val ctx: Context) {
    companion object {
        private const val TAG = "BatteryMonitor"
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent != null) {
                val level: Int = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale: Int = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                chargingLevel = (level / scale.toFloat() * 100).toInt()
                Log.i(TAG, "current level: $chargingLevel %")

                val status: Int = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING
                        || status == BatteryManager.BATTERY_STATUS_FULL
                listeners.forEach { listener -> listener.onUpdate(this@BatteryMonitor) }
            }
        }
    }

    var chargingLevel = -1
    var isCharging = false

    private val listeners: MutableSet<UpdateListener> = LinkedHashSet()

    fun addListener(l: UpdateListener) {
        val wasEmpty = listeners.isEmpty()
        listeners.add(l)
        if (wasEmpty) {
            resume()
        }
    }

    fun removeListener(l: UpdateListener) {
        listeners.remove(l)
        if (listeners.isEmpty()) {
            pause()
        }
    }

    private fun resume() {
        ctx.registerReceiver(receiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    }

    private fun pause() {
        ctx.unregisterReceiver(receiver)
    }

    interface UpdateListener {
        fun onUpdate(batteryMonitor: BatteryMonitor)
    }
}
