package com.mymq.client.client.produce;

import com.mymq.commons.protobuf.MyContentModule;
import com.mymq.client.client.ClientService;
import com.mymq.client.client.MyClient;
import com.mymq.client.config.ClientConfig;
import com.mymq.client.factory.ClientFactory;
import com.mymq.commons.global.ClientType;
import com.mymq.commons.global.DefaultHeartContent;
import com.mymq.commons.pojo.Content;
import com.mymq.commons.threadPools.ThreadPoolFactory;
import com.mymq.commons.util.MessageConvertUtil;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Proxy;
import java.util.concurrent.ExecutorService;

@Slf4j
public class ProduceImpl implements Produce {

    private final ExecutorService executor = ThreadPoolFactory.THREAD_POOL_FACTORY.getNormalPool(3, Runtime.getRuntime().availableProcessors() * 2, 120, 10);
    private final MyClient client;
    private final ClientFactory clientFactory;
    private final boolean startFlag;
    private final String key;

    public ProduceImpl(ClientConfig clientConfig, ClientFactory clientFactory) {
        this.client = MyClient.initClient(clientConfig, clientFactory);
        this.clientFactory = clientFactory;
        this.startFlag = client.start();
        this.key = clientConfig.getHostName()+":"+clientConfig.getPort();
        if (this.startFlag){
            log.info("start success");
            if (register()) {
                log.info("register success");
            } else {
                log.info("already register");
            }
        }else {
            log.error("start failed");
        }
    }

    public boolean register(){
        return clientFactory.registerClientHeart(key, ClientType.PRODUCE, DefaultHeartContent.DEFAULT_PRODUCE_HEART_CONTENT);
    }


    private Object getBean(){
        return Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class<?>[]{ClientService.class}, (proxy, method, args)->{
            client.myClientHandler.setMsg((MyContentModule.Content) args[0]);
            return executor.submit(client.myClientHandler).get();
        });
    }


    @Override
    public Content send(Content content) throws InterruptedException {
        return MessageConvertUtil.toRead(((ClientService) getBean()).send(MessageConvertUtil.toSend(content)));
    }

    @Override
    public boolean isStarted() {
        return this.startFlag;
    }

}
