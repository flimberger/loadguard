package com.purplekraken.loadguard

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

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
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private val batteryListener: BatteryUpdateCallback = {
        if (!it.isCharging) {
            doStopSelf()
        } else {
            updateNotification(it.chargingLevel)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val batmon = (application as LoadGuardApp).batteryMonitor
        batmon.addListener(batteryListener)
        val lvl = batmon.chargingLevel
        startForeground(SVC_NOTIFICATION_ID, createNotification(lvl))
        lastBatteryLevel = lvl
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

    private var notificationBuilder: NotificationCompat.Builder? = null

    private fun createNotification(chargingLevel: Int): Notification {
        val builder = notificationBuilder ?: {
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val pendingIntent = PendingIntent.getActivity(this, 0, intent, 0)
            val builder = NotificationCompat.Builder(this, SVC_CHANNEL_ID)
                .setContentText(getString(R.string.notification_text))
                .setSmallIcon(R.drawable.ic_shield_half_full_white_48dp)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setOnlyAlertOnce(true)
                .setContentIntent(pendingIntent)
            notificationBuilder = builder
            builder
        }()
        return builder
            .setContentTitle("$chargingLevel %")
            .build()
    }

    private var lastBatteryLevel = 0

    private fun updateNotification(chargingLevel: Int) {
        if (lastBatteryLevel != chargingLevel) {
            val n = createNotification(chargingLevel)
            with(NotificationManagerCompat.from(this)) {
                notify(SVC_NOTIFICATION_ID, n)
            }
            lastBatteryLevel = chargingLevel
        }
    }

    // Call this instead of `stopSelf()` to clean up properly
    // TODO: what about external `stopService()` calls?
    private fun doStopSelf() {
        (application as LoadGuardApp).batteryMonitor.removeListener(batteryListener)
        stopSelf()
    }
}
