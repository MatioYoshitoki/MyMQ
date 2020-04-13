package com.mymq.commons.global;

public class DefaultProduceConfig {

    public static final int WORKER_EVEN_LOOP_GROUP_SIZE = Runtime.getRuntime().availableProcessors()*2;
    public static final int CONNECT_TIME_MILLIS = 3000;
    public static final int CLIENT_SOCKET_SND_BUF_SIZE = 65535;
    public static final int CLIENT_SOCKET_RCV_BUF_SIZE = 65535;
    public static final int ALL_IDLE_TIME_SECONDS = 120;
    public static final boolean SO_KEEPALIVE = false;
    public static final boolean TCP_NODE_LAY = true;
    public static final String LOCAL_HOST = "127.0.0.1";

}
