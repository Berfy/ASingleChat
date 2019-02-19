package cn.zcgames.sdk.mqttsdk.message.activity

import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import butterknife.OnClick
import cn.berfy.sdk.mvpbase.base.CommonActivity
import cn.berfy.sdk.mvpbase.iview.IBaseView
import cn.berfy.sdk.mvpbase.prensenter.BasePresenter
import cn.berfy.sdk.mvpbase.util.GsonUtil
import cn.berfy.sdk.mvpbase.util.LogF
import cn.berfy.sdk.mvpbase.util.MD5
import cn.berfy.sdk.mvpbase.util.ToastUtil
import cn.berfy.service.im.cache.db.tab.MessageTab
import cn.berfy.service.im.manager.IMManager
import cn.berfy.service.im.manager.i.OnConnectStatusCallback
import cn.berfy.service.im.manager.i.OnMessageListener
import cn.berfy.service.im.manager.i.OnMessageSendingCallback
import cn.berfy.service.im.model.*
import cn.berfy.service.im.model.conversation.Conversation
import cn.zcgames.sdk.im.R
import cn.zcgames.sdk.mqttsdk.message.adapter.DemoIMConnectAdapter
import com.raizlabs.android.dbflow.sql.language.Delete
import com.raizlabs.android.dbflow.sql.language.SQLite
import kotlinx.android.synthetic.main.activity_demo_im_connect.*

/**
 * IM聊天演示
 * @author Berfy
 * @date 2019.1.1
 * */
