package cn.berfy.service.im.model

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.widget.ImageView
import cn.berfy.sdk.mvpbase.util.CommonUtil
import cn.berfy.sdk.mvpbase.util.DeviceUtils
import cn.berfy.sdk.mvpbase.util.FileUtils
import cn.berfy.sdk.mvpbase.util.LogF
import cn.berfy.service.im.R
import cn.berfy.service.im.manager.i.OnMessageDownloadCallback
import cn.berfy.service.im.zoomImage.WatchMessagePictureActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.animation.GlideAnimation
import com.bumptech.glide.request.target.SimpleTarget
import java.io.Serializable
import java.util.*


/**
author: Berfy
date: 2018/12/25
图片消息
 */
class MessageImage : Message(), Serializable {
    var name: String = ""//文件名
    var md5: String = ""//md5校验码
    var imageUrl: String = ""//云端url
    var ext: String = ""//扩展名
    var w: Int = 0//宽高
    var h: Int = 0 //
    var thumbUrl: String = ""//缩略图url
    var localPath: String = ""//本地图片地址
    var fileLength: Long = 0L//文件大小 字节

    init {
        TAG = "IMLogTag_图片消息"
        type = MessageContentType.TYPE_IMAGE
    }

    override fun getSummaryText(): String {
        return "[图片]"
    }

    override fun showMessage(viewHolder: ChatViewHolder, context: Context) {
        clearView(viewHolder)
        when (this.sendStatus) {
            STATUS_SEND, STATUS_SEND_FAILED -> {
                if (this.sendStatus == STATUS_SEND) {
                    LogF.d(TAG, "图片发送ing")
                } else {
                    LogF.d(TAG, "图片发送失败")
                }
            }
            STATUS_SEND_SUC -> {
                LogF.d(TAG, "图片发送成功")
                LogF.d(TAG, "图片地址$localPath")
            }
        }
        showThumb(context, viewHolder)
        reSizeView(context, viewHolder)
        showStatus(viewHolder, context)
    }

    private fun reSizeView(context: Context, viewHolder: ChatViewHolder) {
        if (isSelf()) {//我发送的
            getBubbleView(viewHolder).setPadding(
                    DeviceUtils.dpToPx(context, 2f),
                    DeviceUtils.dpToPx(context, 2f),
                    DeviceUtils.dpToPx(context, 2f),
                    DeviceUtils.dpToPx(context, 2f)
            )
        } else {
            getBubbleView(viewHolder).setPadding(
                    DeviceUtils.dpToPx(context, 2f),
                    DeviceUtils.dpToPx(context, 2f),
                    DeviceUtils.dpToPx(context, 2f),
                    DeviceUtils.dpToPx(context, 2f)
            )
        }
    }

    //展示缩略图
    private fun showThumb(context: Context, viewHolder: ChatViewHolder) {
        val imageView = ImageView(context)
        getBubbleView(viewHolder).addView(imageView)
        //设置ImageView的参数
        if (FileUtils.exists(localPath)) {
            try {
                imageView.setImageBitmap(getThumb(localPath))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            val params = imageView.layoutParams
            //设置Img的相对于屏幕的宽高比
            params.width = w
            params.height = h
            imageView.layoutParams = params
            imageView.scaleType = ImageView.ScaleType.FIT_XY
            if (!TextUtils.isEmpty(thumbUrl)) {
                val target = object : SimpleTarget<Bitmap>(w, h) {
                    override fun onResourceReady(bitMBitmap: Bitmap?, glideAnimation: GlideAnimation<in Bitmap>?) {
                        imageView.setImageBitmap(bitMBitmap)
                    }

                    override fun onLoadFailed(e: java.lang.Exception?, errorDrawable: Drawable?) {
                        super.onLoadFailed(e, errorDrawable)
                        if (errorDrawable != null) imageView.setImageDrawable(errorDrawable)
                    }

                    override fun onLoadStarted(placeholder: Drawable?) {
                        super.onLoadStarted(placeholder)
                        if (placeholder != null) imageView.setImageDrawable(placeholder)

                    }
                }
                Glide.with(context)
                        .load(thumbUrl)
                        .asBitmap()
                        .dontAnimate()
                        .placeholder(CommonUtil.getDrawable(context, R.drawable.ps_img_loading))
                        .error(CommonUtil.getDrawable(context, R.drawable.im_image_download_failed))
                        .into(target)
            }
        }
        getBubbleView(viewHolder).removeAllViews()
        getBubbleView(viewHolder).addView(imageView)
        //点击图片查看大图
        getBubbleView(viewHolder).setOnClickListener {
            if (!TextUtils.isEmpty(imageUrl)) {
                LogF.d(TAG, "imageUrl==>$imageUrl")
                viewImage(context, viewHolder)
            }
        }
    }

    /**
     * 生成缩略图
     * 缩略图是将原图等比压缩，压缩后宽、高中较小的一个等于198像素
     * 详细信息参见文档
     */
    @Throws(Exception::class)
    private fun getThumb(path: String): Bitmap {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(path, options)
        val reqWidth: Int
        val reqHeight: Int
        val width = options.outWidth
        val height = options.outHeight
        if (width > height) {
            reqWidth = 198
            reqHeight = reqWidth * height / width
        } else {
            reqHeight = 198
            reqWidth = width * reqHeight / height
        }
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            while (halfHeight / inSampleSize > reqHeight && halfWidth / inSampleSize > reqWidth) {
                inSampleSize *= 2
            }
        }
        options.inSampleSize = inSampleSize
        options.inJustDecodeBounds = false
        val mat = Matrix()
        val bitmap = BitmapFactory.decodeFile(path, options)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, mat, true)
    }

    private fun viewImage(context: Context, viewHolder: ChatViewHolder) {
        try {
            val imMessages = ArrayList<MessageImage>()
            if (null != viewHolder.messages) {
                LogF.d(TAG, "消息窗口本地的消息")
                for (message in viewHolder.messages!!) {
                    if (message is MessageImage) {
                        imMessages.add(message)
                    }
                }
                LogF.d(TAG, "viewImage---imMessages.size()==>${imMessages.size}")
                toShowImage(context, imMessages)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    //查看大图
    private fun toShowImage(context: Context, imgMessages: List<MessageImage>) {
        val paths = ArrayList<HashMap<String, String>>()
        var showPosition = 0
        for (imgMsg in imgMessages) {
            val hashMap = HashMap<String, String>()
            hashMap["msgId"] = imgMsg.id
            hashMap["url"] = imgMsg.imageUrl
            paths.add(hashMap)
            if (id == imgMsg.id) {//设置当前图片消息在浏览器中的显示位置
                val size = paths.size
                showPosition = if (size > 0) size - 1 else 0
                LogF.d(TAG, "图片匹配$showPosition")
            }
            LogF.d(TAG, "图片消息")
            LogF.d(TAG, "toShowImage--imgMsg.imageUrl==>${imgMsg.imageUrl}")
            LogF.d(TAG, "toShowImage--imgMsg.smallUrl==>${imgMsg.thumbUrl}")
            LogF.d(TAG, "toShowImage--imgMsg.id==>${imgMsg.id}")
        }
        WatchMessagePictureActivity.start(context, paths, showPosition)
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
            //下载成功  数据库更新消息表
        }
    }

}