package cn.berfy.sdk.mvpbase.model;

import java.util.List;

public class User {


    /**
     * player : {"id":"1089837949689798656","merchid":"1234","externalid":"12341","nickname":"游客","realname":"游客","sex":"0","avatar":"","status":"0"}
     */

    private PlayerBean player;

    public PlayerBean getPlayer() {
        return player;
    }

    public void setPlayer(PlayerBean player) {
        this.player = player;
    }

    public static class PlayerBean {
        /**
         * id : 1089837949689798656
         * merchid : 1234
         * externalid : 12341
         * nickname : 游客
         * realname : 游客
         * sex : 0
         * avatar :
         * status : 0
         */

        private String id;
        private String merchid;
        private String externalid;
        private String nickname;
        private String realname;
        private String sex;
        private String avatar;
        private String status;
        private String qrcode;

        public String getId() {
            return id == null ? "" : id;
        }

        public void setId(String id) {
            this.id = id;
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

        public String getNickname() {
            return nickname;
        }

        public void setNickname(String nickname) {
            this.nickname = nickname;
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

        public String getAvatar() {
            return avatar;
        }

        public void setAvatar(String avatar) {
            this.avatar = avatar;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getQrcode() {
            return qrcode;
        }

        public void setQrcode(String qrcode) {
            this.qrcode = qrcode;
        }
    }
}
