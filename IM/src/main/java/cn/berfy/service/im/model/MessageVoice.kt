package cn.berfy.service.im.model

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.AnimationDrawable
import android.text.TextUtils
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import cn.berfy.sdk.mvpbase.util.*
import cn.berfy.service.download.DownloadListener
import cn.berfy.service.download.DownloadManager
import cn.berfy.service.im.R
import cn.berfy.service.im.manager.i.OnMessageDownloadCallback
import java.io.File
import java.io.FileInputStream
import java.util.*

/**
 * author: Berfy
 * date: 2018/12/25
 * 语音消息
 */
class MessageVoice : Message() {
    var md5: String = ""//md5校验码
    var voiceUrl: String = ""//云端url
    var localPath: String = ""//本地录音地址
    var duration: Long = 0L//录音时长 毫秒
    var fileLength: Long = 0L//文件大小 字节
    var ext: String = ""//扩展名
    var mDownloadManager: DownloadManager? = null//文件下载管理器
    val VOICE_FILE = 200//语音下载标识
    var paths = ArrayList<String>()

    init {
        TAG = "IMLogTag_语音消息"
        type = MessageContentType.TYPE_VOICE
        mDownloadManager = DownloadManager.getInstance()
    }

    override fun getSummaryText(): String {
        return "[语音]"
    }

    @SuppressLint("SetTextI18n")
    override fun showMessage(viewHolder: ChatViewHolder, context: Context) {
        LogF.d(TAG, "showMessage---voiceUrl==>$voiceUrl")
        if (!isExists() && !TextUtils.isEmpty(voiceUrl)) {
            downloadVoice(viewHolder)
            LogF.d(TAG, "showMessage---downloadVoice==>$voiceUrl")
        }
        updateUI(context, viewHolder)
        showStatus(viewHolder, context)
    }

    private fun updateUI(context: Context, viewHolder: ChatViewHolder) {
        val linearLayout = LinearLayout(context)
        linearLayout.orientation = LinearLayout.HORIZONTAL
        linearLayout.gravity = Gravity.CENTER
        val voiceIcon = ImageView(context)
        val voiceBg = if (isSelf()) R.drawable.im_audio_animation_list_right else R.drawable.im_audio_animation_list_left
        voiceIcon.background = CommonUtil.getDrawable(context, voiceBg)
        val frameAnimation = voiceIcon.background as AnimationDrawable
        val tv = TextView(context)
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
        val time = duration
        tv.text = "${duration}s"
        val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        val imageLp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        if (isSelf()) {
            lp.setMargins(0, 0, DeviceUtils.dpToPx(context, (if (time < 10) 10 else time).toFloat()), 0)
            tv.setTextColor(CommonUtil.getColor(context, R.color.color_B85488))
            tv.layoutParams = lp
            linearLayout.addView(tv)
            voiceIcon.layoutParams = imageLp
            linearLayout.addView(voiceIcon)
            getBubbleView(viewHolder).setPadding(DeviceUtils.dpToPx(context, 15f),
                    DeviceUtils.dpToPx(context, 10f),
                    DeviceUtils.dpToPx(context, 20f),
                    DeviceUtils.dpToPx(context, 10f))
        } else {
            voiceIcon.layoutParams = imageLp
            tv.setTextColor(CommonUtil.getColor(context, R.color.color_181927))
            linearLayout.addView(voiceIcon)
            lp.setMargins(DeviceUtils.dpToPx(context, (if (time < 10) 10 else time).toFloat()), 0, 0, 0)
            tv.layoutParams = lp
            linearLayout.addView(tv)
            getBubbleView(viewHolder).setPadding(DeviceUtils.dpToPx(context, 20f),
                    DeviceUtils.dpToPx(context, 10f),
                    DeviceUtils.dpToPx(context, 15f),
                    DeviceUtils.dpToPx(context, 10f))
        }
        clearView(viewHolder)
        getBubbleView(viewHolder).addView(linearLayout)
        setPlayAudio(viewHolder, frameAnimation)
    }

