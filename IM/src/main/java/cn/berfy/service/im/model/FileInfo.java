package cn.berfy.service.im.model;

public class FileInfo {

    private int code;
    private PayLoad data;

    public PayLoad getData() {
        return data;
    }

    public int getCode() {
        return code;
    }

    public static class PayLoad {
        /**
         * url : http://223.203.221.89:9080/imgs/201901291138293133.jpg
         */

        private String url;
        private String small;

        public String getUrl() {
            return url == null ? "" : url;
        }

        public String getSmall() {
            return small == null ? "" : small;
        }
    }
}
