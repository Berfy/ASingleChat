package com.axingxing.pubg.db.base

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Build

/**
 * 互动通知DAO
 *
 * @author Berfy
 * 2017-10-24
 */
class DBHelper constructor(private val mContext: Context, dbName: String, dbVersion: Int) : SQLiteOpenHelper(mContext, dbName, null, dbVersion) {
    val db: SQLiteDatabase

    init {
        // TODO Auto-generated constructor stub
        db = initDb()
    }

    private fun initDb(): SQLiteDatabase {
        // TODO Auto-generated method stub
        var db = readableDatabase
        if (Build.VERSION_CODES.HONEYCOMB <= Build.VERSION.SDK_INT) {
            db.enableWriteAheadLogging()// 允许读写同时进行
        }
        if (db.isReadOnly) {
            db = writableDatabase
        }
        return db
    }

    fun initTab() {
//        TabRecoveryNewReply.init(mContext)
    }

    override fun onCreate(db: SQLiteDatabase) {
        // TODO Auto-generated method stub
//        execSql(db, DBConstants.CREATE_TAB_RECOVERY_NEW_RECOMMENT)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
//        execSql(db, DBConstants.CREATE_TAB_RECOVERY_NEW_RECOMMENT)
    }

    private fun execSql(db: SQLiteDatabase, sql: String) {
        try {
            // 添加修改时间字段
            db.execSQL(sql)// 给表添加是否与服务器同步字段
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    companion object {
        @Volatile
        private var mDbHelper: DBHelper? = null

        @Synchronized
        fun init(context: Context, dbVersion: Int, dbName: String): DBHelper? {
            if (null == mDbHelper)
                synchronized(DBHelper::class.java) {
                    if (null == mDbHelper) {
                        mDbHelper = DBHelper(context, dbName, dbVersion)
                    }
                }
            return mDbHelper
        }

        val instance: DBHelper?
            get() {
                if (null == mDbHelper) {
                    throw NullPointerException("请在Application中初始化DBHelper")
                }
                return mDbHelper
            }
    }

}
