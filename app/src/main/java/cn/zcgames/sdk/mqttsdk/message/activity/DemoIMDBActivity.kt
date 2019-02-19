package cn.zcgames.sdk.mqttsdk.message.activity

import android.os.Bundle
import android.view.View
import butterknife.OnClick
import cn.berfy.sdk.mvpbase.base.CommonActivity
import cn.berfy.sdk.mvpbase.iview.IBaseView
import cn.berfy.sdk.mvpbase.prensenter.BasePresenter
import cn.berfy.sdk.mvpbase.util.LogF
import cn.berfy.service.im.manager.CacheManager
import cn.berfy.service.im.manager.i.IMCallback
import cn.berfy.service.im.model.MessageType
import cn.berfy.service.im.model.conversation.Conversation
import cn.zcgames.sdk.im.R

/**
author: Berfy
date: 2018/12/28
数据库操作
 */
class DemoIMDBActivity : CommonActivity<IBaseView, BasePresenter<IBaseView>>(), IBaseView {

    override fun getContentViewId(): Int {
        return R.layout.activity_demo_db
    }

    override fun initData(savedInstanceState: Bundle?) {

    }

    override fun initView() {
        showTitleBar()
        //清楚所有会话  测试
        CacheManager.instance.deleteAllConversation(null)
    }

    @OnClick(
        R.id.btn_add_conversation,
        R.id.btn_del_conversation,
        R.id.btn_add_conversations,
        R.id.btn_clear_conversations
    )
    fun onClick(v: View) {
        when (v.id) {
            R.id.btn_add_conversation -> {
                val con = Conversation("berfy", MessageType.TYPE_P2P)
                CacheManager.instance.addConversation(con)
            }
            R.id.btn_del_conversation -> {
                val con = Conversation("berfy", MessageType.TYPE_P2P)
                CacheManager.instance.deleteConversation(con, object : IMCallback {
                    override fun onSuc() {
                        CacheManager.instance.getConversations(object : CacheManager.OnDataCallback<Conversation> {
                            override fun onSuc(data: MutableList<Conversation>) {
                                LogF.d(TAG, "一共${data.size}条会话")
                            }

                            override fun onFailed(err: String) {
                            }
                        })
                    }

                    override fun onFailed(err: String) {
                    }
                })
            }
            R.id.btn_add_conversations -> {
                val datas = ArrayList<Conversation>()
                for (i in 0..99) {
                    val con = Conversation("berfy$i", MessageType.TYPE_P2P)
                    datas.add(con)
                }
                CacheManager.instance.addConversations(datas, object : IMCallback {
                    override fun onSuc() {
                        CacheManager.instance.getConversations(object : CacheManager.OnDataCallback<Conversation> {
                            override fun onSuc(data: MutableList<Conversation>) {
                                LogF.d(TAG, "一共${data.size}条会话")
                            }

                            override fun onFailed(err: String) {
                            }
                        })
                    }

                    override fun onFailed(err: String) {
                    }
                })
            }
            R.id.btn_clear_conversations -> {
                CacheManager.instance.deleteAllConversation(object : IMCallback {
                    override fun onSuc() {
                        CacheManager.instance.getConversations(object : CacheManager.OnDataCallback<Conversation> {
                            override fun onSuc(data: MutableList<Conversation>) {
                                LogF.d(TAG, "一共${data.size}条会话")
                            }

                            override fun onFailed(err: String) {
                            }
                        })
                    }

                    override fun onFailed(err: String) {
                    }
                })
            }
        }
    }

    override fun initPresenter(): BasePresenter<IBaseView>? {
        return null
    }

    override fun hiddenLoadingView(msg: String?) {

    }

    override fun showLoadingView(msg: String?) {

    }
}