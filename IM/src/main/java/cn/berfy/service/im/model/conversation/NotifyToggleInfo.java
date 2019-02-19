package cn.berfy.service.im.model.conversation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 用于存储免打扰通知开关设置
 *
 * @author NorthStar
 * @date  2019/2/11 14:11
 */
public class NotifyToggleInfo implements Serializable {

    private List<String> notNotifyIds;//所有被标记为免打扰的会话Id

    //用于存放标记了免打扰信息的toId
    public List<String> getNotNotifyIds() {
        if (notNotifyIds == null) {
            return new ArrayList<>();
        }
        return notNotifyIds;
    }

    public void setNotNotifyIds(List<String> notNotifyIds) {
        this.notNotifyIds = notNotifyIds;
    }
}
