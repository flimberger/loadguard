package com.purplekraken.loadguard

import android.app.Application
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log

class LoadGuardApp : Application() {
    companion object {
        private const val TAG = "LoadGuardApp"
        private const val JOB_ID = 100
    }

    val batteryMonitor = BatteryMonitor(this)

    private var jobScheduled = false
    fun scheduleJob() {
        if (!jobScheduled) {
            Log.d(TAG, "scheduling job")
            val jobScheduler = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
            val jobInfo =
                JobInfo.Builder(JOB_ID, ComponentName(this, StartMonitorServiceJob::class.java))
                    .setRequiresCharging(true)
                    .setOverrideDeadline(0L)
                    .build()
            if (jobScheduler.schedule(jobInfo) == JobScheduler.RESULT_SUCCESS) {
                jobScheduled = true
            } else {
                Log.e(TAG, "failed to schedule job")
            }
        }
    }

    fun startMonitorService() {
        Log.d(TAG, "starting monitor service")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(Intent(this, ChargeMonitorService::class.java))
        } else {
            startService(Intent(this, ChargeMonitorService::class.java))
        }
    }

    fun stopMonitorService() {
        Log.d(TAG, "stopping monitor service")
        stopService(Intent(this, ChargeMonitorService::class.java))
    }
}
