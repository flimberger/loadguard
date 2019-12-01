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

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.purplekraken.loadguard.alarm.AlarmReceiver

class NotificationController private constructor(
    private val ctx: Context,
    private val channelId: String,
    private val notificationId: Int,
    private val builder: NotificationCompat.Builder
) {
    companion object {
        const val ACTION_DISMISS = "com.purplekraken.loadguard.ACTION_DISMISS"
        const val EXTRA_NOTIFICATION_ID = "com.purplekraken.loadguard.EXTRA_NOTIFICATION_ID"

        private fun createBuilder(ctx: Context, channelId: String): NotificationCompat.Builder {
            val intent = Intent(ctx, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val pendingIntent = PendingIntent.getActivity(ctx, 0, intent, 0)
            return NotificationCompat.Builder(ctx, channelId)
                .setSmallIcon(R.drawable.ic_shield_half_full_white_48dp)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setOnlyAlertOnce(true)
                .setAutoCancel(false)
                .setContentIntent(pendingIntent)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setOngoing(true)
        }
    }

    constructor(
        ctx: Context,
        channelId: String,
        notificationId: Int,
        channelName: String,
        channelDescr: String
    ) : this(ctx, channelId, notificationId, createBuilder(ctx, channelId)) {
        createNotificationChannel(ctx, channelId, channelName, channelDescr)
    }

    private fun createNotificationChannel(ctx: Context, id: String, name: String, descr: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(id, name, importance).apply {
                description = descr
            }
            val notificationMgr =
                ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationMgr.createNotificationChannel(channel)
        }
    }

    fun createNotification(text: String): Notification {
        return builder
            .setContentText(text)
            .build()
    }

    fun updateNotification(text: String) {
        val n = createNotification(text)
        showNotification(n)
    }

    private fun showNotification(n: Notification) {
        with(NotificationManagerCompat.from(ctx)) {
            notify(notificationId, n)
        }
    }

    fun showAlarmNotification(chargingLevel: Int) {
        val dismissIntent = Intent(ctx, AlarmReceiver::class.java).apply {
            action = ACTION_DISMISS
            putExtra(
                EXTRA_NOTIFICATION_ID,
                notificationId
            )
        }
        val pendingDismissIntent = PendingIntent.getBroadcast(ctx, 0, dismissIntent, 0)

        val n = createBuilder(ctx, channelId)
            .setContentTitle("$chargingLevel %")
            .setContentText("Charging threshold is reached")
            .addAction(
                R.drawable.ic_shield_half_full_black_24dp,
                ctx.getString(R.string.notification_action_dismiss),
                pendingDismissIntent
            )
            .setDefaults(NotificationCompat.DEFAULT_LIGHTS)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setWhen(0)
            .build()
        showNotification(n)
    }
}
