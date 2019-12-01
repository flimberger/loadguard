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

package com.purplekraken.loadguard.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.purplekraken.loadguard.AsyncHandler
import com.purplekraken.loadguard.LoadGuardApp
import com.purplekraken.loadguard.NotificationController

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(ctx: Context?, intent: Intent?) {
        val result = goAsync()
        AsyncHandler.post {
            handleIntent(ctx, intent)
            result.finish()
        }
    }
}

private const val TAG = "AlarmReceiver"

private fun handleIntent(ctx: Context?, intent: Intent?) {
    if (intent == null) {
        Log.w(TAG, "received null intent")
        return
    }
    if (ctx == null) {
        Log.w(TAG, "received null context")
        return
    }

    when (intent.action) {
        NotificationController.ACTION_DISMISS -> {
            val app = ctx.applicationContext as LoadGuardApp
            app.alarmManager.dismiss()
        }
    }
}
