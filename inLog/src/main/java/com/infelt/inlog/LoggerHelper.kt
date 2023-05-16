package com.infelt.inlog

import android.content.Context
import android.os.Environment
import android.os.Process
import android.text.TextUtils
import android.util.Log
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import java.io.File
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

/**
 * @Author infelt
 * @Date 2021/9/18 11:34
 * @Version 1.0
 */
object LoggerHelper {

    private const val LINE_BREAK = '\n'

    private const val SPIRIT = '/'

    private const val LEFT_BRACKET = '('

    private const val RIGHT_BRACKET = ')'

    private const val SPACE = ' '

    private const val CONNECT = '-'

    private const val COLON = ':'

    private const val MAX_FILE_LENGTH = 30 * 1024 * 1024L


    private const val DATA_FORMAT = "yyyy-MM-dd HH:mm:ss:SSS"

    private const val TIME_FORMAT = "HH:mm:ss"

    private const val FILE_NAME_PREFIXES = "app_log_"

    private const val LOG_DIR = "Logs"

    private const val LOG_KEEP_DAYS = 3

    private val simpleDateFormat = SimpleDateFormat("yyyy.MM.dd", Locale.CHINA)

    //日志相关配置
    private val logFileConfig = LogCacheConfig()

    //外部日志写入类
    private var loggerWriter: ILoggerWriter? = null

    @JvmStatic
    fun initLogger(context: Context, cacheFileAble: Boolean) {
        if (loggerWriter != null) {
            Logger.init(0, loggerWriter)
        } else {
            logFileConfig.logFileDir = getLogPath(context)
            logFileConfig.maxLogFileLength = MAX_FILE_LENGTH
            logFileConfig.curWriteFile = getNewLogFileName()
            logFileConfig.cacheFileAble = true
            var priority = if (cacheFileAble) {
                Logger.VERBOSE
            } else {
                Logger.INFO
            }
            Logger.init(
                1024, loggerWriter, LogcatTree(priority),
                LogCacheTree(priority, logFileConfig),
            )
            if (logFileConfig.cacheFileAble) {
                val logCleanWorker = OneTimeWorkRequest.Builder(LogCleanWorker::class.java)
                    .build()
                WorkManager.getInstance(context).enqueue(logCleanWorker)
            }
        }
    }

    /**
     * 删除过期的日志，默认保存7天的日志
     */
    @JvmStatic
    fun deleteOldLog(context: Context) {
        if (loggerWriter != null) {
            return
        }
        //删除sdk日志
        val parentDir = File(getLogPath(context)).parentFile
        var calendar = Calendar.getInstance()
        calendar.time = Date()
        calendar.add(Calendar.DAY_OF_YEAR, -LOG_KEEP_DAYS)
        var lastedData = calendar.time
        var files = parentDir.listFiles { file ->
            var ret = true
            try {
                var data: Date? = simpleDateFormat.parse(file.name)
                ret = data?.before(lastedData) != false
            } catch (e: Exception) {
                e.printStackTrace()
            }
            ret
        }
        var iterator = files.iterator()
        while (iterator.hasNext()) {
            FileUtils.deleteFile(iterator.next())
        }
        //删除obu日志
        deleteObuLog(context)
    }

    /**
     * 删除obulog
     */
    @JvmStatic
    private fun deleteObuLog(context: Context) {
        if (loggerWriter != null) {
            return
        }
        Logger.debug("LoggerHelper", "deleteObuLog")
        val rootFile = File("${context.filesDir.absolutePath}/obusdk")
        if (!rootFile.exists()) {
            return
        }
        val deleteTime = System.currentTimeMillis() - LOG_KEEP_DAYS * 24 * 60 * 60 * 1000
        rootFile.listFiles()?.forEach { modeFile ->
            val logsDir = File(modeFile.absolutePath, "logs")
            if (logsDir.exists()) {
                logsDir.listFiles()?.forEach { logFile ->
                    if (logFile.lastModified() <= deleteTime) {
                        logFile.delete()
                    }
                }
            }
        }
    }

    /**
     * 获取日志文件夹路径
     */
    @JvmStatic
    fun getLogPath(context: Context): String? {
        if (loggerWriter != null) {
            return null
        }
        if (!TextUtils.isEmpty(logFileConfig.logFileDir)) {
            return logFileConfig.logFileDir
        }
        return if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            "${context.getExternalFilesDir(null)?.absolutePath}/${getRelativeLogDir()}"
        } else {
            "${context.filesDir}/${getRelativeLogDir()}"
        }
    }

    /**
     * 获取日志相对路径
     */
    @JvmStatic
    fun getRelativeLogDir(): String {
        if (loggerWriter != null) {
            return ""
        }
        return "$LOG_DIR/${simpleDateFormat.format(Date())}/"
    }

    /**
     * 获取当前日志文件
     */
    @JvmStatic
    fun getCurLogFile(): String? {
        if (loggerWriter != null) {
            return null
        }
        return logFileConfig.curWriteFile
    }


    /**
     * 设置日志写入策略
     */
    @JvmStatic
    fun setLoggerWriterStrategy(writer: ILoggerWriter): Boolean {
        if (this.loggerWriter != null || getCurLogFile() != null) {
            return false
        }
        this.loggerWriter = writer
        return true
    }


    /**
     * 根据时间获取当前的记录日志文件名称
     */
    fun getNewLogFileName(): String {
        return "$FILE_NAME_PREFIXES${formatCurTime(System.currentTimeMillis(), TIME_FORMAT)}.txt"
    }


    /**
     * 获取堆栈
     */
    fun getStackTraceString(tr: Throwable?): String {
        return Log.getStackTraceString(tr)
    }

    /**
     * 获取当前时间的格式化字符串
     */
    private fun formatCurTime(time: Long, formatStr: String): String? {
        val date = Date(time)
        val format = SimpleDateFormat(formatStr, Locale.getDefault())
        return format.format(date)
    }

    /**
     * 获取日志级别对应的字符
     */
    private fun getPriorityString(priority: Int): String {
        return when (priority) {
            Logger.VERBOSE -> "V"
            Logger.DEBUG -> "D"
            Logger.INFO -> "I"
            Logger.WARN -> "W"
            Logger.ERROR -> "E"
            Logger.ASSERT -> "A"
            else -> "unknown"
        }
    }

    /**
     * 设计日志的头部,组装日志样式："2018-02-26 16:53:25.123 D/Tag(pid-tid)："
     */
    private fun getLogPrefix(sb: StringBuilder, logBean: LogBean): StringBuilder? {
        return sb.append(formatCurTime(logBean.time, DATA_FORMAT))
            .append(SPACE)
            .append(getPriorityString(logBean.priority))
            .append(SPIRIT)
            .append(logBean.tag)
            .append(LEFT_BRACKET)
            .append(Process.myPid())
            .append(CONNECT)
            .append(logBean.threadId)
            .append(RIGHT_BRACKET)
            .append(COLON)
            .append(SPACE)
    }

    /**
     * 合成日志，样式为："2018-02-26 16:53:25.123 D/Tag(pid-tid)：msg + \n + getStackTraceString"
     *
     */
    fun compoundMsg(logBean: LogBean): String {
        val msgBuilder = StringBuilder(256)
        getLogPrefix(msgBuilder, logBean)
        msgBuilder.append(logBean.msg)
            .append(LINE_BREAK)
        if (logBean.tr != null) {
            msgBuilder.append(getStackTraceString(logBean.tr))
            msgBuilder.append(LINE_BREAK)
        }
        return msgBuilder.toString()
    }

    @JvmStatic
    fun release() {
        Logger.release()
        loggerWriter = null
    }


}
