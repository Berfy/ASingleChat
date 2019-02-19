package cn.berfy.service.im.model

import android.content.Context
import android.text.TextUtils
import cn.berfy.sdk.mvpbase.util.FileUtils
import cn.berfy.service.im.manager.i.OnMessageDownloadCallback

/**
author: Berfy
date: 2018/12/25
位置消息
 */
class MessageLocation : Message() {

    var title: String = ""//云端url
    var lng: String? = ""//经度
    var lat: String = ""//纬度

    init {
        type = MessageContentType.TYPE_LOCATION
    }

    override fun getSummaryText(): String {
        return "[位置]"
    }

    override fun showMessage(viewHolder: ChatViewHolder, context: Context) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    //获取图片、语音、视频、文件远程源文件
    override fun getRemoteUrl(callback: OnMessageDownloadCallback) {

    }
}