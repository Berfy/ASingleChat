package cn.berfy.service.im.model.group

import android.content.Context
import cn.berfy.service.im.manager.i.OnMessageDownloadCallback
import cn.berfy.service.im.model.ChatViewHolder
import cn.berfy.service.im.model.Message

/**
author: Berfy
date: 2018/12/25
群提示消息 参照MessageGroupTipType
 */
class MessageGroupTip : Message() {

    var tip: String = ""
    val tip_type: MessageGroupTipType = MessageGroupTipType.TYPE_MODIFY_INFO

    override fun getRemoteUrl(callback: OnMessageDownloadCallback) {
    }

    override fun showMessage(viewHolder: ChatViewHolder, context: Context) {
    }

    override fun getSummaryText(): String {
        return ""
    }

}