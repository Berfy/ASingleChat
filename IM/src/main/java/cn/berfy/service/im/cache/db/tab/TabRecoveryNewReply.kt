package cn.berfy.service.im.cache.db.tab

import android.content.Context
import com.axingxing.pubg.db.base.DBHelper

/**
 * 发现动态新消息回复
 * @author Berfy
 * 2018.5.12
 */
class TabRecoveryNewReply private constructor(private val mContext: Context) {
    private val mDbHelper: DBHelper?
    private val TAG = "表_新回复消息"

    init {
        mDbHelper = DBHelper.instance
    }

//    val allNotice: ArrayList<ReplyInfo>
//        get() {
//            val reply = ArrayList<ReplyInfo>()
//            val cursor = mDbHelper!!.db.rawQuery("SELECT * FROM $TAB_NAME order by $TAB_KEYS_CREATED desc ", null)
//            while (cursor.moveToNext()) {
//                val replyInfo = GsonUtil.getInstance().toClass(cursor.getString(cursor.getColumnIndex(TAB_KEYS_CONTENT_JSON)), ReplyInfo::class.java)
//                if (null != replyInfo) {
//                    reply.add(replyInfo)
//                }
//            }
////            LogF.d(TAG, "获取所有本地回复消息" + GsonUtil.getInstance().toJson(reply))
//            return reply
//        }
//
//    fun addNotice(replyInfo: ReplyInfo) {
//        mDbHelper!!.db.insert(TAB_NAME, null, getValues(replyInfo))
//        LogF.d(TAG, "插入新回复消息" + GsonUtil.getInstance().toJson(replyInfo))
//    }
//
//    fun clear() {
//        mDbHelper!!.db.delete(TAB_NAME, "$TAB_KEYS_USER_ID = ?", arrayOf(BaseApplication.getCurrLoginUser()!!.id))
//    }
//
//    private fun getValues(replyInfo: ReplyInfo): ContentValues {
//        val cv = ContentValues()
//        cv.put(TAB_KEYS_USER_ID, Objects.requireNonNull<User>(BaseApplication.getCurrLoginUser()).getId())
//        cv.put(TAB_KEYS_CONTENT_JSON, GsonUtil.getInstance().toJson(replyInfo))
//        cv.put(TAB_KEYS_CREATED, replyInfo.reply.created)
//        return cv
//    }
//
//    companion object {
//
//        private var mInstances: TabRecoveryNewReply? = null
//        var TAB_NAME = "tab_recovery_new_recomment"//id
//        var TAB_KEYS_USER_ID = "id"//id
//        var TAB_KEYS_CONTENT_JSON = "content_json"//id
//        var TAB_KEYS_CREATED = "created"//创建时间
//
//        fun init(context: Context) {
//            if (null == mInstances) {
//                mInstances = TabRecoveryNewReply(context)
//            }
//        }
//
//        val instances: TabRecoveryNewReply?
//            @Synchronized get() {
//                if (null == mInstances) {
//                    synchronized(TabRecoveryNewReply::class.java) {
//                        if (null == mInstances) {
//                            init(BaseApplication.getContext())
//                        }
//                    }
//                }
//                return mInstances
//            }
//    }
}
