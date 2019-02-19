package cn.berfy.sdk.http.model;

/**
 * Created by Berfy on 2017/12/15.
 * http请求结果类
 */

public class NetMessage {

    public int code;

    public boolean isOk() {
        return code == 1;
    }

    public String msg;
    public String err_msg;
    public long ts = 0;
    public String requestCode;//原样回传
}
