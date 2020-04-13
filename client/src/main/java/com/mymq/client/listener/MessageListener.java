package com.mymq.client.listener;

public interface MessageListener {
    void consumer(String tag, String topic, String msg);
}
