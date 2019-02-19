package cn.berfy.sdk.http.http.interceptor;

import android.content.Context;
import android.text.TextUtils;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import cn.berfy.sdk.http.callback.OnStatusListener;
import cn.berfy.sdk.http.model.HttpParams;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 从本地取出保存的Cookie
 * Created by Rothschild on 2016-09-07.
 */
public class AddCookiesInterceptor implements Interceptor {

    private static final String TAG = "AddCookiesInterceptor";
    private Context mContext;
    private OnStatusListener mOnStatusListener;

    public AddCookiesInterceptor(Context context, OnStatusListener onStatusListener) {
        mContext = context;
        mOnStatusListener = onStatusListener;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {

        final Request.Builder builder = chain.request().newBuilder();

        if (null != mOnStatusListener) {
            HttpParams httpParams = mOnStatusListener.addCookies();
            if (null != httpParams) {
                Iterator<Map.Entry<String, Object>> headers = httpParams.getHeaders().entrySet().iterator();
                while (headers.hasNext()) {
                    Map.Entry<String, Object> entry = headers.next();
                    if (!TextUtils.isEmpty(entry.getKey()) && !TextUtils.isEmpty(entry.getValue().toString())) {
                        builder.addHeader(entry.getKey().trim(), entry.getValue().toString());
                    }
                }
//                builder.addHeader(Constant.COOKIE, cookie);
            }
        }

//        String str = (String) SharedPreferenceUtils.get(mContext, Constant.COOKIE, "");
//        Observable.just(str)
//                .subscribe(cookie -> {
//                    builder.addHeader(Constant.COOKIE, cookie);
//                    LogF.d(Constant.HTTPTAG, "从SharedPreference中获取的Cookie---" + cookie);
//                });

        return chain.proceed(builder.build());
    }
}
