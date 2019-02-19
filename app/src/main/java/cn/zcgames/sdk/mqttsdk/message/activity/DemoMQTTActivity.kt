package cn.zcgames.sdk.mqttsdk.message.activity

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.CompoundButton
import android.widget.EditText
import butterknife.BindView
import butterknife.OnClick
import cn.berfy.sdk.mvpbase.base.CommonActivity
import cn.berfy.sdk.mvpbase.iview.IBaseView
import cn.berfy.sdk.mvpbase.prensenter.BasePresenter
import cn.berfy.sdk.mvpbase.util.DeviceUtils
import cn.berfy.sdk.mvpbase.util.LogF
import cn.berfy.sdk.mvpbase.util.ToastUtil
import cn.berfy.service.im.model.MessageText
import cn.berfy.service.mqtt.manager.MQTTManager
import cn.zcgames.sdk.im.R
import cn.zcgames.sdk.mqttsdk.message.adapter.DemoIMConnectAdapter
import kotlinx.android.synthetic.main.activity_demo_mqtt.*

class DemoMQTTActivity : CommonActivity<IBaseView, BasePresenter<IBaseView>>(), IBaseView, MQTTManager.OnMQTTCallback {

    private lateinit var mMqttManager: MQTTManager
    @BindView(R.id.edit_topic)
    lateinit var mEditTopic: EditText
    @BindView(R.id.btn_sub)
    lateinit var mBtnSubTopic: Button
    @BindView(R.id.edit_send_msg)
    lateinit var mEditSendMsg: EditText
    @BindView(R.id.btn_send)
    lateinit var mBtnSend: Button
    @BindView(R.id.rv_content)
    lateinit var mRvContent: RecyclerView

    var mTopic: String? = null
    lateinit var mAdapter: DemoIMConnectAdapter

    override fun getContentViewId(): Int {
        return R.layout.activity_demo_mqtt
    }

    override fun initData(savedInstanceState: Bundle?) {
        MQTTManager.init(applicationContext)
        mMqttManager = MQTTManager.Builder()
//            .host("tcp://192.168.2.203:8908")
            .host("tcp://223.203.221.79:1883")
            .clientId(DeviceUtils.getDeviceId(mContext))
//            .userName("berfy")
            .userName("root")
            .passWord("zcxy1234")
//            .topic("/1234567890/22/chat/001")
            .topic("notify/lottery/new_fast_three")
            .callBack(this)
            .build()
    }

    override fun initView() {
        showTitleBar()
        titleBar.setLeftIcon(true, View.OnClickListener { finish() })
        titleBar.setTitle("MQTT演示")
        edit_topic.text.insert(0, "notify/lottery/new_fast_three")
        swt_connect.setOnCheckedChangeListener(object : CompoundButton.OnCheckedChangeListener {
            override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
                if (isChecked) {
                    if (!mMqttManager.isConnectting && !mMqttManager.isConnected) {
                        mMqttManager.connect(true)
                    }
                } else {
                    mMqttManager.disconnect()
                }
            }
        })
        mMqttManager.connect(true)
        rv_content.layoutManager = LinearLayoutManager(mContext)
        mAdapter = DemoIMConnectAdapter(mContext)
        rv_content.adapter = mAdapter
    }

    override fun initPresenter(): BasePresenter<IBaseView>? {
        return null
    }

    override fun hiddenLoadingView(msg: String?) {
    }

    override fun showLoadingView(msg: String?) {
    }

    @OnClick(R.id.btn_sub, R.id.btn_send)
    fun onOnclick(v: View) {
        when (v) {
            btn_sub -> {
                var topic = edit_topic.text.trim().toString()
                if (TextUtils.isEmpty(topic)) {
                    ToastUtil.getInstances().showShort("未填写话题")
                    return
                }
                mMqttManager.subscribeToTopic(topic)
            }
            btn_send -> {
                val msg = edit_send_msg.text.trim().toString()
                if (TextUtils.isEmpty(mTopic)) {
                    ToastUtil.getInstances().showShort("未订阅话题")
                    return
                }
                if (TextUtils.isEmpty(msg)) {
                    ToastUtil.getInstances().showShort("未填写消息")
                    return
                }
                mMqttManager.publish(mTopic!!, msg)
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

    override fun connectComplete(reconnect: Boolean, serverURI: String?) {
    }

    override fun connectionLost(exception: Throwable?) {
        tv_connect_status.text = "断开连接"
        swt_connect.isChecked = false
        if (null == exception) {
            tv_connect_info.text = "断开连接"
            return
        }
        tv_connect_info.text = exception.message
    }

    override fun newMessage(topic: String?, message: String?) {
        LogF.d(TAG, "收到新消息  newMessage  ===  topic=$topic        $message")
        if (!TextUtils.isEmpty(message)) {
            val msg = MessageText()
            msg.content = topic + ": " + message!!
            mAdapter.getData().add(msg)
            mAdapter.notifyDataSetChanged()
            rv_content.scrollToPosition(mAdapter.itemCount - 1)
        }
    }

    override fun sendSuc(message: String?) {

    }

    override fun subscribeUpdate(topic: String?, isSuc: Boolean, errmsg: String?) {
        if (isSuc) {
            mTopic = topic
            val msg = MessageText()
            msg.content = "订阅成功 topic=$topic"
            mAdapter.getData().add(msg)
        } else {
            val msg = MessageText()
            msg.content = "订阅失败 topic=$topic"
            mAdapter.getData().add(msg)
        }
        mAdapter.notifyDataSetChanged()
        rv_content.scrollToPosition(mAdapter.itemCount - 1)
    }

    override fun unSubscribeUpdate(topic: String?, isSuc: Boolean, errmsg: String?) {
        if (isSuc) {
            val msg = MessageText()
            msg.content = "取消订阅成功 topic=$topic"
            mAdapter.getData().add(msg)
        } else {
            val msg = MessageText()
            msg.content = "取消订阅失败 topic=$topic"
            mAdapter.getData().add(msg)
        }
        mAdapter.notifyDataSetChanged()
        rv_content.scrollToPosition(mAdapter.itemCount - 1)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mMqttManager.isConnected) {
            mMqttManager.disconnect();
        }
    }
}
