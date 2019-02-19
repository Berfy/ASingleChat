package cn.polaris.mqttuikit.iview;

import cn.berfy.service.im.model.Message;

/**
 * 消息扩展点击事件监听
 * 重发
 */
public interface OnMessageEventListener {
    void reSendMessage(int position);

    //头像的点击事件
    void avatarClick(Message msg);
}
