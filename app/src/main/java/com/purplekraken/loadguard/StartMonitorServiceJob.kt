package com.purplekraken.loadguard

import android.app.job.JobParameters
import android.app.job.JobService
import android.util.Log

class StartMonitorServiceJob : JobService() {
    companion object {
        private const val TAG = "StartMonitorServiceJob"
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        Log.d(TAG, "Job starting")
        (applicationContext as LoadGuardApp).stopMonitorService()
        return true
    }

    override fun onStartJob(params: JobParameters?): Boolean {
        Log.d(TAG, "Job stopping")
        (applicationContext as LoadGuardApp).startMonitorService()
        return true
    }
}
