package cn.berfy.service.im.manager

import cn.berfy.sdk.http.callback.RequestCallBack
import cn.berfy.sdk.http.model.NetError
import cn.berfy.sdk.mvpbase.util.LogF
import cn.berfy.service.im.cache.db.IMDatabase
import com.raizlabs.android.dbflow.sql.language.SQLite

import cn.berfy.service.im.cache.db.tab.ContactModel
import cn.berfy.service.im.cache.db.tab.ContactModel_Table
import cn.berfy.service.im.http.ContactsApi
import cn.berfy.service.im.model.contact.UserInfo
import cn.berfy.service.im.model.response.GroupListResponseData
import com.raizlabs.android.dbflow.config.FlowManager
import com.raizlabs.android.dbflow.structure.database.transaction.ProcessModelTransaction

class ContactManager {

    private val TAG = "ContactManager"

    //获取通讯录列表
    val contactList: List<ContactModel>
        get() = SQLite.select().from(ContactModel::class.java).queryList()

    //从网络获取用户资料
    fun getUserInfo(uid: String, callback: RequestCallBack<UserInfo>) {
//        if (!IMManager.isInstanced() || !IMManager.getInstance().isConnected) {
//            callback.onError(NetError(300, 0, "IM服务未启动"))
//            return
//        }
        ContactsApi.instance.getUserInfo(uid, callback)
    }

    //从网络获取群组列表
    fun getGroupList(callback: RequestCallBack<GroupListResponseData>) {
        if (!IMManager.isInstanced() || !IMManager.instance.isConnected) {
            callback.onError(NetError(300, 0, "IM服务未启动"))
            return
        }
        ContactsApi.instance.getGroupList(callback)
    }

    //单条数据插入数据
//    fun save(contact: ContactModel) {
//        contact.save()
//    }


    //删除
    fun delete(uid: String): Boolean {
        val isDelete: Boolean
        val contactModel = SQLite.select()
                .from(ContactModel::class.java)
                .where(ContactModel_Table.uid.eq(uid))
                .querySingle()
        return contactModel?.delete() ?: false
    }

    //单条数据插入或修改
    fun update(model: ContactModel) {
        var contactModel = SQLite.select()
                .from(ContactModel::class.java)
                .where(ContactModel_Table.uid.eq(model.uid))
                .querySingle()//区别于queryList(),返回的是实体

        if (null != contactModel) {
            contactModel = model
            contactModel.update()
            LogF.d(TAG, "存储联系人 更新")
        } else {
            model.save()
            LogF.d(TAG, "存储联系人 添加")
        }
    }

    //查询单个
    fun selectOne(uid: String): ContactModel? {
        return SQLite.select()
                .from(ContactModel::class.java)
                .where(ContactModel_Table.uid.eq(uid))//条件
                .querySingle()
    }


    //批量插入
    fun insertContactAsync(modelList: List<ContactModel>) {
        FlowManager.getDatabase(IMDatabase::class.java)
                .beginTransactionAsync(ProcessModelTransaction.Builder(
                        ProcessModelTransaction.ProcessModel<ContactModel> { contactModel, wrapper ->
                            //查询联系人是否已存在
                            var cModel = SQLite.select()
                                    .from(ContactModel::class.java)
                                    .where(ContactModel_Table.uid.eq(contactModel.uid)
                                    ).querySingle()
                            var isSuc = false
                            if (null != cModel) {
                                cModel = contactModel
                                isSuc = cModel.update()
                                LogF.d(TAG, "存储联系人 更新")
                            } else {
                                isSuc = contactModel.save()
                                LogF.d(TAG, "存储联系人 添加")
                            }
                            LogF.d(TAG, "批量操作ing  $isSuc")
                        }).addAll(modelList).build())
                .error { transaction, error -> LogF.d(TAG, "error结果" + error.message) }
                .success { LogF.d(TAG, "添加success") }.build()
                .executeSync()//异步
    }


    //清空表数据
    fun deleteList() {
        SQLite.delete(ContactModel::class.java)
                .execute()
    }

    companion object {
        private var mInstance: ContactManager? = null

        val instance: ContactManager
            @Synchronized
            get() {
                if (mInstance == null) {
                    synchronized(ContactManager::class.java) {
                        if (mInstance == null) {
                            mInstance = ContactManager()
                        }
                    }
                }
                return mInstance!!
            }
    }
}
