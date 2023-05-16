package com.infelt.inlog

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import java.io.File
import java.io.FileInputStream

object LogExportUtils {
    const val CONST_REQUEST_CODE = 10001
    private const val ZIP_FILE_NAME = "v2xLog.zip"
    private var listener: ILogExportListener? = null

    /**
     * 导出日志请求,重载版本for java
     */
    @JvmStatic
    fun exportCurLog(activity: Activity) {
        this.exportCurLog(activity, null)
    }

    /**
     * 导出日志请求
     */
    @JvmStatic
    fun exportCurLog(activity: Activity, listener: ILogExportListener? = null) {
        this.listener = listener
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/zip"
            putExtra(Intent.EXTRA_TITLE, ZIP_FILE_NAME)
        }
        activity.startActivityForResult(intent, CONST_REQUEST_CODE)
    }

    /**
     * 删除外部日志
     * v2x obu 日志
     */
    @JvmStatic
    fun deleteExLog(context: Context) {
        getObuLogs(context)?.forEach {
            FileUtils.deleteFile(File(it))
        }
    }

    @JvmStatic
    private fun getObuLogs(context: Context): Array<String>? {
        var obuRootDir = File("${context.getExternalFilesDir(null)}/obusdk/")
        var ret = obuRootDir.list { file, fileName ->
            file.isDirectory && File("${file.absolutePath}/$fileName/logs").exists()
        }?.map { fileName: String? ->
            "${obuRootDir.absolutePath}/$fileName/logs"
        }?.toTypedArray()
        return ret
    }

    /**
     * 导出日志操作
     */
    @JvmStatic
    fun onExportCurLog(
        activity: Activity, requestCode: Int, resultCode: Int, data: Intent?,
        vararg paths: String
    ) {
        if (requestCode == CONST_REQUEST_CODE) {
            var uri: Uri? = data?.data ?: return

            Thread {
                var srcs = arrayOf(
                    LoggerHelper.getLogPath(activity),
                ).plus(getObuLogs(activity) ?: emptyArray())
                    .plus(paths)
                var dir = File(srcs[0])
                var zipPath: String?
                if (dir.exists()) {
                    zipPath = "${dir.parent}/${ZIP_FILE_NAME}"
                    var zipFile = File(zipPath)
                    if (zipFile.exists()) {
                        zipFile.delete()
                    }
                } else {
                    listener?.onLogExportComplete(false, null)
                    return@Thread
                }
                ZipUtils.ZipFolders(srcs, zipPath)
                var inputStream = FileInputStream(zipPath)
                var buffer = ByteArray(1024)
                var outputStream = activity.contentResolver.openOutputStream(uri!!)
                outputStream?.apply {
                    while (inputStream.available() > 0) {
                        var len = inputStream.read(buffer)
                        write(buffer, 0, len)
                    }
                    flush()
                    close()
                    inputStream.close()
                    deleteExLog(activity)
                    listener?.onLogExportComplete(true, uri)
                }
            }.start()
        }
    }
}