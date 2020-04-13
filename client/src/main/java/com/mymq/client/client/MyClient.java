package com.mymq.client.client;

import com.mymq.commons.protobuf.MyContentModule;
import com.mymq.client.config.ClientConfig;
import com.mymq.client.factory.ClientFactory;
import com.mymq.client.handler.MyClientHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class MyClient {

    private final String key;
    private final Lock lock ;
    private final Bootstrap bootstrap;
    private final EventLoopGroup group;
    private final ClientConfig clientConfig;
    private final ClientFactory clientFactory;

    public MyClientHandler myClientHandler;


    private MyClient(ClientConfig clientConfig, String key, ClientFactory clientFactory){
        this.clientConfig = clientConfig;
        this.clientFactory = clientFactory;
        this.key = key;
        this.lock = new ReentrantLock();
        bootstrap = new Bootstrap();
        if (clientConfig.getWorkerEvenLoopGroupSize()!=0){
            group = new NioEventLoopGroup(clientConfig.getWorkerEvenLoopGroupSize());
        }else {
            group = new NioEventLoopGroup();
        }

    }


    public static MyClient initClient(ClientConfig clientConfig, ClientFactory clientFactory){
        String key = clientConfig.getHostName() + ":" + clientConfig.getPort();
        MyClient client = clientFactory.getClientMap().get(key);
        if (client==null){
            client = new MyClient(clientConfig, key, clientFactory);
        }
        return client;
    }

    public boolean start() {
        if (clientFactory.getChannelMap().containsKey(key)){
            return true;
        }
        boolean result;
        lock.lock();
        try {
            if (!clientFactory.getChannelMap().containsKey(key)) {
                bootstrap
                        .group(group)
                        .channel(NioSocketChannel.class)
                        .option(ChannelOption.TCP_NODELAY, true)
                        .option(ChannelOption.SO_KEEPALIVE, false)
                        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, clientConfig.getConnectTimeMillis())
                        .option(ChannelOption.SO_SNDBUF, clientConfig.getClientSocketSndBufSize())
                        .option(ChannelOption.SO_RCVBUF, clientConfig.getClientSocketRcvBufSize())
                        .handler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel ch) {
                                ChannelPipeline pipeline = ch.pipeline();
                                myClientHandler = new MyClientHandler(clientFactory, key);
                                pipeline.addLast(new ProtobufVarint32FrameDecoder());
                                pipeline.addLast(new ProtobufVarint32LengthFieldPrepender());
                                pipeline.addLast(new ProtobufDecoder(MyContentModule.Content.getDefaultInstance()));
                                pipeline.addLast(new ProtobufEncoder());
                                pipeline.addLast(new IdleStateHandler(0, 5, clientConfig.getAllIdleTimeSeconds()));
                                pipeline.addLast(myClientHandler);
                            }
                        });
                this.start0();
            }
            result = true;
        }catch (Exception e){
            log.info("start failed");
            result = false;
        }finally {
            lock.unlock();
        }
        return result;
    }

    public void start0() throws InterruptedException {
        lock.lock();
        try {
            if (bootstrap!=null) {
                ChannelFuture f = bootstrap.connect(clientConfig.getHostName(), clientConfig.getPort()).sync();
                clientFactory.getChannelMap().put(key, f.channel());
                clientFactory.getClientMap().put(key, this);
            }
        } finally {
            lock.unlock();
        }
    }

}
