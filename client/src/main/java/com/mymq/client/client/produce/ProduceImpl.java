package com.mymq.client.client.produce;

import com.mymq.client.client.ClientService;
import com.mymq.client.client.MyClient;
import com.mymq.client.client.builder.MyClientBuilder;
import com.mymq.client.config.ClientConfig;
import com.mymq.commons.pojo.Content;
import com.mymq.commons.protobuf.MyContentModule;
import com.mymq.commons.threadPools.ThreadPoolFactory;
import com.mymq.commons.util.MessageConvertUtil;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Proxy;
import java.util.concurrent.ExecutorService;

@Slf4j
public class ProduceImpl implements Produce {

    private final ExecutorService executor = ThreadPoolFactory.THREAD_POOL_FACTORY.getNormalPool(3, Runtime.getRuntime().availableProcessors() * 2, 120, 10);
    private MyClient client;

    public ProduceImpl(ClientConfig clientConfig) throws InterruptedException {
        this.client = new MyClientBuilder(clientConfig).build();//MyClient.createClient(clientConfig, clientFactory);
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
        return this.client!=null;
    }

}
