package cn.berfy.service.im.model

import android.content.Context
import android.os.Environment
import android.text.TextUtils
import android.widget.TextView
import cn.berfy.sdk.mvpbase.base.BaseApplication
import cn.berfy.sdk.mvpbase.util.CommonUtil
import cn.berfy.sdk.mvpbase.util.DeviceUtils
import cn.berfy.sdk.mvpbase.util.FileUtils
import cn.berfy.sdk.mvpbase.util.ToastUtil
import cn.berfy.service.im.R
import cn.berfy.service.im.manager.i.OnMessageDownloadCallback

/**
author: Berfy
date: 2018/12/25
文件消息
 */
class MessageFile : Message() {

    var name: String = ""//文件名
    var md5: String = ""//md5校验码
    var ext: String = ""//扩展名
    var fileUrl: String = ""//云端url
    var localPath: String = ""//本地录音地址
    var fileLength: Long = 0L//文件大小 字节

    init {
        type = MessageContentType.TYPE_FILE
    }

    override fun showMessage(viewHolder: ChatViewHolder, context: Context) {
        clearView(viewHolder)
        val tv = TextView(BaseApplication.getContext())
        val textColor = if (isSelf()) R.color.color_B85488 else R.color.color_181927
        tv.setTextColor(CommonUtil.getColor(context, textColor))
        val drawable = CommonUtil.getDrawable(context, R.drawable.pic_file)
        drawable.setBounds(0, 0, drawable.minimumWidth, drawable.minimumHeight)// 设置图片宽高
        tv.setCompoundDrawables(null, drawable, null, null)// 设置到控件中
        tv.compoundDrawablePadding = DeviceUtils.dpToPx(context, 5f)
        tv.text = name
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




    /**
     * 保存消息或消息文件
     */
    fun save() {
        val str =name.split("/")
        val filename = str[str.size - 1]
        if (FileUtils.isFileExist(filename, Environment.DIRECTORY_DOWNLOADS)) {
            ToastUtil.getInstances().showShort(BaseApplication.getContext().getString(R.string.im_save_exist))
            return
        }
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

    override fun getSummaryText(): String {
        return "[文件]"
    }
}