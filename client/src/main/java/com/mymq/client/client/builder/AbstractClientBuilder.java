package com.mymq.client.client.builder;

import com.mymq.client.client.MyClient;
import com.mymq.client.config.ClientConfig;

public abstract class AbstractClientBuilder {

    protected MyClient myClient;
    protected ClientConfig clientConfig;

    public AbstractClientBuilder(ClientConfig clientConfig){
        this.clientConfig = clientConfig;
    }
    protected abstract MyClientBuilder config();
    protected abstract MyClientBuilder init();
    protected abstract MyClient start() throws InterruptedException;
    public abstract MyClient build() throws InterruptedException;

}
