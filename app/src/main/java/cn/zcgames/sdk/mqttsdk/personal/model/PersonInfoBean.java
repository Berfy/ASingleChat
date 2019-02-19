package cn.zcgames.sdk.mqttsdk.personal.model;

import java.io.Serializable;

public class PersonInfoBean implements Serializable {

    /**
     * avatar :
     * nickname : 昵称1
     * qrcode : base64-encode
     * uid : 123
     */

    private String avatar;
    private String nickname;
    private String qrcode;
    private String uid;

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getQrcode() {
        return qrcode;
    }

    public void setQrcode(String qrcode) {
        this.qrcode = qrcode;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
