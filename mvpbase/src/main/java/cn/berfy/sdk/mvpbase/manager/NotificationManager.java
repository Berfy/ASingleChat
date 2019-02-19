package cn.berfy.sdk.mvpbase.manager;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.widget.RemoteViews;

import java.util.ArrayList;
import java.util.List;

import cn.berfy.sdk.mvpbase.R;
import cn.berfy.sdk.mvpbase.model.Notify;
import cn.berfy.sdk.mvpbase.util.AppUtils;
import cn.berfy.sdk.mvpbase.util.DeviceUtils;
import cn.berfy.sdk.mvpbase.util.GsonUtil;
import cn.berfy.sdk.mvpbase.util.LogF;

/**
 * Created by Berfy on 2017/11/21.
 * 推送通知管理(自定义弹起通知，销毁指定tag或者id的消息)
 * 修改2019.1.8
 */
public class NotificationManager {

    private final String TAG = "NotificationManager";
    private static NotificationManager mInstance;
    private Context mContext;
    private int mIconResId = R.drawable.ic_launcher;
    private String mTitle;
    private String mContent;
    private Intent mIntent;
    private android.app.NotificationManager mNotificationManager;
    private List<Notify> mCaches = new ArrayList<>();
    private String mAndroid8Channel;

    synchronized public static NotificationManager init(Context context, Class defaultIntentClass) {
        if (null == mInstance) {
            synchronized (NotificationManager.class) {
                if (null == mInstance) {
                    mInstance = new NotificationManager(context, defaultIntentClass);
                }
            }
        }
        return mInstance;
    }

    public static NotificationManager getInstance() {
        if (null == mInstance) {
            throw new NullPointerException("没有初始化NotificationUtil");
        }
        return mInstance;
    }

    public static NotificationManager newInstance(Context context, Class defaultIntentClass) {
        return new NotificationManager(context, defaultIntentClass);
    }

    private NotificationManager(Context context, Class defaultIntentClass) {
        mContext = context;
        mNotificationManager = (android.app.NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mIntent = new Intent(context, defaultIntentClass);
        createChannel();
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // 通知渠道的id
            mAndroid8Channel = DeviceUtils.getDeviceId(mContext);
            // 用户可以看到的通知渠道的名字.
            CharSequence name = "notification channel";
            // 用户可以看到的通知渠道的描述
            String description = "notification description";
            int importance = android.app.NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(mAndroid8Channel, name, importance);
            // 配置通知渠道的属性
            channel.setBypassDnd(true);
            //设置绕过免打扰模式
            channel.canBypassDnd();
            channel.setDescription(description);
            // 设置通知出现时的闪灯（如果 android 设备支持的话）
            channel.enableLights(true);
            channel.setLightColor(Color.RED);
            // 设置通知出现时的震动（如果 android 设备支持的话）
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            //最后在notificationmanager中创建该通知渠道
            mNotificationManager.createNotificationChannel(channel);
        }
    }

    public void setIcon(int iconResId) {
        mIconResId = iconResId;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public void reset() {
        mTitle = "";
        mContent = "";
    }

    public void setContent(String content) {
        mContent = content;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getContent() {
        return mContent;
    }

    public void setIntent(Intent intent) {
        mIntent = intent;
    }

    public void notify(int id) {
        if (TextUtils.isEmpty(mContent)) {
            LogF.d(TAG, "通知==> 没有内容  不予显示" + mContent);
            mContent = "";
        }
        if (TextUtils.isEmpty(mTitle)) {
            mTitle = mContext.getString(R.string.app_name);
        }
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, id, mIntent, PendingIntent
                .FLAG_ONE_SHOT);
        LogF.d(TAG, "通知==>" + mTitle);
        Notification msgNotification = makeNotification(null, null, null,
                true, pendingIntent, mTitle, mContent, mContent, mIconResId, true, true);
        mNotificationManager.notify(id, msgNotification);
        addCache(new Notify("", id));
    }

    public void notify(int id, RemoteViews remoteView, RemoteViews remoteViewBig,
                       RemoteViews remoteViewHeadUp, Boolean isAutoCancel) {
        if (TextUtils.isEmpty(mContent)) {
            LogF.d(TAG, "通知==> 没有内容  不予显示" + mContent);
            mContent = "";
        }
        if (TextUtils.isEmpty(mTitle)) {
            mTitle = mContext.getString(R.string.app_name);
        }
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, id, mIntent, PendingIntent
                .FLAG_ONE_SHOT);
        LogF.d(TAG, "通知==>" + mTitle);
        Notification msgNotification = makeNotification(remoteView, remoteViewBig, remoteViewHeadUp,
                isAutoCancel, pendingIntent, mTitle, mContent, mContent, mIconResId, true, true);
        mNotificationManager.notify(id, msgNotification);
        addCache(new Notify("", id));
    }

