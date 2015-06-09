package com;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Process;

/**
 * Created by WangKui on 2015/6/7.
 */
public class MyThreadPool {
    private final int MIN_POOL_SIZE = 2;
    private final int MAX_POOL_SIZE = 6;
    private final int KEEP_ALIVE_TIME = 2;
    private final Executor mExecutor;

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public MyThreadPool(){
        ThreadFactory factory = new MyThreadFactory("my_thread_factory", Process.THREAD_PRIORITY_BACKGROUND);
        BlockingQueue<Runnable> workQueue = new LinkedBlockingDeque<Runnable>();
        mExecutor = new ThreadPoolExecutor(MIN_POOL_SIZE, MAX_POOL_SIZE, KEEP_ALIVE_TIME, TimeUnit.SECONDS, workQueue, factory);
    }

    public void execute(Runnable r){
        mExecutor.execute(r);
    }
}
