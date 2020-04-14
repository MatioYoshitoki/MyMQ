package com.mymq.client.client.builder;

import com.mymq.client.client.MyClient;
import com.mymq.client.config.ClientConfig;
import com.mymq.client.factory.ClientFactory;
import com.mymq.commons.global.ClientType;
import com.mymq.commons.global.DefaultHeartContent;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MyClientBuilder extends AbstractClientBuilder {

    MyClient client;

    public MyClientBuilder(ClientConfig clientConfig){
        super(clientConfig);
    }

    protected MyClientBuilder config(){
        myClient = new MyClient(this.clientConfig);
        return this;
    }
    protected MyClientBuilder init(){
        myClient.init();
        return this;
    }
    protected MyClient start() throws InterruptedException {
        return myClient.start0();
    }

    public MyClient build() throws InterruptedException {
        client = ClientFactory.getInstance().getClientMap().get(clientConfig.getKey());
        if (client==null){
            synchronized (MyClientBuilder.class){
                if (client==null){
                    client = config().init().start();
                }
            }
        }
        boolean registerResult = ClientFactory.getInstance().registerClientHeart(client, clientConfig.getClientType(), clientConfig.getClientType() == ClientType.CONSUMER ? DefaultHeartContent.DEFAULT_CONSUMER_HEART_CONTENT : DefaultHeartContent.DEFAULT_PRODUCE_HEART_CONTENT);
        log.info("register result:"+registerResult);
        return client;
    }

}
