package cn.berfy.service.im.model

import android.content.Context
import android.view.View
import android.widget.RelativeLayout
import cn.berfy.sdk.http.http.okhttp.utils.NetworkUtil
import cn.berfy.sdk.mvpbase.util.LogF
import cn.berfy.service.im.R
import cn.berfy.service.im.manager.IMManager
import cn.berfy.service.im.manager.i.OnMessageDownloadCallback
import cn.berfy.service.im.manager.i.OnMessageSendingCallback
import cn.berfy.service.im.model.conversation.Conversation
import cn.berfy.service.im.util.IMTimeUtil

/**
author: Berfy
date: 2018/12/25
消息  包含消息内容和发送接收方等信息
 */
abstract class Message {

    protected var TAG: String = "IM_消息"
    var id: String = ""//消息id
    var tagId: Int = 0//标识id
    var acked: Boolean = false//是否已读
    var conversation: Conversation? = null
    var senderId: String? = ""//发送方 id(群id)
    var senderName: String? = ""//发送方 设备名称
    var senderAvatar: String? = ""//发送方的头像

    var time: Long = 0L//发送时间
    var hasTime: Boolean = false//是否需要显示时间
    //get() = senderId == "userId"
    var type: MessageContentType = MessageContentType.TYPE_TEXT//消息类型 say聊天  login登录 logout注销
    //消息发送状态
    var sendStatus: Int = STATUS_SEND //发送状态 发送完毕或者失败需要同步状态

    //消息发送状态
    companion object {
        val STATUS_SEND = 0 //发送中
        val STATUS_SEND_SUC = 1//已发送
        val STATUS_SEND_FAILED = 2//发送失败
    }

    //支持多页面监听集合
    val mSendingCallbacks: MutableList<OnMessageSendingCallback> = ArrayList()
    val downloadCallbacks: MutableList<OnMessageDownloadCallback> = ArrayList()

    //发送消息监听  不可修改
    val mSendingCallback: OnMessageSendingCallback = object : OnMessageSendingCallback {
        override fun onStart(message: Message) {
            sendStatus = STATUS_SEND
            mSendingCallbacks.forEach {
                LogF.d(TAG, "sendStatus = STATUS_SEND")
                it.onStart(message)
            }
        }

        override fun uploadProgress(pro: Float, isDone: Boolean) {
            sendStatus = STATUS_SEND
            mSendingCallbacks.forEach {
                LogF.d(TAG, "sendStatus = STATUS_SENDING")
                it.uploadProgress(pro, isDone)
            }
        }

        override fun onSuc(message: Message) {
            sendStatus = STATUS_SEND_SUC
            mSendingCallbacks.forEach {
                LogF.d(TAG, "sendStatus = STATUS_SEND_SUC")
                it.onSuc(message)
            }
        }

        override fun onFailed(errMsg: String) {
            sendStatus = STATUS_SEND_FAILED
            LogF.d(TAG, "sendStatus = STATUS_SEND_FAILED")
            mSendingCallbacks.forEach {
                it.onFailed(errMsg)
            }
        }
    }

    //下载消息文件监听  不可修改
    val downloadCallback: OnMessageDownloadCallback = object : OnMessageDownloadCallback {
        override fun onStart() {
            downloadCallbacks.forEach {
                it.onStart()
            }
        }

        override fun downloadProgress(pro: Float) {
            downloadCallbacks.forEach {
                it.downloadProgress(pro)
            }
        }

        override fun onSuc(message: Message) {
            downloadCallbacks.forEach {
                it.onSuc(message)
            }
        }

        override fun onFailed(errMsg: String) {
            downloadCallbacks.forEach {
                it.onFailed(errMsg)
            }
        }
    }

    fun sendMessage() {
        if (null != conversation) {
            conversation!!.sendMessage(this)
        }
    }

    fun delete() {
        if (null != conversation) {
            conversation!!.deleteMsg(this)
        }
    }

    fun sendMessageCallback(callback: OnMessageSendingCallback) {
        mSendingCallbacks.add(callback)
    }

    //是否自己为发送方
    fun isSelf(): Boolean {
        val isSelf = senderId == IMManager.instance.getLoginId()
        LogF.d("消息状态", "isSelf===>$isSelf")
        return isSelf
    }

    //获取消息描述文字
    abstract fun getSummaryText(): String

