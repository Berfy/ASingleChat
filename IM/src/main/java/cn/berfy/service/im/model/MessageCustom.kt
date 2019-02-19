package cn.berfy.service.im.model

import android.content.Context
import cn.berfy.service.im.manager.i.OnMessageDownloadCallback

/**
author: Berfy
date: 2018/12/25
自定义消息(推送)
 */
class MessageCustom : Message() {

    var cus_type: MessageCustomType = MessageCustomType.TYPE_ADD_FRIEND
    var cus_from: String = ""
    var cus_to: String = ""
    var groupid: String = ""
    var grouptype: String = ""
    var cus_msg: String = ""

    override fun showMessage(viewHolder: ChatViewHolder, context: Context) {
    }

    init {
        type = MessageContentType.TYPE_CUSTOM
    }

    override fun getRemoteUrl(callback: OnMessageDownloadCallback) {
    }

    override fun getSummaryText(): String {
        return "[自定义消息]"
    }

}