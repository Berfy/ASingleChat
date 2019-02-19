package cn.zcgames.sdk.mqttsdk.personal.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.kevin.crop.UCrop;

import java.io.File;
import java.util.Calendar;
import java.util.Locale;

import cn.berfy.sdk.mvpbase.config.CacheConstant;
import cn.berfy.sdk.mvpbase.util.DisplayUtil;
import cn.zcgames.sdk.mqttsdk.personal.view.activity.CropActivity;
import jp.wasabeef.glide.transformations.CropCircleTransformation;

/**
 * Created by zcgames on 2017/1/13.
 * 图片剪裁工具类
 */

public class ImageCropUtils {
    public static final int REQUEST_ALBUM_ICON = 0;
    public static final int REQUEST_LABEL_PIC = 14;
    private File mFile1;
    private Context context;
    public static final String TAG = "ImageCropUtils";

    public ImageCropUtils(Context context) {
        this.context = context;
    }

     // 裁剪图片方法实现
     // @param uri
    public void startCropActivity(Context context, Uri uri, Activity activity, int flag) {
        int screenWidth = DisplayUtil.getDisplayWidth(context);
        switch (flag) {
            case 0:
//                DataCleanManager.cleanInternalCache(context);
                String name = DateFormat.format("yyyy-MM-dd--hh-mm-ss", Calendar.getInstance(Locale.CHINA)) + ".jpg";
                File file = new File(context.getCacheDir(), name);
                UCrop.of(uri, Uri.fromFile(file))
                        .withAspectRatio(1, 1)
                        .withMaxResultSize(screenWidth, screenWidth)
                        .withTargetActivity(CropActivity.class)
                        .start(activity, REQUEST_ALBUM_ICON);
                break;
            case 1:
                String name1 = DateFormat.format("yyyy-MM-dd--hh-mm-ss", Calendar.getInstance(Locale.CHINA)) + ".jpg";
                File destDir = new File(CacheConstant.DIR_PUBLIC_ROOT + "/TempMultiMedia");
                if (!destDir.exists()) {
                    destDir.mkdirs();
                }
                mFile1 = new File(destDir, name1);
                UCrop.of(uri, Uri.fromFile(mFile1))
                        .withAspectRatio(7, 13)
                        .withMaxResultSize(screenWidth, screenWidth)
                        .withTargetActivity(CropActivity.class)
                        .start(activity, REQUEST_LABEL_PIC);
                break;
        }
    }

     // 处理剪切成功的返回值
     // @param result
    public Uri handleCropResult(Intent result, Uri littleIconUri) {
        littleIconUri = UCrop.getOutput(result);
        if (null != littleIconUri) {
            return littleIconUri;
        } else {
            Toast.makeText(context, "无法剪切选择图片", Toast.LENGTH_SHORT).show();
        }
        return null;
    }

     //处理剪切成功的返回值
     // @param result
    public Uri handleCropResult(Intent result, Uri iconUri, ImageView ivPicture, int flag) {
        iconUri = UCrop.getOutput(result);
        if (null != iconUri) {
            switch (flag) {
                case 0:
                    setIconValue(iconUri, ivPicture);
                    break;
                case 1:
                    setPicValue(iconUri, ivPicture);
                    break;
            }
        } else {
            Toast.makeText(context, "无法剪切选择图片", Toast.LENGTH_SHORT).show();
        }
        return iconUri;
    }

     //处理剪切失败的返回值
     //@param result
    public void handleCropError(Intent result) {
        final Throwable cropError = UCrop.getError(result);
        if (cropError != null) {
            Log.d(TAG, "handleCropError: ", cropError);
            Toast.makeText(context, cropError.getMessage(), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(context, "无法剪切选择图片", Toast.LENGTH_SHORT).show();
        }
    }

    //设置头像图片
    private void setIconValue(Uri uri, ImageView ivPicture) {
        //1-头像
        Glide.with(context)
                .load(uri)
                .priority(Priority.HIGH)
                .bitmapTransform(new CropCircleTransformation(context))
                .into(ivPicture);
        String filePath = uri.getEncodedPath();
        String imagePath = Uri.decode(filePath);
        Log.d(TAG, "图片已经保存到:" + imagePath);
    }

    //设置头像图片
    private void setPicValue(Uri uri, ImageView ivPicture) {
        //1-头像
        Glide.with(context)
                .load(uri)
                .priority(Priority.HIGH)
                .into(ivPicture);
        String filePath = uri.getEncodedPath();
        String imagePath = Uri.decode(filePath);
        Log.d(TAG, "图片已经保存到:" + imagePath);
    }
}
