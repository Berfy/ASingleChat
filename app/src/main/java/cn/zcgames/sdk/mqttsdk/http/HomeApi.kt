package cn.zcgames.sdk.mqttsdk.http

import android.content.Context
import cn.berfy.sdk.http.HttpApi
import cn.berfy.sdk.http.callback.RequestCallBack
import cn.berfy.sdk.http.callback.SuperOkHttpCallBack
import cn.berfy.sdk.mvpbase.base.BaseApplication
import cn.zcgames.sdk.mqttsdk.app.MyApplication
import cn.zcgames.sdk.mqttsdk.home.model.HomeBean

/**
 * @author Berfy
 * @since 2019.1.30
 * 首页接口
 */
class HomeApi private constructor(private val mContext: Context) {

    //获取首页
    fun getAppHomeInfo(version: Int, callBack: RequestCallBack<HomeBean>) {
        val user = BaseApplication.getCurrLoginUser()
        if (null != user)
            HttpApi.getInstances().get("app/v1/sys/config?chid=${user.player.merchid}&version=$version", null,
                object : SuperOkHttpCallBack<HomeBean>(callBack) {})
    }

    companion object {
        private var messageApi: HomeApi? = null

        val instance: HomeApi
            get() {
                if (null == messageApi) {
                    messageApi = HomeApi(BaseApplication.getContext())
                }
                return messageApi!!
            }
    }
}
