package cn.berfy.service.download;

import android.text.TextUtils;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.berfy.sdk.mvpbase.config.CacheConstant;

/**
 * 下载管理器，断点续传
 *
 * @author NorthStar
 * @date 2018/7/30 10:36
 */
public class DownloadManager {

    private String DEFAULT_FILE_DIR;//默认下载目录
    private Map<String, DownloadTask> mDownloadTasks;//文件下载任务索引，String为url,用来唯一区别并操作下载的文件
    private static DownloadManager mInstance;
    private static final String TAG = "DownloadManager";
    private static final int PIC_FILE = 100;//图片下载标识
    private static final int VOICE_FILE = 200;//语音下载标识
    private static final int VIDEO_FILE = 300; //视频下载标识;

    /**
     * 下载文件
     */
    public void download(String... urls) {
        //单任务开启下载或多任务开启下载
        for (int i = 0, length = urls.length; i < length; i++) {
            String url = urls[i];
            if (mDownloadTasks.containsKey(url)) {
                mDownloadTasks.get(url).start();
            }
        }
    }


    // 获取下载文件的名称
    private String getFileName(int fileType, String fileId) {
        String MEDIA_TYPE = "";
        switch (fileType) {
            case PIC_FILE:
                MEDIA_TYPE = ".jpg";
                break;
            case VOICE_FILE:
                MEDIA_TYPE = ".aac";
                break;
        }
        return fileId + MEDIA_TYPE;
    }


    //判断文件是否存在
    public boolean fileIsExists(int fileType, String fileName) {
        try {
            File f = new File(getDefaultDirectory(), getFileName(fileType, fileName));
            if (!f.exists()) return false;
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    //已下载文件的绝对路径
    public String getFilePath(int fileType, String fileName) {
        try {
            File f = new File(getDefaultDirectory() + getFileName(fileType, fileName));
            if (f.exists()) return f.getAbsolutePath();
        } catch (Exception e) {
            return "";
        }
        return "";
    }

    /**
     * 暂停
     */
    public void pause(String... urls) {
        //单任务暂停或多任务暂停下载
        for (int i = 0, length = urls.length; i < length; i++) {
            String url = urls[i];
            if (mDownloadTasks.containsKey(url)) {
                mDownloadTasks.get(url).pause();
            }
        }
    }

    /**
     * 取消下载
     */
    public void cancel(List<String> urls) {
        //单任务取消或多任务取消下载
        for (int i = 0; i < urls.size(); i++) {
            String url = urls.get(i);
            if (mDownloadTasks.containsKey(url)) {
                mDownloadTasks.get(url).cancel();
            }
        }
    }

    /**
     * 添加下载任务
     */
    public void add(String url, DownloadListener l) {
        add(url, null, 0, "", l);
    }

    /**
     * 添加下载任务
     */
    public void add(String url, String filePath, DownloadListener l) {
        add(url, filePath, 0, "", l);
    }

    /**
     * 添加下载任务
     */
    public void add(String url, String filePath, int fileType, String fileId, DownloadListener listener) {
        if (TextUtils.isEmpty(filePath)) {//没有指定下载目录,使用默认目录
            filePath = getDefaultDirectory();
        }
        String fileName = getFileName(fileType, fileId);
        mDownloadTasks.put(url, new DownloadTask(new FileInfo(url, filePath, fileName), listener));
    }

    /**
     * 默认下载目录
     *
     * @return
     */
    private String getDefaultDirectory() {
        if (TextUtils.isEmpty(DEFAULT_FILE_DIR)) {
            DEFAULT_FILE_DIR = CacheConstant.FILE_DIR + File.separator;
        }
        return DEFAULT_FILE_DIR;
    }

    public static DownloadManager getInstance() {//管理器初始化
        if (mInstance == null) {
            synchronized (DownloadManager.class) {
                if (mInstance == null) {
                    mInstance = new DownloadManager();
                }
            }
        }
        return mInstance;
    }

    public DownloadManager() {
        mDownloadTasks = new HashMap<>();
    }

    /**
     * 取消下载
     */
    public boolean isDownloading(String... urls) {
        //这里传一个url就是判断一个下载任务
        //多个url数组适合下载管理器判断是否作操作全部下载或全部取消下载
        boolean result = false;
        for (int i = 0, length = urls.length; i < length; i++) {
            String url = urls[i];
            if (mDownloadTasks.containsKey(url)) {
                result = mDownloadTasks.get(url).isDownloading();
            }
        }
        return result;
    }
}
