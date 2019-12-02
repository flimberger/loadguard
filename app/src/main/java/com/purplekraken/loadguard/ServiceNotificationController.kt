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
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat

class ServiceNotificationController(
    ctx: Context,
    channelId: String,
    channelName: String,
    channelDescr: String,
    notificationId: Int
) : NotificationControllerBase(ctx, channelId, channelName, channelDescr, notificationId) {
    companion object {
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
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setOngoing(true)
        }
    }

    private val builder: NotificationCompat.Builder

    init {
        builder = createBuilder(ctx, channelId)
    }

    fun createNotification(text: String): Notification {
        return builder
            .setContentText(text)
            .build()
    }

    fun updateNotification(text: String) {
        val n = createNotification(text)
        super.showNotification(n)
    }
}
