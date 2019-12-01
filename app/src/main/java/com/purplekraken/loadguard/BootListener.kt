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

// This won't be triggered on API level >= 26, but it is needed for backwards compatibility with
// API level < 23 (before JobScheduler was available)
// Instead, we listen to ACTION_LOCKED_BOOT_COMPLETED and trigger a `JobScheduler`, but since this
// is only available on API level >= 24, which means that API level == 23 needs
// ACTION_BOOT_COMPLETED.
// TODO: use setPersisted on the Job instead
class PowerConnectionListener : BroadcastReceiver() {
    override fun onReceive(ctx: Context?, intent: Intent?) {
        if (ctx == null) return
        if (intent != null) {
            when (intent.action) {
                Intent.ACTION_BOOT_COMPLETED -> scheduleJob(ctx)
                Intent.ACTION_LOCKED_BOOT_COMPLETED -> scheduleJob(ctx)
                Intent.ACTION_POWER_CONNECTED -> startService(ctx)
                Intent.ACTION_POWER_DISCONNECTED -> stopService(ctx)
            }
        }
    }

    private fun startService(ctx: Context) {
        (ctx.applicationContext as LoadGuardApp).startMonitorService()
    }

    private fun stopService(ctx: Context) {
        (ctx.applicationContext as LoadGuardApp).stopMonitorService()
    }

    private fun scheduleJob(ctx: Context) {
        (ctx.applicationContext as LoadGuardApp).scheduleJob()
    }
}
