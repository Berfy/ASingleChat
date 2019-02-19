package cn.berfy.sdk.mvpbase.base;

import android.app.Activity;
import java.util.Stack;

import cn.berfy.sdk.mvpbase.util.LogF;

/**
 * @author Berfy
 * @category Activity堆栈管理
 */
public class ActivityManager {

    private final static String TAG = "ActivityManager";
    private Stack<Activity> mActivityStack;
    public static ActivityManager mInstance;

    public static ActivityManager getInstance() {
        if (null == mInstance) {
            mInstance = new ActivityManager();
        }
        return mInstance;
    }

    public int getActivityNum() {
        if (null != mActivityStack) {
            return mActivityStack.size();
        }
        return 0;
    }

    public void popActivity() {
        Activity activity = mActivityStack.lastElement();
        if (null != activity) {
            activity.finish();
        }
    }

    public void popActivity(Activity activity) {
        if (null != activity) {
            activity.finish();
            mActivityStack.remove(activity);
        }
    }

    public void popActivityNotFinish(Activity activity) {
        if (null != activity) {
            mActivityStack.remove(activity);
        }
    }

    public Activity currentActivity() {
        Activity activity = mActivityStack.lastElement();
        return activity;
    }

    public Class currentClass() {
        Activity activity = mActivityStack.lastElement();
        return activity.getClass();
    }

    public void pushActivity(Activity activity) {
        if (null == mActivityStack) {
            mActivityStack = new Stack<Activity>();
        }
        LogF.d("跳转Activity", activity.getClass().getName());
        mActivityStack.add(activity);
    }

    public void popAllActivityExceptOne(Class<?> cls) {
        while (null != mActivityStack && mActivityStack.size() > 0) {
            Activity activity = currentActivity();
            if (null == activity) {
                break;
            }
            if (null != cls) {
                if (activity.getClass().equals(cls)) {
                    break;
                }
            }
            LogF.d(TAG, "关闭Activity  " + activity.getClass().getSimpleName());
            popActivity(activity);
        }
        System.gc();
    }

    public void popActivity(Class<?> cls) {
        if (null != mActivityStack) {
            for (int i = 0; i < mActivityStack.size(); i++) {
                Activity activity = mActivityStack.elementAt(i);
                if (mActivityStack.elementAt(i).getClass().equals(cls)) {
//                    LogF.e("关闭Activity", cls + "");
                    popActivity(activity);
                }
            }
        }
    }

    public Activity getActivity(int position) {
        if (null != mActivityStack && mActivityStack.size() > position) {
            return mActivityStack.get(position);
        }
        return null;
    }

    public void popAllActivity() {
        while (null != mActivityStack && mActivityStack.size() > 0) {
            Activity activity = currentActivity();
            if (null == activity) {
                break;
            }
            LogF.d(TAG, "关闭Activity  " + activity.getClass().getSimpleName());
            popActivity(activity);
        }
        System.gc();
    }
}
