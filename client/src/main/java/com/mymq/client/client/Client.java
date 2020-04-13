package com.mymq.client.client;

import com.mymq.commons.pojo.Content;

public interface Client {

    Content send(Content content) throws InterruptedException;

    boolean isStarted();

}
