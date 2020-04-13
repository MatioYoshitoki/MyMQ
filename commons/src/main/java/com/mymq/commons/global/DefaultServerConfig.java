package com.mymq.commons.global;

public class DefaultServerConfig {
    public static final int DEFAULT_EVENT_GROUP_SIZE = Runtime.getRuntime().availableProcessors()*2;
    public static final int DEFAULT_CONNECT_SIZE = 128;
}
