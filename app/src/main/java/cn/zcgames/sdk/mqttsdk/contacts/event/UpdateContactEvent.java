package cn.zcgames.sdk.mqttsdk.contacts.event;

public class UpdateContactEvent {
    private boolean isRefresh;

    public UpdateContactEvent(boolean isRefresh) {
        this.isRefresh = isRefresh;
    }

    public boolean isRefresh() {
        return isRefresh;
    }

    public void setRefresh(boolean refresh) {
        isRefresh = refresh;
    }
}
