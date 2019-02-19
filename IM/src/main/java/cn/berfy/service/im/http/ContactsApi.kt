package cn.berfy.service.im.http

import android.content.Context
import cn.berfy.sdk.http.callback.RequestCallBack
import cn.berfy.sdk.http.callback.SuperOkHttpCallBack
import cn.berfy.sdk.http.model.HttpParams
import cn.berfy.sdk.http.model.NetError
import cn.berfy.sdk.http.model.NetResponse
import cn.berfy.sdk.mvpbase.base.BaseApplication
import cn.berfy.sdk.mvpbase.util.LogF
import cn.berfy.sdk.mvpbase.util.SharedPreferenceUtil
import cn.berfy.service.im.cache.CacheConstants
import cn.berfy.service.im.manager.CacheManager
import cn.berfy.service.im.manager.IMManager
import cn.berfy.service.im.model.contact.UserInfo
import cn.berfy.service.im.model.response.GroupListResponseData
import java.lang.NullPointerException

/**
author: Berfy
date: 2019/1/25
联系人相关api（好友、群组等）
 */
class ContactsApi {

    private var TAG = "IM_联系人相关api"

    private fun getContext(): Context? {
        if (IMManager.isInstanced() && IMManager.instance.isConnected) {
            return IMManager.instance.getContext()!!
        }
        return null
    }

    fun getUserInfo(uid: String, callBack: RequestCallBack<UserInfo>) {
        if (null == getContext() || null == IMManager.instance.getHttpApi()) {
            LogF.d(TAG, "IM服务器未连接")
            callBack.onError(NetError(300, 0, "IM服务器未连接"))
            return
        }

        if (null == BaseApplication.getCurrLoginUser()) {
            LogF.d(TAG, "没有登录信息")
            callBack.onError(NetError(300, 0, "没有登录信息"))
            return
        }
        //开始请求
        val params = HttpParams()
        params.putParam("uid", uid)
        IMManager.instance.getHttpApi()!!
            .get<UserInfo>(
                "app/v1/user/members/info",
                params,
                object : SuperOkHttpCallBack<UserInfo>(object :RequestCallBack<UserInfo>{
                    override fun onStart() {
                        callBack.onStart()
                    }

                    override fun onFinish(response: NetResponse<UserInfo>) {
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

    fun getGroupList(callBack: RequestCallBack<GroupListResponseData>) {
        if (null == getContext() || null == IMManager.instance.getHttpApi()) {
            LogF.d(TAG, "IM服务器未连接")
            callBack.onError(NetError(300, 0, "IM服务器未连接"))
            return
        }
        if (null == BaseApplication.getCurrLoginUser()) {
            LogF.d(TAG, "没有登录信息")
            callBack.onError(NetError(300, 0, "没有登录信息"))
            return
        }
        //开始请求
        val params = HttpParams()
        params.putParam("merch_id", BaseApplication.getCurrLoginUser().player.merchid)
        params.putParam("admin_id", BaseApplication.getCurrLoginUser().player.id)
//        params.putParam("player_id", "")
        IMManager.instance.getHttpApi()!!
            .post<GroupListResponseData>(
                "app/v1/group/list",
                params,
                object : SuperOkHttpCallBack<GroupListResponseData>(object :RequestCallBack<GroupListResponseData>{
                    override fun onStart() {
                        callBack.onStart()
                    }

                    override fun onFinish(response: NetResponse<GroupListResponseData>) {
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
        private var mInstance: ContactsApi? = null

        val instance: ContactsApi
            @Synchronized
            get() {
                if (mInstance == null) {
                    synchronized(ContactsApi::class) {
                        if (mInstance == null) {
                            mInstance = ContactsApi()
                        }
                    }
                }
                return mInstance!!
            }

    }
}