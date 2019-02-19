package cn.berfy.service.im.manager

import android.content.Context
import android.os.Handler
import android.text.TextUtils
import android.util.Log
import cn.berfy.sdk.mvpbase.config.Constant
import cn.berfy.sdk.mvpbase.util.GsonUtil
import cn.berfy.sdk.mvpbase.util.LogF
import cn.berfy.service.im.cache.db.IMDatabase
import cn.berfy.service.im.cache.db.tab.ConversationTab
import cn.berfy.service.im.cache.db.tab.ConversationTab_Table
import cn.berfy.service.im.cache.db.tab.MessageTab
import cn.berfy.service.im.cache.db.tab.MessageTab_Table
import cn.berfy.service.im.manager.i.OnMessageListener
import cn.berfy.service.im.manager.i.IMCallback
import cn.berfy.service.im.model.*
import cn.berfy.service.im.model.conversation.Conversation
import cn.berfy.service.im.util.MessageTextUtil
import com.raizlabs.android.dbflow.config.FlowManager
import com.raizlabs.android.dbflow.sql.language.OrderBy
import com.raizlabs.android.dbflow.sql.language.SQLite
import com.raizlabs.android.dbflow.structure.database.transaction.ProcessModelTransaction
import org.json.JSONException
import org.json.JSONObject
import java.lang.NullPointerException

/**
author: Berfy
date: 2019/1/24
IM存储管理器
 */
class CacheManager {

    private val TAG = "IM_存储"
    private var mHandler: MyHandler? = null
    private var mIsStart = false

    init {
        mHandler = MyHandler()
    }

    fun start() {
        if (!mIsStart) {
            mIsStart = true
            IMManager.instance.addMessageListener(mMessageListener)
        }
    }

    //消息监听
    private val mMessageListener: OnMessageListener = object : OnMessageListener {

        override fun newMessage(message: Message?) {
            if (null == message) {
                return
            }
            if (null == message.conversation) {
                LogF.d(TAG, "没有会话")
                return
            }
            if (null != message.conversation && message.conversation!!.type == MessageType.TYPE_P2P
                && message.senderId == IMManager.instance.getLoginId()
            ) {
                LogF.d(TAG, "单聊 自己的消息 作废")
                return
            }
            if (null != message.conversation) {
                message.conversation!!.lastMessage = message.getSummaryText()
                message.conversation!!.unreadNum = 1
                message.conversation!!.lastMessageTime = message.time
            }
            addConversation(message.conversation)
            addMessage(message)
        }

        override fun systemMessage(message: MessageCustom?) {
            if (null == message) {
                return
            }
            if (null != message.conversation && message.conversation!!.type == MessageType.TYPE_P2P && message.senderId
                == IMManager.instance.getLoginId()
            ) {
                LogF.d(TAG, "单聊 自己的消息 作废")
                return
            }
        }

        override fun refreshConversation() {
        }

        override fun sendMessageStatus(message: String?, isSuc: Boolean) {
        }
    }

