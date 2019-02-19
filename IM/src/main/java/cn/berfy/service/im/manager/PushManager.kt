package cn.berfy.service.im.manager

import android.content.Context
import android.content.Intent
import cn.berfy.sdk.mvpbase.manager.NotificationManager
import cn.berfy.sdk.mvpbase.util.AppUtils
import cn.berfy.sdk.mvpbase.util.DeviceUtils
import cn.berfy.sdk.mvpbase.util.LogF
import cn.berfy.service.im.manager.i.OnMessageListener
import cn.berfy.service.im.model.*
import cn.berfy.service.im.model.group.MessageGroupSystem
import cn.berfy.service.im.model.group.MessageGroupTip
import java.lang.NullPointerException

/**
author: Berfy
date: 2019/1/24
消息推送管理
 */
class PushManager private constructor(context: Context, pushIntentClass: Class<*>) : OnMessageListener {

    private val TAG = "IM推送"
    private var mContext: Context? = null
    private var mPushIntentClass: Class<*>? = null
    private lateinit var mNotifyManager: NotificationManager
    private var mIsStart = false
    private var mShockLastTime = 0L
    private val mShockInterval = 2000

    init {
        mContext = context
        mPushIntentClass = pushIntentClass
        initNotifyManager()
    }

    fun start() {
        if (!mIsStart) {
            mIsStart = true
            IMManager.instance.addMessageListener(this)
        }
    }

    override fun newMessage(message: Message?) {
        LogF.d(TAG, "推送管理")
        if (null == message) return
        val imManager = IMManager.instance
        val userId = imManager.getLoginId()
        val conversation = message.conversation
        if (null != message.conversation
                && null != imManager.getCurrentChat()
                && imManager.getCurrentChat()!!.type == message.conversation!!.type
                && imManager.getCurrentChat()!!.toId == message.conversation!!.toId
        ) {
            LogF.d(TAG, "当前进行会话ing  不可以震动")
            return
        }
        if (message.senderId == userId) {
            LogF.d(TAG, "自己的消息 作废")
            return
        }
        if (message.conversation!!.type == MessageType.TYPE_CHATROOM) {
            LogF.d(TAG, "聊天室消息 不推送")
            return
        }
        //免打扰消息推送拦截
        val isNotNotifyMsg = imManager.isNotNotifyMsg(conversation!!.toId)
        if (isNotNotifyMsg) {
            LogF.d(TAG, "是免打扰消息 不推送")
            return
        }
        notifyContent(message)
    }

    override fun systemMessage(message: MessageCustom?) {
        if (null == message) {
            return
        }
        if (null != message.conversation && message.conversation!!.type == MessageType.TYPE_P2P && message.senderId
                == IMManager.instance.getLoginId()) {
            LogF.d(TAG, "单聊 自己的消息 作废")
            return
        }
    }

    override fun refreshConversation() {
    }

    override fun sendMessageStatus(message: String?, isSuc: Boolean) {
    }

    fun stop() {
        mIsStart = false
        IMManager.instance.removeMessageListener(this)
    }

    private fun initNotifyManager() {
        mNotifyManager = NotificationManager.newInstance(mContext, mPushIntentClass)
    }

    private fun notifyContent(message: Message) {
        if (!AppUtils.isBackground(mContext)) {//前台不需要推送  震动
            LogF.d(TAG, "前台不需要推送 仅震动")
            //避免频繁震动 新消息过多时 2秒震动一次
            if (System.currentTimeMillis() - mShockLastTime > mShockInterval) {
                mShockLastTime = System.currentTimeMillis()
                DeviceUtils.doShock(mContext)
            }
            return
        }

        LogF.d(TAG, "后台需要推送")
        when (message) {
            is MessageText -> {
                val intent = Intent(mContext, mPushIntentClass)
                intent.putExtra("type", "0")
                intent.putExtra("data", message.content)
                mNotifyManager.setIntent(intent)
                mNotifyManager.title = "您有新消息"
                mNotifyManager.content = message.senderId + ":" + message.getSummaryText()
                mNotifyManager.notify("chat")
            }
            is MessageImage -> {
                val intent = Intent(mContext, mPushIntentClass)
                intent.putExtra("type", "0")
                intent.putExtra("data", message.imageUrl)
                mNotifyManager.setIntent(intent)
                mNotifyManager.title = "您有新消息"
                mNotifyManager.content = message.senderId + ":" + "[图片]"
                mNotifyManager.notify("chat")
            }
            is MessageVoice -> {
                val intent = Intent(mContext, mPushIntentClass)
                intent.putExtra("type", "0")
                intent.putExtra("data", message.voiceUrl)
                mNotifyManager.setIntent(intent)
                mNotifyManager.title = "您有新消息"
                mNotifyManager.content = message.senderId + ":" + "[语音]"
                mNotifyManager.notify("chat")
            }
            is MessageFile -> {
                val intent = Intent(mContext, mPushIntentClass)
                intent.putExtra("type", "0")
                intent.putExtra("data", message.fileUrl)
                mNotifyManager.setIntent(intent)
                mNotifyManager.title = "您有新消息"
                mNotifyManager.content = message.senderId + ":" + "[文件]"
                mNotifyManager.notify("chat")
            }
            is MessageVideo -> {
                val intent = Intent(mContext, mPushIntentClass)
                intent.putExtra("type", "0")
                intent.putExtra("data", message.videoUrl)
                mNotifyManager.setIntent(intent)
                mNotifyManager.title = "您有新消息"
                mNotifyManager.content = message.senderId + ":" + "[视频]"
                mNotifyManager.notify("chat")
            }
            is MessageLocation -> {//位置消息
                val intent = Intent(mContext, mPushIntentClass)
                intent.putExtra("type", "0")
                intent.putExtra("data", message.lng + "," + message.lat)
                mNotifyManager.setIntent(intent)
                mNotifyManager.title = "您有新消息"
                mNotifyManager.content = message.senderId + ":" + "[位置]"
                mNotifyManager.notify("chat")

            }
            is MessageCustom -> {//自定义消息

            }
            is MessageSystem -> {//群系统消息

            }
            is MessageGroupSystem -> {//群系统消息

            }
            is MessageGroupTip -> {//群提示消息

            }
        }

    }

    //清理通知
    fun clearNotify(id: Int) {
        mNotifyManager.clearNotify(id)
    }

    //清理通知 聊天tag是chat
    fun clearNotify(tag: String) {
        mNotifyManager.clearNotify(tag)
    }

    companion object {

        private var mInstance: PushManager? = null

        val instance: PushManager
            @Synchronized
            get() {
                if (null == mInstance) {
                    throw NullPointerException("未初始化推送管理器")
                }
                return mInstance!!
            }

        @Synchronized
        fun init(context: Context, pushIntentClass: Class<*>): PushManager {
            if (mInstance == null) {
                synchronized(IMManager::class) {
                    if (mInstance == null) {
                        mInstance = PushManager(context, pushIntentClass)
                    }
                }
            } else {//如果此前有实例且已连接 则断开连接

            }
            return mInstance!!
        }
    }

}
