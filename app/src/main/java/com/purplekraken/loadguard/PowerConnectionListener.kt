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
