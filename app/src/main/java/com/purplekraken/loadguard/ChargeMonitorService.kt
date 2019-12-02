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

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log

class ChargeMonitorService : Service() {
    companion object {
        private const val TAG = "ChargeMonitorService"

        private const val SVC_CHANNEL_ID = "com.purplekraken.loadguard.CHARGE_MONITOR_CHANNEL_ID"
        private const val SVC_NOTIFICATION_ID = 1001
    }

    private lateinit var notificationController: ServiceNotificationController

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "service created")
        val cname = getString(R.string.notification_channel_name)
        val cdesc = getString(R.string.notification_channel_description)
        notificationController =
            ServiceNotificationController(this, SVC_CHANNEL_ID, cname, cdesc, SVC_NOTIFICATION_ID)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val app = application as LoadGuardApp
        val batmon = app.batteryMonitor
        batmon.addListener(batteryListener)
        val lvl = batmon.chargingLevel
        val notification = notificationController.createNotification("$lvl %")
        startForeground(SVC_NOTIFICATION_ID, notification)
        Log.d(TAG, "service started")
        return super.onStartCommand(intent, flags, startId)
    }

    private var alarmTriggered = false
    private val batteryListener: BatteryUpdateCallback = {
        val lvl = it.chargingLevel
        val isPowerConnected = it.isPowerConnected
        if (!isPowerConnected && lvl >= LoadGuardApp.levelThreshold && !alarmTriggered) {
            (application as LoadGuardApp).alarmController.triggerAlarm()
            alarmTriggered = true
        } else {
            if (alarmTriggered && !isPowerConnected) {
                (application as LoadGuardApp).alarmController.dismiss()
            }
            notificationController.updateNotification("$lvl %")
        }
    }
}
