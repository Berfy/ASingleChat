package cn.berfy.service.im.model.group

import android.content.Context
import cn.berfy.service.im.manager.i.OnMessageDownloadCallback
import cn.berfy.service.im.model.*

/**
author: Berfy
date: 2018/12/25
群系统消息 具体类型参照
 */
class MessageGroupSystem : Message() {

    val system_type: MessageGroupSystemType = MessageGroupSystemType.TYPE_FIRST_WELCOME
    var system_content: String = ""

    override fun getRemoteUrl(callback: OnMessageDownloadCallback) {
    }

    override fun showMessage(viewHolder: ChatViewHolder, context: Context) {
    }

    override fun getSummaryText(): String {
        return ""
    }

}