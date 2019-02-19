package cn.berfy.service.im.util

import android.text.TextUtils
import cn.berfy.sdk.mvpbase.util.LogF
import cn.berfy.sdk.mvpbase.util.SharedPreferenceUtil
import cn.berfy.service.im.cache.CacheConstants
import cn.berfy.service.im.manager.IMManager
import cn.berfy.service.im.model.*
import cn.berfy.service.im.model.conversation.Conversation
import cn.berfy.service.im.model.response.ServerMessage
import org.json.JSONException
import org.json.JSONObject

/**
author: Berfy
date: 2019/1/21
消息处理类
 */
object MessageTextUtil {

    private val TAG = "IM聊天_StringUtil"

    fun newID(): String {
        val serverTimeLimit =
            SharedPreferenceUtil.getPublic(IMManager.instance.getContext(), CacheConstants.CHAT_SERVER_TIME_LIMIT, 0L)
        val trueTime: Long = System.currentTimeMillis() + serverTimeLimit.toString().toLong()
        return "$trueTime${random(100000, 999999)}"
    }

    fun getTime(): Long {
        val serverTimeLimit =
            SharedPreferenceUtil.getPublic(IMManager.instance.getContext(), CacheConstants.CHAT_SERVER_TIME_LIMIT, 0L)
        return System.currentTimeMillis() + serverTimeLimit.toString().toLong()
    }

    fun random(min: Long, max: Long): Long {
        val range = max - min
        val rand = Math.random()
        val num: Long = min + Math.round(rand * range) //四舍五入
        return num
    }

    fun getStrLen(str: String): Int {
        var realLength = 0
        val len = str.length
        var charCode: Int
        for (i: Int in 0..(len - 1)) {
            charCode = str.codePointAt(i)
            if (charCode in 0..128) {
                realLength += 1
            } else {
                // 如果是中文则长度加3
                realLength += 3
            }
        }
        LogF.d(TAG, "检查String长度 raw=$str  length=$realLength")
        return realLength
    }

