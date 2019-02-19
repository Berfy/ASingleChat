package cn.berfy.sdk.http.http.interceptor;

import android.content.Context;

import java.io.IOException;
import java.util.List;

import cn.berfy.sdk.http.callback.OnStatusListener;
import cn.berfy.sdk.http.config.Constant;
import cn.berfy.sdk.http.http.okhttp.utils.HLogF;
import cn.berfy.sdk.http.http.okhttp.utils.SharedPreferenceUtils;
import okhttp3.Interceptor;
import okhttp3.Response;

/**
 * 接收并保存请求中的返回Cookies
 * Created by Rothschild on 2016-09-07.
 */
public class ReceivedCookiesInterceptor implements Interceptor {

    private String TAG = "ReceivedCookies";
    private Context mContext;
    private OnStatusListener mOnStatusListener;

    public ReceivedCookiesInterceptor(Context context, String tag, OnStatusListener onStatusListener) {
        TAG = tag;
        mContext = context;
        mOnStatusListener = onStatusListener;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {

        Response response = chain.proceed(chain.request());

        if (!response.headers("Set-Cookie").isEmpty()) {
            final StringBuffer sb = new StringBuffer();
            List<String> headers = response.headers("Set-Cookie");
            for (String s : headers) {
                String[] cookArray = s.split(";");
                String cookie = cookArray[0];
                sb.append(cookie).append(";");
            }
            if (null != mOnStatusListener) {
                mOnStatusListener.receiveSetCookie(sb.toString());
            }
            SharedPreferenceUtils.put(mContext, Constant.COOKIE, sb.toString());

            if (Constant.DEBUG)
                HLogF.d(TAG, "接受到的cookie---" + sb.toString());
        }
        return response;
    }
}
