package cn.berfy.sdk.mvpbase.model;

/**
 * Created by Berfy on 2017/11/21.
 * 通知管理bean
 */

public class Notify {

    private String tag;//notifycation TAG
    private int id;//notifycation ID

    public Notify(String tag, int id) {
        this.tag = tag;
        this.id = id;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