class DemoIMConnectActivity : CommonActivity<IBaseView, BasePresenter<IBaseView>>(), IBaseView, OnConnectStatusCallback,
    OnMessageListener {

    private lateinit var mImManager: IMManager
    private var mUrl: String = "tcp://192.168.2.203:8908"
    //    private var mUrl: String = "tcp://223.203.221.89:9008"
    //        private var mUrl: String = "tcp://223.203.221.79:1883"
    private var mUserName: String = ""
    private var mPwd: String = ""
    private var mToClientId: String? = null
    private lateinit var mAdapter: DemoIMConnectAdapter
    private var mLastMsgId: String = "0"

    override fun getContentViewId(): Int {
        return R.layout.activity_demo_im_connect
    }

    override fun initData(savedInstanceState: Bundle?) {
        Delete.table(MessageTab::class.java)
////        //测试新消息来了数据库写入和会话添加
//        val messageTab = MessageTab()
//        messageTab.type = MessageType.TYPE_P2P
//        messageTab.topic= "/1234567890/22/chat/001"
//        messageTab.chat_type = MessageContentType.TYPE_TEXT
//        messageTab.content = "普通消息----${System.currentTimeMillis()}"
//        messageTab.sender_id = "123"
//        messageTab.sender_name = "Berfy"
//        messageTab.receiver_id = "456"
//        LogF.d(TAG, "插入新消息  " + GsonUtil.getInstance().toJson(messageTab))
//        messageTab.save()
        val msgs = SQLite.select()
            .from(MessageTab::class.java)
            .queryList()
        LogF.d(TAG, "消息表  " + GsonUtil.getInstance().toJson(msgs))

//        val conversationTabCache = SQLite.select()
//            .from(ConversationTab::class.java)
//            .where(ConversationTab_Table.peer.`is`(messageTab.sender_id))
//            .querySingle()
//        if (null == conversationTabCache) {
//            val conversationTab = ConversationTab()
//            conversationTab.peer = messageTab.sender_id
//            conversationTab.type = messageTab.type
//            conversationTab.save()
//            LogF.d(TAG, "插入新会话  " + GsonUtil.getInstance().toJson(conversationTab))
//        } else {
//            LogF.d(TAG, "会话已存在不插入" + GsonUtil.getInstance().toJson(conversationTabCache))
//        }
//        val conversationTabCaches = SQLite.select()
//            .from(ConversationTab::class.java)
//            .queryList()
//        LogF.d(TAG, "所有会话  " + GsonUtil.getInstance().toJson(conversationTabCaches))
        mImManager = IMManager.instance
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        IMManager.instance.clearNotify("chat")
    }

    override fun initView() {
        showTitleBar()
        titleBar.setLeftIcon(true, View.OnClickListener { close() })
        titleBar.setTitle("IM演示")
        edit_ip.text.clear()
        edit_ip.text.insert(0, mUrl)
        edit_ip.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val text = if (null == s) "" else s.toString()
                mUrl = text
            }
        })
        edit_username.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val text = if (null == s) "" else s.toString()
                mUserName = text
            }
        })
        edit_pwd.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val text = if (null == s) "" else s.toString()
                mPwd = text
            }
        })
        edit_to_id.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val text = if (null == s) "" else s.toString()
                mToClientId = text
            }
        })
        swt_connect.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                when (swt_connect.isChecked) {
                    true -> {
                        if (mImManager.currentStatus != IMConnectionStatus.CONNECTTING && !mImManager.isConnected) {//没有连接中或者已连接
                            if (TextUtils.isEmpty(mUrl)) {
                                ToastUtil.getInstances().showShort("未连接服务器")
                                swt_connect.isChecked = false
                                return
                            }
                            val userName = edit_username.text.toString().trim()
                            val pwd = edit_pwd.text.toString().trim()
                            if (!TextUtils.isEmpty(userName))
                                mImManager.setUserName(userName)
                            if (!TextUtils.isEmpty(pwd))
                                mImManager.setPwd(pwd)
                            mImManager.startConnect()
                        }
                    }
                    false -> {
                        mImManager.stopConnect()
                    }
                }

            }
        })
        rv_content.layoutManager = LinearLayoutManager(mContext)
        mAdapter = DemoIMConnectAdapter(mContext)
        rv_content.adapter = mAdapter
        mImManager.addConnectListener(this)
        mImManager.addMessageListener(this)
        checkStatus()
    }

    private fun checkStatus() {
        if (mImManager.isConnectting) {
            tv_connect_status.text = "未连接"
            tv_connect_info.text = "正在连接"
            swt_connect.isChecked = false
        } else if (mImManager.isConnected) {
            tv_connect_status.text = "已连接"
            tv_connect_info.text = "连接成功"
            swt_connect.isChecked = true
        } else {
            tv_connect_status.text = "未连接"
            tv_connect_info.text = ""
            swt_connect.isChecked = false
        }
    }

    override fun initPresenter(): BasePresenter<IBaseView>? {
        return null
    }

    override fun hiddenLoadingView(msg: String?) {
    }

    override fun showLoadingView(msg: String?) {
    }

    @OnClick(
        R.id.btn_sub,
        R.id.btn_unsub,
        R.id.btn_send,
        R.id.btn_login,
        R.id.btn_join_room,
        R.id.btn_leave_room,
        R.id.btn_pull,
        R.id.btn_online,
        R.id.btn_room_member
    )
    fun onOnclick(v: View) {
        when (v) {
            btn_sub -> {
                val topic = edit_topic.text.trim().toString()
                if (TextUtils.isEmpty(topic)) {
                    ToastUtil.getInstances().showShort("未填写话题")
                    return
                }
                mImManager.subscribeToTopic(topic)
            }
            btn_unsub -> {
                val topic = edit_topic.text.trim().toString()
                if (TextUtils.isEmpty(topic)) {
                    ToastUtil.getInstances().showShort("未填写话题")
                    return
                }
                mImManager.unSubscribeTopic(topic)
            }
            btn_send -> {
                val msg = edit_send_msg.text.trim().toString()
//                if (!mImManager.isConnected) {
//                    mImManager.startConnect()
//                    ToastUtil.getInstances().showShort("未连接服务器")
//                    return
//                }
                if (TextUtils.isEmpty(msg)) {
                    ToastUtil.getInstances().showShort("未填写消息")
                    return
                }
//                if (TextUtils.isEmpty(mToClientId)) {
//                    ToastUtil.getInstances().showShort("未填写话题")
//                    return
//                }
//                val message = MessageText()
//                message.id = msg
//                message.content = msg
//                message.receiverId = mToClientId

//                val message = MessageImage()
//                message.id = msg
//                message.imageUrl = "http://www.baidu.com"
//                message.name = "afafaf"
//                message.md5 = MD5.getStringMD5(message.imageUrl)
//                message.w = 1280
//                message.h = 720
//                message.ext = ".jpg"
//                message.fileLength = 123456

                val message = MessageVoice()
                message.id = msg
                message.voiceUrl = "http://www.baidu.com"
                message.duration = 22222
                message.md5 = MD5.getStringMD5(message.voiceUrl)
                message.ext = ".jpg"
                message.fileLength = 123456

                val conversation = Conversation(
                    if (null == mToClientId) "" else mToClientId!!,
                    MessageType.TYPE_P2P
                )
                conversation.sendMessage(message, object : OnMessageSendingCallback {
                    override fun onStart(message: Message) {
                        LogF.d(TAG, "消息 开始发送")
                    }

                    override fun uploadProgress(pro: Float, isDone: Boolean) {
                        LogF.d(TAG, "消息 进度=$pro,是否完成 : $isDone")
                    }

                    override fun onSuc(message: Message) {
                        LogF.d(TAG, "消息 发送成功 ${GsonUtil.getInstance().toJson(message)}")
                    }

                    override fun onFailed(errMsg: String) {
                        LogF.d(TAG, "消息 发送失败")
                    }
                })
            }

            btn_login -> {
                if (TextUtils.isEmpty(mUserName)) {
                    ToastUtil.getInstances().showShort("未填写昵称")
                    return
                }
//                if (TextUtils.isEmpty(mPwd)) {
//                    ToastUtil.getInstances().showShort("未填写密码")
//                    return
//                }
            }
            btn_join_room -> {
                val topic = edit_room.text.toString().trim()
                if (TextUtils.isEmpty(topic)) {
                    ToastUtil.getInstances().showShort("请填写聊天室话题")
                    return
                }
                mImManager.sendJoinChatRoom(topic)
            }
            btn_leave_room -> {
                val topic = edit_room.text.toString().trim()
                if (TextUtils.isEmpty(topic)) {
                    ToastUtil.getInstances().showShort("请填写聊天室话题")
                    return
                }
                mImManager.sendLeaveChatRoom(topic)
            }
            btn_pull -> {
                val topic = edit_room.text.toString().trim()
                if (TextUtils.isEmpty(topic)) {
                    ToastUtil.getInstances().showShort("请填写聊天室话题")
                    return
                }
                mImManager.getMessageFromTopic(topic, mLastMsgId, 20)
            }
            btn_online -> {
                val topic = edit_room.text.toString().trim()
                if (TextUtils.isEmpty(topic)) {
                    ToastUtil.getInstances().showShort("请填写聊天室话题")
                    return
                }
                mImManager.sendAllOnlineUsers(topic)
            }
            btn_room_member -> {
                val topic = edit_room.text.toString().trim()
                if (TextUtils.isEmpty(topic)) {
                    ToastUtil.getInstances().showShort("请填写聊天室话题")
                    return
                }
                mImManager.sendRoomMembers(topic)
            }
            else -> {
                LogF.d(TAG, "按键未被监听")
            }

        }
    }

    override fun connectStart() {
        tv_connect_info.text = "正在连接"
        swt_connect.isChecked = false
    }

    override fun connectSuc() {
        tv_connect_status.text = "已连接"
        tv_connect_info.text = "连接成功"
        swt_connect.isChecked = true
    }

    override fun connectFailed(exception: Throwable?) {
        tv_connect_status.text = "连接失败"
        swt_connect.isChecked = false
        if (null == exception) {
            tv_connect_info.text = "连接失败"
            return
        }
        tv_connect_info.text = exception.message
    }

    override fun disConnect(exception: Throwable?) {
        tv_connect_status.text = "断开连接"
        swt_connect.isChecked = false
        if (null == exception) {
            tv_connect_info.text = "断开连接"
            return
        }
        tv_connect_info.text = exception.message
    }

    //P2P消息
    override fun newMessage(message: Message?) {
        LogF.d(TAG, "收到新消息  newMessage   " + GsonUtil.getInstance().toJson(message))
        if (null == message) {
            return
        }
        if (null != message.conversation && message.conversation!!.type == MessageType.TYPE_P2P && message.senderId
            == IMManager.instance.getLoginId()) {
            LogF.d(TAG, "单聊 自己的消息 作废")
            return
        }
        mAdapter.getData().add(message)
        mAdapter.notifyDataSetChanged()
        rv_content.scrollToPosition(mAdapter.itemCount - 1)
    }

    //系统推送
    override fun systemMessage(message: MessageCustom?) {
        if (null == message) {
            return
        }
        if (null != message.conversation && message.conversation!!.type == MessageType.TYPE_P2P && message.senderId
            == IMManager.instance.getLoginId()) {
            LogF.d(TAG, "单聊 自己的消息 作废")
            return
        }
        LogF.d(TAG, "收到系统消息  newMessage   " + GsonUtil.getInstance().toJson(message))
    }

    override fun refreshConversation() {
    }

    override fun sendMessageStatus(message: String?, isSuc: Boolean) {
        tv_connect_info.text = if (isSuc) "消息发送成功" else "消息发送失败"
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            close()
            return false
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun close() {
        mImManager.removeConnectListener(this)
        mImManager.removeMessageListener(this)
        finish()
    }
}
