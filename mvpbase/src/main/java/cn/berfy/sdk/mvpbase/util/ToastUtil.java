package cn.berfy.sdk.mvpbase.util;

import android.app.AppOpsManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Binder;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.view.WindowManager;
import android.widget.Toast;

import java.lang.reflect.Method;

import cn.berfy.sdk.mvpbase.R;
import cn.berfy.sdk.mvpbase.util.toast.Style;
import cn.berfy.sdk.mvpbase.util.toast.SuperToast;

/**
 * 单例吐司工具类
 * Created by jjf on 2015/9/22.
 * Berfy修改2017.12.12
 */
public class ToastUtil {

    private final String TAG = "吐司";
    private static ToastUtil mToastUtil;
    private SuperToast mToast;
    private Context mContext;

    public static void init(Context context) {
        if (null == mToastUtil) {
            mToastUtil = new ToastUtil(context);
        }
    }

    public static ToastUtil getInstances() {
        if (null == mToastUtil) {
            throw new NullPointerException("空指针，未初始化ToastUtil");
        }
        return mToastUtil;
    }

    private ToastUtil(Context context) {
        mContext = context;
    }

    private void init() {
        if (null == mToast) {
            mToast = new SuperToast(mContext)
                    .setText("")
                    .setDuration(Style.DURATION_SHORT)
                    .setFrame(Style.FRAME_STANDARD)
                    .setColor(ContextCompat.getColor(mContext, R.color.transparent_8a))
                    .setAnimations(Style.ANIMATIONS_FADE);
        }
    }

    public void showShort(String text) {
        init();
        LogF.d("吐司", "SuperToast");
        if (mToast.isShowing()) {
            mToast.dismiss();
        }
        mToast.setText(text)
                .setDuration(Style.DURATION_SHORT)
                .setFrame(Style.FRAME_STANDARD)
                .setColor(ContextCompat.getColor(mContext, R.color.transparent_8a))
                .setAnimations(Style.ANIMATIONS_FADE)
                .show();
    }

    public void showShort(int textResId) {
        showShort(mContext.getString(textResId));
    }

    public void showLong(String text) {
        init();
        if (mToast.isShowing()) {
            mToast.dismiss();
        }
        mToast.setText(text)
                .setDuration(Style.DURATION_LONG)
                .setFrame(Style.FRAME_STANDARD)
                .setColor(Color.BLACK)
                .setAnimations(Style.ANIMATIONS_FADE)
                .show();
    }

    public void showLong(int textResId) {
        showLong(mContext.getString(textResId));
    }

}