    fun getMessages(conversation: Conversation, lastMsg: Message, count: Int, onDataCallback: OnDataCallback<Message>) {
        Constant.EXECUTOR.execute(object : Runnable {
            override fun run() {
                //时间正序
                var type = ""
                when (conversation.type) {
                    MessageType.TYPE_P2P -> {
                        LogF.d(TAG, "会话类型-p2p")
                        type = "p2p"
                    }
                    MessageType.TYPE_GROUP -> {
                        LogF.d(TAG, "会话类型-group")
                        type = "group"
                    }
                    MessageType.TYPE_CHATROOM -> {
                        LogF.d(TAG, "会话类型-chatroom")
                        type = "chatroom"
                    }
                    else -> {
                        LogF.d(TAG, "其他类型会话")
                    }
                }
                val cacheMsgs = SQLite.select()
                    .from(MessageTab::class.java)
                    .where(
                        MessageTab_Table.user_id.`is`(IMManager.instance.getLoginId()),
                        MessageTab_Table.msg_id.lessThan(lastMsg.id),
                        MessageTab_Table.conversation_id.`is`(conversation.toId),
                        MessageTab_Table.type.`is`(type)
                    )
                    .limit(count)
                    .orderBy(OrderBy.fromNameAlias(MessageTab_Table.last_update_time.nameAlias).ascending())
                    .queryList()
                LogF.d(TAG, "获取本地消息数量 ${cacheMsgs.size}")
                val msgs = ArrayList<Message>()
                for (cacheMsg in cacheMsgs) {
                    LogF.d(TAG, "获取本地库会话 表数据=${GsonUtil.getInstance().toJson(cacheMsg)}")
                    val newMessage: Message? = MessageTextUtil.tabToMessage(cacheMsg)
                    if (null != newMessage) {
                        when (cacheMsg.chat_type) {
                            "text" -> {
                                val newMessage = MessageText()
                                newMessage.id = cacheMsg.msg_id
                                try {
                                    val payload = JSONObject(cacheMsg.content)
                                    newMessage.content = payload.optString("msg")
                                } catch (e: JSONException) {
                                    e.printStackTrace()
                                }
                            }
                        }
                        conversation.lastMessageTime =
                            cacheMsg.last_update_time
                        LogF.d(TAG, "获取本地库会话 ${GsonUtil.getInstance().toJson(conversation)}")
                        newMessage.conversation = conversation
                        msgs.add(newMessage)
                    }
                }
                mHandler!!.post(object : Runnable {
                    override fun run() {
                        onDataCallback.onSuc(msgs)
                    }
                })
            }
        })
    }

    fun addMessage(msg: Message): Boolean {
        if (!IMManager.isInstanced()) {
            return false
        }
        if (null == msg.conversation) {
            LogF.d(TAG, "存储会话 失败 null == conversation")
            return false
        }
        LogF.d(
            TAG, "存储消息 id=${msg.id} toId=${msg.conversation!!.toId} type=${msg.conversation!!.type}" +
                    " type=${msg.type} time=${msg.time}"
        )
//        val messageTab = MessageTab()
//        messageTab.user_id = IMManager.instance.getLoginId()
//        messageTab.msg_id = msg.id
//        messageTab.raw_id = msg.rawId
//        messageTab.read = if (msg.acked) 1 else 0
//        messageTab.send_status =
//            if (msg.sendStatus == Message.STATUS_SEND) 0 else if (msg.sendStatus == Message.STATUS_SEND_SUC) 1 else 2
//        messageTab.conversation_id = msg.conversation!!.toId
//        when (msg.conversation!!.type) {
//            MessageType.TYPE_P2P -> {
//                LogF.d(TAG, "会话类型-p2p")
//                messageTab.type = "p2p"
//            }
//            MessageType.TYPE_GROUP -> {
//                LogF.d(TAG, "会话类型-group")
//                messageTab.type = "group"
//            }
//            MessageType.TYPE_CHATROOM -> {
//                LogF.d(TAG, "会话类型-chatroom")
//                messageTab.type = "chatroom"
//            }
//            else -> {
//                LogF.d(TAG, "其他类型会话 不操作")
//                return false
//            }
//        }
//        when (msg.type) {
//            MessageContentType.TYPE_TEXT -> {
//                LogF.d(TAG, "消息类型-text")
//                messageTab.chat_type = "image"
//            }
//            MessageContentType.TYPE_IMAGE -> {
//                LogF.d(TAG, "消息类型-image")
//                messageTab.chat_type = "image"
//            }
//            MessageContentType.TYPE_VOICE -> {
//                LogF.d(TAG, "消息类型-voice")
//                messageTab.chat_type = "voice"
//            }
//            MessageContentType.TYPE_VIDEO -> {
//                LogF.d(TAG, "消息类型-video")
//                messageTab.chat_type = "video"
//            }
//            MessageContentType.TYPE_FILE -> {
//                LogF.d(TAG, "消息类型-file")
//                messageTab.chat_type = "file"
//            }
//            MessageContentType.TYPE_LOCATION -> {
//                LogF.d(TAG, "消息类型-location")
//                messageTab.chat_type = "location"
//            }
//            MessageContentType.TYPE_CUSTOM -> {
//                LogF.d(TAG, "消息类型-custom")
//                messageTab.chat_type = "custom"
//            }
//            else -> {
//                LogF.d(TAG, "其他消息类型 不操作")
//                return false
//            }
//        }
//
//        //查询会话是否已存在
//        val cacheConversation = SQLite.select()
//            .from(ConversationTab::class.java)
//            .where(
//                ConversationTab_Table.peer.`is`(conversationTab.peer),
//                ConversationTab_Table.type.`is`(conversationTab.type),
//                ConversationTab_Table.user_id.`is`(conversationTab.user_id)
//            ).querySingle()
        var isSuc = false
//        if (null != cacheConversation) {
//            cacheConversation.last_update_time = System.currentTimeMillis()
//            if (!TextUtils.isEmpty(conversation.title))
//                cacheConversation.title = conversation.title
//            if (!TextUtils.isEmpty(conversation.lastMessage))
//                cacheConversation.last_message = conversation.lastMessage
//            cacheConversation.unread_count = conversation.unreadNum
//            if (conversation.lastMessageTime > 0)
//                cacheConversation.last_message_time = conversation.lastMessageTime
//            isSuc = cacheConversation.update()
//            LogF.d(TAG, "存储会话 更新")
//        } else {
//            if (!TextUtils.isEmpty(conversation.title))
//                conversationTab.title = conversation.title
//            if (!TextUtils.isEmpty(conversation.lastMessage))
//                conversationTab.last_message = conversation.lastMessage
//            conversationTab.unread_count = conversation.unreadNum
//            conversationTab.last_message_time = conversation.lastMessageTime
//            conversationTab.last_update_time = System.currentTimeMillis()
//            isSuc = conversationTab.save()
//            LogF.d(TAG, "存储会话 添加")
//        }
//        if (isSuc) {
//            LogF.d(TAG, "存储会话 成功")
//        } else {
//            LogF.d(TAG, "存储会话 失败")
//        }
        return isSuc
    }

