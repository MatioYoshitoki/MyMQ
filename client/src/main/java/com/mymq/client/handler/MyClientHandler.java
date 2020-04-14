package com.mymq.client.handler;

import com.mymq.commons.protobuf.MyContentModule;
import com.mymq.client.factory.ClientFactory;
import com.mymq.commons.global.ServiceStatus;
import com.mymq.commons.pojo.Content;
import com.mymq.commons.threadPools.ThreadPoolFactory;
import com.mymq.commons.util.MessageConvertUtil;
import com.mymq.commons.util.SnowFlakeUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class MyClientHandler extends SimpleChannelInboundHandler<MyContentModule.Content> implements Callable<MyContentModule.Content> {

    //是否处于重连之中
    public volatile static AtomicBoolean connectFlag = new AtomicBoolean(false);

    private final String key;
    private Channel channel;
    private long msgID;
    private final AtomicReference<ServiceStatus> status;
    private MyContentModule.Content msg;
    private MyContentModule.Content result;
    private final ExecutorService restartThreadPool = ThreadPoolFactory.THREAD_POOL_FACTORY.getSinglePool();
    private final Runnable disconnectHandler;

    private final Lock lock = new ReentrantLock();
    private final Condition condition = lock.newCondition();


    public MyClientHandler(String key){
        this.key = key;
        this.status = new AtomicReference<>(ServiceStatus.DEAD);
        this.disconnectHandler = new DefaultDisconnectHandler(status);
    }

    public AtomicReference<ServiceStatus> getStatus() {
        return status;
    }

    public MyClientHandler(String key, Runnable runnable){
        this.key = key;
        this.status = new AtomicReference<>(ServiceStatus.DEAD);
        this.disconnectHandler = runnable;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        this.channel = ctx.channel();
        ClientFactory.getInstance().getChannelMap().computeIfAbsent(key, k -> this.channel);
        status.compareAndSet(ServiceStatus.DEAD, ServiceStatus.RUNNING);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        status.compareAndSet(ServiceStatus.RUNNING, ServiceStatus.DEAD);
        log.info("disconnected...");
        if (!MyClientHandler.connectFlag.get()){
            MyClientHandler.connectFlag.compareAndSet(false, true);
            restartThreadPool.execute(disconnectHandler);
        }

    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MyContentModule.Content msg) {
        long msgID = msg.getMsgID();
        int type = msg.getType();
        int msgType = msg.getMsgType();
        if (msgID==this.msgID){
            lock.lock();
            try {
                this.result = msg;
                condition.signal();
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                lock.unlock();
            }
        }else if (type==3 && msgType==3){
            String topic = msg.getTopic();
            String tag = msg.getTag();
            String con = msg.getMsg();
            ClientFactory.getInstance().getConsumerMap().forEach((s, consumer) -> {
                if (consumer.getTopic().equals(topic) && consumer.getTag().equals(tag)){
                    consumer.getListener().consumer(tag, topic, con);
                }
            });
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        if (evt instanceof IdleStateEvent){
            IdleStateEvent idleStateEvent = (IdleStateEvent) evt;
            if (idleStateEvent.state() == IdleState.ALL_IDLE) {
                log.info(ctx.channel().remoteAddress() + "\t空闲,将关闭");
//                boolean removeResult = ActiveChannelQueue.activeChannelQueue.remove(ctx.channel());
//                log.info(removeResult ? "从消费者列表移除成功" : "从消费者列表移除失败");
                String addr = ctx.channel().remoteAddress().toString();
                if (ClientFactory.getInstance().getChannelMap().get(addr)!=null) {
                    ClientFactory.getInstance().getChannelMap().remove(addr);
                }
                ctx.close();
            }else if(idleStateEvent.state()==IdleState.WRITER_IDLE){
                Set<String> keyMap = ClientFactory.getInstance().getKeyMap().get(key);
                if (keyMap!=null) {
                    for (String subKey : keyMap) {
                        Content content = ClientFactory.getInstance().getHeartMap().get(subKey);
                        content.setMsgID(SnowFlakeUtil.next());
                        ctx.writeAndFlush(MessageConvertUtil.toSend(content));
                    }
                }
            }
        }
    }

    public void setMsg(MyContentModule.Content msg) {
        this.msgID = msg.getMsgID();
        this.msg = msg;
    }

    @Override
    public MyContentModule.Content call() {
        lock.lock();
        try {
            this.channel.writeAndFlush(msg);
            boolean success = condition.await(5, TimeUnit.SECONDS);
            if (!success){
                return null;
            }
        }catch (Exception ignored){
        }finally {
            lock.unlock();
        }
        return result;
    }
}
