package cn.berfy.service.im.model

import android.text.TextUtils
import cn.berfy.service.im.manager.IMManager
import cn.berfy.service.im.model.conversation.Conversation

/**
author: Berfy
date: 2018/12/25
消息转换
消息  接收到的消息原型
 */
class RawMessage {

    companion object {
        const val TYPE_IMAGE = 1
        const val TYPE_VOICE = 2
        const val TYPE_VIDEO = 3
        const val TYPE_FILE = 4
    }

    var rowid: String = ""
    var id: String = ""//消息id  服务端id
    var rawId: String = ""//创建id
    var topic: String = ""//话题
    var acked: Boolean? = false//消息是否已读
    var senderId: String? = ""//发送方 id(群id)
    var senderName: String? = ""//发送方 设备名称
    var payload: String? = ""//消息内容
    var time: Long = 0L//发送时间
    var type: MessageType = MessageType.TYPE_P2P//消息类型 say聊天  login登录 logout注销
    var chatType: MessageContentType = MessageContentType.TYPE_TEXT//消息内容类型

    fun createConversation(): Conversation? {
        //生成会话 如果查找对方id 如果接收消息（发送方是对方） toId = fromId  如果发送消息toId就是toId
        var conversation: Conversation? = null//接收消息
        when (type) {
            MessageType.TYPE_CUSTOM -> {
                conversation = Conversation(senderId!!, MessageType.TYPE_CUSTOM)
            }
            MessageType.TYPE_SYSTEM -> {
                conversation = Conversation(senderId!!, MessageType.TYPE_SYSTEM)
            }
            MessageType.TYPE_P2P -> {
                conversation = Conversation(senderId!!, MessageType.TYPE_P2P)
            }
            MessageType.TYPE_GROUP -> {
                conversation =
                    Conversation(topic.substring(topic.lastIndexOf("/") + 1, topic.length), MessageType.TYPE_GROUP)
            }
            MessageType.TYPE_CHATROOM -> {
                conversation =
                    Conversation(topic.substring(topic.lastIndexOf("/") + 1, topic.length), MessageType.TYPE_CHATROOM)
            }
            else -> {
                conversation = Conversation(senderId!!, MessageType.TYPE_P2P)
            }
        }
        return conversation
    }
}