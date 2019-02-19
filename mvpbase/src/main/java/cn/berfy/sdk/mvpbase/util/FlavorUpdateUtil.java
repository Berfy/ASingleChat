package cn.berfy.sdk.mvpbase.util;

import android.content.Context;
import android.graphics.Color;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import cn.berfy.sdk.mvpbase.config.Constant;

/**
 * 渠道包市场检测更新
 */
public class FlavorUpdateUtil {
    private static final String TAG = "FlavorUpdateUtil";

    /**
     * 渠道包自动检测更新
     */
    @SuppressWarnings("unchecked")
    public static void updateFlavor(Context context, boolean forceUpdate) {
        LogF.d(TAG, "AppUtils.getChannel(App.getContext())=" + AppUtils.getChannel(context));
        if ("_360".equals(AppUtils.getChannel(context))) {
            _360Update(context, forceUpdate);
        } else if ("baidu".equals(AppUtils.getChannel(context))) {
            baiduUpdate(context, forceUpdate);
        }/* else if ("yingyongbao1".equals(AppUtils.getChannel(App.getContext()))) {
            yingYongbao(context);
        } */ else {
            if (context.getClass().getSimpleName().equals("MainActivity")) {
                zhuxinUpdate(context);
            }
        }
    }

    /**
     * 360渠道
     */
    @SuppressWarnings("unchecked")
    public static void _360Update(Context context, boolean forceUpdate) {
        try {
            Class clazz = Class.forName("com.qihoo.appstore.common.updatesdk.lib.UpdateHelper");
            Constructor constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            Object instance = constructor.newInstance();

            Method m1 = clazz.getMethod("init", Context.class, int.class);
            Method m2 = clazz.getMethod("setDebugMode", boolean.class);
            Method m3 = clazz.getMethod("autoUpdate", String.class, boolean.class, long.class);

            m1.invoke(instance, context, Color.parseColor("#0A93DB"));
            m2.invoke(instance, Constant.DEBUG);
            long intervalMillis = forceUpdate ? 1000L : 0L;//第一次调用autoUpdate出现弹窗后，如果10秒内进行第二次调用不会查询更新
            m3.invoke(instance, context.getPackageName(), forceUpdate, intervalMillis);
        } catch (Exception e) {
            LogF.e(TAG, "-----" + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 百度渠道
     */
    @SuppressWarnings("unchecked")
    private static void baiduUpdate(Context context, boolean forceUpdate) {
        try {
            Class<?> clazz = Class.forName("com.baidu.autoupdatesdk.BDAutoUpdateSDK");
            Class<?> callback = Class.forName("com.baidu.autoupdatesdk.UICheckUpdateCallback");
            BaiduInvocationHandler baiduInvocationHandler = new BaiduInvocationHandler();
            Object mObj = Proxy.newProxyInstance(context.getClassLoader(), new Class[]{callback}, baiduInvocationHandler);
            Method mMethod = clazz.getDeclaredMethod("uiUpdateAction", Context.class, callback);
            mMethod.invoke(null, context, mObj);
        } catch (Exception e) {
            LogF.e(TAG, "--------" + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 筑欣渠道提示版本更新
     *
     * @param context
     */
    private static void zhuxinUpdate(Context context) {
//        MainPresenter presenter = new MainPresenter(context);
//        presenter.updateHint();
    }

    /**
     * 百度更新（动态代理）
     */
    private static class BaiduInvocationHandler implements InvocationHandler {
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            return null;
        }
    }


}