    //语音文件没有存储到本地的通过url将文件存储到本地
    private fun setPlayAudio(viewHolder: ChatViewHolder, frameAnimation: AnimationDrawable) {
        getBubbleView(viewHolder).setOnClickListener {
            if (isExists()) {
                playAudio(frameAnimation)
            } else {
                if (!TextUtils.isEmpty(voiceUrl)) downloadVoice(viewHolder)
            }
        }
    }

    private fun isExists(): Boolean {
        if (FileUtils.exists(localPath)) return true
        val filePath = getNewPath()
        if (FileUtils.exists(filePath)) {
            localPath = filePath
            return true
        }
        return false

    }

    private fun getNewPath(): String {
        val path = FileUtils.getCacheRawFilePath(id)
        LogF.d(TAG, "getCacheRawFilePath===>$path")
        return "$path/$id.aac"
    }

    /**
     * ********************************* 下载 ****************************************
     */
    private fun downloadVoice(viewHolder: ChatViewHolder) {
        val path = getNewPath()
        if (!paths.contains(path)) {
            paths.add(path)
            needDownload(viewHolder, voiceUrl, path, id)
        }
    }

    private fun needDownload(viewHolder: ChatViewHolder, fileUrl: String, localPath: String, fileId: String) {
        LogF.d(TAG, "语音文件的 url==>$fileUrl")//下载路径
        mDownloadManager!!.add(fileUrl, localPath, VOICE_FILE, fileId, object : DownloadListener {
            override fun onStart(url: String) {
                showDownloadStatus(viewHolder, 0)
            }

            override fun onError(errMsg: String) {
                onDownloadFailed(localPath, errMsg)
                showDownloadStatus(viewHolder, 2)
                LogF.d(TAG, "下载失败的localPath-->$localPath")
            }

            override fun onFinished(url: String, localPath: String) {
                showDownloadStatus(viewHolder, 1)
                onDownloadSuccess(localPath)
            }

            override fun onProgress(progress: Float) {
                val pro = getPro(progress)
                LogF.d(TAG, "pro==>$pro")
            }

            override fun onPause() {}

            override fun onCancel() {
                FileUtils.deleteFile(localPath)
                paths.remove(localPath)
            }
        })
        mDownloadManager!!.download(fileUrl)
    }

    private fun onDownloadSuccess(path: String) {
        LogF.d(TAG, "onDownloadSuccess->path$path")
        if (TextUtils.isEmpty(path)) {

            onDownloadFailed(path, "本地路径不存在")
            LogF.d(TAG, "onDownloadSuccess-->本地路径不存在")
            return
        }
        localPath = path
    }

    private fun onDownloadFailed(path: String, errMsg: String) {
        //将下载失败的文件路径从路径路径集合中移除,使其能重新下载
        paths.remove(path)
        ToastUtil.getInstances().showShort(errMsg)
    }

    //获取文件的下载进度
    private fun getPro(pro: Float): Int {
        val progressF = pro * 100
        val progressI = Math.floor(progressF.toDouble()).toInt()
        LogF.d(TAG, "progressI==>$progressI")
        return progressI
    }

    /**
     * ********************************* 播放 ****************************************
     */
    private fun playAudio(frameAnimation: AnimationDrawable) {
        try {
            val file = File(localPath)
            val fis = FileInputStream(file)
            MediaUtil.getInstance().play(fis)
            frameAnimation.start()
            LogF.d(TAG, "播放语音")
            MediaUtil.getInstance().setEventListener(object : MediaUtil.EventListener {
                override fun onStart() {}

                override fun onFinish() {//语音正常结束播放才恢复其他语音的点击
                    LogF.d(TAG, "结束播放语音")
                    frameAnimation.stop()
                    frameAnimation.selectDrawable(0)
                }

                override fun onTerminal() {//另一个语音消息被播放 移除这个消息的监听
                    LogF.d(TAG, "被中断播放语音")
                    frameAnimation.stop()
                    frameAnimation.selectDrawable(0)
                }
            })
        } catch (e: Exception) {
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
}