    fun updateMessage(msg: Message, callback: IMCallback?) {
        if (!IMManager.isInstanced()) {
            return
        }
    }

    fun deleteMessage(msg: Message?, callback: IMCallback?) {
        LogF.d(TAG, "删除消息")
        if (!IMManager.isInstanced()) {
            LogF.d(TAG, "删除消息 失败 服务器未连接")
            if (null != callback) {
                callback.onFailed("删除消息 失败 服务器未连接")
            }
            return
        }
        if (null == msg) {
            LogF.d(TAG, "删除消息 失败 null == msg")
            if (null != callback) {
                callback.onFailed("null == msg")
            }
            return
        }
        if (null == msg.conversation) {
            LogF.d(TAG, "删除消息 失败 null == conversation")
            if (null != callback) {
                callback.onFailed("null == conversation")
            }
            return
        }
        LogF.d(TAG, "删除消息 会话信息 msgId=${msg.id} toId=${msg.conversation!!.toId} type=${msg.conversation!!.type}")
        Constant.EXECUTOR.execute(object : Runnable {
            override fun run() {
                var type = ""
                when (msg.conversation!!.type) {
                    MessageType.TYPE_P2P -> {
                        LogF.d(TAG, "会话类型-p2p")
                        type = "p2p"
                    }
                    MessageType.TYPE_GROUP -> {
                        LogF.d(TAG, "会话类型-group")
                        type = "group"
                    }
                    MessageType.TYPE_CHATROOM -> {
                        LogF.d(TAG, "会话类型-chatroom")
                        type = "chatroom"
                    }
                    else -> {
                        LogF.d(TAG, "其他类型会话 不操作")
                        return
                    }
                }
                val conversationTab = SQLite.select()
                    .from(MessageTab::class.java)
                    .where(
                        MessageTab_Table.msg_id.`is`(msg.id),
                        MessageTab_Table.user_id.`is`(IMManager.instance.getLoginId())
                    )
                    .querySingle()
                if (null != conversationTab) {
                    LogF.d(TAG, "存在 删除")
                    LogF.d(TAG, "删除消息 表条件 conversationTab=${GsonUtil.getInstance().toJson(conversationTab)}")
                    val isSuc = conversationTab.delete()
                    mHandler!!.post(object : Runnable {
                        override fun run() {
                            if (isSuc) {
                                LogF.d(TAG, "删除消息 成功")
                                if (null != callback) {
                                    callback.onSuc()
                                }
                            } else {
                                LogF.d(TAG, "删除消息 失败")
                                if (null != callback) {
                                    callback.onFailed("删除消息失败")
                                }
                            }
                        }
                    })
                }

            }
        })
    }

