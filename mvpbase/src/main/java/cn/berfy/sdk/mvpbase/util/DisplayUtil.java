package cn.berfy.sdk.mvpbase.util;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Matrix;
import android.graphics.Point;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.WindowManager;
import android.widget.ImageView;

import java.lang.reflect.Field;

import cn.berfy.sdk.mvpbase.base.BaseApplication;


/**
 * UI工具类
 */
public class DisplayUtil {
    /**
     * 屏幕密度
     */
    private static WindowManager mWindowManager = null;

    /**
     * 获取屏幕密度
     */
    public static float getDensity() {
        return CommonUtil.getResources(BaseApplication.getContext()).getDisplayMetrics().density;
    }

    /**
     * 根据手机的分辨率从dp转成为px
     */
    public static int dip2px(Context context, float dpValue) {
        return Math.round(dpValue * CommonUtil.getResources(context).getDisplayMetrics().density);
    }

    /**
     * 根据手机的分辨率从dp转成为px
     */
    public static int dip2px(float dpValue) {
        return Math.round(dpValue * CommonUtil.getResources(BaseApplication.getContext()).getDisplayMetrics().density);
    }

    /**
     * 根据手机的分辨率从px转成为dp
     */
    @Deprecated
    public static int px2dip(Context context, float pxValue) {
        return Math.round(pxValue / CommonUtil.getResources(context).getDisplayMetrics().density);
    }

    /**
     * 根据手机的分辨率从px转成为dp
     */
    public static int px2dip(float pxValue) {
        return Math.round(pxValue / CommonUtil.getResources(BaseApplication.getContext()).getDisplayMetrics().density);
    }

    /**
     * 获取WindowManager
     */
    public static WindowManager getWindowManager(Context context) {
        if (mWindowManager == null) {
            mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        }
        return mWindowManager;
    }

    /**
     * 获取手机屏幕分辨率宽度（竖屏状态）
     */
    public static int getDisplayWidth(Context context) {
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager(context).getDefaultDisplay().getMetrics(dm);
        return Math.min(dm.widthPixels, dm.heightPixels);
    }

    /**
     * 获取手机屏幕分辨率高度（竖屏状态）
     */
    public static int getDisplayHeight(Context context) {
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager(context).getDefaultDisplay().getMetrics(dm);
        return Math.max(dm.widthPixels, dm.heightPixels);
    }

    /**
     * 获取实际屏幕宽度
     *
     * @param screenOritation 屏幕方向
     */
    public static int getRealDisplayWidth(Context context, int screenOritation) {
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager(context).getDefaultDisplay().getMetrics(dm);
        return screenOritation == Configuration.ORIENTATION_LANDSCAPE ? Math.max(dm.widthPixels, dm.heightPixels) : Math.min(dm.widthPixels, dm.heightPixels);
    }

    /**
     * 获取实际屏幕高度
     *
     * @param screenOritation 屏幕方向
     */
    public static int getRealDisplayHeight(Context context, int screenOritation) {
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager(context).getDefaultDisplay().getMetrics(dm);
        return screenOritation == Configuration.ORIENTATION_LANDSCAPE ? Math.min(dm.widthPixels, dm.heightPixels) : Math.max(dm.widthPixels, dm.heightPixels);
    }

    public static DisplayMetrics getDisplayMetrics(Context context) {
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager(context).getDefaultDisplay().getMetrics(dm);
        return dm;
    }

    /**
     * 获取手机屏幕密度
     */
    public static float getDisplayDensity(Context context) {
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager(context).getDefaultDisplay().getMetrics(dm);
        return dm.density;
    }

    /**
     * 获取手机字体缩放密度
     */
    public static float getScaledDensity(Context context) {
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager(context).getDefaultDisplay().getMetrics(dm);
        return dm.scaledDensity;
    }

