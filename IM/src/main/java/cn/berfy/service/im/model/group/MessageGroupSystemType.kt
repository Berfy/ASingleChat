package cn.berfy.service.im.model.group

/**
author: Berfy
date: 2018/12/27
群组（聊天室）系统消息类型参照MessageGroupSystemType
 */
enum class MessageGroupSystemType {

    TYPE_FORBIDDEN_WORDS,//禁言
    TYPE_FORBIDDEN_JOIN,//禁止加入
    TYPE_SET_MANAGER,//设置管理员
    TYPE_UNSET_MANAGER,//取消管理员
    TYPE_FIRST_WELCOME,//第一次加入聊天室 欢迎语
    TYPE_DISMISS,//解散
    TYPE_JOIN,//成员进入
    TYPE_LEAVE//成员退出
}