    fun getConversations(onDataCallback: OnDataCallback<Conversation>) {
        LogF.d(TAG, "获取本地会话")
        if (!IMManager.isInstanced()) {
            LogF.d(TAG, "获取本地会话 失败 服务器未连接")
            return
        }
        Constant.EXECUTOR.execute(object : Runnable {
            override fun run() {
                //时间倒叙
                val cacheConversations = SQLite.select()
                    .from(ConversationTab::class.java)
                    .where(ConversationTab_Table.user_id.`is`(IMManager.instance.getLoginId()))
                    .orderBy(OrderBy.fromNameAlias(ConversationTab_Table.last_update_time.nameAlias).descending())
                    .queryList()
                LogF.d(TAG, "获取本地库会话数量 ${cacheConversations.size}")
                val conversations = ArrayList<Conversation>()
                for (cacheConversation in cacheConversations) {
                    LogF.d(TAG, "获取本地库会话 表数据=${GsonUtil.getInstance().toJson(cacheConversation)}")
                    var conversation: Conversation? = null
                    when (cacheConversation.type) {
                        "p2p" -> {
                            LogF.d(TAG, "会话类型-p2p")
                            conversation = Conversation(
                                cacheConversation.peer,
                                MessageType.TYPE_P2P
                            )
                        }
                        "group" -> {
                            LogF.d(TAG, "会话类型-group")
                            conversation = Conversation(
                                cacheConversation.peer,
                                MessageType.TYPE_GROUP
                            )
                        }
                        "chatroom" -> {
                            LogF.d(TAG, "会话类型-chatroom")
                            conversation = Conversation(
                                cacheConversation.peer,
                                MessageType.TYPE_CHATROOM
                            )
                        }
                        else -> {
                            LogF.d(TAG, "其他类型会话")
                        }
                    }
                    if (null != conversation) {
                        conversation.title = cacheConversation.title
                        conversation.unreadNum = cacheConversation.unread_count
                        conversation.lastMessage = cacheConversation.last_message
                        conversation.lastMessageTime = cacheConversation.last_message_time
                        LogF.d(TAG, "获取本地库会话 ${GsonUtil.getInstance().toJson(conversation)}")
                        conversations.add(conversation)
                    }
                }
                mHandler!!.post(object : Runnable {
                    override fun run() {
                        onDataCallback.onSuc(conversations)
                    }
                })
            }
        })
    }

    fun addConversation(conversation: Conversation?): Boolean {
        LogF.d(TAG, "存储会话 start")
        if (!IMManager.isInstanced()) {
            LogF.d(TAG, "存储会话 失败 服务器未连接")
            return false
        }
        if (null == conversation) {
            LogF.d(TAG, "存储会话 失败 null == conversation")
            return false
        }
        LogF.d(
            TAG, "存储会话 会话信息 toId=${conversation.toId} type=${conversation.type}" +
                    " title=${conversation.title} lastMessage=${conversation.lastMessage} lastMessageTime=${conversation.lastMessageTime}"
        )
        val conversationTab = ConversationTab()
        conversationTab.user_id = IMManager.instance.getLoginId()
        conversationTab.peer = conversation.toId
        when (conversation.type) {
            MessageType.TYPE_P2P -> {
                LogF.d(TAG, "会话类型-p2p")
                conversationTab.type = "p2p"
            }
            MessageType.TYPE_GROUP -> {
                LogF.d(TAG, "会话类型-group")
                conversationTab.type = "group"
            }
            MessageType.TYPE_CHATROOM -> {
                LogF.d(TAG, "会话类型-chatroom")
                conversationTab.type = "chatroom"
            }
            else -> {
                LogF.d(TAG, "其他类型会话 不操作")
                return false
            }
        }
        //查询会话是否已存在
        val cacheConversation = SQLite.select()
            .from(ConversationTab::class.java)
            .where(
                ConversationTab_Table.peer.`is`(conversationTab.peer),
                ConversationTab_Table.type.`is`(conversationTab.type),
                ConversationTab_Table.user_id.`is`(conversationTab.user_id)
            ).querySingle()
        var isSuc = false
        if (null != cacheConversation) {
            cacheConversation.last_update_time = System.currentTimeMillis()
            if (!TextUtils.isEmpty(conversation.title))
                cacheConversation.title = conversation.title
            if (!TextUtils.isEmpty(conversation.lastMessage))
                cacheConversation.last_message = conversation.lastMessage
            cacheConversation.unread_count = conversation.unreadNum
            if (conversation.lastMessageTime > 0)
                cacheConversation.last_message_time = conversation.lastMessageTime
            isSuc = cacheConversation.update()
            LogF.d(TAG, "存储会话 更新")
        } else {
            if (!TextUtils.isEmpty(conversation.title))
                conversationTab.title = conversation.title
            if (!TextUtils.isEmpty(conversation.lastMessage))
                conversationTab.last_message = conversation.lastMessage
            conversationTab.unread_count = conversation.unreadNum
            conversationTab.last_message_time = conversation.lastMessageTime
            conversationTab.last_update_time = System.currentTimeMillis()
            isSuc = conversationTab.save()
            LogF.d(TAG, "存储会话 添加")
        }
        if (isSuc) {
            LogF.d(TAG, "存储会话 成功")
        } else {
            LogF.d(TAG, "存储会话 失败")
        }
        return isSuc
    }

