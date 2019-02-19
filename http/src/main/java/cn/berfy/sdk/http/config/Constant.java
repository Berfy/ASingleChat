package cn.berfy.sdk.http.config;

import android.os.Environment;

public class Constant {

    public static boolean DEBUG = true;
    public static final String COOKIE = "Authorization";
    public static String HTTPTAG = "HttpLog";

    /**
     * 文件目录路径
     */
    public static String DIR_PUBLIC_ROOT = Environment.getExternalStorageDirectory().getAbsolutePath();
    public static String FILE_CACHE = DIR_PUBLIC_ROOT + "file";
    public static String XML_FILENAME = "http";

    /**
     * @param rootDirName 目录名
     */
    public static void setCacheDir(String rootDirName) {
        DIR_PUBLIC_ROOT = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + rootDirName;
    }
}
