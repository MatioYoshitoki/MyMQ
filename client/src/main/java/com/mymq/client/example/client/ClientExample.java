package com.mymq.client.example.client;

import com.mymq.client.client.consumer.Consumer;
import com.mymq.client.client.produce.Produce;
import com.mymq.client.example.listener.MyListenerExample;
import com.mymq.client.factory.ClientFactory;
import com.mymq.commons.pojo.Content;
import com.mymq.commons.util.SnowFlakeUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

@Slf4j
public class ClientExample {

    public static void main(String[] args) throws Exception {

        ClientFactory clientFactory = ClientFactory.getInstance();
        Produce produce = clientFactory.createProduce(8833);
        Consumer consumer = clientFactory.createConsumer("hello", "world", new MyListenerExample(), 8833);
        if (produce.isStarted()){
            log.info("启动成功，开始消费");
            Content msg = new Content("world", "hello", "hello world!", 0, 0, SnowFlakeUtil.next());
            for (;;){
                Content result = null;
                try {
                    result = produce.send(msg);
                }catch (Exception e){
                    e.printStackTrace();
                }

                if (result!=null) {
                    log.info("tag=" + result.getTag() + "\tmsgID=" + result.getMsgID() + "\tmsg=" + result.getMsg());
                }
                try{
                    TimeUnit.SECONDS.sleep(10);
                }catch(InterruptedException e){
                    e.printStackTrace();
                }
            }
        }

        log.error("没能启动");

    }

}
