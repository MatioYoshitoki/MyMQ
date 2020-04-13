package com.mymq.server.queue;


import com.mymq.commons.pojo.Content;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;


@Slf4j
public class MyQueue {


    public static MyQueue mqQueues ;
    private BlockingQueue<Content> queue = null;
    private final AtomicInteger len = new AtomicInteger();


    private MyQueue(){
    }

    private MyQueue(BlockingQueue<Content> queue){
        this.queue = queue;
    }

    public static void initLink(int size){
        if (mqQueues==null){
            synchronized (MyQueue.class){
                if (mqQueues==null){
                    mqQueues = new MyQueue(new LinkedBlockingQueue<>(size));
                    log.info("queue init success, size="+size);
                }
            }
        }else {
            log.info("queue exists, size="+size);
        }
    }

    public int add(Content content) {
        if (queue==null){
            throw new NullPointerException("not init");
        }
        try {
            queue.offer(content, 3, TimeUnit.SECONDS);
            len.incrementAndGet();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return len.get();
    }

    public Content remove() {
        if (queue==null){
            throw new NullPointerException("not init");
        }
        if (len.get()<=0){
            return null;
        }
        Content content = null;
        try {
            content = queue.poll(3, TimeUnit.SECONDS);
            if (content==null){
                return null;
            }else {
                len.decrementAndGet();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return content;
    }

}
