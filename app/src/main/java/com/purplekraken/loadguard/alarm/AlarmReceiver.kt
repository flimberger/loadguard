package com.purplekraken.loadguard.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.purplekraken.loadguard.AsyncHandler
import com.purplekraken.loadguard.ChargeMonitorService
import com.purplekraken.loadguard.LoadGuardApp

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
        ChargeMonitorService.ACTION_DISMISS -> {
            val app = ctx.applicationContext as LoadGuardApp
            app.alarmManager.dismiss()
            app.stopMonitorService()
        }
    }
}
