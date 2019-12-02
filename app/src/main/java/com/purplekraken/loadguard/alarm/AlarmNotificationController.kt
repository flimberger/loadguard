/*
 *
 *  * SPDX-License-Identifier: Apache-2.0
 *  *
 *  * Copyright 2019 Florian Limberger <flo@purplekraken.com>
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package com.purplekraken.loadguard.alarm

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.purplekraken.loadguard.MainActivity
import com.purplekraken.loadguard.NotificationControllerBase
import com.purplekraken.loadguard.R

class AlarmNotificationController(
    ctx: Context,
    private val channelId: String,
    channelName: String,
    channelDescr: String,
    notificationId: Int
) : NotificationControllerBase(ctx, channelId, channelName, channelDescr, notificationId) {
    companion object {
        const val ACTION_DISMISS = "com.purplekraken.loadguard.ACTION_DISMISS"
        const val EXTRA_NOTIFICATION_ID = "com.purplekraken.loadguard.EXTRA_NOTIFICATION_ID"
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
        val intent = Intent(ctx, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(ctx, 0, intent, 0)

        val n = NotificationCompat.Builder(ctx, channelId)
            .setSmallIcon(R.drawable.ic_shield_half_full_white_48dp)
            .setOnlyAlertOnce(true)
            .setAutoCancel(false)
            .setContentIntent(pendingIntent)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(true)
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
        super.showNotification(n)
    }
}
