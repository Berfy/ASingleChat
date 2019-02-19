package cn.berfy.sdk.mvpbase.util;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.MimeTypeMap;

import java.io.*;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import cn.berfy.sdk.mvpbase.base.BaseApplication;
import cn.berfy.sdk.mvpbase.config.CacheConstant;

/**
 * 文件工具类
 */
public class FileUtils {
    public static final String TAG = "FileUtil";

    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;
    private static String pathDiv = "/";
    private static File cacheDir = !isExternalStorageWritable() ? BaseApplication.getContext().getFilesDir() : BaseApplication.getContext().getExternalCacheDir();

    private static final ThreadLocal<DateFormat> dateFormatThreadLocal = new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd--HH-mm-ss", Locale.CHINA);
        }
    };


    /**
     * 创建临时文件
     *
     * @param type 文件类型
     */
    public static File getTempFile(FileType type) {
        try {
            File file = File.createTempFile(type.toString(), null, cacheDir);
            file.deleteOnExit();
            return file;
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * 获取缓存文件地址
     */
    public static String getCacheFilePath(String fileName) {
        return cacheDir.getAbsolutePath() + pathDiv + fileName;
    }

    /**
     * 判断文件是否存在
     *
     * @param path 文件路径
     * @return 存在返回true, 否则返回false
     */
    public static boolean isExistFile(String path) {
        if (null == path || "".equals(path.trim())) return false;
        File file = new File(path);
        return file.exists();
    }

    /**
     * 获取缓存的原文件地址(原图)
     */
    public static String getCacheRawFilePath(String fileName) {
        File file = new File(cacheDir.getAbsolutePath() + pathDiv + "raw" + pathDiv);
        if (!file.exists()) {
            file.mkdirs();
        }
        return cacheDir.getAbsolutePath() + pathDiv + "raw" + pathDiv + fileName;
    }


    /**
     * 判断缓存文件是否存在
     */
    public static boolean isCacheFileExist(String fileName) {
        File file = new File(getCacheFilePath(fileName));
        return file.exists();
    }

    /**
     * 判断缓存原文件是否存在
     */
    public static boolean isCacheRawFileExist(String fileName) {
        File file = new File(getCacheRawFilePath(fileName));
        return file.exists();
    }


    /**
     * 将图片存储为文件
     *
     * @param bitmap 图片
     */
    public static String createFile(Bitmap bitmap, String filename) {
        File f = new File(cacheDir, filename);
        try {
            if (f.createNewFile()) {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 0, bos);
                byte[] bitmapdata = bos.toByteArray();
                FileOutputStream fos = new FileOutputStream(f);
                fos.write(bitmapdata);
                fos.flush();
                fos.close();
            }
        } catch (IOException e) {
            Log.e(TAG, "create bitmap file error" + e);
        }
        if (f.exists()) {
            return f.getAbsolutePath();
        }
        return null;
    }

    /**
     * 将数据存储为文件
     *
     * @param data 数据
     */
    public static void createFile(byte[] data, String filename) {
        File f = new File(cacheDir, filename);
        try {
            if (f.createNewFile()) {
                FileOutputStream fos = new FileOutputStream(f);
                fos.write(data);
                fos.flush();
                fos.close();
            }
        } catch (IOException e) {
            Log.e(TAG, "create file error" + e);
        }
    }


    /**
     * 判断缓存文件是否存在
     */
    public static boolean isFileExist(String fileName, String type) {
        if (isExternalStorageWritable()) {
            File dir = BaseApplication.getContext().getExternalFilesDir(type);
            if (dir != null) {
                File f = new File(dir, fileName);
                return f.exists();
            }
        }
        return false;
    }


    /**
     * 将数据存储为文件
     *
     * @param data     数据
     * @param fileName 文件名
     * @param type     文件类型
     */
    public static File createFile(byte[] data, String fileName, String type) {
        if (isExternalStorageWritable()) {
            File dir = BaseApplication.getContext().getExternalFilesDir(type);
            if (dir != null) {
                File f = new File(dir, fileName);
                try {
                    if (f.createNewFile()) {
                        FileOutputStream fos = new FileOutputStream(f);
                        fos.write(data);
                        fos.flush();
                        fos.close();
                        return f;
                    }
                } catch (IOException e) {
                    Log.e(TAG, "create file error" + e);
                    return null;
                }
            }
        }
        return null;
    }


    /**
     * 从URI获取图片文件地址
     *
     * @param context 上下文
     * @param uri     文件uri
     */
    public static String getImageFilePath(Context context, Uri uri) {
        if (uri == null) {
            return null;
        }
        String path = null;
        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
        if (isKitKat) {
            if (!isMediaDocument(uri)) {
                try {
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    Uri contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                    final String selection = "_id=?";
                    final String[] selectionArgs = new String[]{
                            split[1]
                    };
                    path = getDataColumn(context, contentUri, selection, selectionArgs);
                } catch (IllegalArgumentException e) {
                    path = null;
                }
            }
        }
        if (path == null) {
            String[] projection = {MediaStore.Images.Media.DATA};
            Cursor cursor = ((Activity) context).managedQuery(uri, projection, null, null, null);
            if (cursor != null) {
                int column_index = cursor
                        .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                cursor.moveToFirst();
                return cursor.getString(column_index);
            }

            path = null;
        }
        return path;
    }


    /**
     * 从URI获取文件地址
     *
     * @param context 上下文
     * @param uri     文件uri
     */
    public static String getFilePath(Context context, Uri uri) {
        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }


    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    private static String getDataColumn(Context context, Uri uri, String selection,
                                        String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    /**
     * 判断外部存储是否可用
     */
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        Log.e(TAG, "ExternalStorage not mounted");
        return false;
    }

    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }


    public enum FileType {
        IMG,
        AUDIO,
        VIDEO,
        FILE,
    }

    /**
     * 判断SD卡是否挂载
     *
     * @return boolean
     */
    public static boolean isSDCardMounted() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    /**
     * Create a file Uri for saving an image or video
     */
    public static Uri getOutputMediaFileUri(int type) {
        File file = getOutputMediaFile(type);
        if (null == file)
            return null;
        return Uri.fromFile(file);
    }

    /**
     * Create a File for saving an image or video
     */
    public static File getOutputMediaFile(int type) {
        File mediaStorageDir = new File(CacheConstant.MEDIA_FILE_DIR);
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                LogF.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = dateFormatThreadLocal.get().format(new Date());

        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_" + timeStamp + ".jpg");
        } else if (type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_" + timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
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
                LogF.d(TAG, "删除单个文件" + filePath + "成功！");
            } else {
                LogF.d(TAG, "删除单个文件" + filePath + "失败！");
            }
        } else {
            LogF.d(TAG, "删除单个文件失败：" + filePath + "不存在！");
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
        LogF.e(TAG, "删除文件" + flag + "  " + directoryPath);
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
            LogF.d(TAG, "-----delete res =" + res);
            if (res > 0) {
                return file.delete();
            } else {
                LogF.e(TAG, "删除文件失败");
            }
        } else if (filePath.endsWith(".jpg") || filePath.endsWith(".png") || filePath.endsWith(".bmp")) {
            int res = context.getContentResolver().delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    MediaStore.Images.Media.DATA + "= \"" + filePath + "\"",
                    null);
            if (res > 0) {
                return file.delete();
            } else {
                LogF.e(TAG, "删除文件失败");
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
     * 根据路径获取指定文件大小(字节)
     *
     * @param path 路径
     * @return
     * @throws Exception
     */
    public static long getFileSize(String path) {
        long size = 0;
        try {
            File file = new File(path);
            if (file.exists()) {
                FileInputStream fis = null;
                fis = new FileInputStream(file);
                size = fis.available();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return size;
    }

    /**
     * 获取指定文件大小(字节)
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
            LogF.e("获取文件大小", "文件不存在!");
        }
        return size;
    }

    /**
     * 获取指定文件夹占用空间大小(字节)
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
    public static String FormatFileSize(long fileS) {
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

    //获取录音存放路径
    public static String getAppRecordDir(Context context) {
        File recordDir = new File(CacheConstant.VOICE_FILE_DIR);
        if (!recordDir.exists()) {
            recordDir.mkdir();
        }
        return recordDir.getPath();
    }

    //获取录音存放路径
    public static String getAppImageDir(Context context) {
        File recordDir = new File(CacheConstant.PICTURE_FILE_DIR);
        if (!recordDir.exists()) {
            recordDir.mkdir();
        }
        return recordDir.getPath();
    }

    //获取录音存放路径
    public static String getAppFileDir(Context context) {
        File recordDir = new File(CacheConstant.FILE_DIR);
        if (!recordDir.exists()) {
            recordDir.mkdir();
        }
        return recordDir.getPath();
    }

    //获取录音存放路径
    public static String getAppVideoDir(Context context) {
        File recordDir = new File(CacheConstant.VIDEO_STORAGE_DIR);
        if (!recordDir.exists()) {
            recordDir.mkdir();
        }
        return recordDir.getPath();
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
        LogF.i("创建文件", "===========>" + file.getPath());
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

    public static boolean createDir(String dirPath) {
        File file = new File(dirPath);
        if (!file.exists()) {
            boolean rootDirState = file.mkdirs();
            LogF.d(TAG, "rootDirState-" + rootDirState);
            return rootDirState;
        }
        return false;
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
    public static boolean createFile(String filePath, byte[] buffer) {
        File file = new File(filePath);
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
    public static boolean createFile(String filePath, InputStream ins) {
        File file = new File(filePath);
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
