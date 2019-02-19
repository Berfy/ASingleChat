package cn.berfy.service.im.manager

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import cn.berfy.sdk.http.HttpApi
import cn.berfy.sdk.http.callback.HttpUploadCallBack
import cn.berfy.sdk.http.callback.OnStatusListener
import cn.berfy.sdk.http.callback.RequestCallBack
import cn.berfy.sdk.http.model.HttpParams
import cn.berfy.sdk.http.model.NetError
import cn.berfy.sdk.http.model.NetResponse
import cn.berfy.sdk.mvpbase.config.CacheConstant
import cn.berfy.sdk.mvpbase.model.User
import cn.berfy.sdk.mvpbase.util.*
import cn.berfy.service.im.R
import cn.berfy.service.im.http.AuthApi
import cn.berfy.service.im.http.ConversationApi
import cn.berfy.service.im.manager.i.IManager
import cn.berfy.service.im.manager.i.OnConnectStatusCallback
import cn.berfy.service.im.manager.i.OnMessageListener
import cn.berfy.service.im.manager.i.OnMessageSendingCallback
import cn.berfy.service.im.model.*
import cn.berfy.service.im.model.conversation.Conversation
import cn.berfy.service.im.model.conversation.NotifyToggleInfo
import cn.berfy.service.im.model.response.ConversationResponseData
import cn.berfy.service.im.model.response.GroupListResponseData
import cn.berfy.service.im.service.IMCoreService
import cn.berfy.service.im.util.MessageTextUtil
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.lang.Class
import java.lang.InterruptedException
import java.lang.NullPointerException
import java.lang.Runnable
import java.lang.System
import java.nio.charset.Charset
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock
import kotlin.text.String

class IMManager private constructor(context: Context, pushIntentClass: Class<*>) : IManager {

    private val TAG = "IMCore"
    private var mContext: Context? = null

    //配置
    var mIsDebug: Boolean = false
    private var mUrl: String? = null
    private var mIsNeedReconnect: Boolean = true//是否需要断线自动重连
    private var mIsAutoLogin = false //是否自动登录
    private var mUserName: String? = ""
    private var mPwd: String? = ""
    private var mAppId: String? = ""
    private var mQos = 1//消息质量 0最多发送一次 1至少发送一次（推荐） 2只发送一次（服务器去重+确认  资源消耗大）
    private val mTopics: MutableList<String> = java.util.ArrayList()//话题集合
    private val mFailedRetryTopic: MutableList<String> = java.util.ArrayList()//订阅失败的话题集合  重新订阅
    private val NORMAL_FILE = 0   //图片上传标识
    private val PIC_FILE = 1   //图片上传标识
    private val VOICE_FILE = 2 //语音上传标识
    private val VIDEO_FILE = 3 //视频上传标识
    //连接状态
    private var mCurrentStatus = IMConnectionStatus.DISCONNECTED     //websocket连接状态
    private var mCurrentConversation: Conversation? = null//记录当前正在聊天的会话

    private var mIsManualClose = false//是否为手动关闭websocket连接
    private var mLock: Lock? = null
    private val mMainHandler = Handler(Looper.getMainLooper())
    private var mClient: MqttAndroidClient? = null
    private var mMQTTConnectOptions: MqttConnectOptions? = null
    //重连次数
    private var mReconnectCount = 0
    //连接状态
    private var mOnConnectStatusCallbacks: MutableList<OnConnectStatusCallback>? = null
    //消息监听
    private var mMessageListeners: MutableList<OnMessageListener>? = null

    private var mUserContext: Any? = null

    private val mSendMsgStatusCallbacks: MutableList<Message> = ArrayList()

    private var mHttpApi: HttpApi? = null

    init {
        mContext = context
        mHttpApi = HttpApi.newInstance(mContext)
            .setHost(HostSettings.getHost())
            .setLogTAG("HTTP")
            .setStatusListener(object : OnStatusListener {
                override fun statusCodeError(code: Int, usedTime: Long) {
                }

                override fun receiveSetCookie(cookie: String?) {
                }

                override fun addCookies(): HttpParams? {
                    return null
                }

                override fun addParamsOrHeaders(rawParams: HttpParams?): HttpParams? {
                    return null
                }
            })
            .setTimeOut(8, 8, 20)
        mHttpApi!!.startConnection()
        PushManager.init(context, pushIntentClass)
        CacheManager.init(context)
    }

    private val mReconnectTask = Runnable {
        LogF.e(TAG, "服务器重连接中...")
        buildConnect(false)
    }

    //订阅失败重新订阅线程
    private val mReSubTask = Runnable {
        LogF.e(TAG, "服务器重连接中...")
        autoSub()
    }

