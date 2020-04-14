package com.mymq.server.queue;

import com.mymq.commons.pojo.Content;
import com.mymq.commons.util.MessageConvertUtil;
import com.mymq.commons.util.SnowFlakeUtil;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class ActiveChannelQueue {

    public static ActiveChannelQueue activeChannelQueue = new ActiveChannelQueue();
    private final Set<Channel> channelSet = new CopyOnWriteArraySet<>();
    private final AtomicInteger len = new AtomicInteger();
    private final AtomicInteger nowIndex = new AtomicInteger(-1);

    private ActiveChannelQueue(){
    }

    public boolean remove(Channel channel){
        if (len.get()<0){
            return false;
        }
        boolean removeResult = channelSet.remove(channel);
        if (removeResult){
            if (nowIndex.get()>=0){
                nowIndex.decrementAndGet();
            }
            len.decrementAndGet();
        }
        return removeResult;
    }

    public boolean register(Channel channel){
        boolean addResult = channelSet.add(channel);
        if (addResult){
            len.incrementAndGet();
        }
        return addResult;
    }

    public void send(Content content) throws NullPointerException{
        if (content==null) throw new NullPointerException();

        int flag = 0;
        Iterator<Channel> iterator = channelSet.iterator();
        if (len.get()<=nowIndex.get()){
            nowIndex.set(-1);
        }
        log.info("channelSize="+channelSet.size());

        while (iterator.hasNext()){
            Channel channel = iterator.next();
            if (flag>=nowIndex.get()){
//                MyContentModule.Content result = MyContentModule.Content.newBuilder().setTag(content.getTag()).setTopic(content.getTopic()).setMsg(content.getMsg()).setType(3).setMsgType(3).build();
                Content result = new Content(content.getTag(), content.getTopic(), content.getMsg(), 3, 3, SnowFlakeUtil.next());
                channel.writeAndFlush(MessageConvertUtil.toSend(result));
                flag++;
                nowIndex.set(flag);
                break;
            }
            flag++;
        }
    }

}
