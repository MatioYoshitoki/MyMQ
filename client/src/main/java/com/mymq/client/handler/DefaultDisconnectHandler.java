package com.mymq.client.handler;

import com.mymq.client.client.MyClient;
import com.mymq.client.factory.ClientFactory;
import com.mymq.commons.global.ServiceStatus;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
public class DefaultDisconnectHandler implements Runnable{

    AtomicReference<ServiceStatus> flag;


    public DefaultDisconnectHandler(AtomicReference<ServiceStatus> flag){
        this.flag = flag;
    }

    @Override
    public void run() {
        while (flag.get()==ServiceStatus.DEAD){
            try{
                ClientFactory clientFactory = ClientFactory.getInstance();
                for (Map.Entry<String, MyClient> clientEntry : clientFactory.getClientMap().entrySet()) {
                    MyClient client = clientEntry.getValue();
                    if (client.myClientHandler.getStatus().get()==ServiceStatus.DEAD) {
                        client.start0();
                    }
                }
                log.info("trying reconnect....");
            }catch(Exception e){
                log.error("connect refused......");
            }finally {
                try {
                    TimeUnit.SECONDS.sleep(5);
                } catch (InterruptedException interruptedException) {
                    interruptedException.printStackTrace();
                }
            }
        }
        MyClientHandler.connectFlag.compareAndSet(true, false);
    }
}
