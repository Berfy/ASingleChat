package cn.berfy.service.im.model.conversation

import cn.berfy.service.im.model.Message

/**
author: Berfy
date: 2019/1/25
普通会话
 */
class NormalConversation constructor(conversation: Conversation) {

    var conversation: Conversation //会话
    var lastMessage: Message? = null

    init {
        this.conversation = conversation
    }

    //获取最后一条消息描述文字
    fun getSummaryText(): String {
        if (null != lastMessage) {
            return lastMessage!!.getSummaryText()
        }
        return ""
    }
}