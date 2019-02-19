package cn.berfy.sdk.http.http.okhttp.builder;

import cn.berfy.sdk.http.http.okhttp.OkHttpUtils;
import cn.berfy.sdk.http.http.okhttp.request.OtherRequest;
import cn.berfy.sdk.http.http.okhttp.request.RequestCall;

/**
 * Created by zhy on 16/3/2.
 */
public class HeadBuilder extends GetBuilder
{
    @Override
    public RequestCall build()
    {
        return new OtherRequest(null, null, OkHttpUtils.METHOD.HEAD, url, tag, params, headers,id).build();
    }
}
