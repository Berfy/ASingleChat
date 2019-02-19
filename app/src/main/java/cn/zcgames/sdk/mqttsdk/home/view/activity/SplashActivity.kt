package cn.zcgames.sdk.mqttsdk.home.view.activity

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import cn.berfy.sdk.mvpbase.base.BaseApplication
import cn.berfy.sdk.mvpbase.base.CommonActivity
import cn.berfy.sdk.mvpbase.iview.IBaseView
import cn.berfy.sdk.mvpbase.prensenter.BasePresenter
import cn.zcgames.sdk.im.R
import cn.zcgames.sdk.mqttsdk.personal.view.activity.LoginActivity

/**
author: Berfy
date: 2019/2/1
开屏页
 */
class SplashActivity : CommonActivity<IBaseView, BasePresenter<IBaseView>>(), IBaseView {

    override fun getContentViewId(): Int {
        return R.layout.activity_spash
    }

    override fun initData(savedInstanceState: Bundle?) {

    }

    override fun initView() {
        BaseApplication.getMainThreadHandler().postDelayed({
            if (null != BaseApplication.getCurrLoginUser()) {
                startActivity(Intent(mContext, MainActivity::class.java))
                finish()
            } else {
                startActivity(Intent(mContext, LoginActivity::class.java))
                finish()
            }
        }, 2000)
    }

    override fun initPresenter(): BasePresenter<IBaseView>? {
        return null
    }

    override fun hiddenLoadingView(msg: String?) {

    }

    override fun showLoadingView(msg: String?) {

    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK)
            return false
        return super.onKeyDown(keyCode, event)
    }
}