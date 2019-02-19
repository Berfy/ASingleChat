package cn.zcgames.sdk.mqttsdk.message.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import butterknife.OnClick
import cn.berfy.sdk.mvpbase.base.CommonActivity
import cn.berfy.sdk.mvpbase.iview.IBaseView
import cn.berfy.sdk.mvpbase.prensenter.BasePresenter
import cn.berfy.sdk.mvpbase.util.LogF
import cn.berfy.service.im.manager.IMManager
import cn.berfy.service.im.manager.i.OnConnectStatusCallback
import cn.zcgames.sdk.im.R
import kotlinx.android.synthetic.main.activity_im_test.*

//IM测试类
class IMTestActivity : CommonActivity<IBaseView, BasePresenter<IBaseView>>(), IBaseView, OnConnectStatusCallback {


    override fun getContentViewId(): Int {
        return R.layout.activity_im_test
    }

    override fun initData(savedInstanceState: Bundle?) {
    }

    override fun onResume() {
        super.onResume()
        //模拟登录
        LogF.d(
            TAG, "IM连接状态 isConnectting =${IMManager.instance.isConnectting}" +
                    " isConnected=${IMManager.instance.isConnected}" +
                    " isManualClose=${IMManager.instance.isManualClose}"
        )
        //登录
        if (!IMManager.instance.isConnectting && !IMManager.instance.isConnected) {
            IMManager.instance.setUserInfo("berfy", "")
            IMManager.instance.startConnect()
        }
    }

    override fun initView() {
        showTitleBar()
        titleBar.setLeftIcon(false)
        titleBar.setTitle("IM测试")
    }

    override fun initPresenter(): BasePresenter<IBaseView>? {
        return null
    }

    override fun hiddenLoadingView(msg: String?) {
    }

    override fun showLoadingView(msg: String?) {
    }

    @OnClick(R.id.btn_deme1, R.id.btn_start_im_ui, R.id.btn_db)
    fun onClick(v: View) {
        when (v) {
            btn_deme1 -> {
                startActivity(Intent(mContext, DemoIMConnectActivity::class.java))
            }

            btn_start_im_ui -> {
                startActivity(Intent(mContext, MessageActivity::class.java))
            }

            btn_db -> {
                startActivity(Intent(mContext, DemoIMDBActivity::class.java))
            }
        }
    }

    override fun connectStart() {
    }

    override fun connectSuc() {
    }

    override fun connectFailed(exception: Throwable?) {
    }

    override fun disConnect(exception: Throwable?) {
    }
}
