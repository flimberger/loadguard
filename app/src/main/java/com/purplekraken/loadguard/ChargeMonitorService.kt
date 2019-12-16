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
        val batteryMonitor = app.batteryMonitor
        batteryMonitor.addListener(batteryListener)
        val text = createNotificationText(batteryMonitor)
        val notification = notificationController.createNotification(text)
        startForeground(SVC_NOTIFICATION_ID, notification)
        Log.d(TAG, "service started")
        return super.onStartCommand(intent, flags, startId)
    }

    private fun createNotificationText(batteryMonitor: BatteryMonitor): String {
        val level = batteryMonitor.chargingLevel
        val isCharging = batteryMonitor.isCharging
        return "$level %${if (isCharging) " (charging)" else ""}"
    }

    private val batteryListener: BatteryUpdateCallback = {
        val lvl = it.chargingLevel
        val isPowerConnected = it.isPowerConnected
        val app = application as LoadGuardApp
        val alarmController = app.alarmController
        if (isPowerConnected && lvl >= Settings.levelThreshold && !alarmController.isTriggered) {
            alarmController.triggerAlarm()
        } else if (alarmController.isTriggered && !isPowerConnected) {
            alarmController.dismiss()
        } else if (lvl < Settings.levelThreshold && alarmController.isTriggered) {
            alarmController.reset()
        }
        val text = createNotificationText(it)
        notificationController.updateNotification(text)
    }
}
