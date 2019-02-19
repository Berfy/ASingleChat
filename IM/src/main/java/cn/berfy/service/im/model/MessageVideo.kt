package cn.berfy.service.im.model

import android.content.Context
import android.graphics.drawable.AnimationDrawable
import android.text.TextUtils
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import cn.berfy.sdk.mvpbase.base.BaseApplication
import cn.berfy.sdk.mvpbase.util.CommonUtil
import cn.berfy.sdk.mvpbase.util.DeviceUtils
import cn.berfy.sdk.mvpbase.util.FileUtils
import cn.berfy.service.im.R
import cn.berfy.service.im.manager.i.OnMessageDownloadCallback

/**
author: Berfy
date: 2018/12/25
视频消息
 */
class MessageVideo : Message() {

    var md5: String = ""//md5校验码
    var videoUrl: String = ""//云端url
    var thumbUrl: String = ""//缩略图url
    var ext: String = ""//扩展名
    var w: Int = 0//宽高
    var h: Int = 0 //
    var localPath: String = ""//本地录音地址
    var duration: Long = 0L//录音时长 毫秒
    var fileLength: Long = 0L//文件大小 字节

    init {
        TAG = "IMLogTag_视频消息"
        type = MessageContentType.TYPE_VIDEO
    }

    override fun getSummaryText(): String {
        return "[视频]"
    }

    override fun showMessage(viewHolder: ChatViewHolder, context: Context) {
        val linearLayout = LinearLayout(context)
        linearLayout.orientation = LinearLayout.HORIZONTAL
        linearLayout.gravity = Gravity.CENTER
        val voiceIcon = ImageView(BaseApplication.getContext())
        voiceIcon.setBackgroundResource(if (isSelf()) R.drawable.im_audio_animation_list_right else R.drawable.im_audio_animation_list_left)
        val frameAnimatio = voiceIcon.background as AnimationDrawable
        val tv = TextView(BaseApplication.getContext())
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
//        val time = (message.getElement(0) as TIMSoundElem).getDuration()
//        tv.setText(time.toString() + "s")
//        int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 18, context.getResources().getDisplayMetrics());
//        int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 14, context.getResources().getDisplayMetrics());
        val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        val imageLp =
            LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        if (isSelf()) {
            tv.setTextColor(CommonUtil.getColor(R.color.color_443d20))
            lp.setMargins(0, 0, DeviceUtils.dpToPx(context, (if (time < 10) 10 else time).toFloat()), 0)
            tv.layoutParams = lp
            linearLayout.addView(tv)
            voiceIcon.layoutParams = imageLp
            linearLayout.addView(voiceIcon)
            getBubbleView(viewHolder).setPadding(
                DeviceUtils.dpToPx(context, 15f),
                DeviceUtils.dpToPx(context, 10f),
                DeviceUtils.dpToPx(context, 20f),
                DeviceUtils.dpToPx(context, 10f)
            )
        } else {
            tv.setTextColor(CommonUtil.getColor(R.color.color_AFAFAF))
            voiceIcon.layoutParams = imageLp
            linearLayout.addView(voiceIcon)
            lp.setMargins(DeviceUtils.dpToPx(context, (if (time < 10) 10 else time).toFloat()), 0, 0, 0)
            tv.layoutParams = lp
            linearLayout.addView(tv)
            getBubbleView(viewHolder).setPadding(
                DeviceUtils.dpToPx(context, 20f),
                DeviceUtils.dpToPx(context, 10f),
                DeviceUtils.dpToPx(context, 15f),
                DeviceUtils.dpToPx(context, 10f)
            )
        }
        clearView(viewHolder)
        getBubbleView(viewHolder).addView(linearLayout)
        getBubbleView(viewHolder).setOnClickListener {}
        showStatus(viewHolder, context)
    }


    //获取图片、语音、视频、文件远程源文件
    override fun getRemoteUrl(callback: OnMessageDownloadCallback) {
        this.downloadCallbacks.add(callback)
        this.downloadCallback.onStart()
        if (!TextUtils.isEmpty(localPath)
            && FileUtils.exists(localPath)
            && FileUtils.getFileSize(localPath) == fileLength
        ) {//本地文件存在，且大小一致
            this.downloadCallback.downloadProgress(1F)
            this.downloadCallback.onSuc(this)
        } else {
            //下载去吧

        }
    }
}