package cn.zcgames.sdk.mqttsdk.message.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

public class GroupBean implements Serializable {

    private List<ListBean> list;

    public List<ListBean> getList() {
        return list;
    }

    public void setList(List<ListBean> list) {
        this.list = list;
    }

    public static class ListBean {
        /**
         * group_id : 组id
         * admin_id : 创建者id
         * name : 组名
         * bulletin : 公告牌
         * private : 1:私有组，2:公开组
         * invite : 1:不能邀请，2:可以邀请
         * capacity : 最大成员数
         * created : 创建时间
         * group_type : 1:聊天室；2:群聊
         */

        private String group_id;
        private String admin_id;
        private String name;
        private String bulletin;
        @SerializedName("private")
        private String privateX;
        private String invite;
        private String capacity;
        private String created;
        private String group_type;
        private String topic;

        public String getGroup_id() {
            return group_id;
        }

        public void setGroup_id(String group_id) {
            this.group_id = group_id;
        }

        public String getAdmin_id() {
            return admin_id;
        }

        public void setAdmin_id(String admin_id) {
            this.admin_id = admin_id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getBulletin() {
            return bulletin;
        }

        public void setBulletin(String bulletin) {
            this.bulletin = bulletin;
        }

        public String getPrivateX() {
            return privateX;
        }

        public void setPrivateX(String privateX) {
            this.privateX = privateX;
        }

        public String getInvite() {
            return invite;
        }

        public void setInvite(String invite) {
            this.invite = invite;
        }

        public String getCapacity() {
            return capacity;
        }

        public void setCapacity(String capacity) {
            this.capacity = capacity;
        }

        public String getCreated() {
            return created;
        }

        public void setCreated(String created) {
            this.created = created;
        }

        public String getGroup_type() {
            return group_type;
        }

        public void setGroup_type(String group_type) {
            this.group_type = group_type;
        }

        public String getTopic() {
            return topic;
        }

        public void setTopic(String topic) {
            this.topic = topic;
        }
    }
}
