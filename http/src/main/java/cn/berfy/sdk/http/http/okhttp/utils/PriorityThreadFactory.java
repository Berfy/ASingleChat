package cn.berfy.sdk.http.http.okhttp.utils;

import android.os.Process;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 一个线程工厂,创建与给定线程优先级的线程。
 */
public class PriorityThreadFactory implements ThreadFactory {
    private static final int THREAD_PRIORITY_DEFAULT_LESS = Process.THREAD_PRIORITY_DEFAULT + Process.THREAD_PRIORITY_LESS_FAVORABLE * 3;
    private final int mPriority;
    private final AtomicInteger mNumber = new AtomicInteger();
    private final String mName;

    public PriorityThreadFactory(String name) {
        mName = name;
        mPriority = THREAD_PRIORITY_DEFAULT_LESS;
    }

    public PriorityThreadFactory(String name, int priority) {
        mName = name;
        mPriority = priority;
    }

    @Override
    public Thread newThread(Runnable r) {

        return new Thread(r, "PriorityThreadFactory-" + mName + '-' + mNumber.getAndIncrement()) {
            @Override
            public void run() {
//				Process.setThreadPriority(mPriority);
//				super.run();

                //JJ
                if ("ExecutorSocketRequest".equals(mName)) {
                    Process.setThreadPriority(Process.THREAD_PRIORITY_DEFAULT + Process.THREAD_PRIORITY_MORE_FAVORABLE);
                } else if ("ExecutorUpload".equals(mName)) {
                    Process.setThreadPriority(Process.THREAD_PRIORITY_DEFAULT);
                } else {
                    Process.setThreadPriority(mPriority);
                }
                super.run();
            }
        };
    }
}