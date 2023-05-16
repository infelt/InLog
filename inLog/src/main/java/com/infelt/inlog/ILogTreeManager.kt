package com.infelt.inlog

/**
 * @Author infelt
 * @Date 2021/9/18 11:54
 * @Version 1.0
 */
interface ILogTreeManager {
    fun addLogTrees(vararg logTrees: LogTree?): Boolean
}