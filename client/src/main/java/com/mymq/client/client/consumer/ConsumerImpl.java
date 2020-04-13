package com.mymq.client.client.consumer;


import com.mymq.commons.protobuf.MyContentModule;
import com.mymq.client.client.ClientService;
import com.mymq.client.client.MyClient;
import com.mymq.client.config.ClientConfig;
import com.mymq.client.factory.ClientFactory;
import com.mymq.client.listener.MessageListener;
import com.mymq.commons.global.ClientType;
import com.mymq.commons.global.DefaultHeartContent;
import com.mymq.commons.pojo.Content;
import com.mymq.commons.threadPools.ThreadPoolFactory;
import com.mymq.commons.util.MessageConvertUtil;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Proxy;
import java.util.concurrent.ExecutorService;

@Slf4j
public class ConsumerImpl implements Consumer {

    private final ClientFactory clientFactory;
    private final String key;
    private final String keyWithTagTopic;
    private final MessageListener listener;
    private final String tag;
    private final String topic;
    private final MyClient client;
    private final ExecutorService executor = ThreadPoolFactory.THREAD_POOL_FACTORY.getNormalPool(3, Runtime.getRuntime().availableProcessors() * 2, 120, 10);
    private final boolean startFlag;

    public ConsumerImpl(ClientConfig clientConfig, ClientFactory clientFactory, String key){
        this.clientFactory = clientFactory;
        this.tag = clientConfig.getTag();
        this.topic = clientConfig.getTopic();
        this.listener = clientConfig.getListener();
        this.key = key;
        this.keyWithTagTopic = key+"_"+topic+"_"+tag;
        this.client = MyClient.initClient(clientConfig, clientFactory);
        this.startFlag = this.client.start();
        if (this.startFlag) {
            clientFactory.getConsumerMap().put(keyWithTagTopic, this);
            log.info("start success");
            if (register()) {
                log.info("register success");
            } else {
                log.info("register failed");
            }
        }else {
            log.error("start failed");
        }
    }

    public boolean register(){

        return clientFactory.registerClientHeart(key, ClientType.CONSUMER, DefaultHeartContent.DEFAULT_CONSUMER_HEART_CONTENT);
    }

    public String getTag() {
        return tag;
    }

    public String getTopic() {
        return topic;
    }

    public MessageListener getListener() {
        return listener;
    }

    public String getKeyWithTagTopic() {
        return keyWithTagTopic;
    }

    public String getKey() {
        return key;
    }

    private Object getBean(){
        return Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class<?>[]{ClientService.class}, (proxy, method, args)->{
            client.myClientHandler.setMsg((MyContentModule.Content) args[0]);
            return executor.submit(client.myClientHandler).get();
        });
    }

    @Override
    public Content send(Content content) throws InterruptedException {
        if (content.getMsgType()!=1 || content.getType()!=1){
            return null;
        }
        return MessageConvertUtil.toRead(((ClientService) getBean()).send(MessageConvertUtil.toSend(content)));
    }

    @Override
    public boolean isStarted() {
        return this.startFlag;
    }
}
