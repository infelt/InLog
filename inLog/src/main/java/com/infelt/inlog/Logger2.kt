package com.infelt.inlog

import android.content.Context
import android.text.TextUtils
import com.infelt.inlog.LoggerHelper.getLogPath
import java.io.File
import java.io.FileWriter
import java.util.concurrent.ArrayBlockingQueue
import kotlin.concurrent.thread

class Logger2(private var context: Context, private var fileName: String?) {
    @Volatile
    private var isWriting = false

    private var queue = ArrayBlockingQueue<String>(1000)

    fun init() {
        if (!isWriting) {
            queue.clear()
            thread {
                val file = File(getLogPath(context), fileName)
                if (file.exists()) {
                    file.delete()
                }
                file.createNewFile()
                var fileWriter = FileWriter(file)
                isWriting = true
                writeMsg("logger2 start =>>")
                while (isWriting) {
                    fileWriter.write(queue.take())
                    fileWriter.flush()
                }
                fileWriter.close()
            }
        }
    }

    fun writeMsg(msg: String) {
        if (isWriting && !TextUtils.isEmpty(msg)) {
            queue.offer(msg + "\r\n")
        }
    }

    fun release() {
        isWriting = false
        queue.offer("\r\n")
    }
}