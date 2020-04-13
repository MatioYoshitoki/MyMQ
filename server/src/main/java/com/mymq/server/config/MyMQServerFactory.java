package com.mymq.server.config;


import com.mymq.server.netty.socket.NettyServerSocket;
import com.mymq.commons.pojo.Content;
import com.mymq.server.queue.ActiveChannelQueue;
import com.mymq.server.queue.MyQueue;
import com.mymq.commons.threadPools.ThreadPoolFactory;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class MyMQServerFactory {

    //任务队列的大小
    private final int queueSize;
    //netty服务器
    private final NettyServerSocket nettyServerSocket;
    //用于消费的单一线程线程池
    private final ExecutorService executors = ThreadPoolFactory.THREAD_POOL_FACTORY.getSinglePool();
    //消费的时间间隔
    private final int consumerTime;

    public MyMQServerFactory(String host, int port, int queueSize, int consumerTime){
        this.queueSize = queueSize;
        this.consumerTime = consumerTime;
        nettyServerSocket = new NettyServerSocket(host, port);
    }

    public void init(){
        initQueue();
        initConsumerLoop();
        initServer();
    }

    private void initQueue(){
        MyQueue.initLink(queueSize);
    }

    private void initConsumerLoop(){
        executors.execute(()->{
            log.info("consumer started......");
            for (;;) {
                try {
                    TimeUnit.SECONDS.sleep(consumerTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Content content;
                log.info("------consumer------");
                while ((content = MyQueue.mqQueues.remove())!=null){
                    ActiveChannelQueue.activeChannelQueue.send(content);
                }
            }
        });
    }


    private void initServer() {
        nettyServerSocket.initServerSocket();
    }

}