    //发送消息体构建
    fun createSendPayload(msg: Message): String {
        val jsonObject = JSONObject()
        try {
            when (msg.type) {
                MessageContentType.TYPE_TEXT -> {
                    msg as MessageText
                    jsonObject.put("msg", msg.content)
                }
                MessageContentType.TYPE_IMAGE -> {
                    msg as MessageImage
                    jsonObject.put("name", msg.name)
                    jsonObject.put("md5", msg.md5)
                    jsonObject.put("url", msg.imageUrl)
                    jsonObject.put("small", msg.thumbUrl)
                    jsonObject.put("ext", msg.ext)
                    jsonObject.put("w", msg.w)
                    jsonObject.put("h", msg.h)
                    jsonObject.put("size", msg.fileLength.toString())
                }
                MessageContentType.TYPE_VOICE -> {
                    msg as MessageVoice
                    jsonObject.put("dur", msg.duration)
                    jsonObject.put("md5", msg.md5)
                    jsonObject.put("url", msg.voiceUrl)
                    jsonObject.put("ext", msg.ext)
                    jsonObject.put("size", msg.fileLength.toString())
                }
                MessageContentType.TYPE_VIDEO -> {
                    msg as MessageVideo
                    jsonObject.put("dur", msg.duration)
                    jsonObject.put("md5", msg.md5)
                    jsonObject.put("url", msg.videoUrl)
                    jsonObject.put("ext", msg.ext)
                    jsonObject.put("w", msg.w)
                    jsonObject.put("h", msg.h)
                    jsonObject.put("size", msg.fileLength.toString())
                }
                MessageContentType.TYPE_FILE -> {
                    msg as MessageFile
                    jsonObject.put("name", msg.name)
                    jsonObject.put("md5", msg.md5)
                    jsonObject.put("url", msg.fileUrl)
                    jsonObject.put("ext", msg.ext)
                    jsonObject.put("size", msg.fileLength.toString())
                }
                MessageContentType.TYPE_LOCATION -> {
                    msg as MessageLocation
                    jsonObject.put("title", msg.title)
                    jsonObject.put("lng", msg.lng)
                    jsonObject.put("lat", msg.lat)
                }
                MessageContentType.TYPE_CUSTOM -> {
                    msg as MessageCustom

                }
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return jsonObject.toString()
    }

    //收到消息解析
    fun receivePayloadToMsg(rawMessage: RawMessage): Message? {
        try {
            when (rawMessage.chatType) {
                MessageContentType.TYPE_TEXT -> {
                    val newMessage = MessageText()
                    newMessage.id = rawMessage.id
                    newMessage.conversation = rawMessage.createConversation()
                    try {
                        val payload = JSONObject(rawMessage.payload)
                        newMessage.content = payload.optString("msg")
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                    return newMessage
                }
                MessageContentType.TYPE_IMAGE -> {
                    val newMessage = MessageImage()
                    newMessage.id = rawMessage.id
                    newMessage.conversation = rawMessage.createConversation()
                    try {
                        val payload = JSONObject(rawMessage.payload)
                        newMessage.name = payload.optString("name")
                        newMessage.md5 = payload.optString("md5")
                        newMessage.imageUrl = payload.optString("url")
                        newMessage.thumbUrl = payload.optString("small")
                        newMessage.ext = payload.optString("ext")
                        val w = payload.optString("w")
                        if (TextUtils.isEmpty(w)) {
                            newMessage.w = 0
                        } else {
                            newMessage.w = w.toInt()
                        }
                        val h = payload.optString("h")
                        if (TextUtils.isEmpty(h)) {
                            newMessage.h = 0
                        } else {
                            newMessage.h = h.toInt()
                        }
                        val size = payload.optString("size")
                        if (TextUtils.isEmpty(size)) {
                            newMessage.fileLength = 0
                        } else {
                            newMessage.fileLength = size.toLong()
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                    return newMessage
                }
                MessageContentType.TYPE_VOICE -> {
                    val newMessage = MessageVoice()
                    newMessage.id = rawMessage.id
                    newMessage.conversation = rawMessage.createConversation()
                    try {
                        val payload = JSONObject(rawMessage.payload)
                        newMessage.duration = payload.optLong("dur")
                        newMessage.md5 = payload.optString("md5")
                        newMessage.voiceUrl = payload.optString("url")
                        newMessage.ext = payload.optString("ext")
                        val size = payload.optString("size")
                        if (TextUtils.isEmpty(size)) {
                            newMessage.fileLength = 0
                        } else {
                            newMessage.fileLength = size.toLong()
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                    return newMessage
                }
                MessageContentType.TYPE_VIDEO -> {
                    val newMessage = MessageVideo()
                    newMessage.id = rawMessage.id
                    newMessage.conversation = rawMessage.createConversation()
                    try {
                        val payload = JSONObject(rawMessage.payload)
                        newMessage.duration = payload.optLong("dur")
                        newMessage.md5 = payload.optString("md5")
                        newMessage.videoUrl = payload.optString("url")
                        newMessage.ext = payload.optString("ext")
                        val w = payload.optString("w")
                        if (TextUtils.isEmpty(w)) {
                            newMessage.w = 0
                        } else {
                            newMessage.w = w.toInt()
                        }
                        val h = payload.optString("h")
                        if (TextUtils.isEmpty(h)) {
                            newMessage.h = 0
                        } else {
                            newMessage.h = h.toInt()
                        }
                        val size = payload.optString("size")
                        if (TextUtils.isEmpty(size)) {
                            newMessage.fileLength = 0
                        } else {
                            newMessage.fileLength = size.toLong()
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                    return newMessage
                }
                MessageContentType.TYPE_FILE -> {
                    val newMessage = MessageFile()
                    newMessage.id = rawMessage.id
                    newMessage.conversation = rawMessage.createConversation()
                    try {
                        val payload = JSONObject(rawMessage.payload)
                        newMessage.name = payload.optString("name")
                        newMessage.md5 = payload.optString("md5")
                        newMessage.fileUrl = payload.optString("url")
                        newMessage.ext = payload.optString("ext")
                        val size = payload.optString("size")
                        if (TextUtils.isEmpty(size)) {
                            newMessage.fileLength = 0
                        } else {
                            newMessage.fileLength = size.toLong()
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                    return newMessage
                }
                MessageContentType.TYPE_LOCATION -> {
                    val newMessage = MessageLocation()
                    newMessage.id = rawMessage.id
                    newMessage.conversation = rawMessage.createConversation()
                    try {
                        val payload = JSONObject(rawMessage.payload)
                        newMessage.title = payload.optString("title")
                        newMessage.lng = payload.optString("lng")
                        newMessage.lat = payload.optString("lat")
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                    return newMessage
                }
                MessageContentType.TYPE_CUSTOM -> {
                    val newMessage = MessageCustom()
                    newMessage.id = rawMessage.id
                    newMessage.conversation = rawMessage.createConversation()
                    try {
                        val payload = JSONObject(rawMessage.payload)
                        val type = payload.getString("type")
                        when (type) {
                            "101" -> {//添加好友
                                newMessage.cus_type = MessageCustomType.TYPE_ADD_FRIEND
                            }
                            "102" -> {//通过好友申请
                                newMessage.cus_type = MessageCustomType.TYPE_AGREE_ADD_FRIEND
                            }
                            "103" -> {//拒绝好友申请
                                newMessage.cus_type = MessageCustomType.TYPE_REFUSE_ADD_FRIEND
                            }
                        }
                        newMessage.cus_from = payload.optString("from")
                        newMessage.cus_to = payload.optString("to")
                        newMessage.cus_msg = payload.optString("msg")
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                    return newMessage
                }
            }
            return null
        } catch (e: JSONException) {
            e.printStackTrace()
            return null
        }
    }

    //历史消息消息解析
    fun historyPayloadToMsg(serverMessage: ServerMessage): Message? {
        try {
            var newMessage: Message? = null
            when (serverMessage.type) {
                0 -> {
                    newMessage = MessageText()
                    newMessage.id = serverMessage.id
                    try {
                        val payload = JSONObject(serverMessage.payload)
                        newMessage.content = payload.optString("msg")
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }
                1 -> {
                    newMessage = MessageImage()
                    newMessage.id = serverMessage.id
                    try {
                        val payload = JSONObject(serverMessage.payload)
                        newMessage.name = payload.optString("name")
                        newMessage.md5 = payload.optString("md5")
                        newMessage.imageUrl = payload.optString("url")
                        newMessage.thumbUrl = payload.optString("small")
                        newMessage.ext = payload.optString("ext")
                        val w = payload.optString("w")
                        if (TextUtils.isEmpty(w)) {
                            newMessage.w = 0
                        } else {
                            newMessage.w = w.toInt()
                        }
                        val h = payload.optString("h")
                        if (TextUtils.isEmpty(h)) {
                            newMessage.h = 0
                        } else {
                            newMessage.h = h.toInt()
                        }
                        val size = payload.optString("size")
                        if (TextUtils.isEmpty(size)) {
                            newMessage.fileLength = 0
                        } else {
                            newMessage.fileLength = size.toLong()
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }
                2 -> {
                    newMessage = MessageVoice()
                    newMessage.id = serverMessage.id
                    try {
                        val payload = JSONObject(serverMessage.payload)
                        newMessage.duration = payload.optLong("dur")
                        newMessage.md5 = payload.optString("md5")
                        newMessage.voiceUrl = payload.optString("url")
                        newMessage.ext = payload.optString("ext")
                        val size = payload.optString("size")
                        if (TextUtils.isEmpty(size)) {
                            newMessage.fileLength = 0
                        } else {
                            newMessage.fileLength = size.toLong()
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }
                3 -> {
                    newMessage = MessageVideo()
                    newMessage.id = serverMessage.id
                    try {
                        val payload = JSONObject(serverMessage.payload)
                        newMessage.duration = payload.optLong("dur")
                        newMessage.md5 = payload.optString("md5")
                        newMessage.videoUrl = payload.optString("url")
                        newMessage.ext = payload.optString("ext")
                        val w = payload.optString("w")
                        if (TextUtils.isEmpty(w)) {
                            newMessage.w = 0
                        } else {
                            newMessage.w = w.toInt()
                        }
                        val h = payload.optString("h")
                        if (TextUtils.isEmpty(h)) {
                            newMessage.h = 0
                        } else {
                            newMessage.h = h.toInt()
                        }
                        val size = payload.optString("size")
                        if (TextUtils.isEmpty(size)) {
                            newMessage.fileLength = 0
                        } else {
                            newMessage.fileLength = size.toLong()
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }
                4 -> {
                    newMessage = MessageLocation()
                    newMessage.id = serverMessage.id
                    try {
                        val payload = JSONObject(serverMessage.payload)
                        newMessage.title = payload.optString("title")
                        newMessage.lng = payload.optString("lng")
                        newMessage.lat = payload.optString("lat")
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }
                6 -> {
                    newMessage = MessageFile()
                    newMessage.id = serverMessage.id
                    try {
                        val payload = JSONObject(serverMessage.payload)
                        newMessage.name = payload.optString("name")
                        newMessage.md5 = payload.optString("md5")
                        newMessage.fileUrl = payload.optString("url")
                        newMessage.ext = payload.optString("ext")
                        val size = payload.optString("size")
                        if (TextUtils.isEmpty(size)) {
                            newMessage.fileLength = 0
                        } else {
                            newMessage.fileLength = size.toLong()
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }
                100 -> {
                    newMessage = MessageCustom()
                    newMessage.id = serverMessage.id
                    newMessage.conversation = Conversation(serverMessage.sender, MessageType.TYPE_CUSTOM)
                    try {
                        val payload = JSONObject(serverMessage.payload)
                        val type = payload.getString("type")
                        when (type) {
                            "101" -> {//添加好友
                                newMessage.cus_type = MessageCustomType.TYPE_ADD_FRIEND
                            }
                            "102" -> {//通过好友申请
                                newMessage.cus_type = MessageCustomType.TYPE_AGREE_ADD_FRIEND
                            }
                            "103" -> {//拒绝好友申请
                                newMessage.cus_type = MessageCustomType.TYPE_REFUSE_ADD_FRIEND
                            }
                        }
                        newMessage.cus_from = payload.optString("from")
                        newMessage.cus_to = payload.optString("to")
                        newMessage.cus_msg = payload.optString("msg")
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }
            }
            if (null != newMessage) {
                if (null == newMessage.conversation) {
                    if (serverMessage.topic.indexOf("/22/p2p/") != -1) {//P2P消息
                        newMessage.conversation = Conversation(serverMessage.sender, MessageType.TYPE_P2P)
                    } else if (serverMessage.topic.indexOf("/22/chat/") != -1) {//聊天室消息
                        newMessage.conversation = Conversation(
                            serverMessage.topic.substring(
                                serverMessage.topic.lastIndexOf("/") + 1,
                                serverMessage.topic.length
                            ), MessageType.TYPE_CHATROOM
                        )
                    } else if (serverMessage.topic.indexOf("/22/group/") != -1) {//群消息
                        newMessage.conversation = Conversation(
                            serverMessage.topic.substring(
                                serverMessage.topic.lastIndexOf("/") + 1,
                                serverMessage.topic.length
                            ), MessageType.TYPE_GROUP
                        )
                    } else if (serverMessage.topic.indexOf("/22/notify/") != -1) {//系统消息
                        newMessage.conversation = Conversation(serverMessage.sender, MessageType.TYPE_SYSTEM)
                    }
                }
                newMessage.senderId = serverMessage.sender
                newMessage.time =
                    if (TextUtils.isEmpty(serverMessage.timestamp)) 0 else serverMessage.timestamp.toLong()
                newMessage.sendStatus = Message.STATUS_SEND_SUC
            }
            return newMessage
        } catch (e: JSONException) {
            e.printStackTrace()
            return null
        }
    }

    //收到自定义消息解析
    fun receiveCustomToMsg(rawMessage: RawMessage): MessageCustom? {
        try {
            val newMessage = MessageCustom()
            newMessage.id = rawMessage.id
            newMessage.conversation = rawMessage.createConversation()
            val payload = JSONObject(rawMessage.payload)
            val type = payload.getString("type")
            when (type) {
                "101" -> {//添加好友
                    newMessage.cus_type = MessageCustomType.TYPE_ADD_FRIEND
                }
                "102" -> {//通过好友申请
                    newMessage.cus_type = MessageCustomType.TYPE_AGREE_ADD_FRIEND
                }
                "103" -> {//拒绝好友申请
                    newMessage.cus_type = MessageCustomType.TYPE_REFUSE_ADD_FRIEND
                }
                "150" -> {//群通知 进入或离开群
                    newMessage.cus_type = MessageCustomType.TYPE_GROUP_JOIN_LEAVE
                    newMessage.groupid = payload.optString("groupid")
                    newMessage.grouptype = payload.optString("grouptype")
                }
                "151" -> {//邀请进入群
                    newMessage.cus_type = MessageCustomType.TYPE_GROUP_INVITE
                    newMessage.groupid = payload.optString("groupid")
                    newMessage.grouptype = payload.optString("grouptype")
                }
            }
            newMessage.cus_from = payload.optString("from")
            newMessage.cus_to = payload.optString("to")
            newMessage.cus_msg = payload.optString("msg")
            return newMessage
        } catch (e: JSONException) {
            e.printStackTrace()
            return null
        }
    }

}