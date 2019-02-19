package cn.berfy.service.im.cache.db

import cn.berfy.sdk.mvpbase.util.LogF
import cn.berfy.service.im.cache.db.tab.ConversationTab
import cn.berfy.service.im.cache.db.tab.MessageTab
import com.raizlabs.android.dbflow.annotation.Database
import com.raizlabs.android.dbflow.annotation.Migration
import com.raizlabs.android.dbflow.sql.SQLiteType
import com.raizlabs.android.dbflow.sql.migration.AlterTableMigration
import com.raizlabs.android.dbflow.sql.migration.BaseMigration
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper

/**
author: Berfy
date: 2018/12/27
 */
@Database(name = IMDatabase.NAME, version = IMDatabase.VERSION)
class IMDatabase {

    companion object {
        const val NAME = "im_db"

        const val VERSION = 1
    }

    @Migration(version = IMDatabase.VERSION, database = IMDatabase::class)
    class Migration2 : BaseMigration() {

        override fun onPreMigrate() {
            LogF.d("数据库", "Migration2 数据库更新了" + IMDatabase.VERSION)
        }

        override fun migrate(database: DatabaseWrapper) {
        }

        override fun onPostMigrate() {
            super.onPostMigrate()
        }
    }
}