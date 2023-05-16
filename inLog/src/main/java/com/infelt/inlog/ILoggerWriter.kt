package com.infelt.inlog

interface ILoggerWriter {

    fun println(priority: Int, tag: String?, msg: String?, tr: Throwable?)

}