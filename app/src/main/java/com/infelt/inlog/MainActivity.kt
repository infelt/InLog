package com.infelt.inlog

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.infelt.inlog.ext.SystemExt.getActivity
import com.infelt.inlog.ui.theme.LogDemoTheme

class MainActivity : ComponentActivity() {
    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Logger.debug(TAG, "onCreate")
        setContent {
            LogDemoTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.size(120f.dp, 100f.dp),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ExportBtn("导出日志")
                }
            }
        }
        window.decorView.keepScreenOn = true

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        LogExportUtils.onExportCurLog(this,  requestCode, resultCode, data)
    }
}

@Composable
fun ExportBtn(name: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    Button(onClick = {
        context.getActivity()?.let {
            LogExportUtils.exportCurLog(it, object : ILogExportListener {
                override fun onLogExportComplete(success: Boolean, uri: Uri?) {
                    Handler(Looper.getMainLooper()).post {
                        val msg = if (success) "导出成功" else "导出失败"
                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                    }
                }
            })
        }

    }, modifier) {
        Text(text = name)
    }

}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    LogDemoTheme {
        ExportBtn("Android")
    }
}