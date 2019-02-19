package cn.berfy.service.im.cache.db.tab

import cn.berfy.sdk.mvpbase.util.LogF
import cn.berfy.service.im.cache.db.IMDatabase
import com.raizlabs.android.dbflow.annotation.Column
import com.raizlabs.android.dbflow.annotation.Migration
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.sql.migration.AlterTableMigration
import com.raizlabs.android.dbflow.structure.BaseModel

/**
author: Berfy
date: 2018/12/27
消息表
 */
@Table(database = IMDatabase::class)
class MessageTab : BaseModel() {

    @PrimaryKey(autoincrement = true)//消息唯一id  表id
    var id: Long = 0

    @Column
    var user_id: String = ""// 用户id 区分登录身份

    @Column
    var msg_id: String = ""//服务器端msgid

    @Column
    var raw_id: String = ""//创建id

    @Column
    var read: Int = 0//是否已读  0未读 1已读

    @Column
    var send_status: Int = 0//0发送中 1发送成功 2发送失败

    @Column
    var conversation_id: String = ""//会话id 单聊（对方的id） 群聊（群id）

    @Column
    var type: String = ""//p2p单聊 group群聊 chatroom聊天室

    @Column
    var chat_type: String = ""//text聊天 image发送图片 voice语音 file文件 video视频 location位置

    @Column
    var sender_id: String = ""//发送者id

    @Column
    var sender_name: String = ""//发送者昵称

    @Column
    var content: String = ""//消息内容

    @Column
    var create_time: Long = 0L//消息发送时间

    @Column
    var last_update_time: Long = 0L//最后更新时间

    @Migration(version = IMDatabase.VERSION, database = IMDatabase::class)
    class MigrationAlterTable(table: Class<MessageTab>) : AlterTableMigration<MessageTab>(table) {

        override fun onPreMigrate() {
//            addColumn(SQLiteType.INTEGER, "read")
//            addColumn(SQLiteType.INTEGER, "msg_id")
//            addColumn(SQLiteType.INTEGER, "raw_id")
//            addColumn(SQLiteType.TEXT, "topic")

            LogF.d("数据库", "MigrationAlterTable  数据库更新了  消息表变动" + IMDatabase.VERSION)
        }
    }
}