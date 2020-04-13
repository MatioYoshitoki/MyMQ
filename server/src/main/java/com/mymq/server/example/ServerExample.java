package com.mymq.server.example;

import com.mymq.server.config.MyMQServerFactory;

public class ServerExample {

    public static void main(String[] args) {
        new MyMQServerFactory("127.0.0.1", 8833, 50, 5).init();
    }

}
