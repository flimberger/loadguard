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

package com.purplekraken.loadguard

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationManagerCompat

abstract class NotificationControllerBase(
    protected val ctx: Context,
    channelId: String,
    channelName: String,
    channelDescr: String,
    protected val notificationId: Int
) {
    companion object {
        private fun createNotificationChannel(
            ctx: Context,
            id: String,
            name: String,
            descr: String
        ) {
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
    }

    init {
        createNotificationChannel(ctx, channelId, channelName, channelDescr)
    }

    protected fun showNotification(n: Notification) {
        with(NotificationManagerCompat.from(ctx)) {
            notify(notificationId, n)
        }
    }

    fun hideNotification() {
        with(NotificationManagerCompat.from(ctx)) {
            cancel(notificationId)
        }
    }
}
