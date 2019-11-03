package com.purplekraken.loadguard

import android.app.Application

class LoadGuardApp : Application() {
    private var _batteryMonitor: BatteryMonitor? = null
    val batteryMonitor: BatteryMonitor
        get() {
            if (_batteryMonitor == null) {
                _batteryMonitor = BatteryMonitor(this)
            }
            return _batteryMonitor ?: throw AssertionError("backing property was set to null")
        }
}