    fun updateConversation(conversation: Conversation?, callback: IMCallback?) {
        LogF.d(TAG, "更新会话 start")
        if (!IMManager.isInstanced()) {
            LogF.d(TAG, "存储会话 失败 服务器未连接")
            if (null != callback) {
                callback.onFailed("服务器未连接")
            }
            return
        }
        if (null == conversation) {
            LogF.d(TAG, "存储会话 失败 null == conversation")
            if (null != callback) {
                callback.onFailed("null == conversation")
            }
            return
        }
        LogF.d(TAG, "更新会话 会话信息 toId=${conversation.toId} type=${conversation.type}")
        Constant.EXECUTOR.execute(object : Runnable {
            override fun run() {
                val conversationTab = ConversationTab()
                conversationTab.peer = conversation.toId
                when (conversation.type) {
                    MessageType.TYPE_P2P -> {
                        LogF.d(TAG, "会话类型-p2p")
                        conversationTab.type = "p2p"
                    }
                    MessageType.TYPE_GROUP -> {
                        LogF.d(TAG, "会话类型-group")
                        conversationTab.type = "group"
                    }
                    MessageType.TYPE_CHATROOM -> {
                        LogF.d(TAG, "会话类型-chatroom")
                        conversationTab.type = "chatroom"
                    }
                    else -> {
                        LogF.d(TAG, "其他类型会话 不操作")
                        return
                    }
                }
                conversationTab.user_id = IMManager.instance.getLoginId()
                val cacheConversation = SQLite.select()
                    .from(ConversationTab::class.java)
                    .where(
                        ConversationTab_Table.peer.`is`(conversationTab.peer),
                        ConversationTab_Table.type.`is`(conversationTab.type),
                        ConversationTab_Table.user_id.`is`(conversationTab.user_id)
                    ).querySingle()
                var isSuc = false
                if (null != cacheConversation) {
                    if (!TextUtils.isEmpty(conversation.title))
                        cacheConversation.title = conversation.title
                    if (!TextUtils.isEmpty(conversation.lastMessage))
                        cacheConversation.last_message = conversation.lastMessage
                    if (conversation.lastMessageTime > 0)
                        cacheConversation.last_message_time = conversation.lastMessageTime
                    cacheConversation.unread_count = conversation.unreadNum
                    cacheConversation.last_update_time = System.currentTimeMillis()
                    isSuc = cacheConversation.update()
                    LogF.d(TAG, "存储会话 更新")
                } else {
                    if (!TextUtils.isEmpty(conversation.title))
                        conversationTab.title = conversation.title
                    if (!TextUtils.isEmpty(conversation.lastMessage))
                        conversationTab.last_message = conversation.lastMessage
                    conversationTab.unread_count = conversation.unreadNum
                    conversationTab.last_message_time = conversation.lastMessageTime
                    conversationTab.last_update_time = System.currentTimeMillis()
                    isSuc = conversationTab.save()
                    LogF.d(TAG, "存储会话 添加")
                }
                mHandler!!.post(object : Runnable {
                    override fun run() {
                        if (isSuc) {
                            LogF.d(TAG, "更新会话 成功")
                            if (null != callback) {
                                callback.onSuc()
                            }
                        } else {
                            LogF.d(TAG, "更新会话 失败")
                            if (null != callback) {
                                callback.onFailed("更新失败")
                            }
                        }
                    }
                })
            }
        })
    }

