package com.infelt.inlog

import android.os.Process
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicInteger

/**
 * @Author infelt
 * @Date 2021/9/18 11:51
 * @Version 1.0
 */
class TreeManager(
    private var maxLogCountInQueue: Int,
    private var logcatTree: LogcatTree? = LogcatTree.EmptyLogcatTree.EMPTY_LOGCAT_TREE
) : IHandleLog, ILogTreeManager {

    companion object {
        private const val THREAD_CLOSED = -2
        private const val REQUEST_THREAD_CLOSE = -1
        private const val THREAD_RUNNING = 0
    }

    private val isPrintStackInfo = true

    private val trees: CopyOnWriteArrayList<LogTree> = CopyOnWriteArrayList()
    private val msgQueue: Queue<LogBean> = ConcurrentLinkedQueue()

    /**
     * 在内存中日志队列的最大日志条数
     */
    private val logCountInQueue: AtomicInteger = AtomicInteger(0)

    /**
     * -2(THREAD_CLOSED)，表示未启动或者已经关闭
     * -1(REQUEST_THREAD_CLOSE)，表示请求关闭
     * 0(THREAD_RUNNING)， 表示已经正常启动
     */
    private val dispatcherThreadState: AtomicInteger = AtomicInteger(THREAD_CLOSED)

    /**
     * 消息分发线程
     */
    private var msgDispatcherThread: MsgDispatcherThread? = null


    override fun handleMsg(priority: Int, tag: String?, msg: String?) {
        var newMsg: String? = if (isPrintStackInfo) {
            var traces = Thread.currentThread().stackTrace
            if (traces.size > 6) {
                StringBuilder("(")
                    .append(traces[6].fileName).append(':')
                    .append(traces[6].lineNumber).append(')')
                    .append(": ").append(msg).toString()
            } else {
                msg
            }
        } else {
            msg
        }
        logcatTree?.handleMsg(priority, tag, newMsg)
        checkAndOfferMsgToMsgQueue(priority, tag, newMsg, null)

    }

    override fun handleMsg(priority: Int, tag: String?, msg: String?, tr: Throwable?) {
        var newMsg: String? = if (isPrintStackInfo) {
            var traces = Thread.currentThread().stackTrace
            if (traces.size > 6) {
                StringBuilder("(")
                    .append(traces[6].fileName).append(':')
                    .append(traces[6].lineNumber).append(')')
                    .append(": ").append(msg).toString()
            } else {
                msg
            }
        } else {
            msg
        }
        logcatTree?.handleMsg(priority, tag, newMsg, tr)
        checkAndOfferMsgToMsgQueue(priority, tag, newMsg, tr)
    }

    override fun addLogTrees(vararg logTrees: LogTree?): Boolean {
        if (logTrees.isEmpty()) {
            return false
        }
        for (logTree in logTrees) {
            addTree(logTree)
        }
        return true

    }

    private fun addTree(logTree: LogTree?): Boolean {
        var ret = true
        if (logTree is LogcatTree) {
            logcatTree = logTree
            return ret
        } else {
            ret = trees.add(logTree)
            createDispatcherThreadAndStartIfNeed()
        }
        return ret
    }

    private fun createDispatcherThreadAndStartIfNeed() {
        if (!trees.isEmpty() && msgDispatcherThread == null) {
            msgDispatcherThread = MsgDispatcherThread()
            dispatcherThreadState.set(THREAD_RUNNING)
            msgDispatcherThread?.start()

        }
    }

    private fun checkAndOfferMsgToMsgQueue(
        priority: Int,
        tag: String?,
        msg: String?,
        tr: Throwable?
    ) {
        if (trees.isEmpty() || logCountInQueue.get() > maxLogCountInQueue) {
            return
        }
        var logBean = LogBean(System.currentTimeMillis(), priority, tag, msg, tr, Process.myTid())
        logCountInQueue.incrementAndGet()
        msgQueue.offer(logBean)
    }

    fun isThreadStateRunning(): Boolean {
        return dispatcherThreadState.get() == THREAD_RUNNING
    }

    fun isThreadStateClosed(): Boolean {
        return dispatcherThreadState.get() == THREAD_CLOSED
    }

    fun stopTreeThread() {
        dispatcherThreadState.set(REQUEST_THREAD_CLOSE)
    }

    private fun releaseLogTree() {
        if (!trees.isEmpty()) {
            for (logTree in trees) {
                logTree.release()
            }
        }
    }

    private inner class MsgDispatcherThread : Thread() {

        override fun run() {
            while (isThreadStateRunning()) {
                // poll大约8-16us
                val logBean: LogBean? = msgQueue.poll()
                if (sleepIfMsgNull(logBean)) {
                    continue
                }
                logCountInQueue.decrementAndGet()
                dispatchMsg(logBean)
            }
            dispatcherThreadState.set(THREAD_CLOSED)
            releaseLogTree()
        }

        private fun sleepIfMsgNull(logBean: LogBean?): Boolean {
            return if (logBean == null) {
                sleep(10)
                true
            } else false
        }

        private fun dispatchMsg(logBean: LogBean?) {
            logBean?.let {
                var compoundMsg: String? = null
                for (logTree in trees) {
                    if (logTree.isAcceptCompoundMsg) {
                        if (compoundMsg == null) {
                            // 耗时，大约发费1800us
                            compoundMsg = LoggerHelper.compoundMsg(it)
                        }
                        logTree.handleMsg(compoundMsg, it)
                    } else {
                        logTree.handleMsg(null, it)
                    }
                }
            }
        }
    }
}