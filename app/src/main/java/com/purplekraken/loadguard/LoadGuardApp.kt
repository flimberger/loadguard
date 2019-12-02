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

import android.app.Application
import android.content.Intent
import android.os.Build
import android.util.Log
import com.purplekraken.loadguard.alarm.AlarmController

class LoadGuardApp : Application() {
    companion object {
        private const val TAG = "LoadGuardApp"

        const val levelThreshold = 80
    }

    lateinit var alarmController: AlarmController
    lateinit var batteryMonitor: BatteryMonitor

    override fun onCreate() {
        super.onCreate()
        alarmController = AlarmController(this)
        batteryMonitor = BatteryMonitor(this)
    }

    fun startMonitorService() {
        Log.d(TAG, "starting monitor service")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(Intent(this, ChargeMonitorService::class.java))
        } else {
            startService(Intent(this, ChargeMonitorService::class.java))
        }
    }
}
