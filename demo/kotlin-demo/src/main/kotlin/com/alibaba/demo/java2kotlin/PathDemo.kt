package com.alibaba.demo.java2kotlin

import java.io.File
import java.io.IOException

object PathDemo {

    fun deleteRecursively(file: File) {
        if (!file.exists()) {
            return
        }
        val fileList = file.listFiles()
        if (fileList != null) {
            for (childFile in fileList) {
                if (childFile.isDirectory) {
                    deleteRecursively(childFile)
                } else if (!childFile.delete()) {
                    throw IOException()
                }
            }
        }
        if (file.exists() && !file.delete()) {
            throw IOException("Unable to delete file " + file.absolutePath)
        }
    }

}
