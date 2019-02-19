package cn.berfy.sdk.http.callback;

/**
 * Created by Berfy on 2017/12/15.
 * http接口回调
 */

public interface RequestCallBackH5<T> extends RequestCallBack<T> {

    /**@param statusCode 服务器状态码
     * @param errCode 0没有错误 -1没有网络 -2网络超时*/
    void onErrorDetail(int statusCode, int errCode);
}
