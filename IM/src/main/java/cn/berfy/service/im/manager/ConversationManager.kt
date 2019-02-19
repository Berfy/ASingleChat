package cn.berfy.service.im.manager

import cn.berfy.sdk.http.callback.RequestCallBack
import cn.berfy.sdk.http.model.NetError
import cn.berfy.sdk.http.model.NetResponse
import cn.berfy.sdk.mvpbase.util.LogF
import cn.berfy.service.im.http.ConversationApi
import cn.berfy.service.im.manager.i.IMCallback
import cn.berfy.service.im.model.Message
import cn.berfy.service.im.model.MessageType
import cn.berfy.service.im.model.conversation.Conversation
import cn.berfy.service.im.model.response.ConversationResponseData

/**
author: Berfy
date: 2018/12/26
会话相关操作管理类
 */
class ConversationManager {

    private val TAG = "IM会话管理"

    companion object {

        private var mInstance: ConversationManager? = null

        val instance: ConversationManager
            @Synchronized
            get() {
                if (mInstance == null) {
                    synchronized(ConversationManager::class.java) {
                        if (mInstance == null) {
                            mInstance = ConversationManager()
                        }
                    }
                }
                return mInstance!!
            }
    }

    //获取本地会话列表
    fun getLocalConversations(onDataCallback: CacheManager.OnDataCallback<Conversation>) {
        if (!IMManager.isInstanced() || !IMManager.instance.isConnected) {
            onDataCallback.onFailed("IM服务未启动")
        } else {
            CacheManager.instance.getConversations(onDataCallback)
        }
    }

    //获取网络会话列表
    fun getConversations(onDataCallback: CacheManager.OnDataCallback<Conversation>) {
        LogF.d(TAG, "获取网络所有会话")
        if (!IMManager.isInstanced() || !IMManager.instance.isConnected) {
            LogF.d(TAG, "IM服务未启动")
            onDataCallback.onFailed("IM服务未启动")
        } else {
            //网络获取会话
            ConversationApi.instance.getConversation(object : RequestCallBack<ConversationResponseData> {
                override fun onStart() {
                }

                override fun onFinish(response: NetResponse<ConversationResponseData>) {
                    if (response.isOk && null != response.data && null != response.data.sess) {
                        val datas = response.data.sess!!
                        //对比本地会话
                        LogF.d(TAG, "网络会话get  对比本地会话")
                        fixConversationAndLocal(datas, onDataCallback)
                    } else {
                        //本地会话
                        LogF.d(TAG, "网络会话not get  获取本地会话")
                        getLocalConversations(onDataCallback)
                    }
                }

                override fun onError(error: NetError?) {
                    //本地会话
                    LogF.d(TAG, "网络会话not get error  获取本地会话")
                    getLocalConversations(onDataCallback)
                }
            })
        }
    }

    private fun fixConversationAndLocal(
        netDatas: ArrayList<Conversation>,
        callback: CacheManager.OnDataCallback<Conversation>
    ) {
        getLocalConversations(object : CacheManager.OnDataCallback<Conversation> {
            override fun onSuc(data: MutableList<Conversation>) {
                if (data.size == 0) {//本地没有直接添加
                    CacheManager.instance.addConversations(netDatas, object : IMCallback {
                        override fun onSuc() {
                            LogF.d(TAG, "对比完成-第一次覆盖 返回网络会话")
                            callback.onSuc(netDatas)
                        }

                        override fun onFailed(err: String) {
                            LogF.d(TAG, "对比完成-插入数据失败")
                            callback.onFailed(err)
                        }
                    })
                } else {
                    //插入网络数据
                    CacheManager.instance.addConversations(netDatas, object : IMCallback {
                        override fun onSuc() {
                            //插入成功 重新获取本地
                            getLocalConversations(object : CacheManager.OnDataCallback<Conversation> {
                                override fun onSuc(data: MutableList<Conversation>) {
                                    LogF.d(TAG, "对比完成-返回整合数据")
                                    callback.onSuc(data)
                                }

                                override fun onFailed(err: String) {
                                    LogF.d(TAG, "对比失败-获取整合数据失败")
                                    callback.onFailed("对比失败-获取整合数据失败")
                                }
                            })
                        }

                        override fun onFailed(err: String) {
                            LogF.d(TAG, "对比失败-网络存入本地数据失败")
                            callback.onFailed("对比失败-网络存入本地数据失败")
                        }
                    })
                }
            }

            override fun onFailed(err: String) {
                LogF.d(TAG, "对比失败-获取本地会话失败")
                callback.onFailed(err)
            }
        })
    }

    private fun getNetConversationType(type: String): MessageType {
        when (type) {
            "0" -> {
                return MessageType.TYPE_P2P
            }
            "1" -> {
                return MessageType.TYPE_CHATROOM
            }
            else -> {
                return MessageType.TYPE_P2P
            }
        }
    }

    //删除指定消息
    fun deleteConversation(msg: Message) {
        if (!IMManager.isInstanced() || !IMManager.instance.isConnected) {
            LogF.e(TAG, "IM服务未启动")
        } else {
            CacheManager.instance.deleteMessage(
                msg
                , null
            )
        }
    }

    //删除指定会话
    fun deleteConversation(toId: String, type: MessageType, callback: IMCallback?) {
        if (!IMManager.isInstanced() || !IMManager.instance.isConnected) {
            LogF.e(TAG, "IM服务未启动")
            if (null != callback)
                callback.onFailed("IM服务未启动")
        } else {
            CacheManager.instance.deleteConversation(
                Conversation(
                    toId,
                    type
                ), callback
            )
        }
    }
}