package cn.berfy.service.im.model

/**
author: Berfy
date: 2018/12/27
会话消息类型
 */
enum class MessageType {

    //消息协议类型(登录、注销、推送、单聊、群聊==)
//    const val TYPE_LOGIN = "login"//登录
//    const val TYPE_LOGOUT = "logout"//注销
//    const val TYPE_PING = "ping"//心跳
//    const val TYPE_SYSTEM = "system"//推送
//    const val TYPE_P2P = "p2p"//单聊
//    const val TYPE_GROUP = "group"//群聊

    TYPE_PING,//心跳消息
    TYPE_SYSTEM,//系统通知
    TYPE_CUSTOM,//自定义消息 推送
    TYPE_P2P,//单聊
    TYPE_GROUP,//群聊
    TYPE_CHATROOM;//聊天室
}