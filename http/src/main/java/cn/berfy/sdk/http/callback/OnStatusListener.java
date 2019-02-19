package cn.berfy.sdk.http.callback;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import cn.berfy.sdk.http.model.HttpParams;

/**
 * Created by Berfy on 2017/12/19.
 * http状态监听
 * （错误状态码监听、参数处理适配各种加密）
 */
public interface OnStatusListener {

    void statusCodeError(int code, long usedTime);//针对非200-300的状态处理

    void receiveSetCookie(String cookie);

    HttpParams addCookies();

    HttpParams addParamsOrHeaders(HttpParams rawParams);//参数 Header处理


}
