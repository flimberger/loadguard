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

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private var statusText: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        statusText = findViewById(R.id.status_text)
    }

    private val batteryUpdateListener: BatteryUpdateCallback = {
        val batteryMonitor = it
        statusText?.apply {
            text = if (it.chargingLevel == -1) {
                getString(R.string.status_text_initial)
            } else {
                "${batteryMonitor.chargingLevel} % (${if (batteryMonitor.isCharging) "charging" else "not charging"})"
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val batteryMonitor = (application as LoadGuardApp).batteryMonitor
        batteryMonitor.addListener(batteryUpdateListener)
    }

    override fun onPause() {
        super.onPause()
        val batteryMonitor = (application as LoadGuardApp).batteryMonitor
        batteryMonitor.removeListener(batteryUpdateListener)
    }
}
