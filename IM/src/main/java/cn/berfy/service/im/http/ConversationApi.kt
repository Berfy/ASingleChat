package cn.berfy.service.im.http

import android.content.Context
import cn.berfy.sdk.http.callback.RequestCallBack
import cn.berfy.sdk.http.callback.SuperOkHttpCallBack
import cn.berfy.sdk.http.model.HttpParams
import cn.berfy.sdk.http.model.NetError
import cn.berfy.sdk.http.model.NetResponse
import cn.berfy.sdk.mvpbase.util.LogF
import cn.berfy.sdk.mvpbase.util.SharedPreferenceUtil
import cn.berfy.service.im.cache.CacheConstants
import cn.berfy.service.im.manager.IMManager
import cn.berfy.service.im.model.response.ConversationResponseData
import cn.berfy.service.im.model.response.GroupListResponseData

/**
author: Berfy
date: 2019/1/25
会话相关api
 */
class ConversationApi {

    private var TAG = "IM_会话相关api"

    private fun getContext(): Context? {
        if (IMManager.isInstanced() && IMManager.instance.isConnected) {
            return IMManager.instance.getContext()!!
        }
        return null
    }

    fun getConversation(callBack: RequestCallBack<ConversationResponseData>) {
        if (null == getContext() || null == IMManager.instance.getHttpApi()) {
            LogF.d(TAG, "IM服务器未连接")
            return
        }
        //开始请求
        IMManager.instance.getHttpApi()!!.get(
            "/app/v1/chat/sessions",
            HttpParams(), object : SuperOkHttpCallBack<ConversationResponseData>(object :RequestCallBack<ConversationResponseData>{
                override fun onStart() {
                    callBack.onStart()
                }

                override fun onFinish(response: NetResponse<ConversationResponseData>) {
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
        private var mInstance: ConversationApi? = null

        val instance: ConversationApi
            @Synchronized
            get() {
                if (mInstance == null) {
                    synchronized(ConversationApi::class.java) {
                        if (mInstance == null) {
                            mInstance = ConversationApi()
                        }
                    }
                }
                return mInstance!!
            }
    }
}