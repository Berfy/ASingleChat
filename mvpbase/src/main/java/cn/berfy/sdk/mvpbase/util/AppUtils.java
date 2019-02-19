package cn.berfy.sdk.mvpbase.util;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.app.KeyguardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Build;
import android.os.Looper;
import android.os.PowerManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.berfy.sdk.mvpbase.config.CacheConstant;
import cn.berfy.sdk.mvpbase.config.Constant;
import cn.berfy.sdk.mvpbase.config.Gloabal;

/**
 * 跟App相关的辅助类
 * Created by Rothschild on 2016-08-08.
 */
public class AppUtils {

    public static long mClickTime = 0;//点击跳转Ativity时间记录

    /**
     * 生成指定区间的随机数
     */
    public static int getRandomNumber(int start, int end) {
        return (int) (Math.random() * (end - start) + start);
    }

    /**
     * 清除缓存目录
     *
     * @param dir     目录
     * @param curTime 当前系统时间
     * @return int
     */
    public static int clearCacheFolder(File dir, long curTime) {
        int deletedFiles = 0;
        if (dir != null && dir.isDirectory()) {
            try {
                for (File child : dir.listFiles()) {
                    if (child.isDirectory()) {
                        deletedFiles += clearCacheFolder(child, curTime);
                    }
                    if (child.lastModified() < curTime) {
                        if (child.delete()) {
                            deletedFiles++;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return deletedFiles;
    }

    /**
     * 查看服务是否存活
     */
    public static boolean isRuningService(Context context, String serviceName) {

        ActivityManager am = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> info = am.getRunningServices(100);

        for (ActivityManager.RunningServiceInfo item : info) {
            String name = item.service.getClassName();
            if (serviceName.equals(name)) {
                return true;
            }
        }
        return false;
    }

    /*获取缓冲的大小*/
    public static String getAppCacheSize(Context context) {
        String cacheSize = "0KB";
        long fileSize = 0;
        File cacheDir = context.getCacheDir();// /data/data/package_name/cache
        File filesDir = context.getFilesDir();// /data/data/package_name/files

        File multiMediaFile = new File(CacheConstant.MultiMedia_FILE_DIR);
        File mediaFile = new File(CacheConstant.MEDIA_FILE_DIR);
        File voiceFile = new File(CacheConstant.VOICE_FILE_DIR);
        File cacheFile = new File(CacheConstant.CACHE_FILE_DIR);
        //        File logFile = new File(CacheConstant.CRASH_FILE_DIR);

        fileSize += DeviceUtils.getDirSize(multiMediaFile);
        fileSize += DeviceUtils.getDirSize(mediaFile);
        fileSize += DeviceUtils.getDirSize(voiceFile);
        fileSize += DeviceUtils.getDirSize(filesDir);
        fileSize += DeviceUtils.getDirSize(cacheDir);
        fileSize += DeviceUtils.getDirSize(cacheFile);
        //        fileSize += DeviceUtils.getDirSize(logFile);

        // 2.2版本才有将应用缓存转移到sd卡的功能
        if (DeviceUtils.isMethodsCompat(android.os.Build.VERSION_CODES.FROYO)) {
            File externalCacheDir = context.getExternalCacheDir();
            fileSize += DeviceUtils.getDirSize(externalCacheDir);
        }

        if (fileSize > 0)
            cacheSize = DeviceUtils.formatFileSize(fileSize);

        return cacheSize;
    }


    public static boolean isBackground(Context context) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        assert pm != null;
        boolean isScreenOn = pm.isScreenOn();//如果为true，则表示屏幕“亮”了，否则屏幕“暗”了。
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        assert am != null;
        ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
        String currentPackageName = cn.getPackageName();
        boolean isBackground = TextUtils.isEmpty(currentPackageName) || !currentPackageName.equals(context.getPackageName());
        boolean background = isBackground || !isScreenOn;
        LogF.d("AppApplication", "后台" + background);
        return background;
    }

    public static boolean checkOpsPermission(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            AppOpsManager appOpsManager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
            int checkOp2 = appOpsManager.checkOp(AppOpsManager.OPSTR_SYSTEM_ALERT_WINDOW, context.getApplicationInfo().uid, context.getPackageName());
            int checkOp = appOpsManager.checkOp(AppOpsManager.OPSTR_RECORD_AUDIO, context.getApplicationInfo().uid, context.getPackageName());
            int checkOp1 = appOpsManager.checkOp(AppOpsManager.OPSTR_WRITE_EXTERNAL_STORAGE, context.getApplicationInfo().uid, context.getPackageName());
            int checkOp4 = appOpsManager.checkOp(AppOpsManager.OPSTR_CAMERA, context.getApplicationInfo().uid, context.getPackageName());
            LogF.d("AppOpsManager====>", "录音权限被拒绝了" + checkOp);
            if (checkOp == AppOpsManager.MODE_IGNORED || checkOp1 == AppOpsManager.MODE_IGNORED || checkOp4 == AppOpsManager.MODE_IGNORED) {
                return false;
            }
        }

        return true;
    }

    /**
     * 获取应用程序版本名称信息
     *
     * @param context 上下文
     * @return 当前应用的版本名称
     */
    public static String getVersionName(Context context) {
        String versionName = "";
        try {
            // ---get the package info---
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
            versionName = pi.versionName;
            if (versionName == null || versionName.length() <= 0) {
                return "";
            }
        } catch (Exception e) {
            Log.e("VersionInfo", "Exception", e);
        }
        return versionName;
    }

    /**
     * 获取application中指定的meta-data
     *
     * @return 如果没有获取成功(没有对应值 ， 或者异常)，则返回值为空
     */
    public static String getChannel(Context ctx) {
        String resultData = "";
        try {
            PackageManager packageManager = ctx.getPackageManager();
            if (packageManager != null) {
                ApplicationInfo applicationInfo = packageManager.getApplicationInfo(ctx.getPackageName(), PackageManager.GET_META_DATA);
                if (applicationInfo != null) {
                    if (applicationInfo.metaData != null) {
                        resultData = applicationInfo.metaData.getString("CHANNEL_ID");
                    }
                }

            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return resultData;
    }

    /**
     * 跳转Activity延时方法
     *
     * @param context Activity
     * @param intent  传递参数
     */
    public static void startActivity(Context context, Intent intent) {
        if (checkJump()) {
            context.startActivity(intent);
        }
    }

    /**
     * 跳转Activity延时方法
     *
     * @param context     Activity
     * @param intent      传递参数
     * @param requestCode 回调
     */
    public static void startActivityForResult(Activity context, Intent intent, int requestCode) {
        if (checkJump()) {
            context.startActivityForResult(intent, requestCode);
        }
    }

    /*间隔跳转避免频繁操作*/
    public static boolean checkJump() {
        if (System.currentTimeMillis() - mClickTime >= Gloabal.ACTIVITY_INTENT_MIN_TIME) {
            mClickTime = System.currentTimeMillis();
            LogF.d("跳转监测", "可以跳转");
            return true;
        }
        LogF.d("跳转监测", "不可以跳转");
        return false;
    }

    /*间隔跳转避免频繁操作*/
    public static boolean checkJump(long minTime) {
        if (System.currentTimeMillis() - mClickTime >= minTime) {
            mClickTime = System.currentTimeMillis();
            LogF.d("跳转监测", "可以跳转");
            return true;
        }
        LogF.d("跳转监测", "不可以跳转");
        return false;
    }


    /**
     * 弹起软件盘
     *
     * @param context
     */
    public static void initTan(Context context) {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }, 100);
    }

    /**
     * 销毁WebView
     */
    public static void releaseAllWebViewCallback() {
        if (android.os.Build.VERSION.SDK_INT < 16) {
            try {
                Field field = WebView.class.getDeclaredField("mWebViewCore");
                field = field.getType().getDeclaredField("mBrowserFrame");
                field = field.getType().getDeclaredField("sConfigCallback");
                field.setAccessible(true);
                field.set(null, null);
            } catch (NoSuchFieldException e) {
                if (Constant.DEBUG) {
                    e.printStackTrace();
                }
            } catch (IllegalAccessException e) {
                if (Constant.DEBUG) {
                    e.printStackTrace();
                }
            }
        } else {
            try {
                Field sConfigCallback = Class.forName("android.webkit.BrowserFrame").getDeclaredField("sConfigCallback");
                if (sConfigCallback != null) {
                    sConfigCallback.setAccessible(true);
                    sConfigCallback.set(null, null);
                }
            } catch (NoSuchFieldException e) {
                if (Constant.DEBUG) {
//                    e.printStackTrace();
                }
            } catch (ClassNotFoundException e) {
                if (Constant.DEBUG) {
//                    e.printStackTrace();
                }
            } catch (IllegalAccessException e) {
                if (Constant.DEBUG) {
//                    e.printStackTrace();
                }
            }
        }
    }

    public static boolean checkDeviceHasNavigationBar(Context context) {
        boolean hasNavigationBar = false;
        Resources rs = context.getResources();
        int id = rs.getIdentifier("config_showNavigationBar", "bool", "android");
        if (id > 0) {
            hasNavigationBar = rs.getBoolean(id);
        }
        try {
            Class systemPropertiesClass = Class.forName("android.os.SystemProperties");
            Method m = systemPropertiesClass.getMethod("get", String.class);
            String navBarOverride = (String) m.invoke(systemPropertiesClass, "qemu.hw.mainkeys");
            if ("1".equals(navBarOverride)) {
                hasNavigationBar = false;
            } else if ("0".equals(navBarOverride)) {
                hasNavigationBar = true;
            }
        } catch (Exception e) {
        }
        return hasNavigationBar;
    }

    public static void showNavKey(Context context, int systemUiVisibility) {//getWindow().getDecorView().getSystemUiVisibility() 传入0也可以
        ((Activity) context).getWindow().getDecorView().setSystemUiVisibility(systemUiVisibility);
    }

    public static void hideNavKey(Context context) {
        if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) {
            View v = ((Activity) context).getWindow().getDecorView();
            v.setSystemUiVisibility(View.GONE);
        } else if (Build.VERSION.SDK_INT >= 19) {
            //for new api versions.
            View decorView = ((Activity) context).getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            decorView.setSystemUiVisibility(uiOptions);
        }
    }

    @TargetApi(19)
    public static void setTranslucentStatus(Activity context, boolean on) {
        Window win = context.getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        if (on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);
    }

    public static boolean isMainThread() {
        return Looper.getMainLooper() == Looper.myLooper();
    }

    //在主线程中运行
    public static boolean inMainProcess(Context context) {
        String packageName = context.getPackageName();
        String processName = SystemUtil.getProcessName(context);
        return packageName.equals(processName);
    }


    /**
     * 验证手机格式
     */
    public static boolean isMobileNO(String mobiles) {
        Pattern p = Pattern.compile("^((13[0-9])|(15[^4,\\D])|(18[0,5-9]))\\d{8}$");
        Matcher m = p.matcher(mobiles);
        return m.matches();
    }

    /**
     * 判断邮编
     *
     * @param
     * @return
     */
    public static boolean isZipNO(String zipString) {
        String str = "^[0-8]\\d{5}(?!\\d)$";
        return Pattern.compile(str).matcher(zipString).matches();
    }

    public static void wakeUpAndUnlock(Context context) {
        //屏锁管理器
        KeyguardManager km = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        KeyguardManager.KeyguardLock kl = km.newKeyguardLock("unLock");
        //解锁
        kl.disableKeyguard();
        //获取电源管理器对象
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        //获取PowerManager.WakeLock对象,后面的参数|表示同时传入两个值,最后的是LogCat里用的Tag
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_DIM_WAKE_LOCK, "bright");
        //点亮屏幕
        wl.acquire();
        //释放
        //        wl.release();
    }


    /**
     * 获得系统亮度
     *
     * @return
     */
    public static float getSystemBrightness(Context context) {
        float systemBrightness = 0;
        try {
            systemBrightness = Settings.System.getFloat(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        return systemBrightness;
    }

    /**
     * 返回当前的进程名
     */
    public static String getCurrentProcessName() {
        FileInputStream in = null;
        try {
            String fn = "/proc/self/cmdline";
            in = new FileInputStream(fn);
            byte[] buffer = new byte[256];
            int len = 0;
            int b;
            while ((b = in.read()) > 0 && len < buffer.length) {
                buffer[len++] = (byte) b;
            }
            if (len > 0) {
                String s = new String(buffer, 0, len, "UTF-8");
                return s;
            }
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    /**
     * 返回当前的进程名
     */
    public static String getCurrentProcessName2() throws Exception {

        //1. 通过ActivityThread中的currentActivityThread()方法得到ActivityThread的实例对象
        try {
            Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
            Method method = activityThreadClass.getDeclaredMethod("currentActivityThread", activityThreadClass);
            Object activityThread = method.invoke(null);

            //2. 通过activityThread的getProcessName() 方法获取进程名
            Method getProcessNameMethod = activityThreadClass.getDeclaredMethod("getProcessName", activityThreadClass);
            Object processName = getProcessNameMethod.invoke(activityThread);
            return processName.toString();

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return "";
        }
    }
}
