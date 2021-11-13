import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoop;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.net.InetSocketAddress;
import java.util.Scanner;

public class ChatClient {
    private static final String lineDelimitor = "\n";
    private String host;
    private int port;

    public ChatClient(String host, int port){
        this.host = host;
        this.port = port;
    }

    public static void main(String[] args) {
        new ChatClient("localhost", 9000).start();
    }

    public void start(){
        Scanner scanner = new Scanner(System.in);
        String msg;
        EventLoopGroup group= new NioEventLoopGroup();
        try{
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .remoteAddress(new InetSocketAddress(host, port))
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(new LineBasedFrameDecoder(64*1024))
                                    .addLast(new StringDecoder())
                                    .addLast(new StringEncoder())
                                    .addLast(new ChatClientHandler());
                        }
                    });
            Channel serverChannel = bootstrap.connect().sync().channel();
            ChannelFuture future = null;
            System.out.println("CLIENT CONNECT COMPLETE");
            while(true){
                msg = scanner.nextLine();
                future = serverChannel.writeAndFlush(msg.concat(lineDelimitor));
                if("quit".equals(msg)){
                    serverChannel.closeFuture().sync();
                    break;
                }
            }
            if(future != null){
                future.sync();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally{
            group.shutdownGracefully();
        }
    }
}
