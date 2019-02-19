package cn.berfy.service.im.model.group

/**
author: Berfy
date: 2019/1/29
群组列表信息
 */
class Group {

    /**
     * group_id : 组id
     * admin_id : 创建者id
     * name : 组名
     * bulletin : 公告牌
     * private : 1:私有组，2:公开组
     * invite : 1:不能邀请，2:可以邀请
     * capacity : 最大成员数
     * created : 创建时间
     * group_type : 1:聊天室；2:群聊
     */
    var group_id: String? = null
    var admin_id: String? = null
    var name: String? = null
    var bulletin: String? = null
    var private: String? = null
    var invite: String? = null
    var capacity: String? = null
    var created: String? = null
    var group_type: String? = null

}