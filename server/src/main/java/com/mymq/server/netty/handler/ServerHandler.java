package com.mymq.server.netty.handler;

import com.mymq.commons.protobuf.MyContentModule;
import com.mymq.server.queue.ActiveChannelQueue;
import com.mymq.server.queue.MyQueue;
import com.mymq.commons.pojo.Content;
import com.mymq.commons.util.MessageConvertUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ServerHandler extends SimpleChannelInboundHandler<MyContentModule.Content> {

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        log.info("channel add success:"+ctx.channel().remoteAddress());
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        ActiveChannelQueue.activeChannelQueue.remove(ctx.channel());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, MyContentModule.Content content) {

        int type = content.getType();
        int msgType = content.getMsgType();

        switch (type){
            case 0://普通消息
                if (msgType==0){
                    //生产消息
                    Content localContent = MessageConvertUtil.toRead(content);
                    int count = MyQueue.mqQueues.add(localContent);
                    Content heartResult = new Content(3, 1);
                    heartResult.setMsgID(content.getMsgID());
                    channelHandlerContext.channel().writeAndFlush(MessageConvertUtil.toSend(heartResult));
                    log.info("queue count="+count);
                }
                break;
            case 1://心跳消息
                MyContentModule.Content heartResult = MyContentModule.Content.newBuilder().setMsg("收到心跳").setType(2).setMsgType(2).setMsgID(content.getMsgID()).build();
                channelHandlerContext.channel().writeAndFlush(heartResult);
                log.info("return: msgID="+heartResult.getMsgID()+"\tmsg="+heartResult.getMsg());
                if (msgType==1){
                    boolean registerResult = ActiveChannelQueue.activeChannelQueue.register(channelHandlerContext.channel());
                    log.info(registerResult?"register success!":"register failed!!!");
                }
                break;
            default:
        }


    }


    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        if (evt instanceof IdleStateEvent){
            IdleStateEvent idleStateEvent = (IdleStateEvent) evt;

            switch (idleStateEvent.state()){
                case ALL_IDLE:

                    boolean removeResult = ActiveChannelQueue.activeChannelQueue.remove(ctx.channel());

                    ctx.close();
                    break;
                case READER_IDLE:
                    break;
                case WRITER_IDLE:
                    break;
            }
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }


}
