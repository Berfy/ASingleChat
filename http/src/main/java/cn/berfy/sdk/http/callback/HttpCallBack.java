package cn.berfy.sdk.http.callback;

import cn.berfy.sdk.http.model.NetResponse;

/**
 * Created by Berfy on 2017/12/15.
 * http接口回调
 */

public interface HttpCallBack extends RequestCallBack<String> {
    void onFinish(NetResponse<String> response);
}
