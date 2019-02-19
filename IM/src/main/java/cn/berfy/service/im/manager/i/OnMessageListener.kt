package cn.berfy.service.im.manager.i

import cn.berfy.service.im.model.Message
import cn.berfy.service.im.model.MessageCustom

/**
 * @author Berfy
 * 消息监听
 * */
interface OnMessageListener {

    fun newMessage(message: Message?) //消息

    fun systemMessage(message: MessageCustom?) //自定义消息

    fun refreshConversation() //刷新会话

    fun sendMessageStatus(message: String?, isSuc: Boolean) //发送消息  是否成功状态  全局监听用

}