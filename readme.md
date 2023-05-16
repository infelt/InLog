# 使用说明

ilog是一个轻量日志库，可以方便高效的记录日志信息，并提供快捷导出功能。

# 使用步骤

1. 初始化ilog
   ``
   //cacheFileAble ,true 会缓存所有到文件，false 只会环境error级别日志。
   LoggerHelper.initLogger(context: Context, cacheFileAble: Boolean)
   ``

2. 日志打印
   ``
   Logger.info(tag: String?, msg: String?)
   Logger.debug(tag: String?, msg: String?)
   Logger.error(tag: String?, msg: String?)
   // ... 同android日志级别
   ``
3. 日志导出
   ``
   //导出操作函数
   LogExportUtils.exportCurLog(activity: Activity, listener: ILogExportListener? = null)

   //在activity onActivityResult中监听导出处理函数
   override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
   super.onActivityResult(requestCode, resultCode, data)
   LogExportUtils.onExportCurLog(this, requestCode, resultCode, data)
   }

``

4. 获取日志路径
   ``
   //获取当天的日子路径
   LoggerHelper.getLogPath(context: Context)
   ``
