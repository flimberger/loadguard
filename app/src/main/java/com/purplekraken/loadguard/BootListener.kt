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

class BootListener : BroadcastReceiver() {
    override fun onReceive(ctx: Context?, intent: Intent?) {
        if (ctx == null) return
        if (intent != null) {
            when (intent.action) {
                Intent.ACTION_BOOT_COMPLETED -> startMonitorService(ctx)
                Intent.ACTION_LOCKED_BOOT_COMPLETED -> startMonitorService(ctx)
            }
        }
    }

    private fun startMonitorService(ctx: Context) {
        (ctx.applicationContext as LoadGuardApp).startMonitorService()
    }
}
