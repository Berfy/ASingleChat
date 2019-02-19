package cn.berfy.sdk.http.http.okhttp.request;

import java.io.File;
import java.util.Map;

import cn.berfy.sdk.http.callback.Callback;
import cn.berfy.sdk.http.callback.UIProgressRequestListener;
import cn.berfy.sdk.http.http.okhttp.utils.Exceptions;
import cn.berfy.sdk.http.http.okhttp.utils.HLogF;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * Created by zhy on 15/12/14.
 */
public class PostFileRequest extends OkHttpRequest {
    private static MediaType MEDIA_TYPE_STREAM = MediaType.parse("application/octet-stream");
    public static final String TAG = "UploadFile";
    private File file;
    private MediaType mediaType;

    public PostFileRequest(String url, Object tag, Map<String, String> params, Map<String, String> headers, File file, MediaType mediaType, int id) {
        super(url, tag, params, headers, id);
        this.file = file;
        this.mediaType = mediaType;

        if (this.file == null) {
            Exceptions.illegalArgument("the file can not be null !");
        }
        if (this.mediaType == null) {
            this.mediaType = MEDIA_TYPE_STREAM;
        }
    }

    @Override
    protected RequestBody buildRequestBody() {
        return ProgressRequestBody.create(mediaType, file);
    }

    @Override
    protected RequestBody wrapRequestBody(RequestBody requestBody, final Callback callback) {
        if (callback == null) {
            HLogF.d(TAG, "--------callback为空--------");
            return requestBody;
        }
        return new ProgressRequestBody(requestBody, new UIProgressRequestListener() {
            @Override
            public void onUIRequestProgress(long bytesWrite, long contentLength, boolean isDone) {
                HLogF.d(TAG, "wrapRequestBody--当前线程为==>" + Thread.currentThread());
                callback.inProgress(bytesWrite * 1.0f / contentLength, contentLength, isDone, id);
            }
        });
    }

    @Override
    protected Request buildRequest(RequestBody requestBody) {
        return builder.post(requestBody).build();
    }
}
