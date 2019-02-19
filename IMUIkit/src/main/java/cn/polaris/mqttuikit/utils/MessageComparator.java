package cn.polaris.mqttuikit.utils;

import java.util.Comparator;

import cn.berfy.sdk.mvpbase.util.LogF;
import cn.berfy.service.im.model.Message;

/**
 * 会话中消息列表排序器
 *
 * @author NorthStar
 * @date 2018/2/27 15:07
 */
public class MessageComparator implements Comparator<Message> {
    public static final String TAG="MessageComparator";
    @Override
    public int compare(Message lastMessage1, Message lastMessage2) {
        int result = 0;
        long msgTime1 = lastMessage1.getTime();
        long msgTime2 = lastMessage2.getTime();
        LogF.d(TAG, "msgTime1==>" + lastMessage1.getTime()+"msgTime2==>" + lastMessage2.getTime());
        result = msgTime1 < msgTime2 ? -1 : 1;
        LogF.d("IM消息", "result==>" + result);
        return result;
    }
}