    /**
     * 显示消息
     *
     * @param viewHolder 界面样式
     * @param context    显示消息的上下文
     */
    abstract fun showMessage(viewHolder: ChatViewHolder, context: Context)

    /**
     * 获取显示气泡
     *
     * @param viewHolder 界面样式
     */
    fun getBubbleView(viewHolder: ChatViewHolder): RelativeLayout {
        viewHolder.timeMessage.visibility = if (hasTime) View.VISIBLE else View.GONE
        viewHolder.timeMessage.text = IMTimeUtil.getTimeStr(time / 1000)
        if (isSelf()) {
            viewHolder.leftPanel.visibility = View.GONE
            viewHolder.leftSender.visibility = View.GONE
            viewHolder.rightPanel.visibility = View.VISIBLE
            viewHolder.rightSender.visibility = View.VISIBLE
            viewHolder.rightMessage.setBackgroundResource(R.drawable.im_chat_item_right_bg)
            viewHolder.ivSender.setOnClickListener(View.OnClickListener {
                //                    LogF.d(TAG, "点击头像" + message.getSender());
            })
            return viewHolder.rightMessage
        } else {
            viewHolder.leftPanel.visibility = View.VISIBLE
            viewHolder.rightPanel.visibility = View.GONE
            viewHolder.leftSender.visibility = View.GONE
            viewHolder.leftMessage.setBackgroundResource(R.drawable.im_chat_item_left_bg)
            viewHolder.ivReceiver.setOnClickListener(View.OnClickListener {
                //                    LogF.d(TAG, "点击头像" + message.getSender());
            })
            return viewHolder.leftMessage
        }
    }

    /**
     * 显示消息发送状态
     *
     * @param viewHolder 界面样式
     */
    fun showStatus(viewHolder: ChatViewHolder, context: Context) {
        when (this.sendStatus) {
            STATUS_SEND -> {
                LogF.d(TAG, "showStatus  STATUS_SEND")
                viewHolder.error.visibility = View.GONE
                viewHolder.sending.visibility = View.VISIBLE
                if (!NetworkUtil.isNetAvailable(context)) {
                    viewHolder.error.visibility = View.VISIBLE
                    viewHolder.sending.visibility = View.GONE
                }
            }
            STATUS_SEND_SUC -> {
                LogF.d(TAG, "showStatus  STATUS_SEND_SUC")
                viewHolder.error.visibility = View.GONE
                viewHolder.sending.visibility = View.GONE
            }
            STATUS_SEND_FAILED -> {
                LogF.d(TAG, "showStatus  STATUS_SEND_FAILED")
                viewHolder.error.visibility = View.VISIBLE
                viewHolder.sending.visibility = View.GONE
                viewHolder.leftPanel.visibility = View.GONE
            }
        }
    }

    fun showDownloadStatus(viewHolder: ChatViewHolder, downloadStatus: Int) {
        val STATUS_LOADING = 0
        val STATUS_SUCCESS = 1
        val STATUS_FAIL = 2
        when (downloadStatus) {
            STATUS_LOADING -> {
                LogF.d(TAG, "showStatus  STATUS_LOADING")
                viewHolder.error.visibility = View.GONE
                viewHolder.sending.visibility = View.VISIBLE
            }
            STATUS_SUCCESS -> {
                LogF.d(TAG, "showStatus  STATUS_SUCCESS")
                viewHolder.error.visibility = View.GONE
                viewHolder.sending.visibility = View.GONE
            }
            STATUS_FAIL -> {
                LogF.d(TAG, "showStatus  STATUS_FAIL")
                viewHolder.error.visibility = View.VISIBLE
                viewHolder.sending.visibility = View.GONE
                viewHolder.leftPanel.visibility = View.GONE
            }
        }
    }

    /**
     * 清除气泡原有数据
     */
    protected fun clearView(viewHolder: ChatViewHolder) {
        if (getBubbleView(viewHolder).childCount != 0) {
            getBubbleView(viewHolder).removeAllViews()
            getBubbleView(viewHolder).setOnClickListener(null)
        }
    }

    /**
     * 是否需要显示时间设置
     *
     * @param message 上一条消息
     */
    fun setHasTime(message: Message?) {
        if (message == null) {
            hasTime = true
            return
        }
        hasTime = System.currentTimeMillis() / 1000 - time > 120
    }

    //获取图片、语音、视频、文件远程源文件
    abstract fun getRemoteUrl(callback: OnMessageDownloadCallback)
}