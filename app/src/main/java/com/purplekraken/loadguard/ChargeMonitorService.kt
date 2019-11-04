package com.purplekraken.loadguard

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.purplekraken.loadguard.alarm.AlarmReceiver

class ChargeMonitorService : Service() {
    companion object {
        private const val TAG = "ChargeMonitorService"

        private const val CHANNEL_ID = "com.purplekraken.loadguard.CHARGE_MONITOR_CHANNEL_ID"
        private const val SVC_NOTIFICATION_ID = 1001

        const val ACTION_DISMISS = "com.purplekraken.loadguard.ACTION_DISMISS"
        const val EXTRA_NOTIFICATION_ID = "com.purplekraken.loadguard.EXTRA_NOTIFICATION_ID"
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "service created")
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.notification_channel_name)
            val descr = getString(R.string.notification_channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descr
            }
            val notificationMgr =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationMgr.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val app = application as LoadGuardApp
        val batmon = app.batteryMonitor
        batmon.addListener(batteryListener)
        val lvl = batmon.chargingLevel
        startForeground(SVC_NOTIFICATION_ID, createNotification(lvl))
        Log.d(TAG, "service started")
        return super.onStartCommand(intent, flags, startId)
    }

    private var lastBatteryLevel = 0

    private fun createNotification(chargingLevel: Int): Notification {
        lastBatteryLevel = chargingLevel
        return notificationBuilder
            .setContentTitle("$chargingLevel %")
            .build()
    }

    private fun updateNotification(chargingLevel: Int) {
        if (lastBatteryLevel != chargingLevel) {
            val n = createNotification(chargingLevel)
            with(NotificationManagerCompat.from(this)) {
                notify(SVC_NOTIFICATION_ID, n)
            }
            lastBatteryLevel = chargingLevel
        }
    }

    private var alarmTriggered = false
    private val batteryListener: BatteryUpdateCallback = {
        if (!it.isCharging) {
            doStopSelf()
        } else {
            val lvl = it.chargingLevel
            if (!alarmTriggered && lvl >= LoadGuardApp.levelThreshold) {
                showAlarmNotification(lvl)
                (application as LoadGuardApp).alarmManager.triggerAlarm()
                alarmTriggered = true
            } else {
                updateNotification(lvl)
            }
        }
    }

    private var _notificationBuilder: NotificationCompat.Builder? = null
    private val notificationBuilder: NotificationCompat.Builder
        get() {
            return _notificationBuilder ?: {
                val intent = Intent(this, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                val pendingIntent = PendingIntent.getActivity(this, 0, intent, 0)
                val builder = NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentText(this.getString(R.string.notification_text))
                    .setSmallIcon(R.drawable.ic_shield_half_full_white_48dp)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setOnlyAlertOnce(true)
                    .setAutoCancel(false)
                    .setContentIntent(pendingIntent)
                    .setCategory(NotificationCompat.CATEGORY_ALARM)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setOngoing(true)
                _notificationBuilder = builder
                builder
            }()
        }

    private fun showAlarmNotification(chargingLevel: Int) {
        val dismissIntent = Intent(this, AlarmReceiver::class.java).apply {
            action = ACTION_DISMISS
            putExtra(
                EXTRA_NOTIFICATION_ID,
                SVC_NOTIFICATION_ID
            )
        }
        val pendingDismissIntent = PendingIntent.getBroadcast(this, 0, dismissIntent, 0)

        val n = notificationBuilder
            .setContentTitle("$chargingLevel %")
            .setContentText("Charging threshold is reached")
            .addAction(
                R.drawable.ic_shield_half_full_black_24dp,
                getString(R.string.notification_action_dismiss),
                pendingDismissIntent
            )
            .setDefaults(NotificationCompat.DEFAULT_LIGHTS)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setWhen(0)
            .build()
        with(NotificationManagerCompat.from(this)) {
            notify(SVC_NOTIFICATION_ID, n)
        }
    }

    // Call this instead of `stopSelf()` to clean up properly
    // TODO: what about external `stopService()` calls?
    private fun doStopSelf() {
        val app = (application as LoadGuardApp)
        stopForeground(false)
        app.alarmManager.dismiss()
        app.batteryMonitor.removeListener(batteryListener)
        stopSelf()
    }
}
