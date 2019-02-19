package cn.berfy.service.im.model.response

/**
author: Berfy
date: 2019/2/14
服务器获取历史消息
 */
class ServerMessage {

    var id: String = ""
    var rowid: String = ""
    var topic: String = ""
    var payload: String = ""
    var acked: String = ""
    var type: Int = 0//0文本消息 1图片 2语音 3视频 4位置 6文件 100自定义
    var qos: String = ""
    var ttl: String = ""//发送超时时间
    var sender: String = ""//发送者
    var timestamp: String = "0"//发送时间
}