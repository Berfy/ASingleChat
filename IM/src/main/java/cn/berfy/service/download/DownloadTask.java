package cn.berfy.service.download;

import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import cn.berfy.sdk.mvpbase.util.LogF;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * 下载任务
 *
 * @author NorthStar
 * @date 2018/7/30 10:23
 */
public class DownloadTask extends Handler {

    private final int THREAD_COUNT = 4;//线程数
    private FileInfo mFileInfo;
    private long mFileLength;

    private boolean isDownloading = false;
    private int childCancelCount;//子线程取消数量
    private int childPauseCount;//子线程暂停数量
    private int childFinishCount;//子线程完成数量
    private HttpUtil mHttpUtil;
    private long[] mProgress;
    private File[] mCacheFiles;
    private File mTmpFile;//临时占位文件
    private boolean pause;//是否暂停
    private boolean cancel;//是否取消下载

    private final int MSG_START = 0;//开始下载
    private final int MSG_PROGRESS = 1;//进度
    private final int MSG_FINISH = 2;//完成下载
    private final int MSG_PAUSE = 3;//暂停
    private final int MSG_CANCEL = 4;//取消
    private final int MSG_ERROR = 5;//下载出错
    private DownloadListener mListener;//下载回调监听

    /**
     * 任务管理器初始化数据
     *
     * @param info
     * @param listener
     */
    DownloadTask(FileInfo info, DownloadListener listener) {
        this.mFileInfo = info;
        this.mListener = listener;
        this.mProgress = new long[THREAD_COUNT];
        this.mCacheFiles = new File[THREAD_COUNT];
        this.mHttpUtil = HttpUtil.getInstance();
    }

