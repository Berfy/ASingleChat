package cn.berfy.sdk.mvpbase.base;

import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.multidex.MultiDex;
import android.text.TextUtils;

import cn.berfy.sdk.mvpbase.util.LogF;
import com.google.gson.reflect.TypeToken;

import cn.berfy.sdk.mvpbase.config.CacheConstant;
import cn.berfy.sdk.mvpbase.model.User;
import cn.berfy.sdk.mvpbase.util.GsonUtil;
import cn.berfy.sdk.mvpbase.util.ScreenUtil;
import cn.berfy.sdk.mvpbase.util.SharedPreferenceUtil;

/**
 * @author Berfy
 * @category BaseApplication
 */
public abstract class BaseApplication extends Application {

    private final String TAG = "AppApplication";
    private static Handler mMainThreadHandler;
    private static Looper mMainThreadLooper;
    private static Thread mMainThread;
    private static int mMainThreadId;
    private static BaseApplication mInstance;

    //网络状态
    public static int NET_STATE = 1;//0没有网  1wifi 2 移动网络

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        ScreenUtil.init(mInstance);
        mMainThreadHandler = new Handler();
        mMainThreadLooper = getMainLooper();
        mMainThread = Thread.currentThread();
        mMainThreadId = android.os.Process.myTid();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    public static Application getContext() {
        return mInstance;
    }

    /**
     * @return 获取主线程的Handler
     */
    public static Handler getMainThreadHandler() {
        return mMainThreadHandler;
    }

    /**
     * @return 获取主线程的Looper
     */
    public static Looper getMainThreadLooper() {
        return mMainThreadLooper;
    }

    /**
     * @return 获取主线程
     */
    public static Thread getMainThread() {
        return mMainThread;
    }

    /**
     * @return 获取主线程ID
     */
    public static int getMainThreadId() {
        return mMainThreadId;
    }

    /**
     *
     * @return 获取当前登录的用户
     */
    public static User getCurrLoginUser() {
        String userStr = (String) SharedPreferenceUtil.get(getContext(), CacheConstant.XML_USER_DATA, "");
        return TextUtils.isEmpty(userStr) ? null : GsonUtil.getInstance().toClass(userStr, User.class);
    }

    /**
     *
     * @return 获取当前登录的用户的userId
     */
    public static String getUserId() {
        User currLoginUser = getCurrLoginUser();
        boolean noExists = currLoginUser == null || currLoginUser.getPlayer() == null;
        return  noExists? "" : currLoginUser.getPlayer().getId();
    }


    /**
     * 更新当前登录的用户
     *
     * @param user
     */
    public static void updateCurrLoginUser(User user) {
        if (null == user) {//退出登录
            SharedPreferenceUtil.put(getContext(), CacheConstant.XML_USER_DATA, "");
            return;
        }
        String data = GsonUtil.getInstance().toJson(user);
        SharedPreferenceUtil.put(getContext(), CacheConstant.XML_USER_DATA, data);
    }

    //获取app当前的登录状态
    public static boolean getCurrLoginStatus() {
        return !(getCurrLoginUser() == null);
    }

    @Override
    public void onLowMemory() {
        LogF.d(TAG,"内存占用过高，将清理无用缓存");
        super.onLowMemory();
    }
}
