package com.infelt.inlog

import java.util.concurrent.atomic.AtomicBoolean

/**
 * @Author infelt
 * @Date 2021/9/18 11:54
 * @Version 1.0
 */
abstract class LogTree(priority: Int, isAcceptCompoundMsg: Boolean) {
    var priority = 0
    var isReleaseCalled = AtomicBoolean(false)
    var isAcceptCompoundMsg = false


    init {
        this.priority = priority
        this.isAcceptCompoundMsg = isAcceptCompoundMsg;
    }

    fun handleMsg(compoundMsg: String?, logBean: LogBean) {
        if (isLoggAble(logBean.priority)) {
            onMsg(compoundMsg, logBean)
        }
    }

    fun isLoggAble(priority: Int): Boolean {
        return priority >= this.priority;
    }

    /**
     * 该方法将会在子线程中调用，基于性能考虑，除了Logcat的日志外，其他的都是在子线程调用
     */
    abstract fun onMsg(compoundMsg: String?, logBean: LogBean);

    /**
     * 停止日志打印时调用，子类必须调用super.release
     */
    open fun release() {
        isReleaseCalled.set(true)
    }

    protected open fun isReleaseCalled(): Boolean {
        return isReleaseCalled.get()
    }


}