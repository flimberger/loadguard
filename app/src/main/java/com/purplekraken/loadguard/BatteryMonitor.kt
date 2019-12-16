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

package com.purplekraken.loadguard

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.util.Log

typealias BatteryUpdateCallback = (BatteryMonitor) -> Unit

class BatteryMonitor(private val ctx: Context) {
    companion object {
        private const val TAG = "BatteryMonitor"
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent != null) {
                when (intent.action) {
                    Intent.ACTION_POWER_CONNECTED -> {
                        isPowerConnected = true
                        Log.i(TAG, "power connected")
                        listeners.forEach { listener -> listener(this@BatteryMonitor) }
                    }
                    Intent.ACTION_POWER_DISCONNECTED -> {
                        isPowerConnected = false
                        Log.i(TAG, "power disconnected")
                        listeners.forEach { listener -> listener(this@BatteryMonitor) }
                    }
                    Intent.ACTION_BATTERY_CHANGED -> {
                        val level: Int = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                        val scale: Int = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                        chargingLevel = (level / scale.toFloat() * 100).toInt()

                        val status: Int = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                        isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING
                                || status == BatteryManager.BATTERY_STATUS_FULL

                        Log.i(TAG, "current level: $chargingLevel %")

                        listeners.forEach { listener -> listener(this@BatteryMonitor) }
                    }
                }
            }
        }
    }

    var chargingLevel = -1
        private set
    var isCharging = false
        private set
    var isPowerConnected = false
        private set

    private val listeners: MutableSet<BatteryUpdateCallback> = LinkedHashSet()

    fun addListener(l: BatteryUpdateCallback) {
        val wasEmpty = listeners.isEmpty()
        listeners.add(l)
        if (wasEmpty) {
            resume()
        }
    }

    fun removeListener(l: BatteryUpdateCallback) {
        listeners.remove(l)
        if (listeners.isEmpty()) {
            pause()
        }
    }

    private fun resume() {
        val intentFilter = IntentFilter()
        intentFilter.apply {
            addAction(Intent.ACTION_BATTERY_CHANGED)
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
        }
        ctx.registerReceiver(receiver, intentFilter)
    }

    private fun pause() {
        ctx.unregisterReceiver(receiver)
    }
}
