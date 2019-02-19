package cn.berfy.sdk.http.http.okhttp.request;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.ForwardingSink;
import okio.Okio;
import okio.Sink;

/**
 * Decorates an OkHttp request body to count the number of bytes written when writing it. Can
 * decorate any request body, but is most useful for tracking the upload progress of large
 * multipart requests.
 *
 * @author NorthStar
 * @date 2019/1/15 13:34
 */
public class ProgressRequestBody extends RequestBody {

    //实际的待包装请求体
    private final RequestBody mRequestBody;
    //进度回调接口
    private final ProgressRequestListener mProgressListener;
    //包装完成的BufferedSink:一种接收器，它在内部保留一个缓冲区，以便调用者可以执行小的写操作
    private BufferedSink mBufferedSink;

    /**
     * 构造函数 赋值
     *
     * @param requestBody      待包装的请求体
     * @param progressListener 回调接口
     */
    public ProgressRequestBody(RequestBody requestBody, ProgressRequestListener progressListener) {
        mRequestBody = requestBody;
        mProgressListener = progressListener;
    }

    @Nullable
    @Override//重写调用实际的响应体contentType
    public MediaType contentType() {
        return mRequestBody.contentType();
    }

    @Override//重写调用实际的响应体的contentLength
    public long contentLength() {
        try {
            return mRequestBody.contentLength();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }

    @Override//重写进行写入
    public void writeTo(@NonNull BufferedSink sink) throws IOException {
        //返回缓冲区写入{@code sink}的新接收器。
        if (mBufferedSink == null) mBufferedSink = Okio.buffer(getSink(sink));
        //将此请求的内容写入{@code sink}
        mRequestBody.writeTo(mBufferedSink);
        //必须调用flush,否则最后一部分数据可能不会被写入
        mBufferedSink.flush();//刷新，将数据尽可能推向最终目的地。通常目标是一个网络套接字或文件。
    }

    private Sink getSink(Sink sink) {
        return new ForwardingSink(sink) {
            //当前写入字节数
            long bytesWritten = 0L;
            //总字节长度,避免多次调用contentLength()方法
            long contentLength = 0L;

            @Override
            public void write(@NonNull Buffer source, long byteCount) throws IOException {
                super.write(source, byteCount);
                if (contentLength == 0) {
                    //获得contentLength的值,后续不再调用
                    contentLength = contentLength();
                }

                //增加当前写入的字节数
                bytesWritten += byteCount;
                boolean done = bytesWritten == contentLength;//写入字节长度==内容字节长度
                //进度回调
                mProgressListener.onRequestProgress(bytesWritten, contentLength, done);
            }
        };
    }

    public interface ProgressRequestListener {
        /**
         * @param bytesWritten  当前写入字节长度
         * @param contentLength 总字节长度
         * @param done          是否完成写入
         */
        void onRequestProgress(long bytesWritten, long contentLength, boolean done);
    }

}
