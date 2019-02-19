package cn.berfy.service.im.manager.i

import cn.berfy.service.im.model.Message

interface OnMessageDownloadCallback {
    fun onStart()
    fun downloadProgress(pro: Float)//0-1 下载
    fun onSuc(message: Message)
    fun onFailed(errMsg: String)
}