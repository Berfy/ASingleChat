package cn.berfy.service.mqtt.manager

import android.content.Context
import android.content.Intent
import android.text.TextUtils
import cn.berfy.sdk.mvpbase.util.GsonUtil
import cn.berfy.sdk.mvpbase.util.LogF
import cn.berfy.sdk.mvpbase.util.NetworkUtil
import cn.berfy.sdk.mvpbase.util.ToastUtil
import cn.berfy.service.mqtt.service.HeartBeatService
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*
import java.util.*

/**
 * author: Berfy
 * payload: 2018/9/15
 */
class MQTTManager private constructor(private val mContext: Context) {

    private val TAG = "MQTT连接管理"
    private var mClient: MqttAndroidClient? = null
    private var mMQTTConnectOptions: MqttConnectOptions? = null
    private var mIsConnected: Boolean = false//是否连接成功
    var isConnectting: Boolean = false
        private set//是否正在连接
    private var mHost: String? = null//主机ip
    private var mClientId: String? = null//客户端标识码
    private var mUserName: String? = null//登录名
    private var mPassWord: String? = null//登录密码
    private var mQos = 1//消息质量 0最多发送一次 1至少发送一次（推荐） 2只发送一次（服务器去重+确认  资源消耗大）
    private val mTopics: MutableList<String> = ArrayList()//话题集合
    private var mWillTopic: String? = null//指定遗言发送到的话题
    private var mWillMessage: String? = null//遗言
    private var mOnMQTTCallback: OnMQTTCallback? = null

    private val mCallback = object : MqttCallbackExtended {
        override fun connectComplete(reconnect: Boolean, serverURI: String) {
            mIsConnected = true
            isConnectting = false
            if (reconnect) {
                if (mTopics.size > 0) {
                    LogF.d(TAG, "Callback_mTopicSize==>" + mTopics.size)
                    for (i in mTopics.indices) {
                        LogF.d(TAG, "Callback_mTopic==>" + mTopics[i])
                        subscribeToTopic(mTopics[i])
                    }
                }
                LogF.d(TAG, "重连连接完毕")
            } else {
                LogF.d(TAG, "连接完毕")
            }
            if (null != mOnMQTTCallback) {
                mOnMQTTCallback!!.connectComplete(reconnect, serverURI)
            }
        }

        override fun connectionLost(exception: Throwable?) {
            mIsConnected = false
            isConnectting = false
            if (null != mOnMQTTCallback) {
                mOnMQTTCallback!!.connectionLost(exception)
            }
            if (null == exception) {
                LogF.d(TAG, "连接丢失")
                return
            }

            val intent = Intent(mContext, HeartBeatService::class.java)
            intent.putExtra("test", "1")
            mContext.stopService(intent)
            LogF.d(
                TAG, "连接丢失  message=" + exception.message
                        + "   cause=" + exception.cause
                        + "   lLocalizedMessage=" + exception.localizedMessage
            )
        }

        @Throws(Exception::class)
        override fun messageArrived(topic: String, message: MqttMessage?) {//接收到后台推送推送来的消息
            LogF.d(TAG, "收到新消息  topic=$topic")
            if (null == message) {
                LogF.d(TAG, "推送解析失败null == message")
                return
            }
            val json = String(message.payload)
            //
            LogF.d(TAG, "收到新消息   ===  topic=$topic           $json")
            //            if (TextUtils.isEmpty(json)) {
            //                LogF.d(TAG, "null == msg");
            //                return;
            //            }
            //处理推送的消息
            if (null != mOnMQTTCallback) {
                mOnMQTTCallback!!.newMessage(topic, json)
            }
        }

        override fun deliveryComplete(token: IMqttDeliveryToken) {
            try {
                LogF.d(TAG, "发送完毕 " + String(token.message.payload))
                if (null != mOnMQTTCallback) {
                    val json = String(token.message.payload)
                    mOnMQTTCallback!!.sendSuc(json)
                }
            } catch (e: MqttException) {
                e.printStackTrace()
                if (null != mOnMQTTCallback) {
                    mOnMQTTCallback!!.sendSuc(null)
                }
            }

        }

    }