    /**
     * 任务回调消息
     *
     * @param msg
     */
    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        if (null == mListener) {
            return;
        }
        switch (msg.what) {
            case MSG_START://开始下载
                mListener.onStart(mFileInfo.getUrl());
                break;
            case MSG_PROGRESS://下载进度
                long progress = 0;
                for (int i = 0, length = mProgress.length; i < length; i++) {
                    progress += mProgress[i];
                }
                mListener.onProgress(progress * 1.0f / mFileLength);
                break;
            case MSG_PAUSE://暂停下载
                childPauseCount++;
                if (childPauseCount % THREAD_COUNT != 0) return;
                resetStatus();
                mListener.onPause();
                break;
            case MSG_FINISH://下载完成
                childFinishCount++;
                if (childFinishCount % THREAD_COUNT != 0) return;
                File file = new File(mFileInfo.getFilePath(), mFileInfo.getFileName());
                String filePath = file.getAbsolutePath();
                mTmpFile.renameTo(file);//下载完毕后，重命名目标文件名
                resetStatus();
                mListener.onFinished(mFileInfo.getUrl(), filePath);
                break;
            case MSG_CANCEL://下载取消
                childCancelCount++;
                if (childCancelCount % THREAD_COUNT != 0) return;
                resetStatus();
                mProgress = new long[THREAD_COUNT];
                mListener.onCancel();
                break;

            case MSG_ERROR://下载失败
                mListener.onError("加载失败");
                break;
        }
    }

    private static final String TAG = "DownloadTask";

    public synchronized void start() {
        try {
            Log.e(TAG, "start: " + isDownloading + "\t" + mFileInfo.getUrl());
            if (isDownloading) return;
            isDownloading = true;
            mHttpUtil.getContentLength(mFileInfo.getUrl(), new okhttp3.Callback() {
                @Override
                public void onFailure(@NonNull okhttp3.Call call, @NonNull IOException e) {

                }

                @Override
                public void onResponse(@NonNull okhttp3.Call call, @NonNull Response response) throws IOException {
                    ResponseBody body = response.body();
                    if (response.code() != 200) {
                        close(body);
                        resetStatus();
                        return;
                    }
                    if (body != null) {
                        // 获取资源大小
                        mFileLength = body.contentLength();
                        close(body);
                        // 在本地创建一个与资源同样大小的文件来占位
                        mTmpFile = new File(mFileInfo.getFilePath(), mFileInfo.getFileName() + ".tmp");
                        if (!mTmpFile.getParentFile().exists()) mTmpFile.getParentFile().mkdirs();
                        RandomAccessFile tmpAccessFile = new RandomAccessFile(mTmpFile, "rw");
                        tmpAccessFile.setLength(mFileLength);
                        /*将下载任务分配给每个线程*/
                        long blockSize = mFileLength / THREAD_COUNT;// 计算每个线程理论上下载的数量.

                        /*为每个线程配置并分配任务*/
                        for (int threadId = 0; threadId < THREAD_COUNT; threadId++) {
                            long startIndex = threadId * blockSize; // 线程开始下载的位置
                            long endIndex = (threadId + 1) * blockSize - 1; // 线程结束下载的位置
                            if (threadId == (THREAD_COUNT - 1)) { // 如果是最后一个线程,将剩下的文件全部交给这个线程完成
                                endIndex = mFileLength - 1;
                            }
                            download(startIndex, endIndex, threadId);// 开启线程下载
                        }
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
            resetStatus();
        }
    }

    public void download(final long startIndex, final long endIndex, final int threadId) throws IOException {
        long newStartIndex = startIndex;
        // 分段请求网络连接,分段将文件保存到本地.
        // 加载下载位置缓存文件
        final File cacheFile = new File(mFileInfo.getFilePath(), "thread" + threadId + "_" + mFileInfo.getFileName() + ".cache");
        mCacheFiles[threadId] = cacheFile;
        final RandomAccessFile cacheAccessFile = new RandomAccessFile(cacheFile, "rwd");
        if (cacheFile.exists()) {// 如果文件存在
            String startIndexStr = cacheAccessFile.readLine();
            try {
                newStartIndex = Integer.parseInt(startIndexStr);//重新设置下载起点
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        final long finalStartIndex = newStartIndex;
        //开始下载
        sendEmptyMessage(MSG_START);
        mHttpUtil.downloadFileByRange(mFileInfo.getUrl(), finalStartIndex, endIndex, new okhttp3.Callback() {
            @Override
            public void onResponse(@NonNull okhttp3.Call call, @NonNull Response response) throws IOException {
                if (response.code() != 206) {// 206：请求部分资源成功码
                    resetStatus();
                    return;
                }
                ResponseBody body = response.body();
                if (body != null) {
                    InputStream is = body.byteStream();// 获取流
                    RandomAccessFile tmpAccessFile = new RandomAccessFile(mTmpFile, "rw");// 获取前面已创建的文件.
                    tmpAccessFile.seek(finalStartIndex);// 文件写入的开始位置.
                    /*  将网络流中的文件写入本地*/
                    byte[] buffer = new byte[1024 << 2];
                    int length = -1;
                    int total = 0;// 记录本次下载文件的大小
                    long progress = 0;
                    while ((length = is.read(buffer)) > 0) {
                        if (cancel) {
                            //关闭资源
                            close(cacheAccessFile, is, body);
                            cleanFile(cacheFile);
                            sendEmptyMessage(MSG_CANCEL);
                            return;
                        }
                        if (pause) {
                            //关闭资源
                            close(cacheAccessFile, is, body);
                            //发送暂停消息
                            sendEmptyMessage(MSG_PAUSE);
                            return;
                        }
                        tmpAccessFile.write(buffer, 0, length);
                        total += length;
                        progress = finalStartIndex + total;

                        //将当前现在到的位置保存到文件中
                        cacheAccessFile.seek(0);
                        cacheAccessFile.write((progress + "").getBytes("UTF-8"));
                        //发送进度消息
                        mProgress[threadId] = progress - startIndex;
                        sendEmptyMessage(MSG_PROGRESS);
                    }
                    //关闭资源
                    close(cacheAccessFile, is, body);
                    // 删除临时文件
                    cleanFile(cacheFile);
                    //发送完成消息
                    sendEmptyMessage(MSG_FINISH);
                }
            }

            @Override
            public void onFailure(@NonNull okhttp3.Call call, @NonNull IOException e) {
                isDownloading = false;
                sendEmptyMessage(MSG_ERROR);
                LogF.d(TAG, "msg==>" + e.getMessage());
            }
        });
    }

    /**
     * 关闭资源
     *
     * @param closeables
     */
    private void close(Closeable... closeables) {
        int length = closeables.length;
        try {
            for (int i = 0; i < length; i++) {
                Closeable closeable = closeables[i];
                if (null != closeable)
                    closeables[i].close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            for (int i = 0; i < length; i++) {
                closeables[i] = null;
            }
        }
    }

    /**
     * 删除临时文件
     */
    private void cleanFile(File... files) {
        for (int i = 0, length = files.length; i < length; i++) {
            if (null != files[i])
                files[i].delete();
        }
    }

    /**
     * 暂停
     */
    public void pause() {
        pause = true;
    }

    /**
     * 取消
     */
    public void cancel() {
        cancel = true;
        cleanFile(mTmpFile);
        if (!isDownloading) {
            if (null != mListener) {
                cleanFile(mCacheFiles);
                resetStatus();
                mListener.onCancel();
            }
        }
    }

    /**
     * 重置下载状态
     */
    private void resetStatus() {
        pause = false;
        cancel = false;
        isDownloading = false;
    }

    public boolean isDownloading() {
        return isDownloading;
    }
}
