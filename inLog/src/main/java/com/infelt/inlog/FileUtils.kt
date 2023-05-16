package com.infelt.inlog

import java.io.File


object FileUtils {

    /**
     * 删除某个文件夹下的所有文件夹和文件
     *
     * @param file 待删除的文件夹
     */
    @JvmStatic
    fun deleteFile(file: File) {
        file?.let {
            delete(file)
        }
    }

    private fun delete(file: File) {
        if (file.exists()) {
            if (file.isFile) {
                file.delete()
            } else if (file.isDirectory) {
                val files = file.listFiles()
                for (i in files.indices) {
                    delete(files[i])
                }
            }
            file.delete()
        }
    }
}