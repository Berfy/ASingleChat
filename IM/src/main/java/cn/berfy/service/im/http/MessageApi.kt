package cn.berfy.service.im.http

import android.content.Context
import cn.berfy.sdk.http.callback.RequestCallBack
import cn.berfy.sdk.http.callback.SuperOkHttpCallBack
import cn.berfy.sdk.http.model.HttpParams
import cn.berfy.sdk.http.model.NetError
import cn.berfy.sdk.http.model.NetResponse
import cn.berfy.sdk.mvpbase.util.DeviceUtils
import cn.berfy.sdk.mvpbase.util.GsonUtil
import cn.berfy.sdk.mvpbase.util.LogF
import cn.berfy.sdk.mvpbase.util.SharedPreferenceUtil
import cn.berfy.service.im.cache.CacheConstants
import cn.berfy.service.im.manager.IMManager
import cn.berfy.service.im.model.Message
import cn.berfy.service.im.model.response.ServerMessageResponseData
import cn.berfy.service.im.util.MessageTextUtil
import java.util.*

/**
author: Berfy
date: 2019/2/14
鉴权登录相关api
 */
class MessageApi private constructor() {

    private var TAG = "IM_鉴权登录相关api"

    private fun getContext(): Context? {
        if (IMManager.isInstanced() && IMManager.instance.isConnected) {
            return IMManager.instance.getContext()!!
        }
        return null
    }

    init {

    }

    /**
     * 获取历史消息
     * @param merchId 渠道id
     * @param externalId 外部id
     * */
    fun getMessage(loginId: String,toId: String, msgId: String, count: Int, callBack: RequestCallBack<ArrayList<Message>>) {
        if (null == getContext() || null == IMManager.instance.getHttpApi()) {
            LogF.d(TAG, "IM服务器未连接")
            return
        }
        val params = HttpParams()
        val appId = DeviceUtils.getMetaDataFromApp(getContext(), "IM_APPID")
        params.putParam("appid", appId)
        params.putParam("owner_id", loginId)
        params.putParam("target_id", toId)
        params.putParam("msgid", msgId)
        params.putParam("count", "$count")
        IMManager.instance.getHttpApi()!!
            .post(
                "app/v1/message/p2p/pull",
                params,
                object :
                    SuperOkHttpCallBack<ServerMessageResponseData>(object : RequestCallBack<ServerMessageResponseData> {
                        override fun onStart() {
                            callBack.onStart()
                        }

                        override fun onFinish(response: NetResponse<ServerMessageResponseData>) {
                            val responseNew: NetResponse<ArrayList<Message>> = NetResponse()
                            responseNew.netMessage = response.netMessage
                            responseNew.statusCode = response.statusCode
                            responseNew.usedTime = response.usedTime
                            if (response.isOk) {
                                SharedPreferenceUtil.putPublic(
                                    getContext(),
                                    CacheConstants.CHAT_SERVER_TIME_LIMIT,
                                    (response.netMessage.ts - System.currentTimeMillis())
                                )
                                if (null != response.data && null != response.data.msgs) {
                                    val msgs = ArrayList<Message>()
                                    LogF.d(TAG, "排序前原始数据" + GsonUtil.getInstance().toJson(response.data.msgs!!))
                                    for (sMsg in response.data.msgs!!) {//区分消息类型
                                        val msg = MessageTextUtil.historyPayloadToMsg(sMsg)
                                        if (null != msg)
                                            msgs.add(msg)
                                    }
                                    //排序
                                    LogF.d(TAG, "排序前" + GsonUtil.getInstance().toJson(msgs))
                                    Collections.sort(msgs, MessageComparator())
                                    LogF.d(TAG, "排序后" + GsonUtil.getInstance().toJson(msgs))
                                    responseNew.data = msgs
                                } else {
                                    responseNew.data = null
                                }
                            } else {
                                responseNew.data = null
                            }
                            callBack.onFinish(responseNew)
                        }

                        override fun onError(error: NetError?) {
                            callBack.onError(error)
                        }
                    }) {})
    }

    companion object {

        @Volatile
        private var mInstance: MessageApi? = null

        val instance: MessageApi
            @Synchronized
            get() {
                if (mInstance == null) {
                    synchronized(MessageApi::class) {
                        if (mInstance == null) {
                            mInstance = MessageApi()
                        }
                    }
                }
                return mInstance!!
            }

    }

    class MessageComparator : Comparator<Message> {
        override fun compare(o1: Message, o2: Message): Int {
            if (o1.time > o2.time) {
                return 1
            } else if (o1.time < o2.time) {
                return -1
            } else {
                return 0
            }
        }
    }

}