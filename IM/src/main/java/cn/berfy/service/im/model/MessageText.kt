package cn.berfy.service.im.model

import android.content.Context
import android.util.TypedValue
import android.widget.TextView
import cn.berfy.sdk.mvpbase.util.CommonUtil
import cn.berfy.sdk.mvpbase.util.DeviceUtils
import cn.berfy.service.im.R
import cn.berfy.service.im.manager.i.OnMessageDownloadCallback

/**
author: Berfy
date: 2018/12/25
文本消息
 */
class MessageText : Message() {

    var content: String = ""//消息内容

    init {
        type = MessageContentType.TYPE_TEXT
    }

    override fun showMessage(viewHolder: ChatViewHolder, context: Context) {
        clearView(viewHolder)//清理内容
        val tv = TextView(context)
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
        val textColor=if (isSelf()) R.color.color_B85488  else R.color.color_181927
        tv.setTextColor(CommonUtil.getColor(textColor))
        tv.text = content
        getBubbleView(viewHolder).addView(tv)
        if (isSelf()) {//我发送的
            getBubbleView(viewHolder).setPadding(
                DeviceUtils.dpToPx(context, 10f),
                DeviceUtils.dpToPx(context, 10f),
                DeviceUtils.dpToPx(context, 15f),
                DeviceUtils.dpToPx(context, 10f)
            )
        } else {
            getBubbleView(viewHolder).setPadding(
                DeviceUtils.dpToPx(context, 15f),
                DeviceUtils.dpToPx(context, 10f),
                DeviceUtils.dpToPx(context, 10f),
                DeviceUtils.dpToPx(context, 10f)
            )
        }
        showStatus(viewHolder, context)
    }

    override fun getRemoteUrl(callback: OnMessageDownloadCallback) {
    }

    override fun getSummaryText(): String {
        return content
    }

}