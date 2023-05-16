package com.infelt.inlog

/**
 * @Author infelt
 * @Date 2021/9/18 11:34
 * @Version 1.0
 */
object Logger : ILoggerWriter {

    /**
     * Priority constant for the println method; use Log.v.
     */
    val VERBOSE = 2

    /**
     * Priority constant for the println method; use Log.d.
     */
    val DEBUG = 3

    /**
     * Priority constant for the println method; use Log.i.
     */
    val INFO = 4

    /**
     * Priority constant for the println method; use Log.w.
     */
    val WARN = 5

    /**
     * Priority constant for the println method; use Log.e.
     */
    val ERROR = 6

    /**
     * Priority constant for the println method；use Log.wtf.
     */
    val ASSERT = 7

    private var treeManager: TreeManager? = null

    private var loggerWriter: ILoggerWriter? = null

    /**
     * Send a [.VERBOSE] log message.
     *
     * @param tag Used to identify the source of a log message.  It usually identifies
     * the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    @JvmStatic
    fun verbose(tag: String?, msg: String?) {
        println(VERBOSE, tag, msg)
    }

    /**
     * Send a [.VERBOSE] log message and log the exception.
     *
     * @param tag Used to identify the source of a log message.  It usually identifies
     * the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     * @param tr  An exception to log
     */
    @JvmStatic
    fun verbose(tag: String?, msg: String?, tr: Throwable?) {
        println(VERBOSE, tag, msg, tr)
    }

    /**
     * Send a [.DEBUG] log message.
     *
     * @param tag Used to identify the source of a log message.  It usually identifies
     * the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    @JvmStatic
    fun debug(tag: String?, msg: String?) {
        println(DEBUG, tag, msg)
    }

    /**
     * Send a [.DEBUG] log message and log the exception.
     *
     * @param tag Used to identify the source of a log message.  It usually identifies
     * the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     * @param tr  An exception to log
     */
    @JvmStatic
    fun debug(tag: String?, msg: String?, tr: Throwable?) {
        println(DEBUG, tag, msg, tr)
    }

    /**
     * Send an [.INFO] log message.
     *
     * @param tag Used to identify the source of a log message.  It usually identifies
     * the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    @JvmStatic
    fun info(tag: String?, msg: String?) {
        println(INFO, tag, msg)
    }

    /**
     * Send a [.INFO] log message and log the exception.
     *
     * @param tag Used to identify the source of a log message.  It usually identifies
     * the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     * @param tr  An exception to log
     */
    @JvmStatic
    fun info(tag: String?, msg: String?, tr: Throwable?) {
        println(INFO, tag, msg, tr)
    }

    /**
     * Send a [.WARN] log message.
     *
     * @param tag Used to identify the source of a log message.  It usually identifies
     * the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    @JvmStatic
    fun warn(tag: String?, msg: String?) {
        println(WARN, tag, msg)
    }

    /**
     * Send a [.WARN] log message and log the exception.
     *
     * @param tag Used to identify the source of a log message.  It usually identifies
     * the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     * @param tr  An exception to log
     */
    @JvmStatic
    fun warn(tag: String?, msg: String?, tr: Throwable?) {
        println(WARN, tag, msg, tr)
    }

    /**
     * Send a [.WARN] log message and log the exception.
     *
     * @param tag Used to identify the source of a log message.  It usually identifies
     * the class or activity where the log call occurs.
     * @param tr  An exception to log
     */
    @JvmStatic
    fun warn(tag: String?, tr: Throwable?) {
        println(WARN, tag, "", tr)
    }

    /**
     * Send an [.ERROR] log message.
     *
     * @param tag Used to identify the source of a log message.  It usually identifies
     * the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    @JvmStatic
    fun error(tag: String?, msg: String?) {
        println(ERROR, tag, msg)
    }

    /**
     * Send a [.ERROR] log message and log the exception.
     *
     * @param tag Used to identify the source of a log message.  It usually identifies
     * the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     * @param tr  An exception to log
     */
    @JvmStatic
    fun error(tag: String?, msg: String?, tr: Throwable?) {
        println(ERROR, tag, msg, tr)
    }

    /**
     * error只打印错误
     */
    @JvmStatic
    fun error(tag: String?, tr: Throwable?) {
        error(tag, null, tr)
    }

    /**
     * 打印日志
     */
    override fun println(priority: Int, tag: String?, msg: String?, tr: Throwable?) {
        if (loggerWriter != null) {
            loggerWriter?.println(priority, tag, msg, tr)
        } else {
            treeManager?.handleMsg(priority, tag, msg, tr)
        }
    }

    /**
     * 打印日志2
     */
    fun println(priority: Int, tag: String?, msg: String?) {
        println(priority, tag, msg, null)
    }

    /**
     * 初始化
     */
    fun init(maxLogCountInQueue: Int, writer: ILoggerWriter?, vararg logTrees: LogTree?) {
        if (treeManager == null) {
            treeManager = TreeManager(maxLogCountInQueue)
            treeManager?.addLogTrees(*logTrees)
        }
        loggerWriter = writer
    }

    /**
     * 释放
     */
    fun release() {
        treeManager?.stopTreeThread()
        treeManager = null;
    }
}