package cn.zcgames.sdk.mqttsdk.contacts.event;

import cn.zcgames.sdk.mqttsdk.contacts.model.SortModel;

public class UpdateRemarkEvent {
    private SortModel contactModel;

    public UpdateRemarkEvent(SortModel contactModel) {
        this.contactModel = contactModel;
    }

    public SortModel getContactModel() {
        return contactModel;
    }

    public void setContactModel(SortModel contactModel) {
        this.contactModel = contactModel;
    }
}
