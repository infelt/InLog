package com.infelt.inlog

import android.app.Application

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        LoggerHelper.initLogger(this, true)
    }
}