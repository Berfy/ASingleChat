package cn.berfy.service.im.model

/**
author: Berfy
date: 2018/12/27
自定义消息类型
 */
enum class MessageCustomType {

    TYPE_ADD_FRIEND,//添加好友
    TYPE_AGREE_ADD_FRIEND,//通过好友申请
    TYPE_REFUSE_ADD_FRIEND,//拒绝好友申请
    TYPE_GROUP_INVITE,//被邀请进入群或聊天室
    TYPE_GROUP_JOIN_LEAVE,//进入或离开(群或者聊天室)
}