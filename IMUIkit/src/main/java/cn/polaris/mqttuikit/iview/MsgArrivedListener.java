package cn.polaris.mqttuikit.iview;

import cn.berfy.service.im.model.Message;

/**
 * 接受推送消息的回调
 *
 * @author NorthStar
 * @date 2018/9/29 10:57
 */
public interface MsgArrivedListener {
    /**
     * @param message     推送消息实体
     */
    void receiveMessage(Message message);
}
