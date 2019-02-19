package cn.berfy.sdk.http.http.okhttp.response;

import android.support.annotation.Nullable;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;
import okio.Source;

/**
 * 包装响应体,处理进度
 *
 * @author NorthStar
 * @date 2019/1/15 14:14
 */
public class ProgressResponseBody extends ResponseBody {

    //实际的待包装响应体
    private final ResponseBody mResponseBody;

    //下载进度接口
    private final ProgressResponseListener mProgressListener;

    //包装完成的BufferedSource:在内部保留缓冲区的源，以便调用者可以在没有性能的情况下进行小的读取
    private BufferedSource mBufferedSource;

    /**
     * 构造参数 赋值传递
     *
     * @param responseBody     待包装的响应体
     * @param progressListener 进度回调
     */
    public ProgressResponseBody(ResponseBody responseBody, ProgressResponseListener progressListener) {
        mResponseBody = responseBody;
        mProgressListener = progressListener;
    }

    @Nullable
    @Override//重写响应体的contentType
    public MediaType contentType() {
        return mResponseBody.contentType();
    }

    @Override//重写调用实际的响应体的contentLength
    public long contentLength() {
        return mResponseBody.contentLength();
    }

    @Override//重写进行包装source
    public BufferedSource source() {
        //包装bufferedSource
        if (mBufferedSource == null) mBufferedSource = Okio.buffer(mResponseBody.source());
        return mBufferedSource;
    }

    //读取回调进度
    private Source mSource(Source source) {
        return new ForwardingSource(source) {
            //当前读取字节数
            long totalBytesRead = 0;

            @Override
            public long read(Buffer sink, long byteCount) throws IOException {
                long bytesRead = super.read(sink, byteCount);
                //增加当前读取的字节数,如果读完了bytesRead会返回-1
                totalBytesRead += bytesRead != -1 ? bytesRead : 0;
                //进度回调 如果contentLength()不知道长度,会返回-1;
                boolean done = bytesRead == -1;
                mProgressListener.onResponseProgress(totalBytesRead, mResponseBody.contentLength(), done);
                return bytesRead;
            }
        };
    }

    public interface ProgressResponseListener {

        /**
         * @param bytesRead      当前读取字节长度
         * @param contentLength  总字节长度
         * @param done          是否读取完成
         */
        void onResponseProgress(long bytesRead, long contentLength, boolean done);
    }
}
