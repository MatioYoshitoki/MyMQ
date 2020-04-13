package com.mymq.client.factory;

import com.mymq.client.client.MyClient;
import com.mymq.client.client.consumer.Consumer;
import com.mymq.client.client.consumer.ConsumerImpl;
import com.mymq.client.client.produce.Produce;
import com.mymq.client.client.produce.ProduceImpl;
import com.mymq.client.config.ClientConfig;
import com.mymq.client.listener.MessageListener;
import com.mymq.commons.exception.MisMatchClientTypeException;
import com.mymq.commons.global.ClientType;
import com.mymq.commons.global.DefaultProduceConfig;
import com.mymq.commons.pojo.Content;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class ClientFactory {

    private static ClientFactory instance;
    private final Map<String, MyClient> clientMap;
    private final Map<String, Content> heartMap;
    private final Map<String, List<String>> keyMap;

    private final Map<String, ConsumerImpl> consumerMap;
    private final Map<String, Channel> channelMap ;

    private final Lock lock = new ReentrantLock();

    public static ClientFactory getInstance() {
        if (instance==null){
            synchronized (ClientFactory.class){
                if (instance==null){
                    instance = new ClientFactory();
                }
            }
        }
        return instance;
    }

    private ClientFactory(){
        consumerMap = new ConcurrentHashMap<>();
        clientMap = new ConcurrentHashMap<>();
        channelMap = new ConcurrentHashMap<>();
        //用于创建客户端的线程组
        heartMap = new ConcurrentHashMap<>();
        keyMap = new ConcurrentHashMap<>();
    }

    public Map<String, Channel> getChannelMap() {
        return channelMap;
    }

    public Map<String, MyClient> getClientMap() {
        return clientMap;
    }


    public Consumer createConsumer(String topic, String tag, MessageListener listener, int port) {
        return new ConsumerImpl(
                new ClientConfig(
                        DefaultProduceConfig.LOCAL_HOST,
                        port,
                        DefaultProduceConfig.WORKER_EVEN_LOOP_GROUP_SIZE,
                        DefaultProduceConfig.CONNECT_TIME_MILLIS,
                        DefaultProduceConfig.CLIENT_SOCKET_SND_BUF_SIZE,
                        DefaultProduceConfig.CLIENT_SOCKET_RCV_BUF_SIZE,
                        DefaultProduceConfig.ALL_IDLE_TIME_SECONDS,
                        DefaultProduceConfig.SO_KEEPALIVE,
                        DefaultProduceConfig.TCP_NODE_LAY,
                        ClientType.CONSUMER,
                        tag,
                        topic,
                        listener
                ),
                this,
                DefaultProduceConfig.LOCAL_HOST+":"+port
        );
    }

//    public boolean registerConsumer(ConsumerImpl consumer){
//        String keyWithTagTopic = consumer.getKeyWithTagTopic();
//        if (consumerMap.get(keyWithTagTopic)!=null){
//            return false;
//        }
//        try {
//            consumerMap.put(keyWithTagTopic, consumer);
//            log.info("注册心跳");
//            publicExecutor.execute(() -> {
//                Content heart = new Content();
//                heart.setMsgID(SnowFlakeUtil.next());
//                heart.setType(1);
//                heart.setMsgType(1);
//                for (;;) {
//                    log.info("发送心跳");
//                    try {
//                        consumer.send(heart);
//                    }catch (Exception e){
//                        log.error("消息发送失败......");
//                    }
//
//                    try {
//                        TimeUnit.SECONDS.sleep(3);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
//            });
//        }catch (Exception e){
//            e.printStackTrace();
//        }
//
//        return true;
//    }
//
//    public boolean registerProduce(ProduceImpl produce){
//        try {
//            log.info("注册produce心跳");
//            publicExecutor.execute(()->{
//                for (;;){
//                    Content heart = new Content();
//                    heart.setMsgID(SnowFlakeUtil.next());
//                    heart.setType(1);
//                    heart.setMsgType(0);
//                    Content result = null;
//                    try {
//                        result = produce.send(heart);
//                    } catch (Exception e) {
//                        log.error("消息发送失败......");
//                        e.printStackTrace();
//                    }
//                    if (result!=null) {
//                        log.info("tag=" + result.getTag() + "\tmsgID=" + result.getMsgID() + "\tmsg=" + result.getMsg());
//                    }
//                    try{
//                        TimeUnit.SECONDS.sleep(3);
//                    }catch(InterruptedException e){
//                        e.printStackTrace();
//                    }
//                }
//            });
//        }catch (Exception e){
//            e.printStackTrace();
//        }
//        return true;
//    }


    public boolean registerClientHeart(String key, ClientType clientType, Content content){
        try {
            List<String> keyList = keyMap.get(key);
            if (keyList==null){
                lock.lock();
                try {
                    keyList = new CopyOnWriteArrayList<>();
                }catch (Exception e){
                    e.printStackTrace();
                }finally {
                    lock.unlock();
                }
            }

            String subKey = key + "_" + clientType.getType();
            keyList.add(subKey);
            keyMap.put(key, keyList);
            heartMap.put(subKey, content);
        }catch (Exception e){
            e.printStackTrace();
        }
        return true;
    }

    public Map<String, Content> getHeartMap() {
        return heartMap;
    }

    public Map<String, List<String>> getKeyMap() {
        return keyMap;
    }

    public Map<String, ConsumerImpl> getConsumerMap() {
        return consumerMap;
    }

    public Produce createProduce(int port) throws Exception {
        return this.createProduce(DefaultProduceConfig.LOCAL_HOST, port);
    }

    public Produce createProduce(String addr, int port) throws Exception {
        return this.createProduce(new ClientConfig(addr, port, DefaultProduceConfig.WORKER_EVEN_LOOP_GROUP_SIZE, DefaultProduceConfig.CONNECT_TIME_MILLIS, DefaultProduceConfig.CLIENT_SOCKET_SND_BUF_SIZE, DefaultProduceConfig.CLIENT_SOCKET_RCV_BUF_SIZE, DefaultProduceConfig.ALL_IDLE_TIME_SECONDS, DefaultProduceConfig.SO_KEEPALIVE, DefaultProduceConfig.TCP_NODE_LAY, ClientType.PRODUCE, null, null, null));
    }

    public Produce createProduce(ClientConfig clientConfig) throws Exception {
        if (ClientType.PRODUCE!=clientConfig.getClientType()){
            throw new MisMatchClientTypeException("客户端类型不匹配");
        }
        Produce produce = null;
        lock.lock();
        try {
            produce = new ProduceImpl(clientConfig, this);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            lock.unlock();
        }
        return produce;
    }

}
