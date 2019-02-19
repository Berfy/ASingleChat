package cn.berfy.service.im.model.conversation

import android.text.TextUtils
import cn.berfy.sdk.http.callback.RequestCallBack
import cn.berfy.sdk.mvpbase.util.LogF
import cn.berfy.service.im.manager.CacheManager
import cn.berfy.service.im.manager.ConversationManager
import cn.berfy.service.im.manager.IMManager
import cn.berfy.service.im.manager.MessageManager
import cn.berfy.service.im.manager.i.IMCallback
import cn.berfy.service.im.manager.i.OnMessageSendingCallback
import cn.berfy.service.im.model.Message
import cn.berfy.service.im.model.MessageType
import com.google.gson.annotations.SerializedName
import java.lang.NullPointerException

/**
author: Berfy
date: 2018/12/26
IM会话(双方id  会话类型) 核心会话
 */
class Conversation constructor(toId: String, type: MessageType) {

    val TAG: String = "IM聊天_会话"

    @SerializedName("uid")
    var toId: String = ""//接收方ID
    var type: MessageType = MessageType.TYPE_P2P
        get() :MessageType {
            if (!TextUtils.isEmpty(ope)) {
                return if (ope == "0") MessageType.TYPE_P2P else if ((ope == "1")) MessageType.TYPE_CHATROOM else MessageType.TYPE_GROUP
            } else {
                return field
            }
        }

    var avatar: String = "" //头像
    var ope: String = "" //0 p2p 1 chatroom
    var title: String = "" //会话标题
    var lastMessageTime: Long = 0L //预留的最后一条消息时间
    var lastMessage: String = "" //预留的最后一条消息
    var isNotNotify: Boolean = false//是否打开免打扰
    var unreadNum: Long = 0//未读消息数

    init {
        this.toId = toId
        this.type = type
    }

    //删除会话
    fun delete() {
        ConversationManager.instance.deleteConversation(toId, type, null)
    }

    fun deleteMsg(msg: Message?) {
        if (null == msg) {
            LogF.d(TAG, "消息为空")
            return
        }
        MessageManager.instance.deleteMessage(msg, null)
    }

    fun sendMessage(msg: Message) {
        if (null == msg.conversation) {
            msg.conversation = this
        }
        try {
            IMManager.instance.sendMessage(this, msg)
        } catch (e: NullPointerException) {
            e.printStackTrace()
        }
    }

    fun sendMessage(msg: Message, callback: OnMessageSendingCallback) {
        if (null == msg.conversation) {
            msg.conversation = this
        }
        msg.mSendingCallbacks.add(callback)
        try {
            IMManager.instance.sendMessage(this, msg)
        } catch (e: NullPointerException) {
            e.printStackTrace()
        }
    }

    fun getUnReadCount(callback: OnUnreadCountCallback): Long {
        return 0
    }

    /**
     * 清空未读消息记录
     * */
    fun markReadAll() {
        unreadNum = 0
        CacheManager.instance.updateConversation(this, null)
    }

    /**
     * 获取历史消息
     * @param msgId 最老一条的消息 默认为空或0
     * @param count 一次获取条数
     * */
    fun getMessage(
        loginId: String,
        toId: String,
        msgId: String,
        count: Int,
        callback: RequestCallBack<ArrayList<Message>>
    ) {
        MessageManager.instance.getMessage(this, loginId, toId, msgId, count, callback)
    }

    /**
     * 统一通过消息监听收取
     * @param msgId 最老一条的消息 默认为空或0
     * @param count 一次获取条数
     * */
    fun getMessage(msgId: String, count: Int) {
        //测试代码
        try {
            //测试代码 未确定会话的topic
            IMManager.instance.getMessageFromConversation(this, msgId, count)
        } catch (e: NullPointerException) {
            e.printStackTrace()
        }
    }

    interface OnUnreadCountCallback {
        fun unReadCount(count: Int)
    }

}