package cn.berfy.service.im.manager

import cn.berfy.sdk.mvpbase.util.LogF
import cn.berfy.service.im.model.Message

/**
 * Created by Berfy on 2018/1/23.
 * 聊天消息过滤拦截工具类
 */
object FilterManager {

    private val TAG = "IM过滤"

    //是否拦截消息 true拦截  false不拦截
    fun filterMessage(msg: Message): Boolean {
        LogF.d(TAG, "不过滤")
        return false
    }

}
