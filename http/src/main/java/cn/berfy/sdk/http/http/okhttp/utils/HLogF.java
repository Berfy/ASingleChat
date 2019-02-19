package cn.berfy.sdk.http.http.okhttp.utils;

import android.os.Process;
import android.util.Log;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import cn.berfy.sdk.http.config.Constant;
import cn.berfy.sdk.http.http.okhttp.utils.PriorityThreadFactory;


public class HLogF {

    private static boolean DEBUG = true;

    private static final String TAG = "LogF";
    private static final String LEVEL_V = "V";
    private static final String LEVEL_I = "I";
    private static final String LEVEL_D = "D";
    private static final String LEVEL_W = "W";
    private static final String LEVEL_E = "E";
    //    private static final ThreadPoolExecutor mExecutor = new ThreadPoolExecutor(0, 1, 5, TimeUnit.SECONDS,
//            new LinkedBlockingQueue<>(), new PriorityThreadFactory(TAG,
//            Process.THREAD_PRIORITY_BACKGROUND), (RejectedExecutionHandler) (r, executor) -> {
//        if (r instanceof FutureTask<?>) {
//            ((FutureTask<?>) r).cancel(true);
//        }
//        if (LogF.DEBUG) {
//            Log.d(TAG, "mExecutor.rejectedExecution.r = " + r); // 注意，使用Log，别用LogF，否则会死循环
//        }
//    });
    private static final ThreadPoolExecutor mExecutor = new ThreadPoolExecutor(0, 1, 5, TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>(), new PriorityThreadFactory(TAG, Process.THREAD_PRIORITY_BACKGROUND), new RejectedExecutionHandler() {
        @Override
        public void rejectedExecution(Runnable runnable, ThreadPoolExecutor threadPoolExecutor) {

        }
    });

    public static void setDebug(boolean d) {
        DEBUG = d;
    }

    public static boolean isDebug() {
        return DEBUG;
    }

    public static void v(String tag, String msg) {
        if (msg == null) {
            msg = "null";
        }
        formatOutPutLog(LEVEL_V, tag, 0, msg, null);
    }

    public static void v(String tag, String msg, Throwable tr) {
        if (msg == null) {
            msg = "null";
        }
        formatOutPutLog(LEVEL_V, tag, 0, msg, tr);
    }

    public static void i(String tag, String msg) {
        if (msg == null) {
            msg = "null";
        }
        formatOutPutLog(LEVEL_I, tag, 0, msg, null);
    }

    /**
     * 打印debug级别的日志
     *
     * @param tag Used to identify the source of a log message. It usually identifies the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     * @return The number of bytes written.
     */
    public static void d(String tag, String msg) {
        if (!Constant.DEBUG) return;
        if (msg == null) {
            msg = "null";
        }
        formatOutPutLog(LEVEL_D, tag, 0, msg, null);
    }

    /**
     * 打印debug级别的日志
     *
     * @param tag Used to identify the source of a log message. It usually identifies the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     * @param tr  An exception to log
     * @return The number of bytes written.
     */
    public static void d(String tag, String msg, Throwable tr) {
        if (!Constant.DEBUG) return;
        if (msg == null) {
            msg = "null";
        }
        formatOutPutLog(LEVEL_D, tag, 0, msg, tr);
    }

    public static void w(String tag, String msg) {
        if (msg == null) {
            msg = "null";
        }
        formatOutPutLog(LEVEL_W, tag, 0, msg, null);
    }

    public static void w(String tag, String msg, Throwable tr) {
        if (msg == null) {
            msg = "null";
        }
        formatOutPutLog(LEVEL_W, tag, 0, msg, tr);
    }

    public static void e(String tag, String msg) {
        if (!Constant.DEBUG) return;
        if (msg == null) {
            msg = "null";
        }
        formatOutPutLog(LEVEL_E, tag, 0, msg, null);
    }

    public static void e(String tag, String msg, Throwable tr) {
        if (!Constant.DEBUG) return;
        if (msg == null) {
            msg = "null";
        }
        formatOutPutLog(LEVEL_E, tag, 0, msg, tr);
    }

    public static void flush() {
        mExecutor.getQueue().clear();
    }

    public static void destroy() {
        if (!mExecutor.isShutdown()) {
            mExecutor.shutdown();
        }
    }

    private static void formatOutPutLog(String logType, String tag, int startPos, String msg, Throwable t) {
        int length = msg.length();
        if (length > 3000) {
            int size = length / 3000 + 1;
            for (int i = 0; i < size; i++) {
                int end = (i * 3000 + 3000) > length ? length : (i * 3000 + 3000);
                String newMsg = new String(msg.substring(i * 3000, end));
                log(logType, tag, newMsg, t);
                newMsg = null;
            }
//            formatOutPutLog(logType, tag, startPos + 1000, msg, t);
        } else {
            log(logType, tag, msg, t);
        }
        msg = null;
    }

    private static void log(String logType, String tag, String msg, Throwable t) {
        switch (logType) {
            case LEVEL_I:
                if (null != t) {
                    Log.i(tag, msg, t);
                } else {
                    Log.i(tag, msg);
                }
                break;
            case LEVEL_V:
                if (null != t) {
                    Log.v(tag, msg, t);
                } else {
                    Log.v(tag, msg);
                }
                break;
            case LEVEL_D:
                if (null != t) {
                    Log.d(tag, msg, t);
                } else {
                    Log.d(tag, msg);
                }
                break;
            case LEVEL_E:
                if (null != t) {
                    Log.e(tag, msg, t);
                } else {
                    Log.e(tag, msg);
                }
                break;
            case LEVEL_W:
                if (null != t) {
                    Log.w(tag, msg, t);
                } else {
                    Log.w(tag, msg);
                }
                break;
        }
        msg = null;
    }

}
