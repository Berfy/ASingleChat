package cn.berfy.sdk.http.model;

/**
 * Created by Berfy on 2017/12/15.
 * http请求集合类
 * 所有借口返回这个类
 * NetMessage 包含 code msg 等等
 * data data封装类
 * isOK判断业务是否成功
 */

public class NetResponse<T> {

    public NetMessage netMessage;
    public T data;
    public int statusCode;
    public long usedTime;//ms

    public boolean isOk() {
        if (null != netMessage && netMessage.code == 1) {
            return true;
        }
        return false;
    }

}
