package com.infelt.inlog

import android.net.Uri

interface ILogExportListener {
    fun onLogExportComplete(success: Boolean, uri: Uri?)
}