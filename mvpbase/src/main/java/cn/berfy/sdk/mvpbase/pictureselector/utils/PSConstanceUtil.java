package cn.berfy.sdk.mvpbase.pictureselector.utils;


import java.io.File;

import cn.berfy.sdk.mvpbase.config.CacheConstant;

/**
 * created by arvin on 16/9/3 11:20
 * email：1035407623@qq.com
 */
public class PSConstanceUtil {
    public static final String PASS_SHOW = "key1";
    public static final String PASS_SELECTED = "key2";
    public static final String PASS_CURRENT_POS = "key3";
    public static final String PASS_EXTRA = "key4";

    public static final String FOLDER_NAME = "PicSelector";
    public static final String FROM_CAMERA = "from_camera";
    /**
     * 为了不让媒体库扫描
     */
    public static final String CROP_FILE_NAME = ".nomedia";

    public static final int IMAGE_REQUEST_TAKE_PHOTO = 1001;

    public static String getRootDIr() {
        return CacheConstant.DIR_PUBLIC_ROOT + "/";
    }

    public static void clear(String rootPath) {
        File cropFolder = new File(rootPath);
        if (cropFolder.exists()) {
            String[] cropImages = cropFolder.list();
            if (cropImages == null) {
                return;
            }
            for (String path : cropImages) {
                File temp = new File(path);
                temp.delete();
            }
        }
    }

}
