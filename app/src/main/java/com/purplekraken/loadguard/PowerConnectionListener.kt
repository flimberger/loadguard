package com.purplekraken.loadguard

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class PowerConnectionListener : BroadcastReceiver() {
    companion object {
        const val TAG = "PowerConnectionListener"
    }

    override fun onReceive(ctx: Context?, intent: Intent?) {
        if (ctx == null) return
        if (intent != null) {
            when (intent.action) {
                Intent.ACTION_POWER_CONNECTED -> startService(ctx)
                Intent.ACTION_POWER_DISCONNECTED -> stopService(ctx)
            }
        }
    }

    private fun startService(ctx: Context) {
        Log.i(TAG, "power connected")
        ctx.startService(Intent(ctx, ChargeMonitorService::class.java))
    }

    private fun stopService(ctx: Context) {
        Log.i(TAG, "power connected")
        ctx.startService(Intent(ctx, ChargeMonitorService::class.java))
    }
}