package com.purplekraken.loadguard

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat

class ChargeMonitorService : Service() {
    companion object {
        private const val TAG = "ChargeMonitorService"
        private const val SVC_CHANNEL_ID = "com.purplekraken.loadguard.CHARGE_MONITOR_CHANNEL_ID"
        private const val SVC_NOTIFICATION_ID = 1001
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "service created")
        createNotificationChannel()
        (application as LoadGuardApp).batteryMonitor.addListener(object: BatteryMonitor.UpdateListener {
            override fun onUpdate(batteryMonitor: BatteryMonitor) {
                if (!batteryMonitor.isCharging) {
                    stopSelf()
                }
            }
        })
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(SVC_NOTIFICATION_ID, createNotification())
        Log.d(TAG, "service started")
        return super.onStartCommand(intent, flags, startId)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.notification_channel_name)
            val descr = getString(R.string.notification_channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(SVC_CHANNEL_ID, name, importance).apply {
                description = descr
            }
            val notificationMgr: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationMgr.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, SVC_CHANNEL_ID)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(getString(R.string.notification_text))
            .setSmallIcon(R.drawable.ic_shield_half_full_black_48dp)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
    }
}
