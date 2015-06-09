package com;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import android.os.Process;

/**
 * Created by WangKui on 2015/6/7.
 */
public class MyThreadFactory implements ThreadFactory {
    private final String name;
    private final int mPriority;
    private final AtomicInteger mInteger = new AtomicInteger();

    public MyThreadFactory(String name, int priority) {
        this.name = name;
        mPriority = priority;
    }

    @Override
    public Thread newThread(Runnable r) {
        return new Thread(r, name + "-" + mInteger.getAndIncrement()){
            @Override
        public void run(){
                Process.setThreadPriority(mPriority);
                super.run();
            }
        };
    }
}
