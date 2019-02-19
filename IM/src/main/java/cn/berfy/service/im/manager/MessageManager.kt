package cn.berfy.service.im.manager

import cn.berfy.sdk.http.callback.RequestCallBack
import cn.berfy.sdk.http.model.NetError
import cn.berfy.sdk.http.model.NetResponse
import cn.berfy.sdk.mvpbase.util.DeviceUtils
import cn.berfy.sdk.mvpbase.util.LogF
import cn.berfy.service.im.http.ConversationApi
import cn.berfy.service.im.http.MessageApi
import cn.berfy.service.im.manager.i.IMCallback
import cn.berfy.service.im.model.Message
import cn.berfy.service.im.model.MessageType
import cn.berfy.service.im.model.conversation.Conversation
import cn.berfy.service.im.model.response.ConversationResponseData

/**
author: Berfy
date: 2018/12/26
消息管理类
 */
class MessageManager {

    private val TAG = "IM消息管理"

    companion object {

        private var mInstance: MessageManager? = null

        val instance: MessageManager
            @Synchronized
            get() {
                if (mInstance == null) {
                    synchronized(MessageManager::class.java) {
                        if (mInstance == null) {
                            mInstance = MessageManager()
                        }
                    }
                }
                return mInstance!!
            }
    }

    //获取本地会话消息列表
    fun getLocalMessages(
        conversation: Conversation,
        lastMsg: Message,
        count: Int,
        onDataCallback: CacheManager.OnDataCallback<Message>
    ) {
        if (!IMManager.isInstanced() || !IMManager.instance.isConnected) {
            onDataCallback.onFailed("IM服务未启动")
        } else {
            CacheManager.instance.getMessages(conversation, lastMsg, count, onDataCallback)
        }
    }

    /**
     * 获取历史消息
     * @param msgId 最老一条的消息 默认为空或0
     * @param count 一次获取条数
     * */
    fun getMessage(
        conversation: Conversation,
        loginId: String,
        toId: String,
        msgId: String,
        count: Int,
        callback: RequestCallBack<ArrayList<Message>>
    ) {
        if (!IMManager.isInstanced() || !IMManager.instance.isConnected) {
            LogF.d(TAG, "IM服务未启动")
            return
        }
        var topic = ""
        val appId = DeviceUtils.getMetaDataFromApp(IMManager.instance.getContext(), "IM_APPID")
        when (conversation.type) {
            MessageType.TYPE_P2P -> {
                topic = "/$appId/22/p2p/${conversation.toId}"
            }
            MessageType.TYPE_GROUP -> {
                topic = "/$appId/22/group/${conversation.toId}"
            }
            MessageType.TYPE_CHATROOM -> {
                topic = "/$appId/22/chat/${conversation.toId}"
            }
            MessageType.TYPE_CUSTOM -> {
                topic = "/$appId/22/notify/sys"
            }
        }
        MessageApi.instance.getMessage(loginId, toId, msgId, count, callback)
    }

    /**
     * 获取会话的指定消息
     * 统一通过消息监听收取
     * @param conversation 会话
     * @param msgId 最老的消息id
     * @param count 拉去数量
     * */
    fun getMessageOld(conversation: Conversation, msgId: String, count: Int) {
        if (!IMManager.isInstanced() || !IMManager.instance.isConnected) {
            LogF.d(TAG, "IM服务未启动")
            return
        }
        conversation.getMessage(msgId, 10)
        //测试代码
        try {
            //测试代码 未确定会话的topic
            IMManager.instance.getMessageFromConversation(conversation, msgId, count)
        } catch (e: NullPointerException) {
            e.printStackTrace()
        }
    }

    //删除指定消息
    fun deleteMessage(msg: Message, callback: IMCallback?) {
        if (!IMManager.isInstanced() || !IMManager.instance.isConnected) {
            LogF.e(TAG, "IM服务未启动")
            if (null != callback)
                callback.onFailed("IM服务未启动")
        } else {
            CacheManager.instance.deleteMessage(
                msg, callback
            )
        }
    }
}