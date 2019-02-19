package cn.berfy.sdk.mvpbase.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import java.util.HashMap;
import java.util.Map;

import cn.berfy.sdk.mvpbase.base.BaseApplication;


/**
 * 公共工具类
 */
public class CommonUtil {

    /**
     * 在主线程运行
     *
     * @param r Runnable接口的实现类
     */
    public static void runOnUIThread(Runnable r) {
        BaseApplication.getMainThreadHandler().post(r);
    }

    /**
     * @return getResources
     */
    public static Resources getResources(Context context) {
        return context.getResources();
    }

    /**
     * 将指定子view从他爹中移除
     *
     * @param child
     */
    public static void removeSelfFromParent(View child) {
        if (child != null) {
            ViewParent parent = child.getParent();
            if (parent != null && parent instanceof ViewGroup) {
                ViewGroup group = (ViewGroup) parent;
                group.removeView(child);// 移除子view
            }
        }
    }

    /**
     * @param resID 字符串资源ID
     * @return 获取字符串资源
     */
    public static String getString(int resID, Context context) {
        return getResources(context).getString(resID);
    }

    /**
     * @param resID 字符串资源ID
     * @return 获取字符串资源
     */
    public static String getString(Context context, int resID) {
        return getResources(context).getString(resID);
    }

    /**
     * @param resID 图片资源ID
     * @return 获取图片资源
     */
    @Deprecated
    public static Drawable getDrawable(int resID, Context context) {
        return ContextCompat.getDrawable(context, resID);
    }

    /**
     * @param resID 图片资源ID
     * @return 获取图片资源
     */
    public static Drawable getDrawable(Context context, int resID) {
        return ContextCompat.getDrawable(context, resID);
    }

    /**
     * @param resID 字符串数组资源ID
     * @return 获取字符串数组资源
     */
    public static String[] getStringArray(int resID, Context context) {
        return getResources(context).getStringArray(resID);
    }

    /**
     * @param resID Dimens资源ID
     * @return 获取Dimens值
     */
    public static float getDimens(int resID) {
        return getResources(BaseApplication.getContext()).getDimension(resID);
    }

    /**
     * @param resID Dimens资源ID
     * @return 获取Dimens值
     */
    public static float getDimens(Context context, int resID) {
        return getResources(context).getDimension(resID);
    }

    /**
     * @param resID Color资源ID
     * @return 获取Color值
     */
    public static int getColor(int resID) {
        return ContextCompat.getColor(BaseApplication.getContext(), resID);
    }

    /**
     * @param resID Color资源ID
     * @return 获取Color值
     */
    public static int getColor(Context context, int resID) {
        return ContextCompat.getColor(context, resID);
    }

    /**
     * 获取版本名称
     *
     * @return 当前应用的版本名称
     */
    public static String getVersionName(Context context) {
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            String version = info.versionName;
            return version;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * 获取版本号
     *
     * @return 当前应用的版本号
     */
    public static int getVersionCode(Context context) {
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            int version = info.versionCode;
            return version;
        } catch (Exception e) {
            e.printStackTrace();
            return 1;
        }
    }

    /**
     * 获取手机型号
     *
     * @return
     */
    public static String getSystemModel() {
        return android.os.Build.MODEL;
    }
}
