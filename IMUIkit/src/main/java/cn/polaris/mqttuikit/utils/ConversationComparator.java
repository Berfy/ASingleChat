package cn.polaris.mqttuikit.utils;

import java.util.Comparator;

import cn.berfy.sdk.mvpbase.util.LogF;
import cn.berfy.service.im.model.Message;
import cn.berfy.service.im.model.MessageType;
import cn.berfy.service.im.model.conversation.NormalConversation;

/**
 * 会话排序器
 *
 * @author NorthStar
 * @date 2018/2/27 15:07
 */
public class ConversationComparator implements Comparator<NormalConversation> {

    public static final String TAG = "ConversationComparator";

    @Override
    public int compare(NormalConversation o1, NormalConversation o2) {
        int result = 0;
        int conversationType = -1;//0:本地会话 1:线上会话
        Message lastMessage1 = o1.getLastMessage();
        Message lastMessage2 = o2.getLastMessage();
        //只有Comparator接口的compare方法返回正数，才会交换o1，o2的位置（o1的索引小于o2的索引），
        //如果不希望交换位置，则返回负数或0
        boolean msgType1 = MessageType.TYPE_CHATROOM == o1.getConversation().getType();
        boolean msgType2 = MessageType.TYPE_CHATROOM == o2.getConversation().getType();

        long lastMessageTime1 = o1.getConversation().getLastMessageTime();
        long lastMessageTime2 = o2.getConversation().getLastMessageTime();

        boolean hasMsgTime1 = lastMessage1 != null && lastMessage1.getTime() != 0;
        boolean hasMsgTime2 = lastMessage2 != null && lastMessage2.getTime() != 0;
        if (msgType1) {
            return -1;//聊天室会话置顶
        } else if (msgType2){
            return 1;//聊天室会话置顶
        } else if (hasMsgTime1 && hasMsgTime2) {
            long time1 = lastMessage1.getTime();
            long time2 = lastMessage2.getTime();
            conversationType = 1;
            result = time1 > time2 ? -1 : 1;//有Message,时间戳大的是新消息,排在前面
        } else if (hasMsgTime1) {
            return -1;//说明lastMessage1不是本地会话的历史数据,而lastMessage2是,本身1就在前面故不用交互位置
        } else if (hasMsgTime2) {
            return 1;//说明lastMessage2不是本地会话的历史数据,而lastMessage1是,所以2要排在1前面
        } else if (lastMessageTime1 != 0 && lastMessageTime2 != 0) {
            conversationType = 0;
            result = lastMessageTime1 > lastMessageTime2 ? -1 : 1;//这是历史会话,没有msg,只能对比本地发送时间
        } else if (lastMessageTime1 != 0) {
            return -1;
        } else if (lastMessageTime2 != 0) {
            return 1;
        }
        LogF.d("IM会话排序", "conversationType==>" + conversationType + " ,CompareResult==>" + result);
        return result;
    }
}
