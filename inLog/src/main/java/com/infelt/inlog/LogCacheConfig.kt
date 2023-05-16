package com.infelt.inlog

import java.io.File

/**
 * @Author infelt
 * @Date 2021/9/26 15:28
 * @Version 1.0
 */
class LogCacheConfig {

    /**
     * 日志文件的存放路径，如果为空，则表示不缓存到本地磁盘中
     */
    var logFileDir: String? = null

    /**
     * 当前文件的名称
     */
    var curWriteFile: String? = null

    /**
     * 保存到本地日志文件中的最大长度。
     */
    var maxLogFileLength: Long = 0

    /**
     * 是否写到文件中
     */
    var cacheFileAble: Boolean = true

    fun getCurWriteFilePath(): String {
        return logFileDir + File.separator + curWriteFile
    }

}