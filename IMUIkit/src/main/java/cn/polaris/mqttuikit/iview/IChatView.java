package cn.polaris.mqttuikit.iview;


import cn.berfy.service.im.model.Message;
import cn.polaris.mqttuikit.view.ChatInput;

/**
 * 聊天界面的接口
 */
public interface IChatView {
    /**
     * 发送照片消息
     */
    void sendPhoto();

    /**
     * 检查mqTt连接状态
     */
    void checkMqTtState();

    /**
     * 发送文字消息
     */
    void sendText();

    /**
     * 发送文件
     */
    void sendFile();

    /**
     * 发送语音消息
     */
    void sendVoice(long duration, String audioFilePath);


    /**
     * 发送小视频消息
     *
     * @param fileName 文件名
     */
    void sendVideo(String fileName);


    /**
     * 视频按钮点击事件
     */
    void videoAction();

    /**
     * 文件上传进度
     * @param pro    上传进度
     * @param isDone  是否传完
     */
    void uploadProgress(float pro,boolean isDone);

    /**
     * 开始发送消息
     *
     * @param message 返回的消息
     */
    void onSendStart(Message message);

    /**
     * 发送消息成功
     *
     * @param message 返回的消息
     */
    void onSendMessageSuccess(Message message);

    /**
     * 发送消息失败
     *
     * @param code    返回码
     * @param desc    返回描述
     * @param message 发送的消息
     */
    void onSendMessageFail(int code, String desc, Message message);


    void onInputModeChanged(ChatInput.InputMode inputMode);

}
