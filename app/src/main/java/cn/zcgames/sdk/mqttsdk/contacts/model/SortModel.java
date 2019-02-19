package cn.zcgames.sdk.mqttsdk.contacts.model;

import java.io.Serializable;

public class SortModel implements Serializable{
    /**
     "id": "朋友id",
     "nickname": "昵称",
     "avatar": "头像地址",
     "secret": "加密字符串",
     "remark": "备注",
     "from": "1:主动添加，2:请求添加，3:朋友推荐",
     "created": "成为好友时间",
     "status": "1:朋友请求，2:成为朋友，3:朋友关系解除"

     "id":"1089837949689798656",
     "merchid":"1234",
     "externalid":"12341",
     "nickname":"游客",
     "realname":"游客",
     "sex":"0",
     "avatar":"",
     "status":"0"
     */
    private String id;
    private String nickname;
    private String avatar;
    private String remark;
    private String secret;
    private String from;
    private String status;
    private String merchid;
    private String externalid;
    private String realname;
    private String sex;
    private String created;
    private String letters;//显示拼音的首字母
    private boolean isSelect = false;//是否选择，本地字段

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMerchid() {
        return merchid;
    }

    public void setMerchid(String merchid) {
        this.merchid = merchid;
    }

    public String getExternalid() {
        return externalid;
    }

    public void setExternalid(String externalid) {
        this.externalid = externalid;
    }

    public String getRealname() {
        return realname;
    }

    public void setRealname(String realname) {
        this.realname = realname;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public String getLetters() {
        return letters;
    }

    public void setLetters(String letters) {
        this.letters = letters;
    }

    public boolean isSelect() {
        return isSelect;
    }

    public void setSelect(boolean select) {
        isSelect = select;
    }
}
