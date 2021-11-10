package com.hive.bee.socket;

import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SocketHandler extends ChannelInboundHandlerAdapter {

    private Logger logger = LoggerFactory.getLogger(SocketHandler.class);

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        super.handlerAdded(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//        super.channelRead(ctx, msg);
//        ctx.writeAndFlush("000");
//        ctx.write(msg);
//        logger.info("================");
//        logger.info("SOCKET MESSAGE: {}", msg.toString());
//        logger.info("================");
//        ctx.write("success");
        ByteBuf byteBufMessage = (ByteBuf) msg;
        int size = byteBufMessage.readableBytes();
        logger.info("SIZE: {}", size);
        byte[] byteMessage = new byte[size];
        for(int i=0; i<size; i++){
            byteMessage[i]=byteBufMessage.getByte(i);
        }
        String str = new String(byteMessage);
        logger.info("===============================");
        logger.info("SOCKET MESSAGE: {}", str);
        logger.info("===============================");

//        Channel channel = ctx.channel();
//        channel.writeAndFlush("SUCCESS!!!");
//        ChannelFuture future = ctx.writeAndFlush("SUCCESS");
//        future.addListener(ChannelFutureListener.CLOSE);
        ctx.write("SUCCESS");

    }



    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
    }
}
