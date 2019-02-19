package cn.berfy.service.im.manager.i

import cn.berfy.service.im.model.Message
import io.reactivex.annotations.Nullable

interface OnMessageSendingCallback {
    fun onStart(@Nullable message: Message)
    fun uploadProgress(pro: Float,isDone:Boolean)//0-1 上传文件用 ,isDone 是否完成
    fun onSuc(message: Message)
    fun onFailed(errMsg: String)
}