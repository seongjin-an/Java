package com.hive.bee.socket;

import com.hive.bee.msg.Msg;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class SocketServer {
    private Logger logger = LoggerFactory.getLogger(SocketServer.class);

    private static SocketServer instance = null;
    private final static Object judge = new Object();

    ServerBootstrap serverBootstrap = null;
    EventLoopGroup master = null;
    EventLoopGroup worker = null;

    private int port = 22222;
    private int MAX_WORKER_THREAD = 128;

    public SocketServer(){}
    public SocketServer(int port){
        this.port = port;
    }
    public static SocketServer getInstance(int port){
        synchronized(judge){
            if(instance == null){
                instance = new SocketServer(port);
            }
            return instance;
        }
    }

    public void init(){
        master = new NioEventLoopGroup();
        worker = new NioEventLoopGroup();

        try{
            serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(master, worker)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline()
                                    .addLast("encoder", new MessageToByteEncoder<Msg>() {
                                        @Override
                                        protected void encode(ChannelHandlerContext channelHandlerContext, Msg msg, ByteBuf byteBuf) throws Exception {
                                            byteBuf.writeBytes(msg.getMsg());
                                        }
                                    })
                                    .addLast("decoder", new ByteToMessageDecoder() {
                                        @Override
                                        protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
                                            byteBuf.markReaderIndex();

                                            byte[] byteBody = new byte[11];
                                            logger.info("SOCKET MESSAGE LENGTH: {}", byteBody.length);
                                            byteBuf.readBytes(byteBody);

                                            String strBody = new String(byteBody, StandardCharsets.UTF_8);

                                            logger.info("SOCKET MESSAGE: {}", strBody);
                                        }
                                    })
                                    .addLast("handler", new SocketHandler());

                        }
                    });
            ChannelFuture fText = serverBootstrap.bind(this.port).sync();
            System.out.println("HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH");
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
