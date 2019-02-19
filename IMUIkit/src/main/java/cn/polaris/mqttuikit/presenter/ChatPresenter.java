package cn.polaris.mqttuikit.presenter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import cn.berfy.sdk.mvpbase.util.LogF;
import cn.berfy.sdk.mvpbase.util.ToastUtil;
import cn.berfy.service.im.manager.i.OnMessageSendingCallback;
import cn.berfy.service.im.model.Message;
import cn.berfy.service.im.model.MessageType;
import cn.berfy.service.im.model.conversation.Conversation;
import cn.polaris.mqttuikit.iview.IChatView;

/**
 * 聊天界面逻辑
 */
public class ChatPresenter {

    private Context mContext;
    private IChatView mIChatView;
    private boolean isGettingMessage = false;
    private final int LAST_MESSAGE_NUM = 20;
    private static final String TAG = "ChatFragment";

    public ChatPresenter(Context context, IChatView IChatView) {
        mContext = context;
        mIChatView = IChatView;
    }

    /**
     * 加载页面逻辑
     */
    public void start() {
        //注册消息监听
    }

    /**
     * 中止页面逻辑
     */
    public void stop() {
        //注销消息监听
        LogF.d(TAG, "");
    }


    /**
     * 发送消息
     * val STATUS_SEND = 0 //发送中
     * val STATUS_SEND_SUC = 1//已发送
     * val STATUS_SEND_FAILED = 2//发送失败
     *
     * @param message 要发送的消息
     */
    public void sendMessage(String toId, Message message, MessageType msgType) {
        if (TextUtils.isEmpty(toId)) {
            ToastUtil.getInstances().showShort("未设置toId");
            return;
        }
        mIChatView.checkMqTtState();
        if (msgType == null) {
            ToastUtil.getInstances().showShort("未设置消息的会话类型");
            return;
        }
        LogF.d(TAG, "msgType==>" + msgType + " ,toId==>" + toId);
        Conversation conversation = new Conversation(toId, msgType);

        conversation.sendMessage(message, new OnMessageSendingCallback() {
            @Override
            public void onStart(@NonNull Message msg) {
                mIChatView.onSendStart(msg);
            }

            @Override
            public void uploadProgress(float pro, boolean isDone) {
                mIChatView.uploadProgress(pro, isDone);
            }

            @Override
            public void onSuc(@NonNull Message message) {
                mIChatView.onSendMessageSuccess(message);
            }

            @Override
            public void onFailed(@NonNull String errMsg) {
                mIChatView.onSendMessageFail(-1, errMsg, message);
            }
        });
    }


    /**
     * 获取消息
     *
     * @param message 最后一条消息
     */
    public void getMessage(@Nullable Message message) {
        if (!isGettingMessage) {
            isGettingMessage = true;
        }
    }

    /**
     * 设置会话为已读
     */
    public void readMessages() {
    }


    /**
     * 保存草稿
     *
     * @param message 消息数据
     */
    public void saveDraft(String message) {
        LogF.d(TAG, "saveDraft==>" + message);
    }

}
