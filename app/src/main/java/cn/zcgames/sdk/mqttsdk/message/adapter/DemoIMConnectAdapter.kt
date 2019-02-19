package cn.zcgames.sdk.mqttsdk.message.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import cn.berfy.sdk.mvpbase.util.TimeUtil
import cn.berfy.service.im.model.*
import cn.zcgames.sdk.im.R
import java.util.*

/**
author: Berfy
date: 2018/12/20
消息列表适配器
 */
class DemoIMConnectAdapter(context: Context) : RecyclerView.Adapter<DemoIMConnectAdapter.Companion.ViewHolder>() {

    private var mContext: Context? = null
    private var mInflater: LayoutInflater? = null
    private var mData: MutableList<Message> = ArrayList()

    init {
        mContext = context
        mInflater = LayoutInflater.from(mContext)
    }

    fun getData(): MutableList<Message> {
        return mData
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        return ViewHolder(
                mInflater!!.inflate(
                        R.layout.adapter_deml_im_connect_item,
                        p0,
                        false
                )
        )
    }

    override fun getItemCount(): Int {
        return mData.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val msg = mData[position]
        if (msg is MessageText) {
            holder.tvMsg!!.text = "${msg.senderId}: ${msg.content}"
        } else if (msg is MessageImage) {
            holder.tvMsg!!.text = "[图片]"
        } else if (msg is MessageVoice) {
            holder.tvMsg!!.text = "[语音]"
        } else if (msg is MessageVideo) {
            holder.tvMsg!!.text = "[视频]"
        } else if (msg is MessageFile) {
            holder.tvMsg!!.text = "[文件]"
        }
        holder.tvTime!!.text = TimeUtil.format("yyyy-MM-dd HH:mm:ss", msg.time)
    }

    companion object {
        val TAG: String = "消息列表适配器"

        class ViewHolder(convertView: View) : RecyclerView.ViewHolder(convertView) {
            var tvMsg: TextView? = null
            var tvTime: TextView? = null

            init {
                tvMsg = convertView.findViewById(R.id.tv_msg)
                tvTime = convertView.findViewById(R.id.tv_msg_time)
            }
        }
    }
}