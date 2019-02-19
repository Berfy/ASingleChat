package cn.berfy.service.im.http

import android.content.Context
import cn.berfy.sdk.http.HttpApi
import cn.berfy.sdk.http.callback.RequestCallBack
import cn.berfy.sdk.http.callback.SuperOkHttpCallBack
import cn.berfy.sdk.http.model.HttpParams
import cn.berfy.sdk.http.model.NetError
import cn.berfy.sdk.http.model.NetResponse
import cn.berfy.sdk.mvpbase.model.User
import cn.berfy.sdk.mvpbase.util.GsonUtil
import cn.berfy.sdk.mvpbase.util.SharedPreferenceUtil
import cn.berfy.service.im.cache.CacheConstants
import cn.berfy.service.im.manager.IMManager

/**
author: Berfy
date: 2019/2/14
鉴权登录相关api
 */
class AuthApi private constructor() {

    private var TAG = "IM_鉴权登录相关api"

    private fun getContext(): Context? {
        if (IMManager.isInstanced()) {
            return IMManager.instance.getContext()!!
        }
        return null
    }

    init {

    }

    /**
     * @param merchId 渠道id
     * @param externalId 外部id
     * */
    fun login(merchId: String, externalId: String, callBack: RequestCallBack<User>) {
        val params = HttpParams()
        params.putParam("merch_id", merchId)
        params.putParam("external_id", externalId)
        IMManager.instance.getHttpApi()!!
            .post("app/v1/user/generate", params, object : SuperOkHttpCallBack<User>(object : RequestCallBack<User> {
                override fun onStart() {
                    callBack.onStart()
                }

                override fun onFinish(response: NetResponse<User>) {
                    if (response.isOk) {
                        SharedPreferenceUtil.putPublic(
                            getContext(),
                            CacheConstants.CHAT_SERVER_TIME_LIMIT,
                            (response.netMessage.ts - System.currentTimeMillis())
                        )
                    }
                    callBack.onFinish(response)
                }

                override fun onError(error: NetError?) {
                    callBack.onError(error)
                }
            }) {})
    }

    companion object {

        @Volatile
        private var mInstance: AuthApi? = null

        val instance: AuthApi
            @Synchronized
            get() {
                if (mInstance == null) {
                    synchronized(AuthApi::class) {
                        if (mInstance == null) {
                            mInstance = AuthApi()
                        }
                    }
                }
                return mInstance!!
            }

    }
}