    fun addConversations(conversations: List<Conversation>, callback: IMCallback?) {
        LogF.d(TAG, "存储会话 start")
        if (!IMManager.isInstanced()) {
            LogF.d(TAG, "存储会话 失败 服务器未连接")
            if (null != callback) {
                callback.onFailed("服务器未连接")
            }
            return
        }

        val tabs = ArrayList<ConversationTab>()

        for (conversation: Conversation? in conversations) {
            if (null == conversation) {
                LogF.d(TAG, "存储会话 失败 null == conversation")
                if (null != callback) {
                    callback.onFailed("null == conversation")
                }
                return
            }
            LogF.d(TAG, "存储会话 会话信息 toId=${conversation.toId} type=${conversation.type}")
            val conversationTab = ConversationTab()
            conversationTab.peer = conversation.toId
            if (!TextUtils.isEmpty(conversation.title))
                conversationTab.title = conversation.title
            if (!TextUtils.isEmpty(conversation.lastMessage))
                conversationTab.last_message = conversation.lastMessage
            conversationTab.unread_count = conversation.unreadNum
            conversationTab.last_message_time = conversation.lastMessageTime
            when (conversation.type) {
                MessageType.TYPE_P2P -> {
                    LogF.d(TAG, "会话类型-p2p")
                    conversationTab.type = "p2p"
                }
                MessageType.TYPE_GROUP -> {
                    LogF.d(TAG, "会话类型-group")
                    conversationTab.type = "group"
                }
                MessageType.TYPE_CHATROOM -> {
                    LogF.d(TAG, "会话类型-chatroom")
                    conversationTab.type = "chatroom"
                }
                else -> {
                    LogF.d(TAG, "其他类型会话 不操作")
                    return
                }
            }
            conversationTab.user_id = IMManager.instance.getLoginId()
            tabs.add(conversationTab)
        }
        insertConversationsAsync(tabs, callback)
    }

    //批量插入会话
    private fun insertConversationsAsync(datas: ArrayList<ConversationTab>, callback: IMCallback?) {
        FlowManager.getDatabase(IMDatabase::class.java)
            .beginTransactionAsync(
                ProcessModelTransaction.Builder(
                    ProcessModelTransaction.ProcessModel<ConversationTab> { conversationTab, wrapper ->
                        //查询会话是否已存在
                        val cacheConversation = SQLite.select()
                            .from(ConversationTab::class.java)
                            .where(
                                ConversationTab_Table.peer.`is`(conversationTab.peer),
                                ConversationTab_Table.type.`is`(conversationTab.type),
                                ConversationTab_Table.user_id.`is`(conversationTab.user_id)
                            ).querySingle()
                        var isSuc = false
                        if (null != cacheConversation) {
                            if (!TextUtils.isEmpty(conversationTab.title))
                                cacheConversation.title = conversationTab.title
                            if (!TextUtils.isEmpty(conversationTab.last_message))
                                cacheConversation.last_message = conversationTab.last_message
                            cacheConversation.unread_count = conversationTab.unread_count
                            cacheConversation.last_message_time = conversationTab.last_message_time
                            cacheConversation.last_update_time = System.currentTimeMillis()
                            isSuc = cacheConversation.update()
                            LogF.d(TAG, "存储会话 更新")
                        } else {
                            conversationTab.last_update_time = System.currentTimeMillis()
                            isSuc = conversationTab.save()
                            LogF.d(TAG, "存储会话 添加")
                        }
                        Log.i(TAG, "批量操作ing  $isSuc")
                    }).addAll(datas).build()
            )
            .error { transaction, error ->
                LogF.d(TAG, "存储会话 失败")
                if (null != callback) {
                    callback.onFailed(if (null != error.message) error.message!! else "存储会话失败")
                }
            }
            .success {
                LogF.d(TAG, "存储会话 成功")
                if (null != callback) {
                    callback.onSuc()
                }
            }.build().executeSync()
    }

