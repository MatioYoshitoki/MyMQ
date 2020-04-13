package com.mymq.server.netty.socket;

import com.mymq.commons.global.DefaultServerConfig;
import com.mymq.server.netty.initializer.ServerInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NettyServerSocket {


    private final String host;
    private final int port;
    private final int connectSize;

    private final ServerBootstrap serverBootstrap ;
    private final EventLoopGroup boss;
    private final EventLoopGroup worker;



    public NettyServerSocket(String host, int port){
        this(host, port, DefaultServerConfig.DEFAULT_CONNECT_SIZE);
    }

    public NettyServerSocket(String host, int port, int connectSize){
        this(host, port, connectSize, DefaultServerConfig.DEFAULT_EVENT_GROUP_SIZE, DefaultServerConfig.DEFAULT_EVENT_GROUP_SIZE);
    }

    public NettyServerSocket(String host, int port, int connectSize, int bossGroupSize, int workerGroupSize){
        this(host, port, connectSize, new ServerBootstrap(), new NioEventLoopGroup(bossGroupSize), new NioEventLoopGroup(workerGroupSize));
    }

    public NettyServerSocket(String host, int port, int connectSize, ServerBootstrap serverBootstrap, EventLoopGroup boss, EventLoopGroup worker){
        this.host = host;
        this.port = port;
        this.serverBootstrap = serverBootstrap;
        this.boss = boss;
        this.worker = worker;
        this.connectSize = connectSize;
    }

    public void initServerSocket(){
        try {
            serverBootstrap
                    .group(boss, worker)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, connectSize)//设置服务器可连接队列大小为128
                    .childHandler(new ServerInitializer());
            ChannelFuture f = serverBootstrap.bind(host, port).sync();
            f.channel().closeFuture().sync();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }

}
