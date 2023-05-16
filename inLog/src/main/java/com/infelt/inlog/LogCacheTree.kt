package com.infelt.inlog

import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue

/**
 *
 * @Author infelt
 * @Date 2021/9/26 14:59
 * @Version 1.0
 *
 * @param priority      日志输出优先级
 * @param logFileConfig 指定日志备份文件，当前写文件路径和单个文件最大字节数
 *
 */
class LogCacheTree(priority: Int, private var logFileConfig: LogCacheConfig) :
    LogTree(priority, true) {

    var curWriteFileLength: Long = 0
    var fos: FileOutputStream? = null
    var curMsgCacheSize: Int = 0
    var msgCacheQueue: Queue<ByteArray>? = null

    init {
        createDirIfNeed()
        checkAndCreateCurWriteFileIfNeed()
        createMsgCacheQueueIfNeed()
    }


    private fun createMsgCacheQueueIfNeed() {
        if (isMsgMemoryCacheDisable()) {
            return
        }
        msgCacheQueue = ConcurrentLinkedQueue()
    }

    private fun checkAndCreateCurWriteFileIfNeed() {
        if (isFileCacheDisable()) {
            return
        }
        var file = File(logFileConfig.getCurWriteFilePath())
        if (!file.exists()) {
            curMsgCacheSize = 0
        }
        if (!isCurFileSizeExceed()) {
            curWriteFileLength = file.length()
        } else {
            logFileConfig.curWriteFile = LoggerHelper.getNewLogFileName()
            file = File(logFileConfig.getCurWriteFilePath())
            closeAndSetMsgStreamNull()
        }
        createMsgStreamIfNull(file)
    }

    /**
     * 打开当前的日志记录文件流
     */
    private fun createMsgStreamIfNull(file: File) {
        if (fos == null) {
            fos = FileOutputStream(file, true)
        }
    }

    /**
     * 当前文件超过最大值
     */
    private fun isCurFileSizeExceed(): Boolean {
        return compareValues(curWriteFileLength, logFileConfig.maxLogFileLength) > 0
    }


    private fun createDirIfNeed() {
        if (isFileCacheDisable()) {
            return
        }
        val dir = File(logFileConfig.logFileDir)
        if (!dir.exists()) {
            dir.mkdirs()
        }
    }

    private fun isFileCacheDisable(): Boolean {
        return compareValues(logFileConfig.cacheFileAble, false) == 0
    }

    private fun isMsgMemoryCacheDisable(): Boolean {
        return compareValues(logFileConfig.maxLogFileLength, 1) <= 0
    }

    override fun onMsg(compoundMsg: String?, logBean: LogBean) {
        if (isReleaseCalled()) {
            return
        }
        onMsgAndCheckFileLength(compoundMsg);
    }

    private fun onMsgAndCheckFileLength(compoundMsg: String?) {
        if (isFileCacheDisable() && isMsgMemoryCacheDisable()) {
            return
        }

        // 大约30us
        val msgBytes: ByteArray? = compoundMsg?.toByteArray()
        val length = msgBytes?.size
        // 写入到文件大概是100us
        writeLogToFileIfNeed(msgBytes, length)

    }

    private fun writeLogToFileIfNeed(msgBytes: ByteArray?, length: Int?) {
        if (isFileCacheDisable()) {
            return
        }
        fos?.let {
            try {
                it.write(msgBytes)
                curWriteFileLength += length!!
                if (isCurFileSizeExceed()) {
                    checkAndCreateCurWriteFileIfNeed()
                }
            } catch (exception: Exception) {
                exception.printStackTrace()
            }
        }


    }

    override fun release() {
        super.release()
        closeAndSetMsgStreamNull()
    }

    private fun closeAndSetMsgStreamNull() {
        try {
            fos?.flush()
            fos?.close()
        } catch (e: IOException) {
            Log.e("logger", "closeAndSetMsgStreamNull IOException:${e.stackTrace}")
        } finally {
            fos = null
            curWriteFileLength = 0
        }

    }

}