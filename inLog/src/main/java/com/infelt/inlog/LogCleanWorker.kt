package com.infelt.inlog

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class LogCleanWorker(appContext: Context, workerParams: WorkerParameters)
    : Worker(appContext, workerParams) {

    override fun doWork(): Result {
        LoggerHelper.deleteOldLog(applicationContext)
        return Result.success()
    }
}