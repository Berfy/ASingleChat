package cn.berfy.sdk.http.http.okhttp.log;

import android.text.TextUtils;
import android.util.Log;

import cn.berfy.sdk.http.http.okhttp.utils.HLogF;

import java.io.IOException;

import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;

/**
 * Created by zhy on 16/3/1.
 */
public class LoggerInterceptor implements Interceptor {
    public static final String TAG = "OkHttpUtils";
    private String tag;
    private boolean showResponse;

    public LoggerInterceptor(String tag, boolean showResponse) {
        if (TextUtils.isEmpty(tag)) {
            tag = TAG;
        }
        this.showResponse = showResponse;
        this.tag = tag;
    }

    public LoggerInterceptor(String tag) {
        this(tag, true);
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        logForRequest(request);
        Response response = chain.proceed(request);
        return logForResponse(request, response);
    }

    private Response logForResponse(Request request, Response response) {
        try {
            //===>response log
            HLogF.d(tag, "========response'log=======start");
            Response.Builder builder = response.newBuilder();
            Response clone = builder.build();
            HLogF.d(tag, "耗时 : " + (System.currentTimeMillis() - Long.valueOf(request.tag() + "")) + "ms");
            HLogF.d(tag, "method : " + clone.request().method());
            HLogF.d(tag, "url : " + clone.request().url());
            HLogF.d(tag, "code : " + clone.code());
            HLogF.d(tag, "protocol : " + clone.protocol());
            if (!TextUtils.isEmpty(clone.message()))
                HLogF.d(tag, "message : " + clone.message());

            if (showResponse) {
                ResponseBody body = clone.body();
                if (body != null) {
                    MediaType mediaType = body.contentType();
                    if (mediaType != null) {
                        HLogF.d(tag, "responseBody's contentType : " + mediaType.toString());
                        if (isText(mediaType)) {
                            String resp = body.string();
                            String newResult = new String(resp);
                            HLogF.d(tag, "responseBody's content : " + resp);
                            body = ResponseBody.create(mediaType, newResult);
                            response = response.newBuilder().body(body).build();
                        } else {
                            HLogF.d(tag, "responseBody's content : " + " maybe [file part] , too large too print , ignored!");
                        }
                    }
                }
            }
            HLogF.d(tag, "========response'log=======end");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return response;
    }

    private void logForRequest(Request request) {
        try {
            String url = request.url().toString();
            Headers headers = request.headers();
            HLogF.d(tag, "========request'log=======start");
            HLogF.d(tag, "method : " + request.method());
            HLogF.d(tag, "url : " + url);
            if (headers != null && headers.size() > 0) {
                HLogF.d(tag, "headers : " + headers.toString());
            }
            RequestBody requestBody = request.body();
            if (requestBody != null) {
                MediaType mediaType = requestBody.contentType();
                HLogF.d(tag, "requestBody's contentLength : " + requestBody.contentLength());
                if (mediaType != null) {
                    HLogF.d(tag, "requestBody's contentType : " + mediaType.toString());
                    if (isText(mediaType)) {
                        HLogF.d(tag, "requestBody's content : " + bodyToString(request));
                    } else {
                        HLogF.d(tag, "requestBody's content : " + " maybe [file part] , too large too print , ignored!");
                    }
                }
            }
            HLogF.d(tag, "========request'log=======end");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isText(MediaType mediaType) {
        if (mediaType.type() != null && mediaType.type().equals("text") || mediaType.type().equals("application")) {
            return true;
        }
        if (mediaType.subtype() != null) {
            if (mediaType.subtype().equals("json") ||
                    mediaType.subtype().equals("xml") ||
                    mediaType.subtype().equals("html") ||
                    mediaType.subtype().equals("webviewhtml")
                    )
                return true;
        }
        return false;
    }

    private String bodyToString(final Request request) {
        try {
            final Buffer buffer = new Buffer();
            request.body().writeTo(buffer);
            return buffer.readUtf8();
        } catch (final IOException e) {
            return "something error when show requestBody.";
        }
    }
}
