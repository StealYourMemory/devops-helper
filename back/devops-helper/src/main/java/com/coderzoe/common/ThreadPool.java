package com.coderzoe.common;

import com.coderzoe.config.ThreadPoolConfig;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.*;

/**
 * 线程池
 * 增加了虚拟线程支持
 *
 * @author yinhuasheng
 * @date 2024/8/19 11:29
 */
@Setter
@Getter
@Component
public class ThreadPool {
    private static ExecutorService executorService;
    private static ScheduledExecutorService scheduledExecutorService;
    private static ThreadPoolConfig threadPoolConfig;
    private static boolean virtualEnabled;


    public static ExecutorService executorService() {
        if (executorService == null) {
            synchronized (ThreadPool.class) {
                executorService = new ThreadPoolExecutor(threadPoolConfig.getCorePoolSize(),
                        threadPoolConfig.getMaximumPoolSize(),
                        threadPoolConfig.getKeepAliveTime(),
                        TimeUnit.SECONDS,
                        new ArrayBlockingQueue<>(100),
                        virtualEnabled ? Thread.ofVirtual().factory() : Executors.defaultThreadFactory(),
                        new ThreadPoolExecutor.AbortPolicy());
            }
        }
        return executorService;
    }

    public static ScheduledExecutorService scheduledExecutorService() {
        if (scheduledExecutorService == null) {
            synchronized (ThreadPool.class) {
                scheduledExecutorService = new ScheduledThreadPoolExecutor(threadPoolConfig.getCorePoolSize()
                        , virtualEnabled ? Thread.ofVirtual().factory() : Executors.defaultThreadFactory());
            }
        }
        return scheduledExecutorService;
    }

    @Autowired
    public void setThreadPoolConfig(ThreadPoolConfig threadPoolConfig) {
        ThreadPool.threadPoolConfig = threadPoolConfig;
    }

    @Value("${spring.threads.virtual.enabled}")
    public void setTempVirtualEnabled(boolean virtualEnabled) {
        ThreadPool.virtualEnabled = virtualEnabled;
    }
}
