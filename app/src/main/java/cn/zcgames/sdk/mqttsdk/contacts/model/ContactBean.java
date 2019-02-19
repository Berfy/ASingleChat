package cn.zcgames.sdk.mqttsdk.contacts.model;

import java.io.Serializable;
import java.util.List;

public class ContactBean implements Serializable {
    private List<SortModel> contact;
    private List<SortModel> players;

    public List<SortModel> getContact() {
        return contact;
    }

    public void setContact(List<SortModel> contact) {
        this.contact = contact;
    }

    public List<SortModel> getPlayers() {
        return players;
    }

    public void setPlayers(List<SortModel> players) {
        this.players = players;
    }
}
