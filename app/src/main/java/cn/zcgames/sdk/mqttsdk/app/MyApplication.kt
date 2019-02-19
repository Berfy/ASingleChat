package cn.zcgames.sdk.mqttsdk.app

import android.content.Context
import android.support.multidex.MultiDex
import cn.berfy.sdk.http.HttpApi
import cn.berfy.sdk.http.callback.OnStatusListener
import cn.berfy.sdk.http.model.HttpParams
import cn.berfy.sdk.mvpbase.base.BaseApplication
import cn.berfy.sdk.mvpbase.config.CacheConstant
import cn.berfy.sdk.mvpbase.util.CrashException
import cn.berfy.sdk.mvpbase.util.HostSettings
import cn.berfy.sdk.mvpbase.util.LogF
import cn.berfy.sdk.mvpbase.util.ToastUtil
import cn.berfy.service.im.manager.IMManager
import cn.berfy.service.im.manager.i.OnConnectStatusCallback
import cn.zcgames.sdk.mqttsdk.home.view.activity.MainActivity
import cn.zcgames.sdk.mqttsdk.personal.view.activity.LoginActivity
import com.raizlabs.android.dbflow.config.FlowConfig
import com.raizlabs.android.dbflow.config.FlowManager

/**
author: Berfy
date: 2018/12/21
程序入口
 */
class MyApplication : BaseApplication(), OnConnectStatusCallback {

    private val TAG = "MyApplication"
    val TEST_DEMO = false

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    override fun onCreate() {
        super.onCreate()
        HttpApi.init(applicationContext)
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

                override fun addParamsOrHeaders(rawParams: HttpParams): HttpParams? {
                    val params = HttpParams()
                    if (null != getCurrLoginUser()) {
                        params.headers.put("Uid", getCurrLoginUser().player.id)
                    }
                    //rawParams 一系列算法加密
                    params.headers.put("Sign", "123456")
                    return params
                }
            })
            .setTimeOut(8, 8, 20)
            .startConnection()
        FlowManager.init(FlowConfig.Builder(applicationContext).build())
        CacheConstant.setRootDir("ZX_IMSDK")
        ToastUtil.init(applicationContext)
        initIM()
        //异常捕获
        CrashException.getInstance().init(getContext(), LoginActivity::class.java)
    }

    private fun initIM() {
        LogF.d(TAG, "初始化IMSDK")
        val config = IMManager.Config()
//            .url("tcp://192.168.2.203:8908")
            .debug(TEST_DEMO)
            .url(HostSettings.getMQTTHost())
//                    .wsUrl("tcp://223.203.221.79:1883")
//                    .autoLogin("root", "zcxy1234")
            .needReconnect(true)
//                    .topic("notify/lottery/new_fast_three")
            .topic("/1234567890/22/chat/001")
            .callback(this)
        val user = getCurrLoginUser()
        if (null != user) {
            config.autoLogin(user.player.id, "")
        }
        IMManager.init(applicationContext, MainActivity::class.java)
            .config(config)
    }

    //IM连接开始
    override fun connectStart() {
    }

    //IM已连接
    override fun connectSuc() {
    }

    //IM连接失败
    override fun connectFailed(exception: Throwable?) {
    }

    //IM连接断开
    override fun disConnect(exception: Throwable?) {
    }
}