package cn.berfy.sdk.mvpbase.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import cn.berfy.sdk.mvpbase.base.ActivityManager;
import cn.berfy.sdk.mvpbase.config.CacheConstant;

/**
 * 捕获整个应用的异常
 * Created by Rothschild on 2016-08-08.
 */
public class CrashException implements Thread.UncaughtExceptionHandler {

    private CrashException() {
    }

    private static CrashException INSTANCE = new CrashException();

    public static CrashException getInstance() {
        return INSTANCE;
    }

    private static final String TAG = "CrashException";

    /**
     * 系统默认的UncaughtException处理类
     */
    private Thread.UncaughtExceptionHandler mDefaultHandler;

    /**
     * 错误报告文件的扩展名
     */
    private static final String CRASH_REPORTER_EXTENSION = ".txt";

    /**
     * 程序的Context对象
     */
    private Context mContext;
    private Class mSplashClass;//重启App后的入口类
    /**
     * 使用Properties来保存设备的信息和错误堆栈信息
     */
//	private Properties mDeviceCrashInfo = new Properties();
    private static final String VERSION_NAME = "versionName";
    private static final String VERSION_CODE = "versionCode";
    private static final String STACK_TRACE = "STACK_TRACE";

    // 用来存储设备信息和异常信息
    private Map<String, String> infos = new HashMap<>();

    // 用于格式化日期,作为日志文件名的一部分
    private DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.CHINA);


    private String path = CacheConstant.CRASH_FILE_DIR;


    /**
     * 是否开启日志输出,在Debug状态下开启, 在Release状态下关闭以提示程序性能
     */
    public static final boolean DEBUG = false;

    /**
     * 初始化,注册Context对象, 获取系统默认的UncaughtException处理器, 设置该CrashHandler为程序的默认处理器
     */
    public void init(Context context, Class splashClass) {
        mContext = context;
        mSplashClass = splashClass;
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        if (!handleException(throwable) && mDefaultHandler != null) {
            // 如果用户没有处理则让系统默认的异常处理器来处理
            mDefaultHandler.uncaughtException(thread, throwable);
            LogF.d(TAG, "uncaughtException" + "==true==");
        } else {
            // Sleep一会后结束程序
            LogF.d(TAG, "uncaughtException" + "==false==");
            ActivityManager.getInstance().popAllActivityExceptOne(null);
            Intent intent = new Intent(mContext, mSplashClass);
            intent.putExtra("pos", 0);
            intent.putExtra("pos", 0);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent restartIntent = PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_ONE_SHOT);
            AlarmManager mgr = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
            mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 1000,
                    restartIntent);
            android.os.Process.killProcess(android.os.Process.myPid());  //结束进程之前可以把你程序的注销或者退

        }
    }


    /**
     * 自定义错误处理,收集错误信息 ,发送错误报告等操作均在此完成. 开发者可以根据自己的情况来自定义异常处理逻辑
     *
     * @param ex Throwable
     * @return true:如果处理了该异常信息;否则返回false
     */
    private boolean handleException(Throwable ex) {
        if (ex == null) {
            LogF.e(TAG, "handleException --- ex==null");
            return true;
        }
        LogF.e(TAG, "handleException --- 有ex");
        // 使用Toast来显示异常信息
//        new Thread() {
//            @Override
//            public void run() {
//                Looper.prepare();
//                LogF.e(TAG, "handleException --- 程序出错Toast");
//                ToastUtil.getInstances().showShort("程序出错，即将退出:\r\n");
//                Looper.loop();
//            }
//        }.start();

        //使用TalkingData自动捕获异常
//        MobclickAgent.onError(mContext, ex);

        //使用友盟统计异常
//        MobclickAgent.reportError(mContext, ex);

        LogF.e(TAG, "handleException --- 收集信息");
        // 收集设备信息
        collectCrashDeviceInfo(mContext);

        // 保存错误报告文件
        saveCrashInfoToFile(ex);

        return true;
    }

    /**
     * 收集程序崩溃的设备信息
     *
     * @param ctx 上下文
     */
    private void collectCrashDeviceInfo(Context ctx) {

        PackageManager pm = ctx.getPackageManager();

        try {
            PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(), PackageManager.GET_ACTIVITIES);

            if (pi != null) {
                // mDeviceCrashInfo.put(VERSION_NAME, pi.versionName == null ?
                // "not set" : pi.versionName);
                // mDeviceCrashInfo.put(VERSION_CODE, "" + pi.versionCode);
                infos.put(VERSION_NAME, pi.versionName == null ? "not set" : pi.versionName);
                infos.put(VERSION_CODE, "" + pi.versionCode);
            }

        } catch (PackageManager.NameNotFoundException e) {
            LogF.e(TAG, "当收集包信息时发生错误", e);
        }

        // 使用反射来收集设备信息.在Build类中包含各种设备信息,
        // 例如: 系统版本号,设备生产商 等帮助调试程序的有用信息
        Field[] fields = Build.class.getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                // mDeviceCrashInfo.put(field.getName(), "" + field.get(null));
                infos.put(field.getName(), "" + field.get(null));
                if (DEBUG) {
                    LogF.d(TAG, field.getName() + " : " + field.get(null));
                }
            } catch (Exception e) {
                e.printStackTrace();
                LogF.e(TAG, "当收集事故信息时发生错误", e);
            }
        }
    }

    /***
     * 保存错误信息到文件中
     *
     * @param ex Throwable
     */
    private String saveCrashInfoToFile(Throwable ex) {
        StringBuffer sb = new StringBuffer();
        for (Map.Entry<String, String> entry : infos.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            sb.append(key + "=" + value + "\n");
        }

        Writer info = new StringWriter();
        PrintWriter printWriter = new PrintWriter(info);
        ex.printStackTrace(printWriter);
        Throwable cause = ex.getCause();
        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        String result = info.toString();
        sb.append(result);
        printWriter.close();

        infos.put("EXCEPTION", ex.getLocalizedMessage());
        infos.put(STACK_TRACE, result);
        try {

            long timestamp = System.currentTimeMillis();
            String time = formatter.format(new Date());
            String fileName = "crash-" + time + "-" + timestamp + CRASH_REPORTER_EXTENSION;

            if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {

                File dir = new File(path);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                FileOutputStream fos = new FileOutputStream(path + File.separator + fileName);
                fos.write(sb.toString().getBytes());
                fos.close();
            }

            return fileName;
        } catch (Exception e) {
            LogF.e(TAG, "an error occured while writing report file...", e);
        }
        return null;
    }
}
