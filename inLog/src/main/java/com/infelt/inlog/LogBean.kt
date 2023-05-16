package com.infelt.inlog

/**
 * @Author infelt
 * @Date 2021/9/18 18:26
 * @Version 1.0
 *
 * 日志信息bean文件
 */
data class LogBean constructor(
    var time: Long,
    var priority: Int,
    var tag: String?,
    var msg: String?,
    var tr: Throwable?,
    var threadId: Int
)