    fun deleteConversation(conversation: Conversation?, callback: IMCallback?) {
        LogF.d(TAG, "删除会话 start")
        if (!IMManager.isInstanced()) {
            LogF.d(TAG, "删除会话 失败 服务器未连接")
            if (null != callback) {
                callback.onFailed("服务器未连接")
            }
            return
        }
        if (null == conversation) {
            LogF.d(TAG, "删除会话 失败 null == conversation")
            if (null != callback) {
                callback.onFailed("null == conversation")
            }
            return
        }
        LogF.d(TAG, "删除会话 会话信息 toId=${conversation.toId} type=${conversation.type}")
        Constant.EXECUTOR.execute(object : Runnable {
            override fun run() {
                var type = ""
                when (conversation.type) {
                    MessageType.TYPE_P2P -> {
                        LogF.d(TAG, "会话类型-p2p")
                        type = "p2p"
                    }
                    MessageType.TYPE_GROUP -> {
                        LogF.d(TAG, "会话类型-group")
                        type = "group"
                    }
                    MessageType.TYPE_CHATROOM -> {
                        LogF.d(TAG, "会话类型-chatroom")
                        type = "chatroom"
                    }
                    else -> {
                        LogF.d(TAG, "其他类型会话 不操作")
                        return
                    }
                }
                val conversationTab = SQLite.select()
                    .from(ConversationTab::class.java)
                    .where(
                        ConversationTab_Table.peer.`is`(conversation.toId),
                        ConversationTab_Table.type.`is`(type),
                        ConversationTab_Table.user_id.`is`(IMManager.instance.getLoginId())
                    )
                    .querySingle()
                if (null != conversationTab) {
                    LogF.d(TAG, "存在 删除")
                    LogF.d(TAG, "删除会话 表条件 conversationTab=${GsonUtil.getInstance().toJson(conversationTab)}")
                    val isSuc = conversationTab.delete()
                    mHandler!!.post(object : Runnable {
                        override fun run() {
                            if (isSuc) {
                                LogF.d(TAG, "删除会话 成功")
                                if (null != callback) {
                                    callback.onSuc()
                                }
                            } else {
                                LogF.d(TAG, "删除会话 失败")
                                if (null != callback) {
                                    callback.onFailed("删除会话失败")
                                }
                            }
                        }
                    })
                }

            }
        })
    }

    fun deleteAllConversation(callback: IMCallback?) {
        LogF.d(TAG, "删除所有会话 start")
        if (!IMManager.isInstanced()) {
            LogF.d(TAG, "删除所有会话 失败 服务器未连接")
            if (null != callback) {
                callback.onFailed("服务器未连接")
            }
            return
        }
        SQLite.delete(ConversationTab::class.java).execute()
        LogF.d(TAG, "删除所有会话 完成")
    }

    fun stop() {
        mIsStart = false
        IMManager.instance.removeMessageListener(mMessageListener)
    }

    companion object {

        private var mInstance: CacheManager? = null

        val instance: CacheManager
            @Synchronized
            get() {
                if (null == mInstance) {
                    throw NullPointerException("未初始化存储管理器")
                }
                return mInstance!!
            }

        @Synchronized
        fun init(context: Context): CacheManager {
            if (mInstance == null) {
                synchronized(IMManager::class) {
                    if (mInstance == null) {
                        mInstance = CacheManager()
                    }
                }
            } else {//如果此前有实例且已连接 则断开连接

            }
            return mInstance!!
        }
    }

    interface OnDataCallback<Any> {
        fun onSuc(data: MutableList<Any>)
        fun onFailed(err: String)
    }

    class MyHandler : Handler() {
        override fun handleMessage(msg: android.os.Message?) {
            if (null == msg) {
                return
            }

        }
    }

}