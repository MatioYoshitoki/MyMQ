package com.mymq.commons.threadPools;

import java.util.concurrent.*;

public enum  ThreadPoolFactory {
    THREAD_POOL_FACTORY;
    public ExecutorService getSinglePool(){
        return Executors.newSingleThreadExecutor();
    }
    public ExecutorService getNormalPool(int coreSize, int maxSize, long keepAliveTime, int workQueueSize){
        return new ThreadPoolExecutor(coreSize, maxSize, keepAliveTime, TimeUnit.SECONDS, new LinkedBlockingQueue<>(workQueueSize));
    }
}
