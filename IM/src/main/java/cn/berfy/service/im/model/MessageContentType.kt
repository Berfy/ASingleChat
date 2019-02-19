package cn.berfy.service.im.model

/**
author: Berfy
date: 2018/12/27
原始消息类型
 */
enum class MessageContentType {
//    //消息类型
//    const val TYPE_SYSTEM = "system"//系统消息
//    //群消息
//    const val TYPE_GROUP_SYSTEM = "group_system"//群系统消息  1申请入群(管理员收到，如果群不用申请，自动加入群聊) 2申请入群的同意和拒绝入群（申请人收到）
//    // 3邀请入群请求（被邀请用户收到） 4被邀请人同意或拒绝(邀请人收到)
//    // 5被踢出群组(管理员和被踢用户收到) 6被解散(所有成员收到) 7创建群 8邀请入群（用户进群收到）
//    // 9主动退群（自己收到） 10设置取消管理员(被设置的用户收到)
//    //群提示
//    const val TYPE_GROUP_TIP = "group_system"//群提示（全部成员收到） 1加入群聊（自己和全部成员收到）
//    // 2退出群聊（全部成员收到） 3被踢出群组（全部成员收到） 4被设置取消管理员（全部成员）
//    // 5资料变更
//
//    聊天
//    const val TYPE_TEXT = "text"//聊天
//    const val TYPE_IMAGE = "image"//发送图片
//    const val TYPE_VOICE = "voice"//语音
//    const val TYPE_FILE = "file"//文件
//    const val TYPE_VIDEO = "video"//视频

    TYPE_SYSTEM,//系统消息
    TYPE_CUSTOM,//自定义消息
    TYPE_GROUP_SYSTEM,//群系统消息  1申请入群(管理员收到，如果群不用申请，自动加入群聊) 2申请入群的同意和拒绝入群（申请人收到）
    // 3邀请入群请求（被邀请用户收到） 4被邀请人同意或拒绝(邀请人收到)
    // 5被踢出群组(管理员和被踢用户收到) 6被解散(所有成员收到) 7创建群 8邀请入群（用户进群收到）
    // 9主动退群（自己收到） 10设置取消管理员(被设置的用户收到)
    TYPE_GROUP_TIP,//群提示（全部成员收到） 1加入群聊（自己和全部成员收到）
    // 2退出群聊（全部成员收到） 3被踢出群组（全部成员收到） 4被设置取消管理员（全部成员）
    // 5资料变更
    TYPE_TEXT,//聊天
    TYPE_IMAGE,//图片
    TYPE_VOICE,//语音
    TYPE_FILE,//文件
    TYPE_VIDEO,//视频
    TYPE_LOCATION;//位置
}