    private val mListener = object : IMqttActionListener {
        override fun onSuccess(asyncActionToken: IMqttToken) {
            mIsConnected = true
            isConnectting = false
            LogF.d(TAG, "连接成功  当前所有订阅topic=====" + GsonUtil.getInstance().toJson(asyncActionToken.topics))
            val disconnectedBufferOptions = DisconnectedBufferOptions()
            disconnectedBufferOptions.isBufferEnabled = true
            disconnectedBufferOptions.bufferSize = 100
            disconnectedBufferOptions.isPersistBuffer = false
            disconnectedBufferOptions.isDeleteOldestMessages = false
            mClient!!.setBufferOpts(disconnectedBufferOptions)
            if (mTopics.size > 0) {
                LogF.d(TAG, "ActionListener_mTopicSize==>" + mTopics.size)
                for (i in mTopics.indices) {
                    LogF.d(TAG, "ActionListener_mTopic==>" + mTopics[i])
                    subscribeToTopic(mTopics[i])
                }
            }
            if (null != mOnMQTTCallback) {
                mOnMQTTCallback!!.connectSuc()
            }

            val intent = Intent(mContext, HeartBeatService::class.java)
            intent.putExtra("test", "1")
            mContext.startService(intent)
        }

        override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable?) {
            mIsConnected = false
            isConnectting = false
            if (null != mOnMQTTCallback) {
                mOnMQTTCallback!!.connectFailed(exception)
            }
            if (null == exception) {
                LogF.d(TAG, "连接失败")
                return
            }
            LogF.d(
                TAG, "连接失败 Failed to connect to: " + asyncActionToken
                        + "   message=" + exception.message
                        + "   cause=" + exception.cause
                        + "   lLocalizedMessage=" + exception.localizedMessage
            )
        }
    }

    val isConnected: Boolean
        get() = null != mClient && mClient!!.isConnected

    class Builder {

        private var mHost: String? = null//主机ip
        private var mClientId: String? = null//客户端标识码
        private var mUserName: String? = null//登录名
        private var mPassWord: String? = null//登录密码
        private var mQos: Int = 0//消息质量 0最多发送一次 1至少发送一次（推荐） 2只发送一次（服务器去重+确认  资源消耗大）
        private var mWillTopic: String? = null//指定遗言发送到的话题
        private var mWillMessage: String? = null//遗言
        private val mTopics: MutableList<String> = ArrayList()//话题集合
        private var mOnMQTTCallback: OnMQTTCallback? = null

        fun host(serverIp: String): Builder {
            mHost = serverIp
            return this
        }

        fun clientId(clientId: String): Builder {
            mClientId = clientId
            return this
        }

        fun userName(userName: String): Builder {
            mUserName = userName
            return this
        }

        fun passWord(password: String): Builder {
            mPassWord = password
            return this
        }

        fun qos(qos: Int): Builder {
            mQos = qos
            return this
        }

        fun willTopic(willTopic: String): Builder {
            mWillTopic = willTopic
            return this
        }

        fun willMessage(willMessage: String): Builder {
            mWillMessage = willMessage
            return this
        }

        fun topic(topic: String): Builder {
            mTopics.add(topic)
            return this
        }

        fun topics(topics: MutableList<String>): Builder {
            mTopics.addAll(topics)
            return this
        }

        fun callBack(callBack: OnMQTTCallback): Builder {
            mOnMQTTCallback = callBack
            return this
        }

        fun build(): MQTTManager {
            val manager = MQTTManager.instance
            manager.setHost(mHost)
            manager.setClientId(mClientId)
            manager.setUserName(mUserName)
            manager.setPassWord(mPassWord)
            manager.setPassWord(mPassWord)
            manager.setPassWord(mPassWord)
            if (mTopics.size > 0) {
                manager.setTopic(mTopics)
            }
            manager.setCallBack(mOnMQTTCallback)
            return manager
        }

    }

    fun setHost(serverIp: String?): MQTTManager? {
        mHost = serverIp
        return mMQTTManager
    }

    fun setClientId(clientId: String?): MQTTManager? {
        mClientId = clientId
        return mMQTTManager
    }

    fun setUserName(userName: String?): MQTTManager? {
        mUserName = userName
        return mMQTTManager
    }

    fun setPassWord(password: String?): MQTTManager? {
        mPassWord = password
        return mMQTTManager
    }

    fun setQos(qos: Int) {
        mQos = qos
    }

    fun setWillTopic(willTopic: String) {
        mWillTopic = willTopic
    }

    fun setWillMessage(willMessage: String) {
        mWillMessage = willMessage
    }

    fun setTopic(topic: String): MQTTManager? {
        mTopics.add(topic)
        return mMQTTManager
    }

    fun setTopic(topics: List<String>): MQTTManager? {
        mTopics.addAll(topics)
        return mMQTTManager
    }

    fun setCallBack(callBack: OnMQTTCallback?) {
        mOnMQTTCallback = callBack
    }

    fun connect(isNeedNewConnection: Boolean) {
        mIsConnected = false
        isConnectting = false
        if (!NetworkUtil.isNetworkAvailable(mContext)) {
            if (null != mOnMQTTCallback) {
                mOnMQTTCallback!!.connectFailed(Exception("没有网络"))
            }
            return
        }
        if (TextUtils.isEmpty(mHost)) {
            LogF.d(TAG, "主机名为空")
        } else if (TextUtils.isEmpty(mClientId)) {
            LogF.d(TAG, "clientId为空")
        }
//        else if (TextUtils.isEmpty(mUserName) || TextUtils.isEmpty(mPassWord)) {
//            LogF.d(TAG, "用户名密码为空")
//        }
        else {
            if (isNeedNewConnection) {
                LogF.d(TAG, "新连接")
                disconnect() //断开之前的连接
                mClient = MqttAndroidClient(mContext, mHost, mClientId)
                mClient!!.setCallback(mCallback)
                mMQTTConnectOptions = MqttConnectOptions()
                mMQTTConnectOptions!!.isAutomaticReconnect = true//自动重连
                mMQTTConnectOptions!!.isCleanSession = false
                if (!TextUtils.isEmpty(mWillTopic) && !TextUtils.isEmpty(mWillMessage)) {
                    LogF.d(TAG, "设置遗言 topic=$mWillTopic message=$mWillMessage")
                    mMQTTConnectOptions!!.setWill(mWillTopic!!, mWillMessage!!.toByteArray(), mQos, false)
                }
                if (!TextUtils.isEmpty(mUserName))
                    mMQTTConnectOptions!!.userName = mUserName
                if (!TextUtils.isEmpty(mPassWord))
                    mMQTTConnectOptions!!.password = mPassWord!!.toCharArray()
            } else {
                LogF.d(TAG, "连接已存在")
                if (null == mClient || null == mMQTTConnectOptions) {//实例被销毁，递归
                    connect(true)
                }
            }
            if (null != mClient && mClient!!.isConnected) {
                LogF.d(TAG, "已连接，不用再次连接")
                mIsConnected = true
                isConnectting = false
                return
            }
            LogF.d(
                TAG, "连接参数  host=" + mHost + " clienId=" + mClientId +
                        " username=" + mUserName + " password=" + mPassWord + " topics=" + GsonUtil.getInstance().toJson<List<String>>(
                    mTopics
                )
            )
            //直接连接
            try {
                mClient!!.connect(mMQTTConnectOptions, null, mListener)
            } catch (ex: MqttException) {
                ex.printStackTrace()
                mIsConnected = false
                isConnectting = false
                if (null != mOnMQTTCallback) {
                    mOnMQTTCallback!!.connectFailed(ex)
                }
            }

        }
    }

    fun publish(topic: String, msg: String) {
        if (null == mClient) {
            ToastUtil.getInstances().showShort("未连接服务")
            return
        }
        if (!isConnected) {
            connect(false)
            return
        }
        try {
            val mqttMessage = MqttMessage()
            mqttMessage.payload = msg.toByteArray()
            mqttMessage.qos = mQos//Qos服务质量等级 0:最多分发一次 1:至少分发一次 2:只分发一次
            mqttMessage.id = System.currentTimeMillis().toInt()
            mClient!!.publish(topic, mqttMessage)
        } catch (e: MqttException) {
            e.printStackTrace()
        }

    }

    //取消订阅
    fun unsubscribeToTopic(topics: List<String>) {
        for (i in topics.indices) {
            unSubscribeTopic(topics[i])
            if (i == topics.size - 1) {
                disconnect()
            }
        }
    }

    fun unSubscribeTopic(topic: String) {
        try {
            mClient!!.unsubscribe(topic, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken) {
                    LogF.d(
                        TAG,
                        "注销订阅成功 topic==>" + topic + "  当前topic=" + GsonUtil.getInstance().toJson(asyncActionToken.topics)
                    )
                    if (null != mOnMQTTCallback) {
                        mOnMQTTCallback!!.unSubscribeUpdate(topic, true, "注销订阅成功")
                    }
                }

                override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable?) {
                    var message: String? = ""
                    if (exception != null) {
                        message = exception.message
                    }
                    LogF.d(TAG, "注销订阅失败 $asyncActionToken   $message")
                    if (null != mOnMQTTCallback) {
                        mOnMQTTCallback!!.unSubscribeUpdate(topic, false, message)
                    }
                }
            })
        } catch (ex: MqttException) {
            ex.printStackTrace()
        }

    }


    fun subscribeToTopic(topic: String) {
        try {
            mClient!!.subscribe(topic, 0, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken) {
                    LogF.d(
                        TAG,
                        "订阅话题成功 topic:" + topic + "  当前topic=" + GsonUtil.getInstance().toJson(asyncActionToken.topics)
                    )
                    if (null != mOnMQTTCallback) {
                        mOnMQTTCallback!!.subscribeUpdate(topic, true, "订阅话题成功")
                    }
                }

                override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable?) {
                    var message: String? = ""
                    if (exception != null) {
                        message = exception.message
                    }
                    LogF.d(TAG, "订阅话题失败 $asyncActionToken   $message")
                    if (null != mOnMQTTCallback) {
                        mOnMQTTCallback!!.subscribeUpdate(topic, false, message)
                    }
                }
            })
        } catch (ex: MqttException) {
            System.err.println("Exception whilst subscribing")
            ex.printStackTrace()
        }

    }

    fun disconnect() {
        if (null == mClient) return
        try {
            mClient!!.disconnect()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        //关闭心跳服务
        val intent = Intent(mContext, HeartBeatService::class.java)
        intent.putExtra("test", "1")
        mContext.stopService(intent)
    }

    interface OnMQTTCallback {
        fun connectStart() //连接开始

        fun connectSuc() //连接成功

        fun connectFailed(exception: Throwable?) //连接失败

        fun connectComplete(reconnect: Boolean, serverURI: String?) //连接完成

        fun connectionLost(exception: Throwable?) //连接丢失

        fun newMessage(topic: String?, message: String?) //有新消息

        fun sendSuc(message: String?) //发送消息成功

        fun subscribeUpdate(topic: String?, isSuc: Boolean, errmsg: String?) //订阅状态

        fun unSubscribeUpdate(topic: String?, isSuc: Boolean, errmsg: String?) //取消订阅状态

    }

    companion object {
         var mMQTTManager: MQTTManager? = null

        fun init(context: Context) {
            if (null == mMQTTManager) {
                mMQTTManager = MQTTManager(context)
            }
        }

        val instance: MQTTManager
            get() {
                if (null == mMQTTManager) {
                    throw NullPointerException("没有在Applicaion中初始化(init)MQTTManager")
                }
                return mMQTTManager!!
            }
    }
}
