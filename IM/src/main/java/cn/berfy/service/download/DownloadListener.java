package cn.berfy.service.download;

/**
 * 下载监听
 *
 * @author NorthStar
 * @date 2018/7/30 10:32
 */
public interface DownloadListener {
    void onStart(String url);

    void onError(String errMsg);

    void onFinished(String url, String localPath);

    void onProgress(float progress);

    void onPause();

    void onCancel();
}
