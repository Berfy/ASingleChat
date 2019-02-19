package cn.berfy.sdk.http.http.okhttp.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * 文件工具类
 */
public class HttpFileUtils {
    public static final String TAG = "FileUtil";

    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    private static final ThreadLocal<DateFormat> dateFormatThreadLocal = new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd--HH-mm-ss", Locale.CHINA);
        }
    };

    /**
     * 判断SD卡是否挂载
     *
     * @return boolean
     */
    public static boolean isSDCardMounted() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    /**
     * 判断文件是否存在
     *
     * @param filePath 文件路径
     * @return boolean
     */
    public static boolean exists(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return false;
        }
        File file = new File(filePath);
        return file.exists();
    }


    /**
     * 删除单个文件
     *
     * @param filePath 要删除的文件的文件名
     * @return 单个文件删除成功返回true，否则返回false
     */
    public static void deleteFile(String filePath) {
        File file = new File(filePath);
        // 如果文件路径所对应的文件存在，并且是一个文件，则直接删除
        if (file.exists() && file.isFile()) {
            if (file.delete()) {
                HLogF.d(TAG, "删除单个文件" + filePath + "成功！");
            } else {
                HLogF.d(TAG, "删除单个文件" + filePath + "失败！");
            }
        } else {
            HLogF.d(TAG, "删除单个文件失败：" + filePath + "不存在！");
        }
    }

    /**
     * 删除单个文件
     *
     * @param files 要删除的文件数组
     * @return 单个文件删除成功返回true，否则返回false
     */
    public static void deleteFiles(List<File> files) {
        for (File file : files) {
            if (file.exists() && file.isFile()) {
                file.delete();
            }
        }
    }

    /**
     * 删除文件夹下所有文件以及所有子文件（不会删除文件夹）
     *
     * @param directoryPath 文件路径
     * @return boolean
     */
    public static boolean delAllFile(String directoryPath) {
        boolean flag = false;
        File file = new File(directoryPath);
        if (!file.exists()) {
            return flag;
        }
        if (!file.isDirectory()) {
            return flag;
        }
        String[] tempList = file.list();
        File temp = null;
        for (String filePath : tempList) {
            if (directoryPath.endsWith(File.separator)) {
                temp = new File(directoryPath + filePath);
            } else {
                temp = new File(directoryPath + File.separator + filePath);
            }
            if (temp.isFile()) {
                flag = temp.delete();
            }
            if (temp.isDirectory()) {
                flag = delAllFile(directoryPath + "/" + filePath);
            }
        }
        HLogF.e(TAG, "删除文件" + flag + "  " + directoryPath);
        return flag;
    }

    /**
     * 删除文件夹下所有文件以及所有子媒体文件（不会删除文件夹）
     *
     * @param directoryPath 文件路径
     * @return boolean
     */
    public static boolean delAllMediaFile(Context context, String directoryPath) {
        boolean flag = false;
        File file = new File(directoryPath);
        if (!file.exists()) {
            return flag;
        }
        if (!file.isDirectory()) {
            return flag;
        }
        String[] tempList = file.list();
        File temp = null;
        for (String filePath : tempList) {
            if (directoryPath.endsWith(File.separator)) {
                temp = new File(directoryPath + filePath);
            } else {
                temp = new File(directoryPath + File.separator + filePath);
            }
            if (temp.isFile()) {
                flag = deleteMediaFile(context, temp.getAbsolutePath());
            }
            if (temp.isDirectory()) {
                flag = delAllFile(directoryPath + "/" + filePath);
            }
        }
        return flag;
    }

    /**
     * 删除多媒体数据库中的数据
     */
    public static boolean deleteMediaFile(Context context, String filePath) {
        File file = new File(filePath);
        if (filePath.endsWith(".mp4")) {
            int res = context.getContentResolver().delete(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    MediaStore.Video.Media.DATA + "= \"" + filePath + "\"",
                    null);
            HLogF.d(TAG, "-----delete res =" + res);
            if (res > 0) {
                return file.delete();
            } else {
                HLogF.e(TAG, "删除文件失败");
            }
        } else if (filePath.endsWith(".jpg") || filePath.endsWith(".png") || filePath.endsWith(".bmp")) {
            int res = context.getContentResolver().delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    MediaStore.Images.Media.DATA + "= \"" + filePath + "\"",
                    null);
            if (res > 0) {
                return file.delete();
            } else {
                HLogF.e(TAG, "删除文件失败");
            }
        } else {
            return file.delete();
        }
        return false;
    }

    /**
     * 获取文件类型
     *
     * @param file 文件
     * @return String type
     */
    public static String getMimeType(File file) {
        String extension = getExtension(file);
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
    }

    /**
     * 通过文件，获取扩展名
     *
     * @param file 文件
     * @return String
     */
    public static String getExtension(File file) {
        String suffix = "";
        String name = file.getName();
        int idx = name.lastIndexOf(".");
        if (idx > 0) {
            suffix = name.substring(idx + 1);
        }
        return suffix;
    }

    /**
     * 根据路径获取指定文件大小
     *
     * @param path 路径
     * @return
     * @throws Exception
     */
    public static long getFileSize(String path) throws Exception {
        long size = 0;
        File file = new File(path);
        if (file.exists()) {
            FileInputStream fis = null;
            fis = new FileInputStream(file);
            size = fis.available();
        }
        return size;
    }

    /**
     * 获取指定文件大小
     *
     * @param file
     * @return
     * @throws Exception
     */
    public static long getFileSize(File file) throws Exception {
        long size = 0;
        if (file.exists()) {
            FileInputStream fis = null;
            fis = new FileInputStream(file);
            size = fis.available();
        } else {
            file.createNewFile();
            HLogF.e("获取文件大小", "文件不存在!");
        }
        return size;
    }

    /**
     * 获取指定文件夹
     *
     * @param f
     * @return
     * @throws Exception
     */
    public static long getFileSizes(File f) throws Exception {
        long size = 0;
        File flist[] = f.listFiles();
        for (int i = 0; i < flist.length; i++) {
            if (flist[i].isDirectory()) {
                size = size + getFileSize(flist[i]);
            } else {
                size = size + getFileSize(flist[i]);
            }
        }
        return size;
    }

    /**
     * 转换文件大小
     *
     * @param fileS
     * @return
     */
    public static String formatFileSize(long fileS) {
        DecimalFormat df = new DecimalFormat("#.00");
        String fileSizeString = "";
        String wrongSize = "0B";
        if (fileS == 0) {
            return wrongSize;
        }
        if (fileS < 1024) {
            fileSizeString = df.format((double) fileS) + "B";
        } else if (fileS < 1048576) {
            fileSizeString = df.format((double) fileS / 1024) + "KB";
        } else if (fileS < 1073741824) {
            fileSizeString = df.format((double) fileS / 1048576) + "MB";
        } else {
            fileSizeString = df.format((double) fileS / 1073741824) + "GB";
        }
        return fileSizeString;
    }

    /**
     * 将bitmap存为字节
     *
     * @param character 品质 1-100
     */
    public static byte[] bitmap2Bytes(Bitmap bm, int character) {
        if (bm != null) {
            ByteArrayOutputStream bas = new ByteArrayOutputStream();
            bm.compress(Bitmap.CompressFormat.JPEG, character, bas);
            byte[] byteArray = bas.toByteArray();
            try {
                bas.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return byteArray;
        }
        return null;
    }

    // 创建文件
    public static boolean createFile(File file) {
        HLogF.i("创建文件", "===========>" + file.getPath());
        try {
            if (file.exists()) {
                return true;
            } else {
                if (file.getParentFile().isDirectory()) {
                    file.getParentFile().mkdirs();
                }
                File parentFile = file.getParentFile();
                if (!parentFile.exists()) {
                    parentFile.mkdirs();
                }
                file.createNewFile();
            }

            if (!file.exists()) {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    // 创建文件并写入字节
    public static boolean createFile(File file, byte[] buffer) {
        if (createFile(file)) {
            FileOutputStream fos;
            try {
                fos = new FileOutputStream(file);
                fos.write(buffer);
                fos.flush();
                fos.close();
                buffer = null;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    // 创建文件并写入字节
    public static boolean createFile(File file, InputStream ins) {
        if (createFile(file)) {
            inputStreamTofile(ins, file);
        }
        return true;
    }

    public static void inputStreamTofile(InputStream ins, File file) {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            int bytesRead = 0;
            byte[] buffer = new byte[8192];
            while ((bytesRead = ins.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
            fos.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
                ins.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
