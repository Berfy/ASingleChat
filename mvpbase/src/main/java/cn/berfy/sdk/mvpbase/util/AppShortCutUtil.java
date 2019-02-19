package cn.berfy.sdk.mvpbase.util;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import cn.berfy.sdk.mvpbase.R;
import cn.berfy.sdk.mvpbase.config.CacheConstant;
import cn.berfy.sdk.mvpbase.config.Constant;

/***
 * 应用的快捷方式工具类
 * 目前收录的有
 *
 * 三星 samsung √
 * 小米  Xiaomi √
 * 联想   ZUK   √
 * vivo 需要官方通过才能用
 * 华为  只对前五即时通讯App开放
 * sony 未测试
 * @author yang
 *
 */
public class AppShortCutUtil {

    private final String TAG = "AppShortCutUtil";
    private static AppShortCutUtil mAppShortCutUtil;
    private Context mContext;

    //默认圆角半径
    private final int DEFAULT_CORNER_RADIUS_DIP = 8;
    //默认边框宽度
    private final int DEFAULT_STROKE_WIDTH_DIP = 2;
    //边框的颜色
    private final int DEFAULT_STROKE_COLOR = Color.WHITE;
    //中间数字的颜色
    private final int DEFAULT_NUM_COLOR = Color.parseColor("#CCFF0000");

    private boolean mIsCanDo = true;
    private final long DELAY_TIME = 1000;
    private long mTime = 0;
    private int mNum = 0;//未读消息数

    synchronized public static AppShortCutUtil getInstance(Context context) {
        if (null == mAppShortCutUtil) {
            synchronized (AppShortCutUtil.class) {
                if (null == mAppShortCutUtil) {
                    mAppShortCutUtil = new AppShortCutUtil(context);
                }
            }
        }
        return mAppShortCutUtil;
    }

    private AppShortCutUtil(Context context) {
        mContext = context;
    }

