package com.mymq.client.example.listener;

import com.mymq.client.listener.MessageListener;

public class MyListenerExample implements MessageListener {
    @Override
    public void consumer(String tag, String topic, String msg) {
        System.out.println("===================\n消费了:\n\ttag:"+tag+"\n\ttopic:"+topic+"\n\tmsg:"+msg);
    }
}
