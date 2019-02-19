package cn.zcgames.sdk.mqttsdk.message.model;

import java.io.Serializable;
import java.util.List;

public class FriendBean implements Serializable {
    private List<ContactBean> add_friends;
    private List<ContactBean> contact;
    private List<ContactBean> new_friends;

    public List<ContactBean> getAdd_friends() {
        return add_friends;
    }

    public void setAdd_friends(List<ContactBean> add_friends) {
        this.add_friends = add_friends;
    }

    public List<ContactBean> getContact() {
        return contact;
    }

    public void setContact(List<ContactBean> contact) {
        this.contact = contact;
    }

    public List<ContactBean> getNew_friends() {
        return new_friends;
    }

    public void setNew_friends(List<ContactBean> new_friends) {
        this.new_friends = new_friends;
    }

    public static class ContactBean {
        /**
         * id : 1091208779418050560
         * nickname : 游客
         * avatar :
         * secret : 123456
         * from : 1
         * remark :
         * created : 1549005467209555820
         * status : 1
         */

        private String id;
        private String nickname;
        private String avatar;
        private String secret;
        private int from;
        private String remark;
        private long created;
        private int status;

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

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }

        public int getFrom() {
            return from;
        }

        public void setFrom(int from) {
            this.from = from;
        }

        public String getRemark() {
            return remark;
        }

        public void setRemark(String remark) {
            this.remark = remark;
        }

        public long getCreated() {
            return created;
        }

        public void setCreated(long created) {
            this.created = created;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }
    }
}