    /***
     * 在应用图标的快捷方式上加数字
     * @param clazz 启动的activity
     * @param isShowNum 是否显示数字
     * @param num 显示的数字：整型
     *
     */
    public void addNumShortCut(Class<?> clazz, String appName, int iconResId, boolean isShowNum, int num) {
        SharedPreferenceUtil.put(mContext, CacheConstant.XML_MESSAGE_UNREAD_NUM_FOR_MIUI, num);
        Log.e(TAG, "manufacturer=" + Build.MANUFACTURER + "  " + Build.MODEL);
        //500ms后操作 避免频繁请求
        mNum = num;
        if (mIsCanDo) {
            mIsCanDo = false;
            Constant.EXECUTOR_NOTICE.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(DELAY_TIME);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    mIsCanDo = true;
                    if (Build.MANUFACTURER.equalsIgnoreCase("Xiaomi")) {
                        //MIUI6和以下版本   新版本小米系统自动根据通知栏显示，不做处理
                        //                sendToXiaoMi(context, clazz, Integer.valueOf(num));
                    } else if (Build.MANUFACTURER.equalsIgnoreCase("samsung")) {
                        //三星
                        samsungShortCut(mNum);
                    } else if (Build.MANUFACTURER.equalsIgnoreCase("vivo")) {
                        //vivo
                        vivoShortCut(mNum);
                    } else if (Build.MANUFACTURER.equalsIgnoreCase("sony")) {
                        sonyShortCut(mNum);
                    } else if (Build.MANUFACTURER.equalsIgnoreCase("ZUK")) {
                        sendToZUK(Integer.valueOf(mNum));
                    } else {//其他原生系统手机
                        installRawShortCut(clazz, appName, iconResId, isShowNum, mNum, isAddShortCut());
                    }
                }
            });
        }
    }


    /***
     * 在vivo应用图标的快捷方式上加数字<br>
     * @param num 显示的数字：大于99，为"99"，当为""时，不显示数字，相当于隐藏了)<br><br>
     * 注意点：
     *
     */
    private void vivoShortCut(int num) {
        Log.e(TAG, "vivoShortCut...." + num);
        try {
            Intent localIntent = new Intent("launcher.action.CHANGE_APPLICATION_NOTIFICATION_NUM");
            localIntent.putExtra("packageName", mContext.getPackageName());
            localIntent.putExtra("className", getLauncherClassName(mContext));
            if (num > 0) {
                if (num > 99) {
                    num = 99;
                }
            } else {
                num = 0;
            }
            localIntent.putExtra("notificationNum", num);
            mContext.sendBroadcast(localIntent);
            Log.e(TAG, "vivoShortCut...." + num);
        } catch (Exception e) {
            if (Constant.DEBUG)
                e.printStackTrace();
        }
    }

    //获取类名
    private String getLauncherClassName(Context context) {
        PackageManager packageManager = context.getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setPackage(context.getPackageName());
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        ResolveInfo info = packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
        if (info == null) {
            info = packageManager.resolveActivity(intent, 0);
        }
        return info.activityInfo.name;
    }

    /**
     * 向小米手机发送未读消息数广播
     *
     * @param count
     */
    private void sendToXiaoMi(Context context, Class clazz, int count) {
        Log.e(TAG, "sendToXiaoMi...." + count);

        try {
            Class miuiNotificationClass = Class.forName("android.app.MiuiNotification");
            Object miuiNotification = miuiNotificationClass.newInstance();
            Field field = miuiNotification.getClass().getDeclaredField("messageCount");
            field.setAccessible(true);
            field.set(miuiNotification, count);  // 设置信息数-->这种发送必须是miui 6才行
        } catch (Exception e) {
            if (Constant.DEBUG)
                e.printStackTrace();
            try {
                // miui 6之前的版本
                Intent localIntent = new Intent(
                        "android.intent.action.APPLICATION_MESSAGE_UPDATE");
                localIntent.putExtra(
                        "android.intent.extra.update_application_component_name",
                        context.getPackageName() + "/" + clazz.getName());
                localIntent.putExtra(
                        "android.intent.extra.update_application_message_text", String.valueOf(count == 0 ? "" : count));
                context.sendBroadcast(localIntent);
            } catch (Exception e2) {
                if (Constant.DEBUG)
                    e2.printStackTrace();
            }
        }
    }

    /***
     * 索尼手机：应用图标的快捷方式上加数字
     * @param num
     */
    private void sonyShortCut(int num) {
        Log.e(TAG, "sonyShortCut...." + num);
        try {
            String activityName = getLaunchActivityName(mContext);
            if (activityName == null) {
                return;
            }
            Intent localIntent = new Intent();
            boolean isShow = true;
            if (num < 1) {
                num = 0;
                isShow = false;
            } else if (num > 99) {
                num = 99;
            }
            localIntent.putExtra("com.sonyericsson.home.intent.extra.badge.SHOW_MESSAGE", isShow);
            localIntent.setAction("com.sonyericsson.home.action.UPDATE_BADGE");
            localIntent.putExtra("com.sonyericsson.home.intent.extra.badge.ACTIVITY_NAME", activityName);
            localIntent.putExtra("com.sonyericsson.home.intent.extra.badge.MESSAGE", num);
            localIntent.putExtra("com.sonyericsson.home.intent.extra.badge.PACKAGE_NAME", mContext.getPackageName());
            mContext.sendBroadcast(localIntent);
        } catch (Exception e) {
            if (Constant.DEBUG)
                e.printStackTrace();
        }
    }

    /***
     * 三星手机：应用图标的快捷方式上加数字
     * @param num
     */
    private void samsungShortCut(int num) {
        Log.e(TAG, "samsungShortCut...." + num);
        try {
            if (num < 1) {
                num = 0;
            } else if (num > 99) {
                num = 99;
            }
            String activityName = getLaunchActivityName(mContext);
            Intent localIntent = new Intent("android.intent.action.BADGE_COUNT_UPDATE");
            localIntent.putExtra("badge_count", num);
            localIntent.putExtra("badge_count_package_name", mContext.getPackageName());
            localIntent.putExtra("badge_count_class_name", activityName);
            mContext.sendBroadcast(localIntent);
        } catch (Exception e) {
            if (Constant.DEBUG)
                e.printStackTrace();
        }
    }


    /***
     *
     * &#x751f;&#x6210;&#x6709;&#x6570;&#x5b57;&#x7684;&#x56fe;&#x7247;(&#x6ca1;&#x6709;&#x8fb9;&#x6846;)
     * @param context
     * @param icon &#x56fe;&#x7247;
     * @param isShowNum &#x662f;&#x5426;&#x8981;&#x7ed8;&#x5236;&#x6570;&#x5b57;
     * @param num &#x6570;&#x5b57;&#x5b57;&#x7b26;&#x4e32;&#xff1a;&#x6574;&#x578b;&#x6570;&#x5b57; &#x8d85;&#x8fc7;99&#xff0c;&#x663e;&#x793a;&#x4e3a;"99+"
     * @return
     */
    private Bitmap generatorNumIcon(Context context, Bitmap icon, boolean isShowNum, String num) {

        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        //基准屏幕密度
        float baseDensity = 1.5f;//240dpi
        float factor = dm.density / baseDensity;

        Log.e(TAG, "density:" + dm.density);
        Log.e(TAG, "dpi:" + dm.densityDpi);
        Log.e(TAG, "factor:" + factor);

        // 初始化画布
        int iconSize = (int) context.getResources().getDimension(android.R.dimen.app_icon_size);
        Bitmap numIcon = Bitmap.createBitmap(iconSize, iconSize, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(numIcon);

        // 拷贝图片
        Paint iconPaint = new Paint();
        iconPaint.setDither(true);// 防抖动
        iconPaint.setFilterBitmap(true);// 用来对Bitmap进行滤波处理，这样，当你选择Drawable时，会有抗锯齿的效果
        Rect src = new Rect(0, 0, icon.getWidth(), icon.getHeight());
        Rect dst = new Rect(0, 0, iconSize, iconSize);
        canvas.drawBitmap(icon, src, dst, iconPaint);

        if (isShowNum) {

            if (TextUtils.isEmpty(num)) {
                num = "0";
            }

            if (!TextUtils.isDigitsOnly(num)) {
                //非数字
                Log.e(TAG, "the num is not digit :" + num);
                num = "0";
            }

            int numInt = Integer.valueOf(num);

            if (numInt > 99) {//超过99

                num = "99+";

                // 启用抗锯齿和使用设备的文本字体大小
                Paint numPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DEV_KERN_TEXT_FLAG);
                numPaint.setColor(Color.WHITE);
                numPaint.setTextSize(20f * factor);
                numPaint.setTypeface(Typeface.DEFAULT_BOLD);
                int textWidth = (int) numPaint.measureText(num, 0, num.length());

                Log.e(TAG, "text width:" + textWidth);

                int circleCenter = (int) (15 * factor);//中心坐标
                int circleRadius = (int) (13 * factor);//圆的半径

                //绘制左边的圆形
                Paint leftCirPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                leftCirPaint.setColor(Color.RED);
                canvas.drawCircle(iconSize - circleRadius - textWidth + (10 * factor), circleCenter, circleRadius, leftCirPaint);

                //绘制右边的圆形
                Paint rightCirPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                rightCirPaint.setColor(Color.RED);
                canvas.drawCircle(iconSize - circleRadius, circleCenter, circleRadius, rightCirPaint);

                //绘制中间的距形
                Paint rectPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                rectPaint.setColor(Color.RED);
                RectF oval = new RectF(iconSize - circleRadius - textWidth + (10 * factor), 2 * factor, iconSize - circleRadius, circleRadius * 2 + 2 * factor);
                canvas.drawRect(oval, rectPaint);

                //绘制数字
                canvas.drawText(num, (float) (iconSize - textWidth / 2 - (24 * factor)), 23 * factor, numPaint);

            } else {//<=99

                // 启用抗锯齿和使用设备的文本字体大小
                Paint numPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DEV_KERN_TEXT_FLAG);
                numPaint.setColor(Color.WHITE);
                numPaint.setTextSize(20f * factor);
                numPaint.setTypeface(Typeface.DEFAULT_BOLD);
                int textWidth = (int) numPaint.measureText(num, 0, num.length());

                Log.e(TAG, "text width:" + textWidth);

                //绘制外面的圆形
                //Paint outCirPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                //outCirPaint.setColor(Color.WHITE);
                //canvas.drawCircle(iconSize - 15, 15, 15, outCirPaint);

                //绘制内部的圆形
                Paint inCirPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                inCirPaint.setColor(Color.RED);
                canvas.drawCircle(iconSize - 15 * factor, 15 * factor, 15 * factor, inCirPaint);

                //绘制数字
                canvas.drawText(num, (float) (iconSize - textWidth / 2 - 15 * factor), 22 * factor, numPaint);
            }
        }
        return numIcon;
    }


    /***
     *
     * 生成有数字的图片(没有边框)
     * @param context
     * @param icon 图片
     * @param isShowNum 是否要绘制数字
     * @param num 数字字符串：整型数字 超过99，显示为"99+"
     * @return
     */
    private Bitmap generatorNumIcon2(Context context, Bitmap icon, boolean isShowNum, String num) {

        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        //基准屏幕密度
        float baseDensity = 1.5f;//240dpi
        float factor = dm.density / baseDensity;

        Log.e(TAG, "density:" + dm.density);
        Log.e(TAG, "dpi:" + dm.densityDpi);
        Log.e(TAG, "factor:" + factor);

        // 初始化画布
        int iconSize = (int) context.getResources().getDimension(android.R.dimen.app_icon_size);
        Bitmap numIcon = Bitmap.createBitmap(iconSize, iconSize, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(numIcon);

        // 拷贝图片
        Paint iconPaint = new Paint();
        iconPaint.setDither(true);// 防抖动
        iconPaint.setFilterBitmap(true);// 用来对Bitmap进行滤波处理，这样，当你选择Drawable时，会有抗锯齿的效果
        Rect src = new Rect(0, 0, icon.getWidth(), icon.getHeight());
        Rect dst = new Rect(0, 0, iconSize, iconSize);
        canvas.drawBitmap(icon, src, dst, iconPaint);

        if (isShowNum) {

            if (TextUtils.isEmpty(num)) {
                num = "0";
            }

            if (!TextUtils.isDigitsOnly(num)) {
                //非数字
                Log.e(TAG, "the num is not digit :" + num);
                num = "0";
            }

            int numInt = Integer.valueOf(num);

            if (numInt > 99) {//超过99
                num = "99+";
            }

            //启用抗锯齿和使用设备的文本字体大小
            //测量文本占用的宽度
            Paint numPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DEV_KERN_TEXT_FLAG);
            numPaint.setColor(Color.WHITE);
            numPaint.setTextSize(20f * factor);
            numPaint.setTypeface(Typeface.DEFAULT_BOLD);
            int textWidth = (int) numPaint.measureText(num, 0, num.length());
            Log.e(TAG, "text width:" + textWidth);

            /**----------------------------------*
             * TODO 绘制圆角矩形背景 start
             *------------------------------------*/
            //圆角矩形背景的宽度
            int backgroundHeight = (int) (2 * 15 * factor);
            int backgroundWidth = textWidth > backgroundHeight ? (int) (textWidth + 10 * factor) : backgroundHeight;

            canvas.save();//保存状态

            ShapeDrawable drawable = getDefaultBackground(context);
            drawable.setIntrinsicHeight(backgroundHeight);
            drawable.setIntrinsicWidth(backgroundWidth);
            drawable.setBounds(0, 0, backgroundWidth, backgroundHeight);
            canvas.translate(iconSize - backgroundWidth, 0);
            drawable.draw(canvas);

            canvas.restore();//重置为之前保存的状态

            /**----------------------------------*
             * TODO 绘制圆角矩形背景 end
             *------------------------------------*/

            //绘制数字
            canvas.drawText(num, (float) (iconSize - (backgroundWidth + textWidth) / 2), 22 * factor, numPaint);
        }
        return numIcon;
    }

    /***
     *
     * 生成有数字的图片(有边框)
     * @param context
     * @param icon 图片
     * @param isShowNum 是否要绘制数字
     * @param num 数字字符串：整型数字 超过99，显示为"99+"
     * @return
     */
    private Bitmap generatorNumIcon3(Context context, Bitmap icon, boolean isShowNum, String num) {

        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        //基准屏幕密度
        float baseDensity = 1.5f;//240dpi
        float factor = dm.density / baseDensity;

        Log.e(TAG, "density:" + dm.density);
        Log.e(TAG, "dpi:" + dm.densityDpi);
        Log.e(TAG, "factor:" + factor);

        // 初始化画布
        int iconSize = (int) context.getResources().getDimension(android.R.dimen.app_icon_size);
        Bitmap numIcon = Bitmap.createBitmap(iconSize, iconSize, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(numIcon);

        // 拷贝图片
        Paint iconPaint = new Paint();
        iconPaint.setDither(true);// 防抖动
        iconPaint.setFilterBitmap(true);// 用来对Bitmap进行滤波处理，这样，当你选择Drawable时，会有抗锯齿的效果
        Rect src = new Rect(0, 0, icon.getWidth(), icon.getHeight());
        Rect dst = new Rect(0, 0, iconSize, iconSize);
        canvas.drawBitmap(icon, src, dst, iconPaint);

        if (isShowNum) {

            if (TextUtils.isEmpty(num)) {
                num = "0";
            }

            if (!TextUtils.isDigitsOnly(num)) {
                //非数字
                Log.e(TAG, "the num is not digit :" + num);
                num = "0";
            }

            int numInt = Integer.valueOf(num);

            if (numInt > 99) {//超过99
                num = "99+";
            }

            //启用抗锯齿和使用设备的文本字体大小
            //测量文本占用的宽度
            Paint numPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DEV_KERN_TEXT_FLAG);
            numPaint.setColor(Color.WHITE);
            numPaint.setTextSize(20f * factor);
            numPaint.setTypeface(Typeface.DEFAULT_BOLD);
            int textWidth = (int) numPaint.measureText(num, 0, num.length());
            Log.e(TAG, "text width:" + textWidth);

            /**----------------------------------*
             * TODO 绘制圆角矩形背景：先画边框，再画内部的圆角矩形 start
             *------------------------------------*/
            //圆角矩形背景的宽度
            int backgroundHeight = (int) (2 * 15 * factor);
            int backgroundWidth = textWidth > backgroundHeight ? (int) (textWidth + 10 * factor) : backgroundHeight;
            //边框的宽度
            int strokeThickness = (int) (2 * factor);

            canvas.save();//保存状态

            int strokeHeight = backgroundHeight + strokeThickness * 2;
            int strokeWidth = textWidth > strokeHeight ? (int) (textWidth + 10 * factor + 2 * strokeThickness) : strokeHeight;
            ShapeDrawable outStroke = getDefaultStrokeDrawable(context);
            outStroke.setIntrinsicHeight(strokeHeight);
            outStroke.setIntrinsicWidth(strokeWidth);
            outStroke.setBounds(0, 0, strokeWidth, strokeHeight);
            canvas.translate(iconSize - strokeWidth - strokeThickness, strokeThickness);
            outStroke.draw(canvas);

            canvas.restore();//重置为之前保存的状态

            canvas.save();//保存状态

            ShapeDrawable drawable = getDefaultBackground(context);
            drawable.setIntrinsicHeight((int) (backgroundHeight + 2 * factor));
            drawable.setIntrinsicWidth((int) (backgroundWidth + 2 * factor));
            drawable.setBounds(0, 0, backgroundWidth, backgroundHeight);
            canvas.translate(iconSize - backgroundWidth - 2 * strokeThickness, 2 * strokeThickness);
            drawable.draw(canvas);

            canvas.restore();//重置为之前保存的状态

            /**----------------------------------*
             * TODO 绘制圆角矩形背景 end
             *------------------------------------*/

            //绘制数字
            canvas.drawText(num, (float) (iconSize - (backgroundWidth + textWidth + 4 * strokeThickness) / 2), (22) * factor + 2 * strokeThickness, numPaint);
        }
        return numIcon;
    }

    /***
     *
     * 生成有数字的图片(有边框的)
     * @param context
     * @param icon 图片
     * @param isShowNum 是否要绘制数字
     * @param num 数字字符串：整型数字 超过99，显示为"99+"
     * @return
     */
    private Bitmap generatorNumIcon4(Context context, Bitmap icon, boolean isShowNum, String num) {

        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        //基准屏幕密度
        float baseDensity = 1.5f;//240dpi
        float factor = dm.density / baseDensity;

        Log.e(TAG, "density:" + dm.density);
        Log.e(TAG, "dpi:" + dm.densityDpi);
        Log.e(TAG, "factor:" + factor);

        // 初始化画布
        int iconSize = (int) context.getResources().getDimension(android.R.dimen.app_icon_size);
        Bitmap numIcon = Bitmap.createBitmap(iconSize, iconSize, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(numIcon);

        // 拷贝图片
        Paint iconPaint = new Paint();
        iconPaint.setDither(true);// 防抖处理
        iconPaint.setFilterBitmap(true);// 用来对Bitmap进行滤波处理，这样，当你选择Drawable时，会有抗锯齿的效果
        Rect src = new Rect(0, 0, icon.getWidth(), icon.getHeight());
        Rect dst = new Rect(0, 0, iconSize, iconSize);
        canvas.drawBitmap(icon, src, dst, iconPaint);

        if (isShowNum) {

            if (TextUtils.isEmpty(num)) {
                num = "0";
            }

            if (!TextUtils.isDigitsOnly(num)) {
                //非数字
                Log.e(TAG, "the num is not digit :" + num);
                num = "0";
            }

            int numInt = Integer.valueOf(num);

            if (numInt > 99) {//超过99
                num = "99+";
            }

            //启用抗锯齿和使用设备的文本字体
            //测量文本占用的宽度
            Paint numPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DEV_KERN_TEXT_FLAG);
            numPaint.setColor(Color.WHITE);
            numPaint.setTextSize(25f * factor);
            numPaint.setTypeface(Typeface.DEFAULT_BOLD);
            int textWidth = (int) numPaint.measureText(num, 0, num.length());
            Log.e(TAG, "text width:" + textWidth);

            /**----------------------------------*
             * TODO 绘制圆角矩形背景 start
             *------------------------------------*/
            //边框的宽度
            int strokeThickness = (int) (DEFAULT_STROKE_WIDTH_DIP * factor);
            //圆角矩形背景的宽度
            float radiusPx = 15 * factor;
            int backgroundHeight = (int) (2 * (radiusPx + strokeThickness));//2*(半径+边框宽度)
            int backgroundWidth = textWidth > backgroundHeight ? (int) (textWidth + 10 * factor + 2 * strokeThickness) : backgroundHeight;

            canvas.save();//保存状态

            ShapeDrawable drawable = getDefaultBackground2(context);
            drawable.setIntrinsicHeight(backgroundHeight);
            drawable.setIntrinsicWidth(backgroundWidth);
            drawable.setBounds(0, 0, backgroundWidth, backgroundHeight);
            canvas.translate(iconSize - backgroundWidth - strokeThickness, 2 * strokeThickness);
            drawable.draw(canvas);

            canvas.restore();//重置为之前保存的状态

            /**----------------------------------*
             * TODO 绘制圆角矩形背景 end
             *------------------------------------*/

            //绘制数字
            canvas.drawText(num, (float) (iconSize - (backgroundWidth + textWidth + 2 * strokeThickness) / 2), (float) (25 * factor + 2.5 * strokeThickness), numPaint);
        }
        return numIcon;
    }

    /***
     * 创建原生系统的快捷方式
     * @param clazz 启动的activity
     * @param isShowNum 是否显示数字
     * @param num 显示的数字：整型
     * @param isStroke 是否加上边框
     */

    private void installRawShortCut(Class<?> clazz, String appName, int iconResId, boolean isShowNum, int num, boolean isStroke) {
        Log.e(TAG, "installShortCut....");
        try {
            Intent shortcutIntent = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
            //名称
            shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, appName);

            // 是否可以有多个快捷方式的副本，参数如果是true就可以生成多个快捷方式，如果是false就不会重复添加
            shortcutIntent.putExtra("duplicate", false);

            //点击快捷方式：打开activity
            Intent mainIntent = new Intent(Intent.ACTION_MAIN);
            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            mainIntent.setClass(mContext, clazz);
            shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, mainIntent);

            //快捷方式的图标
            if (isStroke) {
                shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON,
                        generatorNumIcon4(
                                mContext,
                                ((BitmapDrawable) CommonUtil.getDrawable(mContext, iconResId)).getBitmap(),
                                isShowNum,
                                num + ""));
            } else {
                shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON,
                        generatorNumIcon2(
                                mContext,
                                ((BitmapDrawable) CommonUtil.getDrawable(mContext, iconResId)).getBitmap(),
                                isShowNum,
                                num + ""));
            }
            mContext.sendBroadcast(shortcutIntent);
        } catch (Exception e) {
            if (Constant.DEBUG)
                e.printStackTrace();
        }
    }

    //ZUK
    private void sendToZUK(int num) {
        LogF.d(TAG, "zuk图标" + num);
        try {
            Bundle extra = new Bundle();
            ArrayList<String> ids = new ArrayList<String>();
            // 以列表形式传递快捷方式id，可以添加多个快捷方式id
//        ids.add("custom_id_1");
//        ids.add("custom_id_2");
            extra.putStringArrayList("app_shortcut_custom_id", null);
            extra.putInt("app_badge_count", num);
            Bundle b = null;
            b = mContext.getContentResolver().call(Uri.parse("content://" + "com.android.badge" + "/" + "badge"), "setAppBadgeCount", null, extra);
        } catch (Exception e) {
            if (Constant.DEBUG)
                e.printStackTrace();
        }
    }


    /***
     * 是否已经创建了快捷方式
     * @return
     */
    private boolean isAddShortCut() {
        Log.e(TAG, "isAddShortCut....");
        boolean isInstallShortcut = false;
        try {
            final ContentResolver cr = mContext.getContentResolver();
            //TODO 注释的代码，在有的手机：修改了ROM的系统，不能支持
            /*int versionLevel = android.os.Build.VERSION.SDK_INT;
                        String AUTHORITY = "com.android.launcher2.settings";
                        //2.2以上的系统的文件文件名字是不一样的
                        if (versionLevel >= 8) {
                            AUTHORITY = "com.android.launcher2.settings";
                        } else {
                            AUTHORITY = "com.android.launcher.settings";
                        }*/

            String AUTHORITY = getAuthorityFromPermission(mContext, "com.android.launcher.permission.READ_SETTINGS");
            Log.e(TAG, "AUTHORITY  :  " + AUTHORITY);
            final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
                    + "/favorites?notify=true");

            Cursor c = cr.query(CONTENT_URI,
                    new String[]{"title"}, "title=?",
                    new String[]{mContext.getString(R.string.app_name)}, null);

            if (c != null && c.getCount() > 0) {
                isInstallShortcut = true;
            }

            if (c != null) {
                c.close();
            }

            Log.e(TAG, "isAddShortCut....isInstallShortcut=" + isInstallShortcut);
        } catch (Exception e) {
            if (Constant.DEBUG)
                e.printStackTrace();
        }
        return isInstallShortcut;
    }

    /**
     * 删除快捷方式
     *
     * @param appName
     * @param clazz
     */
    public void deleteShortCut(String appName, Class<?> clazz) {
        Log.e(TAG, "delShortcut....");
        SharedPreferenceUtil.put(mContext, CacheConstant.XML_MESSAGE_UNREAD_NUM_FOR_MIUI, 0);
        if (Build.MANUFACTURER.equalsIgnoreCase("Xiaomi")) {
            //小米
            //当为""时，不显示数字，相当于隐藏了)
//                sendToXiaoMi(context, clazz, 0);
        } else if (Build.MANUFACTURER.equalsIgnoreCase("samsung")) {
            //三星
            samsungShortCut(0);
        } else if (Build.MANUFACTURER.equalsIgnoreCase("samsung")) {
            //samsung
            vivoShortCut(0);
        } else if (Build.MANUFACTURER.equalsIgnoreCase("sony")) {
            //sony
            sonyShortCut(0);
        } else if (Build.MANUFACTURER.equalsIgnoreCase("ZUK")) {
            //ZUK
            sendToZUK(0);
        } else {//其他原生系统手机
            //删除显示数字的快捷方式
            deleteRawShortCut(appName, clazz);
            //安装不显示数字的快捷方式
            //installRawShortCut(context, clazz, false, "0");
        }
    }

    /***
     * 删除原生系统的快捷方式
     * @param clazz 启动的activity
     */
    private void deleteRawShortCut(String appName, Class<?> clazz) {
        Intent intent = new Intent("com.android.launcher.action.UNINSTALL_SHORTCUT");
        //快捷方式的名称
        intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, mContext.getString(R.string.app_name));

        Intent intent2 = new Intent();
        intent2.setClass(mContext, clazz);
        intent2.setAction(Intent.ACTION_MAIN);
        intent2.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, intent2);

        mContext.sendBroadcast(intent);
    }


    /***
     * 取得权限相应的认证URI
     * @param context
     * @param permission
     * @return
     */
    private String getAuthorityFromPermission(Context context, String permission) {
        if (TextUtils.isEmpty(permission)) {
            return null;
        }
        List<PackageInfo> packInfos = context.getPackageManager().getInstalledPackages(PackageManager.GET_PROVIDERS);
        if (packInfos == null) {
            return null;
        }
        for (PackageInfo info : packInfos) {
            ProviderInfo[] providers = info.providers;
            if (providers != null) {
                for (ProviderInfo provider : providers) {
                    if (permission.equals(provider.readPermission)
                            || permission.equals(provider.writePermission)) {
                        return provider.authority;
                    }
                }
            }
        }
        return null;
    }


    /***
     * 取得当前应用的启动activity的名称：
     * mainfest.xml中配置的 android:name:"
     * @param context
     * @return
     */
    private String getLaunchActivityName(Context context) {
        PackageManager localPackageManager = context.getPackageManager();
        Intent localIntent = new Intent("android.intent.action.MAIN");
        localIntent.addCategory("android.intent.category.LAUNCHER");
        try {
            Iterator<ResolveInfo> localIterator = localPackageManager.queryIntentActivities(localIntent, 0).iterator();
            while (localIterator.hasNext()) {
                ResolveInfo localResolveInfo = localIterator.next();
                if (!localResolveInfo.activityInfo.applicationInfo.packageName.equalsIgnoreCase(context.getPackageName()))
                    continue;
                String str = localResolveInfo.activityInfo.name;
                return str;
            }
        } catch (Exception localException) {
            return null;
        }
        return null;
    }

    /***
     * 得到一个默认的背景：圆角矩形<br><br>
     * 使用代码来生成一个背景：相当于用<shape>的xml的背景
     *
     * @return
     */
    private ShapeDrawable getDefaultBackground(Context context) {

        //这个是为了应对不同分辨率的手机，屏幕兼容性
        int r = DeviceUtils.dpToPx(context, DEFAULT_CORNER_RADIUS_DIP);
        float[] outerR = new float[]{r, r, r, r, r, r, r, r};

        //圆角矩形
        RoundRectShape rr = new RoundRectShape(outerR, null, null);
        ShapeDrawable drawable = new ShapeDrawable(rr);
        drawable.getPaint().setColor(DEFAULT_NUM_COLOR);//设置颜色
        return drawable;

    }

    /***
     * 得到一个默认的背景：圆角矩形<br><br>
     * 使用代码来生成一个背景：相当于用<shape>的xml的背景
     *
     * @return
     */
    private ShapeDrawable getDefaultBackground2(Context context) {

        //这个是为了应对不同分辨率的手机，屏幕兼容性
        int r = DeviceUtils.dpToPx(context, DEFAULT_CORNER_RADIUS_DIP);
        float[] outerR = new float[]{r, r, r, r, r, r, r, r};
        int distance = DeviceUtils.dpToPx(context, DEFAULT_STROKE_WIDTH_DIP);

        //圆角矩形
        RoundRectShape rr = new RoundRectShape(outerR, null, null);
        ShapeDrawable drawable = new ShapeDrawable();
//        drawable.getFillpaint().setColor(DEFAULT_NUM_COLOR);//设置填充颜色
//        drawable.getStrokepaint().setColor(DEFAULT_STROKE_COLOR);//设置边框颜色
//        drawable.getStrokepaint().setStrokeWidth(distance);//设置边框宽度
        return drawable;

    }


    /***
     * 得到一个默认的背景：圆角矩形<br><br>
     * 使用代码来生成一个背景：相当于用<shape>的xml的背景
     *
     * @return
     */
    private ShapeDrawable getDefaultStrokeDrawable(Context context) {

        //这个是为了应对不同分辨率的手机，屏幕兼容性
        int r = DeviceUtils.dpToPx(context, DEFAULT_CORNER_RADIUS_DIP);
        int distance = DeviceUtils.dpToPx(context, DEFAULT_STROKE_WIDTH_DIP);
        float[] outerR = new float[]{r, r, r, r, r, r, r, r};

        //圆角矩形
        RoundRectShape rr = new RoundRectShape(outerR, null, null);
        ShapeDrawable drawable = new ShapeDrawable(rr);
        drawable.getPaint().setStrokeWidth(distance);
        drawable.getPaint().setStyle(Paint.Style.FILL);
        drawable.getPaint().setColor(DEFAULT_STROKE_COLOR);//设置颜色
        return drawable;
    }

    //小米
    public void notifyXiaomi(NotificationManager notificationManager, Notification notification, int notifyId, int num) {
        SharedPreferenceUtil.put(mContext, CacheConstant.XML_MESSAGE_UNREAD_NUM_FOR_MIUI, num);
        Log.e(TAG, "小米手机推送角标" + num);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    Field field = notification.getClass().getDeclaredField("extraNotification");
                    Object extraNotification = field.get(notification);
                    Method method = extraNotification.getClass().getDeclaredMethod("setMessageCount", int.class);
                    method.invoke(extraNotification, num);
                } catch (Exception e) {
                    e.printStackTrace();
                    LogF.e("Xiaomi" + " Badge error", "set Badge failed");
                }
                if (num != 0) notificationManager.notify(notifyId, notification);
                else notificationManager.cancel(notifyId);
            }
        }, 550);
    }
}