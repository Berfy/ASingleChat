package cn.zcgames.sdk.mqttsdk.contacts.event;

public class UpdateUserInfoEvent {
    private boolean isRefresh;

    public UpdateUserInfoEvent(boolean isRefresh) {
        this.isRefresh = isRefresh;
    }

    public boolean isRefresh() {
        return isRefresh;
    }

    public void setRefresh(boolean refresh) {
        isRefresh = refresh;
    }
}
