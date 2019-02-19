package cn.berfy.service.im.model

import android.content.Context
import cn.berfy.service.im.manager.i.OnMessageDownloadCallback

/**
author: Berfy
date: 2018/12/25
系统消息
 */
class MessageSystem : Message() {

    var sys_msg: String? = ""//消息内容
    val is_jump: Boolean = false//是否可跳转
    val isButton_pressed: Boolean = false
    val is_agree: Boolean = false
    var isShowTitleTime: Boolean = false//是否显示标题时间

    override fun getSummaryText(): String {
        return ""
    }

    override fun showMessage(viewHolder: ChatViewHolder, context: Context) {
    }

    init {
        type = MessageContentType.TYPE_CUSTOM
    }

    override fun getRemoteUrl(callback: OnMessageDownloadCallback) {
    }

}