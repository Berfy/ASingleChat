package cn.berfy.sdk.http.callback;

/**
 * Created by Berfy on 2018/1/22.
 * 下载文件回调
 */

public interface DownloadFileCallBack {

    void onStart(String url);

    void onSuccess(String url, String localPath);

    void onError(String url, String e);
}