    /**
     * 根据ImageView的宽度动态设置高度，并保证image的宽高比例，注：center_crop按比例缩放
     */
    public static void adjustImageHeight(final ImageView imageView) {
        ViewTreeObserver vto = imageView.getViewTreeObserver();
        // 保证Imageview测量完成
        vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                imageView.getViewTreeObserver().removeGlobalOnLayoutListener(this);

                // 获得ImageView中Image的真实宽高，
                int dw = imageView.getDrawable().getBounds().width();
                int dh = imageView.getDrawable().getBounds().height();
                LogF.d("lxy", "drawable_X = " + dw + ", drawable_Y = " + dh);

                // 获得ImageView中Image的变换矩阵
                Matrix m = imageView.getImageMatrix();
                float[] values = new float[10];
                m.getValues(values);

                // Image在绘制过程中的变换矩阵，从中获得x和y方向的缩放系数
                float sx = values[0];
                float sy = values[4];
                LogF.d("lxy", "scale_X = " + sx + ", scale_Y = " + sy);

                // 计算Image在屏幕上实际绘制的宽高
                int cw = (int) (dw * sx);
                int ch = (int) (dh * sy);
                LogF.d("lxy", "caculate_W = " + cw + ", caculate_H = " + ch);

                ViewGroup.LayoutParams lp = imageView.getLayoutParams();
                lp.height = ch;
                imageView.setLayoutParams(lp);
            }
        });
    }

    /**
     * @return 获取状态栏高度
     */
    public static int getStatusBarHeight(Context context) {
        Class<?> c = null;

        Object obj = null;

        Field field = null;

        int x = 0, sbar = 0;

        try {

            c = Class.forName("com.android.internal.R$dimen");

            obj = c.newInstance();

            field = c.getField("status_bar_height");

            x = Integer.parseInt(field.get(obj).toString());

            sbar = context.getResources().getDimensionPixelSize(x);

        } catch (Exception e1) {

            e1.printStackTrace();

        }

        return sbar;
    }

    /**
     * @return actionBar 高度
     */
    public static int getActionBarHeight(Context context) {
        // Calculate ActionBar height
        TypedValue tv = new TypedValue();
        int actionBarHeight = 0;
        if (context.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, context.getResources().getDisplayMetrics());
        }
        return actionBarHeight;
    }

    //动态调整控件的位置
    public static void setMargins(View v, int l, int t, int r, int b) {
        if (v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            p.setMargins(l, t, r, b);
            v.requestLayout();
        }
    }

    //获取虚拟按键的高度
    public static int getNavigationBarHeight(Activity context) {
        int result = 0;
        if (hasNavBar(context)) {
            Resources res = context.getResources();
            int resourceId = res.getIdentifier("navigation_bar_height", "dimen", "android");
            if (resourceId > 0) {
                result = res.getDimensionPixelSize(resourceId);
            }
        }
        return result;
    }

    /**
     * 检查是否存在虚拟按键栏
     *
     * @param context
     * @return
     */
    public static boolean hasNavBar(Activity context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            Display display = context.getWindowManager().getDefaultDisplay();
            Point size = new Point();
            Point realSize = new Point();
            display.getSize(size);
            display.getRealSize(realSize);
            return realSize.y != size.y;
        } else {
            boolean menu = ViewConfiguration.get(context).hasPermanentMenuKey();
            boolean back = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK);
            if (menu || back) {
                return false;
            } else {
                return true;
            }
        }
    }

    public static boolean canScrollVertically(RecyclerView recyclerView, int direction) {
        final int offset = recyclerView.computeVerticalScrollOffset();
        final int range = recyclerView.computeVerticalScrollRange() - recyclerView.computeVerticalScrollExtent();
        LogF.d("canScrollVertically", "RecyclerView滑动检测canScrollVertically  direction=" + direction + "  offset=" + offset + "  range=" + range);
        if (range == 0) return false;
        if (direction < 0) {
            return offset > 0;
        } else {
            return offset < range - 1;
        }
    }
}
