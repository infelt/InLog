package com.infelt.inlog

/**
 * @Author infelt
 * @Date 2021/9/18 18:33
 * @Version 1.0
 */
interface IHandleLog {

    fun handleMsg(priority: Int, tag: String?, msg: String?)

    fun handleMsg(priority: Int, tag: String?, msg: String?, tr: Throwable?)
}