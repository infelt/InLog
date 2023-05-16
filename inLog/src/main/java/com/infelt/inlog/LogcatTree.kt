package com.infelt.inlog

import android.util.Log


/**
 * @Author infelt
 * @Date 2021/9/22 16:18
 * @Version 1.0
 */
open class LogcatTree(priority: Int, isAcceptCompoundMsg: Boolean = false) :
    LogTree(priority, isAcceptCompoundMsg), IHandleLog {

    override fun handleMsg(priority: Int, tag: String?, msg: String?) {
        if (isLoggAble(priority)) {
            Log.println(priority, tag, msg + '\n')
        }
    }

    override fun handleMsg(priority: Int, tag: String?, msg: String?, tr: Throwable?) {
        if (isLoggAble(priority)) {
            Log.println(priority, tag, msg + '\n' + LoggerHelper.getStackTraceString(tr))
        }
    }

    override fun onMsg(compoundMsg: String?, logBean: LogBean) {
        throw  UnsupportedOperationException("call handleMsg(int, String, String, Throwable) instead!!")
    }


    class EmptyLogcatTree(priority: Int) : LogcatTree(priority) {
        /**
         * 空Logcat对象
         */
        companion object {
            val EMPTY_LOGCAT_TREE = EmptyLogcatTree(Logger.ASSERT)
        }
    }


}