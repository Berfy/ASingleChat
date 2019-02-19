package cn.zcgames.sdk.mqttsdk.message.view.model;

import java.io.Serializable;

import cn.zcgames.sdk.mqttsdk.message.model.FriendBean;

public class FriendPassEvent implements Serializable {
    private boolean isRefresh;
    private FriendBean.ContactBean person;

    public FriendPassEvent(boolean flush, FriendBean.ContactBean info){
        this.isRefresh = flush;
        this.person = info;
    }

    public boolean isRefresh() {
        return isRefresh;
    }

    public void setRefresh(boolean refresh) {
        isRefresh = refresh;
    }

    public FriendBean.ContactBean getPerson() {
        return person;
    }

    public void setPerson(FriendBean.ContactBean person) {
        this.person = person;
    }
}
