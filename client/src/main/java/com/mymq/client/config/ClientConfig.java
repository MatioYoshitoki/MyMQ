package com.mymq.client.config;

import com.mymq.client.listener.MessageListener;
import com.mymq.commons.global.ClientType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class ClientConfig {


    private String hostName;
    private int port;

    private int workerEvenLoopGroupSize;
    private int connectTimeMillis;
    private int clientSocketSndBufSize;
    private int clientSocketRcvBufSize;
    private int allIdleTimeSeconds;
    private boolean keepAlive;
    private boolean tcpNoDelay;

    private ClientType clientType;

    private String tag;
    private String topic;
    private MessageListener listener;


    public String getKey(){
        return this.hostName+":"+this.port;
    }
}
