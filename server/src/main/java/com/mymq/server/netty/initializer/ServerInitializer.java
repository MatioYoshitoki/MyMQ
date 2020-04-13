package com.mymq.server.netty.initializer;

import com.mymq.commons.protobuf.MyContentModule;
import com.mymq.server.netty.handler.ServerHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

public class ServerInitializer extends ChannelInitializer<NioSocketChannel> {

    private final int readTimeOut;
    private final int writeTimeOut;
    private final int allIdleTimeOut;
    private final TimeUnit timeUnit;

    public ServerInitializer(){
        this(0, 0, 120, TimeUnit.SECONDS);
    }

    public ServerInitializer(int readTimeOut, int writeTimeOut, int allIdleTimeOut, TimeUnit timeUnit){
        this.readTimeOut = readTimeOut;
        this.writeTimeOut = writeTimeOut;
        this.allIdleTimeOut = allIdleTimeOut;
        this.timeUnit = timeUnit;
    }



    @Override
    protected void initChannel(NioSocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(new ProtobufVarint32FrameDecoder());
        pipeline.addLast(new ProtobufVarint32LengthFieldPrepender());
        pipeline.addLast("protoDecoder", new ProtobufDecoder(MyContentModule.Content.getDefaultInstance()));
        pipeline.addLast("protoEncoder", new ProtobufEncoder());
        pipeline.addLast(new IdleStateHandler(readTimeOut,writeTimeOut,allIdleTimeOut, timeUnit));
        pipeline.addLast(new ServerHandler());
    }
}
