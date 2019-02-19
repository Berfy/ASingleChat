package cn.berfy.service.im.model.response

import cn.berfy.service.im.model.group.Group
import com.google.gson.annotations.SerializedName

import java.io.Serializable

/**
author: Berfy
date: 2019/1/29
群组列表data解析
 */
class GroupListResponseData : Serializable {

    var list: ArrayList<Group>? = null

}
