package cn.berfy.sdk.http.http.interceptor;

import android.content.Context;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * UserAgent
 */
public class UserAgentInterceptor implements Interceptor {
    private String TAG = "UserAgentInterceptor";
    private static final String USER_AGENT_HEADER_NAME = "User-Agent";
    private final String userAgentHeaderValue;
    private Context mContext;

    public UserAgentInterceptor(Context context, String userAgentHeaderValue, String tag) {
        mContext = context;
        TAG = tag;
        this.userAgentHeaderValue = userAgentHeaderValue;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        final Request originalRequest = chain.request();
        final Request requestWithUserAgent = originalRequest.newBuilder()
//                .removeHeader(USER_AGENT_HEADER_NAME)
//                .addHeader(USER_AGENT_HEADER_NAME, userAgentHeaderValue)
//                .addHeader("version", AppUtils.getVersionName(mContext))
//                .addHeader("version", "5.0.0")
//                .addHeader("Language", getLanguage())
//                .addHeader("Channel", AppUtils.getChannel(mContext))
//                .addHeader("Latitude", Gloabal.latitude)
//                .addHeader("Longitude", Gloabal.longitude)
//                .addHeader("ServerStatus", "test")//TODO 正式发版注释掉该行
                .build();
//        LogF.d(TAG, requestWithUserAgent.headers().toString());
//        LogF.d(TAG, new Gson().toJson(requestWithUserAgent.url()) + "   header= " + new Gson().toJson(requestWithUserAgent.headers()));
        return chain.proceed(requestWithUserAgent);
    }

//    private String getLanguage() {
//        String la = DeviceUtils.getLanguage(mContext);
//        if ("zh".equals(la)) {
//            if ("CN".equals(DeviceUtils.getCountry(mContext))) {
//                la = "zh-Hans-CN";
//            } else {
//                la = "zh-Hant-CN";
//            }
//        }
//        switch (la) {
//            case "en":
//                la = "en-CN";
//                break;
//            case "ko":
//                la = "ko-CN";
//                break;
//            case "th":
//                la = "th-CN";
//                break;
//            case "ja":
//                la = "ja-CN";
//                break;
//        }
//        LogF.d(TAG, "lan==>" + la);
//
//        return la;
//    }


}
