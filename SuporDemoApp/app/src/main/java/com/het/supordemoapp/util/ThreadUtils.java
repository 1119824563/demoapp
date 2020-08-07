package com.het.supordemoapp.util;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * ---------------------------------------------------------------- <br>
 * Copyright (C) 2014-2020, by het, Shenzhen, All rights reserved.  <br>
 * ---------------------------------------------------------------- <br>
 * <p>
 * 描述: ThreadUtils <br>
 * 作者: lei <br>
 * 日期: 2017/5/10 <br>
 */
public class ThreadUtils {
    private static ThreadFactory threadFactory = new BasicThreadFactory.Builder().namingPattern("util-schedule-pool-%d").daemon
            (true).build();

    private static ExecutorService cachedExecutorService = new ThreadPoolExecutor(0, Integer.MAX_VALUE,
            60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(), threadFactory);

    private static ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(Runtime.getRuntime()
            .availableProcessors() * 2, threadFactory);

    private static ExecutorService defaultExecutorService = new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors() *
            10, Runtime.getRuntime().availableProcessors() * 16, 0L, TimeUnit.NANOSECONDS,
            new LinkedBlockingQueue<Runnable>(1024), threadFactory);

    private static ExecutorService fixedExecutorService = new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors
            () * 2, Runtime.getRuntime().availableProcessors() * 2, 0L, TimeUnit.MILLISECONDS, new
            LinkedBlockingQueue<Runnable>(1024), threadFactory);

    private static ExecutorService singleExecutorService = new ThreadPoolExecutor(1, 1, 0L,
            TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(1024), threadFactory);


    public static void execute(Runnable paramRunnable) {
        defaultExecutorService.execute(paramRunnable);
    }

    public static void execute(Runnable paramRunnable, long delay) {
        scheduledExecutorService.schedule(paramRunnable, delay, TimeUnit.MILLISECONDS);
    }

    public static void execute(Runnable paramRunnable, long delay, long period) {
        scheduledExecutorService.scheduleAtFixedRate(paramRunnable, delay, period, TimeUnit.MILLISECONDS);
    }

    public static ExecutorService getDefaultExecutorservice() {
        return defaultExecutorService;
    }

    public static ScheduledExecutorService getScheduledExecutorService() {
        return scheduledExecutorService;
    }

    public static ExecutorService getFixedExecutorservice() {
        return fixedExecutorService;
    }

    public static ExecutorService getSingleExecutorservice() {
        return singleExecutorService;
    }
}
