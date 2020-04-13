package com.mymq.client.client;

import com.mymq.commons.protobuf.MyContentModule;


public interface ClientService {

    MyContentModule.Content send(MyContentModule.Content content) throws InterruptedException;

}
