package com.infelt.inlog.ext

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper

object SystemExt {
    fun Context.getActivity(): Activity? = when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.getActivity()
        else -> null
    }
}