package cn.berfy.sdk.http.callback;

/**
 * http上传文件接口回调
 *
 * @author NorthStar
 * @date 2019/1/16 10:46
 */
public interface HttpUploadCallBack extends HttpCallBack {
    void uploadProgress(float pro, boolean isDone, int id);
}