    public void notify(String tag) {
        if (!AppUtils.isBackground(mContext)) {
            return;
        }
        if (TextUtils.isEmpty(mContent)) {
            return;
        }
        if (TextUtils.isEmpty(mTitle)) {
            mTitle = mContext.getString(R.string.app_name);
        }
        int id = (int) System.currentTimeMillis();
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, id, mIntent, PendingIntent
                .FLAG_ONE_SHOT);
        LogF.d(TAG, "通知==>" + mTitle);
        Notification msgNotification = makeNotification(null, null, null,
                true, pendingIntent, mTitle, mContent, mContent, mIconResId, true, true);
        mNotificationManager.notify(tag + "", id, msgNotification);
        addCache(new Notify(tag, id));
    }

    public void notify(String tag, RemoteViews remoteView, RemoteViews remoteViewBig,
                       RemoteViews remoteViewHeadUp, Boolean isAutoCancel) {
        if (TextUtils.isEmpty(mContent)) {
            return;
        }
        if (TextUtils.isEmpty(mTitle)) {
            mTitle = mContext.getString(R.string.app_name);
        }
        int id = (int) System.currentTimeMillis();
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, id, mIntent, PendingIntent
                .FLAG_ONE_SHOT);
        LogF.d(TAG, "通知==>" + mTitle);
        Notification msgNotification = makeNotification(remoteView, remoteViewBig, remoteViewHeadUp,
                isAutoCancel, pendingIntent, mTitle, mContent, mContent, mIconResId, true, true);
        mNotificationManager.notify(tag + "", id, msgNotification);
        addCache(new Notify(tag, id));
    }

    /**
     * @param remoteView
     */
    private Notification makeNotification(RemoteViews remoteView, RemoteViews remoteViewBig,
                                          RemoteViews remoteViewHeadUp, Boolean isAutoCancel, PendingIntent pendingIntent,
                                          String title, String content, String tickerText,
                                          int iconId, boolean ring, boolean vibrate) {
        NotificationCompat.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = new NotificationCompat.Builder(mContext, mAndroid8Channel);
        } else {
            builder = new NotificationCompat.Builder(mContext);
        }

        builder.setAutoCancel(isAutoCancel)
                .setContentIntent(pendingIntent)
                .setTicker(tickerText)
                .setSmallIcon(iconId);

        if (null != remoteView) {
            builder.setCustomContentView(remoteView);
        }
        if (null != remoteViewBig) {
            builder.setCustomContentView(remoteViewBig);
        }
        if (null != remoteViewHeadUp) {
            builder.setCustomContentView(remoteViewHeadUp);
        }
        if (!TextUtils.isEmpty(title)) {
            builder.setContentTitle(title);
        }
        if (!TextUtils.isEmpty(content)) {
            builder.setContentText(content);
        }
        int defaults = Notification.DEFAULT_LIGHTS;
        if (vibrate) {
            defaults |= Notification.DEFAULT_VIBRATE;
        }

        if (ring) {
            defaults |= Notification.DEFAULT_SOUND;
        }

        builder.setDefaults(defaults);
        return builder.build();
    }

    private void addCache(Notify notify) {
        LogF.d(TAG, "通知缓存 " + GsonUtil.getInstance().toJson(notify) + "  size=" + mCaches.size());
        boolean isHas = false;
        for (Notify cache : mCaches) {
            if (cache.equals(notify)) {
                isHas = true;
            }
        }
        if (!isHas) {
            mCaches.add(notify);
        }
        LogF.d(TAG, "通知缓存后大小 " + mCaches.size());
    }

    private void removeCache(Notify notify) {
        mCaches.remove(notify);
    }

    public void clearNotify(String tag) {
        LogF.d(TAG, "清除通知缓存 tag=" + tag);
        int size = mCaches.size();
        if (size == 0) {
            return;
        }
        for (int i = size - 1; i >= 0; i--) {
            Notify cache = mCaches.get(i);
            if (tag.equals(cache.getTag())) {
                LogF.d(TAG, "清除通知缓存 删除  id=" + cache.getId() + " tag=" + tag);
                mNotificationManager.cancel(tag, cache.getId());
                removeCache(cache);
                continue;
            }
        }
    }

    public void clearNotify(int id) {
        LogF.d(TAG, "清除通知缓存 id=" + id);
        int size = mCaches.size();
        if (size == 0) {
            return;
        }
        for (int i = size - 1; i >= 0; i--) {
            Notify cache = mCaches.get(i);
            if (id == cache.getId()) {
                LogF.d(TAG, "清除通知缓存 删除  id=" + cache.getId() + " tag=" + cache.getTag());
                mNotificationManager.cancel(cache.getId());
                removeCache(cache);
            }
        }
    }

    public void clearAll() {
        LogF.d(TAG, "清除全部通知缓存");
        int size = mCaches.size();
        if (size == 0) {
            return;
        }
        for (int i = size - 1; i >= 0; i--) {
            Notify cache = mCaches.get(i);
            mNotificationManager.cancel(cache.getId());
        }
        mCaches.clear();
        mNotificationManager.cancelAll();
    }
}
