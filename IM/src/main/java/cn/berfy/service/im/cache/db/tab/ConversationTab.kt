package cn.berfy.service.im.cache.db.tab

import cn.berfy.sdk.mvpbase.util.LogF
import cn.berfy.service.im.cache.db.IMDatabase
import com.raizlabs.android.dbflow.annotation.Column
import com.raizlabs.android.dbflow.annotation.Migration
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.sql.SQLiteType
import com.raizlabs.android.dbflow.sql.migration.AlterTableMigration
import com.raizlabs.android.dbflow.structure.BaseModel

/**
author: Berfy
date: 2018/12/27
会话表
从服务器获取会话后 第一次没有数据时插入表数据
本地表有数据时，以本地为准，显示在聊天列表  不存在的不存入 新消息来的时候再次插入
 */
@Table(database = IMDatabase::class)
class ConversationTab : BaseModel() {

    @PrimaryKey(autoincrement = true)//本地会话id
    var id: Long = -1

    @Column
    var user_id: String = ""// 用户id 区分登录身份

    @Column
    var peer: String = ""//会话对象id

    //p2p chatroom group
    @Column
    var type: String = ""//会话类型

    @Column
    var title: String = ""//会话标题

    @Column
    var unread_count: Long = 0//未读消息

    @Column
    var last_message: String = ""//最后一条消息内容

    @Column
    var last_message_time: Long = 0L//最后一条消息时间

    @Column
    var last_update_time: Long = 0L//最后更新时间

    @Migration(version = IMDatabase.VERSION, database = IMDatabase::class)
    class MigrationAlterTable(table: Class<ConversationTab>) : AlterTableMigration<ConversationTab>(table) {

        override fun onPreMigrate() {
//            addColumn(SQLiteType.TEXT, "user_id")
//            addColumn(SQLiteType.INTEGER, "unread_count")
//            addColumn(SQLiteType.INTEGER, "last_update_time")
//            addColumn(SQLiteType.INTEGER, "raw_id")
//            addColumn(SQLiteType.TEXT, "topic")

            LogF.d("数据库", "MigrationAlterTable  数据库更新了 会话表变动" + IMDatabase.VERSION)
        }
    }
}