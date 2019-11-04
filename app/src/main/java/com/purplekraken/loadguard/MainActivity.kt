package com.purplekraken.loadguard

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private var statusText: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (applicationContext as LoadGuardApp).scheduleJob()
        setContentView(R.layout.activity_main)
        statusText = findViewById(R.id.status_text)
    }

    private val batteryUpdateListener = object : BatteryMonitor.UpdateListener {
        override fun onUpdate(batteryMonitor: BatteryMonitor) {
            statusText?.apply {
                text = if (batteryMonitor.chargingLevel == -1) {
                    getString(R.string.status_text_initial)
                } else {
                    "${batteryMonitor.chargingLevel} % (${if (batteryMonitor.isCharging) "charging" else "not charging"})"
                }
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