    private val mConnectingListener = object : IMqttActionListener {
        override fun onSuccess(token: IMqttToken) {
            connectSuc(token)
        }

        override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
            try {
                val errMsg = "连接失败   " + if (null == exception) "[服务器连接失败]" else
                    "${exception.cause} : ${exception.message}    ${exception.printStackTrace()}"
                LogF.e(TAG, errMsg)
                connectFailedCallback(Throwable(errMsg))
                tryReconnect()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private val mCallback = object : MqttCallbackExtended {
        override fun connectComplete(reconnect: Boolean, serverURI: String) {
            if (null == mContext) {
                return
            }
            LogF.e(TAG, "服务器连接成功")
            connectSucCallback()
        }

        override fun connectionLost(exception: Throwable?) {
            if (null == mContext) {
                return
            }
            try {
                val errMsg =
                    "[服务器连接关闭]   " + if (null == exception) "[服务器连接失败]" else "${exception.cause} : ${exception.message}"
                if (null != exception) {
                    exception.printStackTrace()
                }
                LogF.e(TAG, errMsg)
                disConnectCallback(Throwable(mContext!!.getString(R.string.im_tip_connect_closed)))
                tryReconnect()
                //连接断开 清空发送消息监听
                mSendMsgStatusCallbacks.forEach {
                    it.mSendingCallback.onFailed(errMsg)
                    mSendMsgStatusCallbacks.remove(it)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        override fun messageArrived(topic: String, message: MqttMessage?) {//接收到后台推送推送来的消息
            if (null == message) {
                LogF.d(TAG, "消息为空 作废")
                return
            }
            try {
                LogF.d(TAG, "收到新消息  topic=$topic")
                if (Looper.myLooper() != Looper.getMainLooper()) {
                    mMainHandler.post {
                        receiveMessage(
                            topic, message.payload
                        )
                    }
                } else {
                    receiveMessage(topic, message.payload)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        override fun deliveryComplete(token: IMqttDeliveryToken) {
            try {
                LogF.d(TAG, "发送成功deliveryComplete payload=${String(token.message.payload)} ${token.message.id}")
                LogF.d(TAG, "发送完毕 缓存start ${mSendMsgStatusCallbacks.size}")
                var y = 0
                for (i in 0..(mSendMsgStatusCallbacks.size - 1)) {
                    val msg: Message = mSendMsgStatusCallbacks.get(i - y)
                    LogF.d(TAG, "找状态 ${msg.id}  ${token.message.id}")
                    if (msg.tagId == token.message.id) {
                        msg.mSendingCallback.onSuc(msg)
                        mSendMsgStatusCallbacks.remove(msg)
                        //发送成功 更新消息和会话
                        //测试代码 数据库更新消息
                        msg.time = System.currentTimeMillis()
                        addDB(msg)
                        y++
                    }
                }
                LogF.d(TAG, "发送完毕 缓存end ${mSendMsgStatusCallbacks.size}")
            } catch (e: MqttException) {
                e.printStackTrace()
            }
        }
    }

    //连接成功状态设置
    private fun connectSuc(token: IMqttToken) {
        LogF.d(TAG, "连接成功  当前所有订阅topic=====" + GsonUtil.getInstance().toJson(token.topics))
        mUserContext = token.userContext
        updateCurrentStatus(IMConnectionStatus.CONNECTED)
        //启动管理器
        PushManager.instance.start()
        CacheManager.instance.start()
        //连接成功 取消重连线程
        cancelReconnect()
        val disconnectedBufferOptions = DisconnectedBufferOptions()
        disconnectedBufferOptions.isBufferEnabled = true
        disconnectedBufferOptions.bufferSize = 256
        disconnectedBufferOptions.isPersistBuffer = false
        disconnectedBufferOptions.isDeleteOldestMessages = false
        mClient!!.setBufferOpts(disconnectedBufferOptions)
        //自动订阅
        autoSub()
        //连接成功 通知刷新
        LogF.d(TAG, "需要刷新会话 连接成功")
        if (null != mMessageListeners) {
            for (listener in mMessageListeners!!) {
                listener.refreshConversation()
            }
        }
    }

    //自动订阅
    private fun autoSub() {
        LogF.d(TAG, "自动订阅话题")
        //测试代码
        if (mIsDebug) {
            for (topic in mTopics) {
                subscribeToTopic(topic)
            }
        }
        //p2p通道
        subscribeToTopic("/$mAppId/22/p2p/${getLoginId()}")
        //系统通道
        subscribeToTopic("/$mAppId/22/notify/sys")
        ContactManager.instance.getGroupList(object : RequestCallBack<GroupListResponseData> {
            override fun onStart() {
            }

            override fun onFinish(response: NetResponse<GroupListResponseData>?) {
                if (null != response && response.isOk && null != response.data && null != response.data.list) {
                    for (i in response.data.list!!) {
                        if (i.group_type == "1") {//聊天室通道
                            subscribeToTopic("/$mAppId/22/chat/${i.group_id}")
                        } else {//群聊通道
                            subscribeToTopic("/$mAppId/22/group/${i.group_id}")
                        }
                    }
                }
            }

            override fun onError(error: NetError?) {
            }
        })

        ConversationApi.instance.getConversation(object : RequestCallBack<ConversationResponseData> {

            override fun onStart() {
            }

            override fun onFinish(response: NetResponse<ConversationResponseData>) {
                if (response.isOk && null != response.data && null != response.data.sess) {
                    for (conversation in response.data.sess!!) {
                        if (conversation.type == MessageType.TYPE_CHATROOM) {
                            subscribeToTopic("/$mAppId/22/chat/${conversation.toId}")
                        } else if (conversation.type == MessageType.TYPE_GROUP) {
                            subscribeToTopic("/$mAppId/22/group/${conversation.toId}")
                        }
                    }
                }
            }

            override fun onError(error: NetError?) {
            }
        })
    }

    //重试订阅失败的话题
    fun retrySub() {
        LogF.d(TAG, "自动订阅失败的话题 size=${mFailedRetryTopic.size}")
        for (topic in mFailedRetryTopic) {
            subscribeToTopic(topic)
        }
        mFailedRetryTopic.clear()
    }

    /**
     * 登录鉴权
     * @param merchId 渠道id
     * @param externalId 外部id
     * */
    fun login(merchId: String, externalId: String, callBack: RequestCallBack<User>) {
        AuthApi.instance.login(merchId, externalId, callBack)
    }

    fun getHttpApi(): HttpApi? {
        return mHttpApi
    }

    //清理通知
    fun clearNotify(id: Int) {
        PushManager.instance.clearNotify(id)
    }

    //清理通知 聊天tag是chat
    fun clearNotify(tag: String) {
        PushManager.instance.clearNotify(tag)
    }

    fun receiveMessage(topic: String, bytes: ByteArray) {
        //解析消息协议
        LogF.d(TAG, "新消息解析 topic=$topic len=${bytes.size}  ${GsonUtil.getInstance().toJson(bytes)}")
        val length = bytes.size
        val bais = ByteArrayInputStream(bytes)
        val dis = DataInputStream(bais)
        val action = dis.readByte()
        LogF.d(TAG, "新消息解析 action=$action")
        when (action.toInt()) {
            MSG_ACTION_PUBLISH -> {//98 发布消息
                LogF.d(TAG, "action普通消息")
                val rawMessage = RawMessage()
                //获取id 读取 小端模式的short id长度
                val idl1 = dis.read()
                val idl2 = dis.read()
                val idlBytes = ByteArray(2)
                idlBytes[0] = idl2.toByte()
                idlBytes[1] = idl1.toByte()
                //byte[]转换short
                val idl: Short = StringUtils.byte2short(idlBytes)
                LogF.d(TAG, "收到消息  idl==$idl")
                //根据长度获取id
                val idBytes = ByteArray(idl.toInt())
                for (i in 0..(idl.toInt() - 1)) {
                    idBytes[i] = dis.read().toByte()
                }
                val id = String(idBytes, Charset.forName("UTF-8"))
                LogF.d(TAG, "收到消息  id==$id")

                //获取payload 读取 小端模式的int payload长度
                val pl1 = dis.read()
                val pl2 = dis.read()
                val pl3 = dis.read()
                val pl4 = dis.read()
                val plBytes = ByteArray(4)
                plBytes[0] = pl4.toByte()
                plBytes[1] = pl3.toByte()
                plBytes[2] = pl2.toByte()
                plBytes[3] = pl1.toByte()
                //byte[]转换short
                val pl: Int = StringUtils.byte2int(plBytes)
                var payload = ""
                LogF.d(TAG, "收到消息  pl==$pl")
                //根据长度获取payload
                val payloadBytes = ByteArray(pl)
                //从第3位开始
                for (i in 0..(pl - 1)) {
                    payloadBytes[i] = dis.read().toByte()
                }
                payload = String(payloadBytes, Charset.forName("UTF-8"))
                LogF.d(TAG, "收到消息  payload==$payload")

                val ackedByte = dis.readByte()
                val acked: Boolean = ackedByte == 49.toByte()
                LogF.d(TAG, "收到消息  ackedByte=$ackedByte acked==$acked")

                //获取topic 读取 小端模式的int payload长度
                val tl1 = dis.read()
                val tl2 = dis.read()
                val tlBytes = ByteArray(2)
                //读取 小端模式的short topic长度
                tlBytes[0] = tl2.toByte()
                tlBytes[1] = tl1.toByte()
                //byte[]转换short
                val tl
                        : Short = StringUtils.byte2short(tlBytes)
                LogF.d(TAG, "收到消息  tl==$tl    bytes=${GsonUtil.getInstance().toJson(tlBytes)}")
                //根据长度获取话题
                val topicBytes = ByteArray(tl.toInt())
                for (i in 0..(tl.toInt() - 1)) {
                    topicBytes[i] = dis.read().toByte()
                }
                val topicR = String(topicBytes, Charset.forName("UTF-8"))
                LogF.d(TAG, "收到消息  topic==$topicR    bytes=${GsonUtil.getInstance().toJson(topicBytes)}")

                //越过10个字节
                var type = 0
                for (i in 0..9) {
                    if (i == 0) {
                        type = dis.read()
                    } else {
                        dis.read()
                    }
                }
                LogF.d(TAG, "收到消息  type==$type    bytes=${type.toByte()}")
                //获取sender 读取 小端模式的int payload长度
                val sl1 = dis.read()
                val sl2 = dis.read()
                val slBytes = ByteArray(2)
                //读取 小端模式的short topic长度
                slBytes[0] = sl2.toByte()
                slBytes[1] = sl1.toByte()
                //byte[]转换short
                val sl: Short = StringUtils.byte2short(slBytes)
                LogF.d(TAG, "收到消息  sl==$sl  bytes=${GsonUtil.getInstance().toJson(slBytes)}")
                //根据长度获取话题
                val senderBytes = ByteArray(sl.toInt())
                for (i in 0..(sl.toInt() - 1)) {
                    senderBytes[i] = dis.read().toByte()
                }
                val sender = String(senderBytes, Charset.forName("UTF-8"))
                LogF.d(TAG, "收到消息  sender==$sender   bytes=${GsonUtil.getInstance().toJson(senderBytes)}")

                //获取ts 读取 小端模式的int payload长度
                val tsl1 = dis.read()
                val tsl2 = dis.read()
                val tslBytes = ByteArray(2)
                //读取 小端模式的short topic长度
                tslBytes[0] = tsl2.toByte()
                tslBytes[1] = tsl1.toByte()
                //byte[]转换short
                val tsl
                        : Short = StringUtils.byte2short(tslBytes)
                LogF.d(TAG, "收到消息  tsl==$tsl     bytes=${GsonUtil.getInstance().toJson(tslBytes)}")
                //根据长度获取话题
                val tsBytes = ByteArray(tsl.toInt())
                for (i in 0..(tsl.toInt() - 1)) {
                    tsBytes[i] = dis.read().toByte()
                }
                var ts = String(tsBytes, Charset.forName("UTF-8"))
                if (TextUtils.isEmpty(ts)) {
                    ts = "0"
                }
                LogF.d(TAG, "收到消息  ts==$ts  bytes=${GsonUtil.getInstance().toJson(tsBytes)}")
                LogF.d(TAG, "收到消息  ts==$ts  date=${TimeUtil.format("yyyy-MM-dd HH:mm:ss", ts.toLong())}")

                //rawId 计算剩余字符长度获取utf-8数据
//                    val rawIdBytes = ByteArray(length - 1 - idl.toInt() - pl - sl.toInt() - tsl.toInt())
                val rawIdBytes = ByteArray(19)
                LogF.d(TAG, "收到消息  rawIdl==${rawIdBytes.size}")
                for (i in 0..(rawIdBytes.size - 1)) {
                    rawIdBytes[i] = dis.read().toByte()
//                        LogF.d(TAG, "用户  data=${rawIdBytes[i]}")
                }
                val rawId = String(rawIdBytes, Charset.forName("UTF-8"))

                LogF.d(TAG, "收到消息 topic=$topicR id=$id sender=$sender payload=$payload ts=$ts rawId=$rawId")
                //处理消息 转换成FormatMessage
                rawMessage.topic = topicR
                rawMessage.payload = payload
                if (topicR.indexOf(mAppId + "/22/p2p/") != -1) {//P2P消息
                    rawMessage.type = MessageType.TYPE_P2P //根据话题和action区分
                } else if (topicR.indexOf(mAppId + "/22/chat/") != -1) {//聊天室消息
                    rawMessage.type = MessageType.TYPE_CHATROOM
                } else if (topicR.indexOf(mAppId + "/22/group/") != -1) {//群消息
                    rawMessage.type = MessageType.TYPE_GROUP
                } else if (topicR.indexOf(mAppId + "/22/notify/") != -1) {//系统消息
                    rawMessage.type = MessageType.TYPE_SYSTEM
                } else { //测试代码
                    rawMessage.type = MessageType.TYPE_P2P
                }

                //区分消息类型
                when (type) {
                    0 -> {
                        LogF.d(TAG, "新消息解析 文本消息")
                        rawMessage.chatType = MessageContentType.TYPE_TEXT //文本消息
                    }
                    1 -> {
                        LogF.d(TAG, "新消息解析 图片消息")
                        rawMessage.chatType = MessageContentType.TYPE_IMAGE//图片消息
                    }
                    2 -> {
                        LogF.d(TAG, "新消息解析 语音消息")
                        rawMessage.chatType = MessageContentType.TYPE_VOICE//语音消息
                    }
                    3 -> {
                        LogF.d(TAG, "新消息解析 视频消息")
                        rawMessage.chatType = MessageContentType.TYPE_VIDEO//视频消息
                    }
                    4 -> {
                        LogF.d(TAG, "新消息解析 位置消息")
                        rawMessage.chatType = MessageContentType.TYPE_LOCATION//位置消息
                    }
                    6 -> {
                        LogF.d(TAG, "新消息解析 文件消息")
                        rawMessage.chatType = MessageContentType.TYPE_FILE//文件
                    }
                    100 -> {
                        LogF.d(TAG, "新消息解析 自定义消息")
                        //自定义消息 单独分发
                        rawMessage.chatType = MessageContentType.TYPE_CUSTOM//文件
                        rawMessage.type = MessageType.TYPE_CUSTOM
                    }
                    else -> {
                        rawMessage.chatType = MessageContentType.TYPE_TEXT //不是文件都是文本
                    }
                }
                rawMessage.id = id
                rawMessage.senderId = sender
                rawMessage.acked = acked
                rawMessage.rawId = rawId
                rawMessage.time = ts.toLong()
                //测试代码 缺少发送者id  区分自己和别人都需要用
                receiveCallbackNormalMessage(rawMessage)
            }
            MSG_ACTION_UNREAD_COUNT -> {
                LogF.d(TAG, "action未读消息")
                //未读消息
                //获取未读消息 读取 小端模式的int payload长度
                val count: Int = dis.readInt()
                LogF.d(TAG, "未读消息 count=$count")
            }
            MSG_ACTION_ONLINE_USERS -> {
                LogF.d(TAG, "action在线用户")
                //所有在线用户 113
                val users: MutableList<String> = ArrayList()
                var last = 1
                while (last < length) {
                    val ul = bytes[last].toInt()
                    val u: List<Byte> = bytes.slice(IntRange(last + 1, last + 1 + ul - 1))
                    val user = String(u.toByteArray(), Charset.forName("UTF-8"))
                    users.add(user)
//                        LogF.d(TAG, "所有在线用户 user=$user last=$last ul=$ul")
                    last += 1 + ul
                }
                LogF.d(TAG, "所有在线用户 count=${users.size} users=${GsonUtil.getInstance().toJson(users)}")
            }
            MSG_ACTION_JOIN_ROOM -> {
                LogF.d(TAG, "action加入聊天室")
                //加入聊天室 115
                val tl1 = dis.read()
                val tl2 = dis.read()
                val tlBytes = ByteArray(2)
                //读取 小端模式的short topic长度
                tlBytes[0] = tl2.toByte()
                tlBytes[1] = tl1.toByte()
                //byte[]转换short
                val tl: Short = StringUtils.byte2short(tlBytes)
                LogF.d(TAG, "加入聊天室  tl==$tl")
                //根据长度获取话题
                val topicBytes = ByteArray(tl.toInt())
                for (i in 0..(tl.toInt() - 1)) {
                    topicBytes[i] = dis.read().toByte()
                }
                val topicR = String(topicBytes, Charset.forName("UTF-8"))
                LogF.d(TAG, "下线  topic==$topicR")
                //计算剩余字符长度获取utf-8数据
                val userBytes = ByteArray(length - 1 - 2 - tl.toInt())
                for (i in 0..(userBytes.size - 1)) {
//                        LogF.d(TAG, "用户  data=${userBytes[i]}")
                    userBytes[i] = dis.read().toByte()
                }
                val user = String(userBytes, Charset.forName("UTF-8"))
                LogF.d(TAG, "加入聊天室  topic=$topicR user=$user")
            }
            MSG_ACTION_LEAVE_ROOM -> {
                LogF.d(TAG, "action离开聊天室")
                //离开聊天室 116
                val tl1 = dis.read()
                val tl2 = dis.read()
                val tlBytes = ByteArray(2)
                //读取 小端模式的short topic长度
                tlBytes[0] = tl2.toByte()
                tlBytes[1] = tl1.toByte()
                //byte[]转换short
                val tl: Short = StringUtils.byte2short(tlBytes)
                LogF.d(TAG, "离开聊天室  tl==$tl")
                //根据长度获取话题
                val topicBytes = ByteArray(tl.toInt())
                for (i in 0..(tl.toInt() - 1)) {
                    topicBytes[i] = dis.read().toByte()
                }
                val topicR = String(topicBytes, Charset.forName("UTF-8"))
                LogF.d(TAG, "下线  topic==$topicR")
                //计算剩余字符长度获取utf-8数据
                val userBytes = ByteArray(length - 1 - 2 - tl.toInt())
                for (i in 0..(userBytes.size - 1)) {
//                        LogF.d(TAG, "用户  data=${userBytes[i]}")
                    userBytes[i] = dis.read().toByte()
                }
                val user = String(userBytes, Charset.forName("UTF-8"))
                LogF.d(TAG, "离开聊天室  topic=$topicR user=$user")
            }
            MSG_ACTION_ONLINE -> {
                LogF.d(TAG, "action上线")
                //上线 117
                val tl1 = dis.read()
                val tl2 = dis.read()
                val tlBytes = ByteArray(2)
                //读取 小端模式的short topic长度
                tlBytes[0] = tl2.toByte()
                tlBytes[1] = tl1.toByte()
                //byte[]转换short
                val tl: Short = StringUtils.byte2short(tlBytes)
                LogF.d(TAG, "上线  tl==$tl")
                //根据长度获取话题
                val topicBytes = ByteArray(tl.toInt())
                for (i in 0..(tl.toInt() - 1)) {
                    topicBytes[i] = dis.read().toByte()
                }
                val topicR = String(topicBytes, Charset.forName("UTF-8"))
                LogF.d(TAG, "上线  topic==$topicR")
                //计算剩余字符长度获取utf-8数据
                val userBytes = ByteArray(length - 1 - 2 - tl.toInt())
                for (i in 0..(userBytes.size - 1)) {
                    userBytes[i] = dis.read().toByte()
//                        LogF.d(TAG, "用户  data=${userBytes[i]}")
                }
                val user = String(userBytes, Charset.forName("UTF-8"))
                LogF.d(TAG, "上线  topic=$topicR user=$user")
            }
            MSG_ACTION_OFFLINE -> {
                LogF.d(TAG, "action下线")
                //下线 118
                val tl1 = dis.read()
                val tl2 = dis.read()
                val tlBytes = ByteArray(2)
                //读取 小端模式的short topic长度
                tlBytes[0] = tl2.toByte()
                tlBytes[1] = tl1.toByte()
                //byte[]转换short
                val tl: Short = StringUtils.byte2short(tlBytes)
                LogF.d(TAG, "下线  tl==$tl")
                //根据长度获取话题
                val topicBytes = ByteArray(tl.toInt())
                for (i in 0..(tl.toInt() - 1)) {
                    topicBytes[i] = dis.read().toByte()
                }
                val topicR = String(topicBytes, Charset.forName("UTF-8"))
                LogF.d(TAG, "下线  topic==$topicR")
                //计算剩余字符长度获取utf-8数据
                val userBytes = ByteArray(length - 1 - 2 - tl.toInt())
                for (i in 0..(userBytes.size - 1)) {
//                        LogF.d(TAG, "下线 用户  data=${userBytes[i]}")
                    userBytes[i] = dis.read().toByte()
                }
                val user = String(userBytes, Charset.forName("UTF-8"))
                LogF.d(TAG, "下线  topic=$topicR user=$user")
            }
            MSG_ACTION_ALL_MEMBER -> {
                LogF.d(TAG, "action全部成员")
                //聊天室成员 119
                val count: Int = dis.readByte().toInt()
                val users: MutableList<String> = ArrayList()
                var last = 1

                while (last < length) {
                    val ul = bytes[last].toInt()
                    val u: List<Byte> = bytes.slice(IntRange(last + 1, last + 1 + ul - 1))
                    users.add(String(u.toByteArray(), Charset.forName("UTF-8")))
                    last += 1 + ul
                }
                LogF.d(TAG, "聊天室成员 count=$count users=${GsonUtil.getInstance().toJson(users)}")
            }
            MSG_ACTION_CLIENT_SEARCH -> {
                LogF.d(TAG, "action CLIENT_SEARCH")
                //客户端检索消息 120
                val tl1 = dis.read()
                val tl2 = dis.read()
                val tlBytes = ByteArray(2)
                //读取 小端模式的short topic长度
                tlBytes[0] = tl2.toByte()
                tlBytes[1] = tl1.toByte()
                //byte[]转换short
                val tl: Short = StringUtils.byte2short(tlBytes)
                LogF.d(TAG, "消息检索  tl==$tl")
                //根据长度获取话题
                val topicBytes = ByteArray(tl.toInt())
                for (i in 0..(tl.toInt() - 1)) {
                    topicBytes[i] = dis.read().toByte()
                }
                val topicR = String(topicBytes, Charset.forName("UTF-8"))
                LogF.d(TAG, "消息检索  topic==$topicR")
                //计算剩余字符长度获取utf-8数据
                val msgIdBytes = ByteArray(length - 1 - 2 - tl.toInt())
                for (i in 0..(msgIdBytes.size - 1)) {
                    LogF.d(TAG, "消息检索 ing  data=${msgIdBytes[i]}")
                    msgIdBytes[i] = dis.read().toByte()
                }
                val msgId = String(msgIdBytes, Charset.forName("UTF-8"))
                LogF.d(TAG, "消息检索  msgId==$msgId")
            }
            else -> {
                LogF.d(TAG, "消息action未知")
                //测试代码
//                    val rawMessage = RawMessage()
//                    rawMessage.topic = topic
//                    rawMessage.content = String(bytes)
//                    rawMessage.type = MessageType.TYPE_P2P //根据话题和action区分
//                    rawMessage.chatType = MessageContentType.TYPE_TEXT //根据action区分
//                    val ts = System.currentTimeMillis()
//                    rawMessage.id = ts.toString()
//                    rawMessage.acked = false
//                    rawMessage.time = ts
//                    rawMessage.senderName = topic
//                    //测试代码 缺少发送者id  区分自己和别人都需要用
//                    receiveCallbackNormalMessage(rawMessage)
            }
        }
        dis.close()
        bais.close()
    }

    /**
     * 接收消息封装处理
     * @param rawMessage 收到的原始消息
     * */
    private fun receiveCallbackNormalMessage(rawMessage: RawMessage) {
        LogF.d(TAG, "新消息解析receiveCallbackNormalMessage ${GsonUtil.getInstance().toJson(rawMessage)}")
//        if (rawMessage.senderId == getLoginId()) {
//            LogF.d(TAG, "自己的消息 作废")
//            return
//        }
        if (rawMessage.type == MessageType.TYPE_PING) {
            LogF.d(TAG, "新消息  心跳包  ${rawMessage.payload}  不做处理")
            return
        }
        var newMessage: Message?
        //这里群分消息类型 分发接口
        if (rawMessage.type == MessageType.TYPE_P2P
            || rawMessage.type == MessageType.TYPE_GROUP
            || rawMessage.type == MessageType.TYPE_CHATROOM
        ) {//单聊
            if (null != mMessageListeners) {//如果没有监听  不做分发
                //根据消息内容类型生成消息体
                newMessage = MessageTextUtil.receivePayloadToMsg(rawMessage)
                when (rawMessage.chatType) {
                    MessageContentType.TYPE_TEXT -> {
                    }
                    MessageContentType.TYPE_IMAGE -> {
                    }
                    MessageContentType.TYPE_VOICE -> {
                    }
                    MessageContentType.TYPE_FILE -> {
                    }
                    MessageContentType.TYPE_VIDEO -> {
                    }
                    MessageContentType.TYPE_LOCATION -> {//位置消息

                    }
//                    MessageContentType.TYPE_CUSTOM -> {//自定义消息
//
//                    }
                    MessageContentType.TYPE_GROUP_SYSTEM -> {//群系统消息

                    }
                    MessageContentType.TYPE_GROUP_TIP -> {//群提示消息

                    }
                    else -> {
                        LogF.d(TAG, "新消息  不支持的消息类型")
                    }
                }
                if (null != newMessage) {
                    newMessage.senderId = rawMessage.senderId
                    newMessage.senderName = rawMessage.senderName
                    newMessage.time = rawMessage.time
                    //测试代码 数据库存储消息和会话
                    if (!FilterManager.filterMessage(newMessage)) {
                        for (listener in mMessageListeners!!) {
                            LogF.d(TAG, "新消息监听")
                            listener.newMessage(newMessage)
                        }
                    }
                }
                LogF.d(
                    TAG,
                    "新消息 封装后 ${GsonUtil.getInstance().toJson(newMessage)}"
                )
            }
        } else if (rawMessage.type == MessageType.TYPE_SYSTEM) {//自定义消息
            if (null != mMessageListeners) {//如果没有监听  不做分发
                newMessage = MessageSystem()
                newMessage.conversation = rawMessage.createConversation()
            }
        } else if (rawMessage.type == MessageType.TYPE_CUSTOM) {//自定义消息
            LogF.d(TAG, "自定义消息")
            if (null != mMessageListeners) {//如果没有监听  不做分发
                //根据消息内容类型生成消息体
                newMessage = MessageTextUtil.receiveCustomToMsg(rawMessage)
                if (null != newMessage) {
                    newMessage as MessageCustom
                    when (newMessage.cus_type) {
                        MessageCustomType.TYPE_ADD_FRIEND -> {//申请添加好友
                            LogF.d(TAG, "自定义消息 申请添加好友")
                        }
                        MessageCustomType.TYPE_AGREE_ADD_FRIEND -> {//通过好友申请
                            LogF.d(TAG, "自定义消息 通过好友申请")
                            //加入新好友的会话
                            val conversation = Conversation(newMessage.cus_from, MessageType.TYPE_P2P)
                            conversation.lastMessage = "我们已经是好友了，开始聊天吧"
                            conversation.unreadNum = 1
                            CacheManager.instance.addConversation(conversation)
                            LogF.d(TAG, "需要刷新会话 通过好友消息")
                            if (!FilterManager.filterMessage(newMessage)) {
                                for (listener in mMessageListeners!!) {
                                    listener.refreshConversation()
                                }
                            }
                            return
                        }
                        MessageCustomType.TYPE_REFUSE_ADD_FRIEND -> {//拒绝好友申请
                            LogF.d(TAG, "自定义消息 拒绝好友申请")
                            //不操作
                        }
                        MessageCustomType.TYPE_GROUP_INVITE -> {//邀请入群
                            LogF.d(TAG, "自定义消息 邀请入群 ${newMessage.groupid}")
                            //收到群id订阅
                            val conversation = Conversation(newMessage.groupid, MessageType.TYPE_GROUP)
                            if (newMessage.grouptype == "1") {
                                conversation.type = MessageType.TYPE_CHATROOM
                                conversation.lastMessage = "欢迎进入聊天室"
                                subscribeToTopic("/$mAppId/22/chat/${newMessage.groupid}")
                            } else {
                                conversation.type = MessageType.TYPE_GROUP
                                conversation.lastMessage = "欢迎进入群组"
                                subscribeToTopic("/$mAppId/22/group/${newMessage.groupid}")
                            }
                            //加入新群的会话
                            conversation.unreadNum = 1
                            CacheManager.instance.addConversation(conversation)
                            LogF.d(TAG, "需要刷新会话 邀请入群消息")
                            //测试代码 数据库存储消息和会话
                            if (!FilterManager.filterMessage(newMessage)) {
                                for (listener in mMessageListeners!!) {
                                    listener.refreshConversation()
                                }
                            }
                        }
                        MessageCustomType.TYPE_GROUP_JOIN_LEAVE -> {
                            LogF.d(TAG, "自定义消息 群变动  ${newMessage.cus_msg}")
                            //收到群id取消订阅
//                        if (newMessage.grouptype == "0") {
//                            unSubscribeTopic("/$mAppId/22/chat/${newMessage.groupid}")
//                        } else {
//                            unSubscribeTopic("/$mAppId/22/group/${newMessage.groupid}")
//                        }
                        }
                    }
                    if (!FilterManager.filterMessage(newMessage)) {
                        for (listener in mMessageListeners!!) {
                            LogF.d(TAG, "自定义消息发送监听")
                            listener.systemMessage(newMessage)
                        }
                    }
                }
            }
        }
    }

    private fun onSendMessageStatus(msg: Message?, isSuc: Boolean, errMsg: String?) {
        if (null != mMessageListeners) {
            for (listener in mMessageListeners!!) {
                listener.sendMessageStatus(errMsg, isSuc)
            }
        }
        if (null != msg) {
            //发送失败 更新消息和会话
            msg.time = System.currentTimeMillis()
            addDB(msg)
        }
        //测试代码 数据库更新消息
        //没有消息泽插入消息，没有会话也插入
        //如果是登录注销 消息 不插入
    }

    /**
     * 模拟测试发消息
     * @param toId 模拟发给谁
     * */
    fun testSendMsg(toId: String) {

    }

    /**
     * 模拟测试收消息
     * @param toId 模拟谁发给你
     * */
    fun testReceiveMsg(toId: String) {

    }

    fun config(config: Config): IMManager {
        mIsDebug = config.isDebug
        mUrl = config.wsUrl
        mIsNeedReconnect = config.needReconnect
        if (null != config.onConnectStatusCallback)
            addConnectListener(config.onConnectStatusCallback!!)
        mIsAutoLogin = config.isAutoLogin
        mUserName = config.userName
        mPwd = config.pwd
        mTopics.addAll(config.autoSubTopic)
        this.mLock = ReentrantLock()
        if (mIsAutoLogin) {//自动连接
            if (!TextUtils.isEmpty(mUserName)) {
                startConnect()
            }
        }
        return this
    }

    fun setUrl(url: String) {
        mUrl = url
    }

    fun setUserInfo(userName: String, pwd: String) {
        mUserName = userName
        mPwd = pwd
    }

    fun setUserName(userName: String) {
        mUserName = userName
    }

    fun setPwd(pwd: String) {
        mPwd = pwd
    }

    override val isConnectting: Boolean
        get() = mCurrentStatus == IMConnectionStatus.CONNECTTING

    override val isConnected: Boolean
        get() = mCurrentStatus == IMConnectionStatus.CONNECTED

    override val currentStatus: Int
        get() = mCurrentStatus

    override val isManualClose: Boolean
        get() = mIsManualClose

    /**获取正在进行的会话*/
    fun getCurrentChat(): Conversation? {
        return mCurrentConversation
    }

    /**开始正在进行的会话*/
    fun startChat(conversation: Conversation) {
        mCurrentConversation = conversation
    }

    /**结束记录正在进行的会话*/
    fun stopChat() {
        mCurrentConversation = null
    }

    @Synchronized
    override fun updateCurrentStatus(currentStatus: Int) {
        this.mCurrentStatus = currentStatus
    }

    //增加连接监听
    override fun addConnectListener(listener: OnConnectStatusCallback) {
        if (null == mOnConnectStatusCallbacks)
            mOnConnectStatusCallbacks = ArrayList()
        if (!mOnConnectStatusCallbacks!!.contains(listener)) {
            LogF.d(TAG, "加入连接监听完成")
            mOnConnectStatusCallbacks!!.add(listener)
        }
    }

    //移除连接监听
    override fun removeConnectListener(listener: OnConnectStatusCallback) {
        if (null != mOnConnectStatusCallbacks) {
            mOnConnectStatusCallbacks!!.remove(listener)
        }
    }

    //增加消息监听
    override fun addMessageListener(listener: OnMessageListener) {
        if (null == mMessageListeners)
            mMessageListeners = ArrayList()
        if (!mMessageListeners!!.contains(listener)) {
            LogF.d(TAG, "加入消息监听完成")
            mMessageListeners!!.add(listener)
        }
    }

    //移除消息监听
    override fun removeMessageListener(listener: OnMessageListener) {
        if (null != mMessageListeners) {
            LogF.d(TAG, "移除消息监听完成")
            mMessageListeners!!.remove(listener)
        }
    }

    fun startConnect(isNewClient: Boolean) {
        LogF.d(TAG, "开始连接 isNewClient=$isNewClient")
        buildConnect(isNewClient)
    }

    override fun startConnect() {
        buildConnect(true)
    }

    @Synchronized
    private fun buildConnect(isNewClient: Boolean) {
        mIsManualClose = false
        //启动心跳保护
        heartBeatProtect(true)
        connectStartCallback()
        when (currentStatus) {
            IMConnectionStatus.CONNECTED, IMConnectionStatus.CONNECTTING -> {
                LogF.d(TAG, "已连接或者连接中  currentStatus=$currentStatus")
            }
            else -> {
                if (null == mClient || (null != mClient && !mClient!!.isConnected)) {
                    initClient(true)
                } else {
                    initClient(isNewClient)
                }
            }
        }
        LogF.e(TAG, "连接中...")
        LogF.e(TAG, "连接信息 url=$mUrl username=$mUserName pwd=$mPwd ")
        if (!isNetworkConnected()) {
            updateCurrentStatus(IMConnectionStatus.DISCONNECTED)
            connectFailedCallback(Throwable(mContext!!.getString(R.string.im_tip_connect_network_error)))
            LogF.e(TAG, "[请您检查网络，未连接]")
        }
    }

    fun getClient(): MqttAndroidClient? {
        return mClient
    }

    private fun initClient(isNewClient: Boolean) {
        //初始化MQTT
        LogF.d(
            TAG, "连接  是否新连接=$isNewClient"
        )
        if (TextUtils.isEmpty(mUrl)) {
            updateCurrentStatus(IMConnectionStatus.DISCONNECTED)
            connectFailedCallback(Throwable(mContext!!.getString(R.string.im_tip_connect_ip_null)))
            LogF.e(TAG, "[服务器地址不能为空]")
            return
        }
        mAppId = DeviceUtils.getMetaDataFromApp(mContext, "IM_APPID")
        if (TextUtils.isEmpty(mAppId)) {
            updateCurrentStatus(IMConnectionStatus.DISCONNECTED)
            connectFailedCallback(Throwable(mContext!!.getString(R.string.im_tip_connect_appid_null)))
            LogF.e(TAG, "[AppId未设置]")
            return
        }
        if (TextUtils.isEmpty(mUserName)) {
            updateCurrentStatus(IMConnectionStatus.DISCONNECTED)
            connectFailedCallback(Throwable(mContext!!.getString(R.string.im_tip_connect_ip_null)))
            LogF.e(TAG, "[用户信息为空]")
            return
        }
        //初始化MQTT
        LogF.d(
            TAG, "连接参数  host=$mUrl  " +
                    "clientId=${DeviceUtils.getDeviceId(mContext)}  username=$mUserName  password=$mPwd"
        )
        if (isNewClient) {
            disconnect()
            mClient = MqttAndroidClient(mContext, mUrl, DeviceUtils.getDeviceId(mContext))
            mClient!!.setCallback(mCallback)
            mMQTTConnectOptions = MqttConnectOptions()
            mMQTTConnectOptions!!.isAutomaticReconnect = true//自动重连
            mMQTTConnectOptions!!.isCleanSession = true
//            LogF.d(TAG, "设置遗言 topic=will message=我的遗言111")
//            mMQTTConnectOptions!!.setWill("will", "我的遗言111".toByteArray(), mQos, false)
            if (!TextUtils.isEmpty(mUserName))
                mMQTTConnectOptions!!.userName = mUserName
            if (!TextUtils.isEmpty(mPwd))
                mMQTTConnectOptions!!.password = mPwd!!.toCharArray()
        }

        if (null == mLock) {
            this.mLock = ReentrantLock()
        }
        updateCurrentStatus(IMConnectionStatus.CONNECTTING)
        try {
            mLock!!.lockInterruptibly()
            try {
                //直接连接
                try {
//                    if (mClient!!.isConnected || mCurrentStatus == IMConnectionStatus.CONNECTED) {
//                        stopConnect()
//                    }
                    mClient!!.connect(mMQTTConnectOptions, null, mConnectingListener)
                } catch (ex: Exception) {
                    ex.printStackTrace()
                    updateCurrentStatus(IMConnectionStatus.DISCONNECTED)
                    connectFailedCallback(Throwable(mContext!!.getString(R.string.im_tip_connect_network_error)))
                    LogF.e(TAG, "[连接失败]  error=${ex.message}")
                }
            } finally {
                mLock!!.unlock()
            }
        } catch (e: InterruptedException) {
        }
    }

    private fun connectStartCallback() {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            mMainHandler.post {
                if (null != mOnConnectStatusCallbacks) {
                    for (connectStatusCallback: OnConnectStatusCallback in mOnConnectStatusCallbacks!!) {
                        connectStatusCallback.connectStart()
                    }
                }
            }
        } else {
            if (null != mOnConnectStatusCallbacks) {
                for (connectStatusCallback: OnConnectStatusCallback in mOnConnectStatusCallbacks!!) {
                    connectStatusCallback.connectStart()
                }
            }
        }
    }

    private fun connectSucCallback() {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            mMainHandler.post {
                if (null != mOnConnectStatusCallbacks) {
                    for (connectStatusCallback: OnConnectStatusCallback in mOnConnectStatusCallbacks!!) {
                        connectStatusCallback.connectSuc()
                    }
                }
            }
        } else {
            if (null != mOnConnectStatusCallbacks) {
                for (connectStatusCallback: OnConnectStatusCallback in mOnConnectStatusCallbacks!!) {
                    connectStatusCallback.connectSuc()
                }
            }
        }
    }

    private fun connectFailedCallback(exception: Throwable) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            mMainHandler.post {
                if (null != mOnConnectStatusCallbacks) {
                    for (connectStatusCallback: OnConnectStatusCallback in mOnConnectStatusCallbacks!!) {
                        connectStatusCallback.connectFailed(exception)
                    }
                }
            }
        } else {
            if (null != mOnConnectStatusCallbacks) {
                for (connectStatusCallback: OnConnectStatusCallback in mOnConnectStatusCallbacks!!) {
                    connectStatusCallback.connectFailed(exception)
                }
            }
        }
    }

    private fun disConnectCallback(exception: Throwable) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            mMainHandler.post {
                if (null != mOnConnectStatusCallbacks) {
                    for (connectStatusCallback: OnConnectStatusCallback in mOnConnectStatusCallbacks!!) {
                        connectStatusCallback.disConnect(exception)
                    }
                }
            }
        } else {
            if (null != mOnConnectStatusCallbacks) {
                for (connectStatusCallback: OnConnectStatusCallback in mOnConnectStatusCallbacks!!) {
                    connectStatusCallback.disConnect(exception)
                }
            }
        }
        PushManager.instance.stop()
        CacheManager.instance.stop()
    }

    override fun stopConnect() {
        LogF.d(TAG, "手动断开连接")
        mIsManualClose = true
        cancelReconnect()
        disconnect()
    }

    override fun destroy() {
        heartBeatProtect(false)
        stopConnect()
        PushManager.instance.stop()
        CacheManager.instance.stop()
        mLock = null
        mInstance = null
    }

    /**
     * 心跳保护
     * */
    private fun heartBeatProtect(isProtect: Boolean) {
        LogF.d(TAG, "heartBeatProtect isProtect=$isProtect")
        try {
            if (null != mContext) {
                if (isProtect) {
                    mContext!!.startService(Intent(mContext!!, IMCoreService::class.java))
                } else {
                    if (null != mContext) {
                        mContext!!.stopService(Intent(mContext!!, IMCoreService::class.java))
                        mContext = null
                    }
                }
            }
        } catch (e: RuntimeException) {
            e.printStackTrace()
        }
    }

    /**
     * 重连
     * */
    private fun tryReconnect() {
        LogF.e(TAG, "重新连接[$mReconnectCount]  是否设置了重连=$mIsNeedReconnect  手动断开连接=$mIsManualClose")
        if (!mIsNeedReconnect or mIsManualClose) {
            LogF.e(TAG, "[不需要重连或者手动关闭]")
            stopConnect()
            //放弃心跳
            mContext!!.stopService(Intent(mContext!!, IMCoreService::class.java))
            connectFailedCallback(Throwable(mContext!!.getString(R.string.im_tip_connect_manual_close)))
            return
        }
        if (!isNetworkConnected()) {
            updateCurrentStatus(IMConnectionStatus.DISCONNECTED)
            connectFailedCallback(Throwable(mContext!!.getString(R.string.im_tip_connect_network_error)))
            LogF.e(TAG, "[${mContext!!.getString(R.string.im_tip_connect_network_error)}]")
        }
        updateCurrentStatus(IMConnectionStatus.RECONNECT)
        val delay = (mReconnectCount * RECONNECT_INTERVAL).toLong()
        mMainHandler.postDelayed(mReconnectTask, if (delay > RECONNECT_MAX_TIME) RECONNECT_MAX_TIME else delay)
        mReconnectCount++
    }

    /**
     * 关闭重连线程
     * */
    private fun cancelReconnect() {
        mMainHandler.removeCallbacks(mReconnectTask)
        mReconnectCount = 0
    }

    /**
     * 断开连接
     * */
    private fun disconnect() {
        if (mCurrentStatus == IMConnectionStatus.DISCONNECTED) {
            return
        }
        //断开连接
        if (null == mClient) return
        try {
            mClient!!.disconnect()
            mClient!!.unregisterResources()
        } catch (e: MqttException) {
            e.printStackTrace()
        }
        PushManager.instance.stop()
        CacheManager.instance.stop()
        LogF.d(TAG, "已断开连接")
        updateCurrentStatus(IMConnectionStatus.DISCONNECTED)
    }

    /**
     * 发送消息
     * @param conversation 当前会断
     * @param msg 消息体
     * */
    fun sendMessage(conversation: Conversation, msg: Message) {
        msg.conversation = conversation
        sendMessage(conversation, msg, null)
    }

    /**
     * 发送消息
     * @param conversation 当前会断
     * @param msg 消息体
     * @param callback 发送状态回调
     * */
    fun sendMessage(conversation: Conversation, msg: Message, callback: OnMessageSendingCallback?) {
        msg.conversation = conversation
        if (null != callback)
            msg.mSendingCallbacks.add(callback)
        msg.mSendingCallback.onStart(msg)
        checkMsgFile(msg)
    }

    /**
     * 登录用户id
     * */
    fun getLoginId(): String {
        if (TextUtils.isEmpty(mUserName)) {
            return ""
        }
        return mUserName!!
    }

    /**
     * 是否为免打扰消息
     */
    fun isNotNotifyMsg(toId: String): Boolean {
        val userId = getLoginId()
        var isNotNotifyMsg = false
        val mTogglesMap = SharedPreferenceUtil.getHashMapData(
            mContext, CacheConstant.XML_NOT_NOTIFY_DATA, NotifyToggleInfo::class.java
        ) ?: return false
        if (!TextUtils.isEmpty(userId) && !TextUtils.isEmpty(toId)) {
            val toggleInfo = mTogglesMap[userId]
            if (toggleInfo != null) run {
                val notNotifyIds = toggleInfo.notNotifyIds
                if (notNotifyIds != null && notNotifyIds.isNotEmpty()) {
                    isNotNotifyMsg = notNotifyIds.contains(toId)
                }
            }
        }
        return isNotNotifyMsg
    }

    /**
     * 执行发送命令
     * */
    private fun commandSend(topic: String, bytes: ByteArray) {
        return commandSend(topic, null, bytes)
    }

    /**
     * 执行发送命令
     * */
    private fun commandSend(topic: String, msg: Message?, bytes: ByteArray) {
        LogF.d(
            TAG,
            "发送消息ing  topic=$topic payload=${GsonUtil.getInstance().toJson(bytes)} msg=${GsonUtil.getInstance().toJson(
                msg
            )}"
        )
        var isSend = false
        if (!isNetworkConnected()) {
            LogF.e(TAG, "[${mContext!!.getString(R.string.im_tip_connect_network_error)}]")
        } else {
            if (mClient != null && mCurrentStatus == IMConnectionStatus.CONNECTED) {
                try {
                    val mqttMessage = MqttMessage()
                    mqttMessage.payload = bytes
                    val bais = ByteArrayInputStream(bytes)
                    val dis = DataInputStream(bais)
                    val action = dis.readByte()
                    mqttMessage.qos = mQos//Qos服务质量等级 0:最多分发一次 1:至少分发一次 2:只分发一次
                    var id = bytes.hashCode()
                    if (null != msg) {
                        msg.tagId = id
                        mSendMsgStatusCallbacks.add(msg)
                    }
                    mqttMessage.id = id
                    mClient!!.publish(topic, mqttMessage)
                    isSend = true
                } catch (e: MqttException) {
                    e.printStackTrace()
                    isSend = false
                }
            } else {
                LogF.d(TAG, "发送消息失败 服务器未连接  重新连接")
                tryReconnect()
            }
        }
        //发送消息失败，尝试重连
        if (!isSend) {
            if (null != msg)
                msg.mSendingCallback.onFailed("发送失败")
            if (null != msg)
                LogF.d(
                    TAG,
                    "发送消息失败  topic=$topic 二进制=${GsonUtil.getInstance().toJson(bytes)} payload=${GsonUtil.getInstance().toJson(
                        msg
                    )}"
                )
            onSendMessageStatus(msg, false, mContext!!.getString(R.string.im_tip_msg_send_failed))
            if (mCurrentStatus == IMConnectionStatus.DISCONNECTED || null == mClient) {
                tryReconnect()
            }
        } else {
            LogF.d(
                TAG,
                "发送完成  topic=$topic 二进制=${GsonUtil.getInstance().toJson(bytes)} payload=${GsonUtil.getInstance().toJson(
                    msg
                )}"
            )
            onSendMessageStatus(msg, true, mContext!!.getString(R.string.im_tip_msg_send_suc))
        }
    }

    //设置文件消息的url
    private fun checkMsgFile(msg: Message) {
        //效验是否包含会话信息，否则不发送消息(登录注销 不算)
        val errMsg = mContext!!.getString(R.string.im_tip_msg_send_failed_conversation_null)
        if (null == msg.conversation) {
            LogF.d(TAG, "发送消息失败 未找到当前会话" + GsonUtil.getInstance().toJson(msg))
            onSendMessageStatus(
                msg,
                false,
                errMsg
            )
            msg.mSendingCallback.onFailed(errMsg)
            return
        }
        packgeMessage(msg)
        var urlType = ""
        /**
         * ### 上传图片
        # 图片： /upload/images
        # 音频: /upload/voices
        # 视频: /upload/videos
        # 文件: /upload/files
         */
        when (msg) {

            is MessageImage -> { //图片
                urlType = "/upload/images"
                uploadFile(PIC_FILE, urlType, msg.localPath, msg)
            }
            is MessageVoice -> {//语音
                urlType = "/upload/voices"
                uploadFile(VOICE_FILE, urlType, msg.localPath, msg)
            }
            is MessageVideo -> { //视频
                urlType = "/upload/videos"
                uploadFile(VIDEO_FILE, urlType, msg.localPath, msg)
            }
            is MessageFile -> {//文件
                urlType = "/upload/files"
                uploadFile(NORMAL_FILE, urlType, msg.localPath, msg)
            }
            else -> sendNormalMessage(msg)
        }
    }

    /**
     * 上传文件
     *
     * @param fileType  文件类型 100:图片  200:语音
     * @param localPath 文件的本地地址
     *
     */
    private fun uploadFile(fileType: Int, urlType: String, localPath: String, msg: Message) {
        HttpApi.getInstances()
            .postFile("http://223.203.221.89:9080/", urlType, localPath, object : HttpUploadCallBack {
                override fun onStart() {
                }

                override fun onFinish(response: NetResponse<String>?) {
                    val data = response!!.data
                    if (response.statusCode == 200) {
                        val fileInfo = GsonUtil.getInstance().toClass<FileInfo>(data, FileInfo::class.java)
                        if (fileInfo.code == 1) {
                            LogF.d(TAG, "response!!.data==>$data")
                            when (fileType) {
                                PIC_FILE -> {
                                    if (msg is MessageImage) {
                                        val url = fileInfo.data.url
                                        val small = fileInfo.data.small
                                        if (!TextUtils.isEmpty(url)) msg.imageUrl = url
                                        if (!TextUtils.isEmpty(small)) msg.thumbUrl = small
                                        LogF.d("IMLogTag_图片消息", "onFinish--url==> $url ,small==>$small")
                                        sendNormalMessage(msg)

                                    }
                                }
                                VOICE_FILE -> {
                                    if (msg is MessageVoice) {
                                        val url = fileInfo.data.url
                                        if (!TextUtils.isEmpty(url)) msg.voiceUrl = url
//                                        msg.voiceUrl = "http://ra01.sycdn.kuwo.cn/resource/n3/32/56/3260586875.mp3"
                                        LogF.d(TAG, "onFinish--音频的voiceUrl为==>${msg.voiceUrl}")
                                        sendNormalMessage(msg)
                                    }
                                }

                                NORMAL_FILE->{
                                    if (msg is MessageFile) {
                                        val url = fileInfo.data.url
                                        if (!TextUtils.isEmpty(url)) msg.fileUrl = url
//                                        msg.voiceUrl = "http://ra01.sycdn.kuwo.cn/resource/n3/32/56/3260586875.mp3"
                                        LogF.d(TAG, "onFinish--文件消息的fileUrl为==>${msg.fileUrl}")
                                        sendNormalMessage(msg)
                                    }
                                }
                            }
                        } else {
                            msg.mSendingCallback.onFailed("消息发送失败")
                            LogF.d(TAG, "statusCode==>" + response.statusCode)
                        }
                    } else {
                        msg.mSendingCallback.onFailed("消息发送失败")
                        LogF.d(TAG, "statusCode==>" + response.statusCode)
                    }
                }

                override fun onError(error: NetError) {
                    LogF.d(TAG, "errMsg==>" + error.errMsg)
                    msg.mSendingCallback.onFailed("发送消息失败" + error.errMsg)
                }

                override fun uploadProgress(pro: Float, isDone: Boolean, id: Int) {
                    msg.mSendingCallback.uploadProgress(pro, isDone)
                }
            })
    }

    //消息封装
    private fun packgeMessage(msg: Message) {
        val id = MessageTextUtil.newID()//生成消息id
        msg.id = id
        if (TextUtils.isEmpty(msg.senderId))
            msg.senderId = mUserName
        if (TextUtils.isEmpty(msg.senderName))
            msg.senderName = mUserName
        //发送中 更新消息和会话
        //测试代码 数据库更新消息
        msg.time = System.currentTimeMillis()
        addDB(msg)
    }

    //发送中 插入更新
    private fun addDB(msg: Message) {
        msg.conversation!!.lastMessage = msg.getSummaryText()
        msg.conversation!!.lastMessageTime = msg.time
        CacheManager.instance.addConversation(msg.conversation)
    }

    //发送消息
    private fun sendNormalMessage(msg: Message) {
        var payload = ""
        var topic: String = ""
        LogF.d(TAG, "会话id=${msg.conversation!!.toId}")
        when (msg.conversation!!.type) {
            MessageType.TYPE_P2P -> {
                if (mIsDebug) {
                    topic = "/1234567890/11/chat/001"
                } else {
                    topic = "/$mAppId/22/p2p/${msg.conversation!!.toId}"
                }
            }
            MessageType.TYPE_GROUP -> {
                topic = "/$mAppId/22/group/${msg.conversation!!.toId}"
            }
            MessageType.TYPE_CHATROOM -> {
                topic = "/$mAppId/22/chat/${msg.conversation!!.toId}"
            }
            MessageType.TYPE_CUSTOM -> {
                topic = "/$mAppId/22/notify/sys"
            }
        }

        payload = MessageTextUtil.createSendPayload(msg)

        //消息格式
        //测试topic
        val id = msg.id
        val ml = id.length //id长度
        val tl = topic.length//话题长度
        val pl = MessageTextUtil.getStrLen(payload)
        val baos = ByteArrayOutputStream(24 + ml + tl + pl + ml)
        val dos = DataOutputStream(baos)
        dos.write(IMManager.MSG_ACTION_PUBLISH)
        LogF.d(TAG, "测试发送消息  内容 第0位:action=${IMManager.MSG_ACTION_PUBLISH}")
        //id
//        dos.writeShort(ml)
        baos.write(ml.ushr(0) and 0xFF)
        baos.write(ml.ushr(8) and 0xFF)
        LogF.d(TAG, "测试发送消息  内容 第1位:ml=$ml byte=${ml.toByte()}")
        dos.write(id.toByteArray())
        LogF.d(TAG, "测试发送消息  内容 第3位:id=$id byte=${GsonUtil.getInstance().toJson(id.toByteArray())}")
        //payload
//        dos.writeInt(pl)
        baos.write(pl.ushr(0) and 0xFF)
        baos.write(pl.ushr(8) and 0xFF)
        baos.write(pl.ushr(16) and 0xFF)
        baos.write(pl.ushr(24) and 0xFF)
        LogF.d(TAG, "测试发送消息  内容 第3+ml位:pl=$pl byte=${pl.toByte()}")
        dos.write(payload.toByteArray())
        LogF.d(TAG, "测试发送消息  内容 第7+ml位:payload=$payload byte=${GsonUtil.getInstance().toJson(payload.toByteArray())}")
        //ack
        dos.writeByte(0)
        LogF.d(TAG, "测试发送消息  内容 第7+ml+pl位:ack=0 byte=${0.toByte()}")
        //topic
//        dos.writeShort(tl)
        baos.write(tl.ushr(0) and 0xFF)
        baos.write(tl.ushr(8) and 0xFF)
        LogF.d(TAG, "测试发送消息  内容 第8+ml+pl位:tl=$tl byte=${tl.toByte()}")
        dos.write(topic.toByteArray())
        LogF.d(TAG, "测试发送消息  内容 第10+ml+pl位:topic=$topic byte=${GsonUtil.getInstance().toJson(topic.toByteArray())}")
        //type
        var type = 0
        when (msg.type) {
            MessageContentType.TYPE_TEXT -> {
                type = 0
            }
            MessageContentType.TYPE_IMAGE -> {
                type = 1
            }
            MessageContentType.TYPE_VOICE -> {
                type = 2
            }
            MessageContentType.TYPE_VIDEO -> {
                type = 3
            }
            MessageContentType.TYPE_LOCATION -> {
                type = 4
            }
            MessageContentType.TYPE_FILE -> {
                type = 6
            }
            MessageContentType.TYPE_CUSTOM -> {
                type = 100
            }
        }
        dos.writeByte(type)
        LogF.d(TAG, "测试发送消息  内容 第10+ml+pl+tl位:type=$type byte=${type.toByte()}")
        //qos
        dos.writeByte(1)
        LogF.d(TAG, "测试发送消息  内容 第11+ml+pl+tl位:qos=1 byte=${1.toByte()}")
        //ttl
        val ttl: Long = 64
//        dos.writeLong(ttl)
        baos.write(ttl.ushr(0).toByte().toInt())
        baos.write(ttl.ushr(8).toByte().toInt())
        baos.write(ttl.ushr(16).toByte().toInt())
        baos.write(ttl.ushr(24).toByte().toInt())
        baos.write(ttl.ushr(32).toByte().toInt())
        baos.write(ttl.ushr(40).toByte().toInt())
        baos.write(ttl.ushr(48).toByte().toInt())
        baos.write(ttl.ushr(56).toByte().toInt())
        LogF.d(TAG, "测试发送消息  内容 第12+ml+pl+tl位:ttl=$ttl byte=${ttl.toByte()}")
        //sender
//        dos.writeShort(0)
        baos.write(0.ushr(0) and 0xFF)
        baos.write(0.ushr(8) and 0xFF)
        LogF.d(TAG, "测试发送消息  内容 第20+ml+pl+tl位:sender=0 byte=${0.toByte()}")
        //timestamp
//        dos.writeShort(0)
        baos.write(0.ushr(0) and 0xFF)
        baos.write(0.ushr(8) and 0xFF)
        LogF.d(TAG, "测试发送消息  内容 第22+ml+pl+tl位:ts=0 byte=${0.toByte()}")
        //raw msgid
        dos.write(id.toByteArray())
        LogF.d(TAG, "测试发送消息  内容 第24+ml+pl+tl位:id=$id byte=${GsonUtil.getInstance().toJson(id.toByteArray())}")
        dos.close()
        val bytes = baos.toByteArray()
        dos.close()
        baos.close()
        commandSend(topic, msg, bytes)
//        //给自己发送一份回执
//        commandSend("/$mAppId/11/p2p/${getLoginId()}", msg, bytes)
    }

    //拉取消息
    fun getMessageFromConversation(conversation: Conversation, msgId: String, count: Int) {
        var topic = ""
        if (mIsDebug) {
            topic = "/1234567890/22/chat/001"
        } else {
            when (conversation.type) {
                MessageType.TYPE_P2P -> {
                    topic = "/$mAppId/22/p2p/${conversation.toId}"
                }
                MessageType.TYPE_GROUP -> {
                    topic = "/$mAppId/22/group/${conversation.toId}"
                }
                MessageType.TYPE_CHATROOM -> {
                    topic = "/$mAppId/22/chat/${conversation.toId}"
                }
                MessageType.TYPE_CUSTOM -> {
                    topic = "/$mAppId/22/notify/sys"
                }
            }
        }
        getMessageFromTopic(topic, msgId, count)
    }

    //拉取消息
    fun getMessageFromTopic(topic: String, msgId: String, count: Int) {
        LogF.d(TAG, "拉取消息 topic=$topic msgId=$msgId count=$count")
        val ml = msgId.length //offset length  int
        val baos = ByteArrayOutputStream(3 + ml)
        val dos = DataOutputStream(baos)
        // pull command
        dos.writeByte(MSG_ACTION_PULL)
        // count
//        dos.writeShort(count)
        dos.writeByte(count.ushr(0) and 0xFF)
        dos.writeByte(count.ushr(8) and 0xFF)
        // offset
        dos.write(msgId.toByteArray())
        val bytes = baos.toByteArray()
        dos.close()
        baos.close()
//        暂时不走
        commandSend(topic, bytes)
    }

    //减小计数
    fun sendReduceCount(topic: String, count: Int) {
        val baos = ByteArrayOutputStream(3)
        val dos = DataOutputStream(baos)
        // pull command
        dos.writeByte(MSG_ACTION_REDUCE_COUNT)
        // count
//        dos.writeShort(count)
        baos.write(count.ushr(0) and 0xFF)
        baos.write(count.ushr(8) and 0xFF)
        val bytes = baos.toByteArray()
        dos.close()
        baos.close()
        commandSend(topic, bytes)
    }

    //标记已读
    fun markRead(topic: String, msgId: String) {
        val tl = topic.length
        val ml = msgId.length
        val baos = ByteArrayOutputStream(5 + tl + ml)
        val dos = DataOutputStream(baos)
        dos.writeByte(MSG_ACTION_MARK_READ)
//        dos.writeShort(tl)
        baos.write(tl.ushr(0) and 0xFF)
        baos.write(tl.ushr(8) and 0xFF)
        dos.write(topic.toByteArray())

//        dos.writeShort(ml)
        baos.write(ml.ushr(0) and 0xFF)
        baos.write(ml.ushr(8) and 0xFF)
        dos.write(msgId.toByteArray())
        val bytes = baos.toByteArray()
        dos.close()
        baos.close()
        commandSend(topic, bytes)
    }

    //获取在线用户 后期回调
    fun sendAllOnlineUsers(topic: String) {
        val tl = topic.length
        val baos = ByteArrayOutputStream(1 + tl)
        val dos = DataOutputStream(baos)
        dos.writeByte(MSG_ACTION_ONLINE_USERS)
        dos.write(topic.toByteArray())
        val bytes = baos.toByteArray()
        dos.close()
        baos.close()
        commandSend(topic, bytes)
    }

    //获取聊天室成员  后期回调
    fun sendRoomMembers(topic: String) {
        val tl = topic.length
        val baos = ByteArrayOutputStream(1 + tl)
        val dos = DataOutputStream(baos)
        dos.writeByte(MSG_ACTION_ALL_MEMBER)
        dos.write(topic.toByteArray())
        val bytes = baos.toByteArray()
        dos.close()
        baos.close()
        commandSend(topic, bytes)
    }

    //加入聊天室
    fun sendJoinChatRoom(topic: String) {
        val tl = topic.length
        val baos = ByteArrayOutputStream(1 + tl)
        val dos = DataOutputStream(baos)
        dos.writeByte(MSG_ACTION_JOIN_ROOM)
        dos.write(topic.toByteArray())
        val bytes = baos.toByteArray()
        dos.close()
        baos.close()
        commandSend(topic, bytes)
    }

    //退出聊天室
    fun sendLeaveChatRoom(topic: String) {
        val tl = topic.length
        val baos = ByteArrayOutputStream(1 + tl)
        val dos = DataOutputStream(baos)
        dos.writeByte(MSG_ACTION_LEAVE_ROOM)
        dos.write(topic.toByteArray())
        val bytes = baos.toByteArray()
        dos.close()
        baos.close()
        commandSend(topic, bytes)
    }

    //检查网络是否连接
    fun isNetworkConnected(): Boolean {
        return if (mContext != null) {
            NetworkUtil.isNetworkAvailable(mContext)
        } else false
    }

    /**
     * 获取会话管理器
     * */
    fun getConversationManager(): ConversationManager {
        return ConversationManager.instance
    }

    /**
     * 获取联系人管理器
     * */
    fun getContactsManager(): ContactManager {
        return ContactManager.instance
    }

    /**
     * 批量订阅
     * @param topics 多个话题
     * */
    fun subscribeToTopics(topics: List<String>) {
        if (null == mClient || mCurrentStatus != IMConnectionStatus.CONNECTED) {
            LogF.d(TAG, "没连接服务器")
            return
        }
        for (i in topics.indices) {
            subscribeToTopic(topics[i])
        }
    }

    /**
     * 订阅
     * @param topic 话题
     * */
    fun subscribeToTopic(topic: String) {
        LogF.d(TAG, "订阅subscribeToTopic topic=$topic")
        if (null == mClient || mCurrentStatus != IMConnectionStatus.CONNECTED) {
            LogF.d(TAG, "没连接服务器 $topic")
            return
        }
        try {
            mClient!!.subscribe(topic, mQos, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken) {
                    LogF.d(
                        TAG,
                        "订阅话题成功 topic:$topic  长度=${topic.length}  二进制=${GsonUtil.getInstance().toJson(topic.toByteArray())}  " +
                                "当前topic=${GsonUtil.getInstance().toJson(
                                    asyncActionToken.topics
                                )}"
                    )
                }

                override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable?) {
                    val errMsg =
                        "[订阅话题失败]   $topic" + if (null == exception) "[订阅话题失败]" else "${exception.cause} : ${exception.message}"
                    LogF.d(TAG, errMsg)
                    mFailedRetryTopic.add(topic)
                }
            })
        } catch (ex: MqttException) {
            System.err.println("Exception whilst subscribing")
            ex.printStackTrace()
        }

    }

    /**
     * 取消订阅
     * @param topics 多个话题
     * @param isFinishAutoDisconnect 是否取消后自动断开连接
     * */
    fun unsubscribeToTopic(topics: List<String>, isFinishAutoDisconnect: Boolean) {
        if (null == mClient || mCurrentStatus != IMConnectionStatus.CONNECTED) {
            LogF.d(TAG, "没连接服务器")
            return
        }
        for (i in topics.indices) {
            unSubscribeTopic(topics[i])
            if (isFinishAutoDisconnect && i == topics.size - 1) {
                stopConnect()
            }
        }
    }

    /**
     * 取消订阅
     * @param topic 话题
     * */
    fun unSubscribeTopic(topic: String) {
        LogF.d(TAG, "注销订阅 topic=$topic")
        if (null == mClient || mCurrentStatus != IMConnectionStatus.CONNECTED) {
            LogF.d(TAG, "没连接服务器 $topic")
            return
        }
        try {
            mClient!!.unsubscribe(topic, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken) {
                    LogF.d(
                        TAG,
                        "注销订阅成功 topic==>" + topic + "  当前topic=" + GsonUtil.getInstance().toJson(asyncActionToken.topics)
                    )
                }

                override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable?) {
                    val errMsg =
                        "[注销订阅失败]  $topic " + if (null == exception) "[注销订阅失败]" else "${exception.cause} : ${exception.message}"
                    LogF.d(TAG, errMsg)
                }
            })
        } catch (ex: MqttException) {
            ex.printStackTrace()
        }

    }

    /**
     * IM连接配置
     * */
    class Config {
        var isDebug: Boolean = false
        var wsUrl: String? = ""
        var needReconnect = true
        var onConnectStatusCallback: OnConnectStatusCallback? = null
        var isAutoLogin = false//是否自动登录
        var userName: String? = ""
        var pwd: String? = ""
        var autoSubTopic: MutableList<String> = ArrayList()

        fun debug(isDebug: Boolean): Config {
            this.isDebug = isDebug
            return this
        }

        fun url(`val`: String): Config {
            wsUrl = `val`
            return this
        }

        fun needReconnect(`val`: Boolean): Config {
            needReconnect = `val`
            return this
        }

        fun autoLogin(username: String, password: String): Config {
            this.isAutoLogin = true
            this.userName = username
            this.pwd = password
            return this
        }

        fun topic(topic: String): Config {
            this.autoSubTopic.add(topic)
            return this
        }

        fun topic(topics: ArrayList<String>): Config {
            this.autoSubTopic.addAll(topics)
            return this
        }

        fun callback(callback: OnConnectStatusCallback): Config {
            onConnectStatusCallback = callback
            return this
        }
    }

    /**
     * 上下文
     * */
    fun getContext(): Context? {
        return mContext
    }

    companion object {
        private const val PING_INTERVAL: Long = 15L//ping 间隔 秒
        private const val RECONNECT_INTERVAL = 10 * 1000//重连自增步长
        private const val RECONNECT_MAX_TIME = (120 * 1000).toLong()//最大重连间隔

        //消息意图
        const val MSG_ACTION_PUBLISH: Int = 98//发布消息和收到的消息
        const val MSG_ACTION_UNREAD_COUNT: Int = 107//未读消息
        const val MSG_ACTION_PULL: Int = 108//拉取新消息
        const val MSG_ACTION_REDUCE_COUNT: Int = 112//减少计数  不明白什么意思
        const val MSG_ACTION_ONLINE_USERS: Int = 113//所有在线用户
        const val MSG_ACTION_MARK_READ: Int = 114//标记已读
        const val MSG_ACTION_JOIN_ROOM: Int = 115//加入聊天室
        const val MSG_ACTION_LEAVE_ROOM: Int = 116//退出聊天室
        const val MSG_ACTION_ONLINE: Int = 117//上线
        const val MSG_ACTION_OFFLINE: Int = 118//下线
        const val MSG_ACTION_ALL_MEMBER: Int = 119//所有聊天室成员
        const val MSG_ACTION_CLIENT_SEARCH: Int = 120//客户端检索消息

        @Volatile
        private var mInstance: IMManager? = null

        val instance: IMManager
            @Synchronized
            get() {
                if (null == mInstance) {
                    throw NullPointerException("IMSDK没有初始化，请在Application中调用IMManager.init(applicationContext)")
                }
                return mInstance!!
            }

        fun isInstanced(): Boolean {
            return null != mInstance
        }

        @Synchronized
        fun init(context: Context, pushIntentClass: Class<*>): IMManager {
            if (mInstance == null) {
                synchronized(IMManager::class) {
                    if (mInstance == null) {
                        mInstance = IMManager(context, pushIntentClass)
                    }
                }
            } else {//如果此前有实例且已连接 则断开连接

            }
            return mInstance!!
        }
    }
}
