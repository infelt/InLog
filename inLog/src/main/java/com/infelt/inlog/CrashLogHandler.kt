package com.infelt.inlog

import android.app.ActivityManager
import android.content.Context
import android.widget.Toast
import kotlin.system.exitProcess

class CrashLogHandler constructor() : Thread.UncaughtExceptionHandler {

    private var appContext: Context? = null

    private var mDefaultCrashHandler: Thread.UncaughtExceptionHandler? = null
    private var logger2: Logger2? = null


    companion object {
        private val TAG = "CrashLogHandler"
        val instance: CrashLogHandler by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            CrashLogHandler()
        }
    }

    public fun init(context: Context, logger2: Logger2) {
        this.logger2 = logger2
        mDefaultCrashHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler(this)
        appContext = context
    }

    override fun uncaughtException(p0: Thread, p1: Throwable) {
        logger2?.writeMsg("$TAG, uncaughtException ---->>>>> start ")
        dumpAppProcessInfo(appContext!!)
        logger2?.writeMsg(
            "$TAG,${String.format("ThreadName:%s threadId:%d => %s", p0.name, p0.id, p1.cause)}"
        )
        logger2?.writeMsg("$TAG,${p1.message}")
        var stacks = p1.stackTrace
        var index = 0
        while (index < stacks.size) {
            var stack = stacks[index++]
            logger2?.writeMsg(
                "$TAG,${
                    String.format(
                        "%s(%s:%d)", stack.methodName, stack.className,
                        stack.lineNumber
                    )
                }"
            )
        }
        logger2?.writeMsg("$TAG,uncaughtException ---->>>>> end ")
        Toast.makeText(appContext, "app发送异常，即将自动退出", Toast.LENGTH_LONG).show()
        exitProcess(0)
    }

    fun dumpAppProcessInfo(context: Context) {
        var activityManager: ActivityManager =
            context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager;
        var tasks = activityManager.runningAppProcesses
        var it = tasks.iterator()
        while (it.hasNext()) {
            var task = it.next()
            logger2?.writeMsg("$TAG,dumpAppProcessInfo : " + task.processName + "  id:" + task.pid)
        }
    }

}