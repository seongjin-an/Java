import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class EchoClientHandler extends ChannelInboundHandlerAdapter {
//    private Logger logger;
//
//    {
//        logger = LoggerFactory.getLogger(EchoClientHandler.class);
//    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        String sendMessage = "Hello, Netty from client!";
        ByteBuf messageBuffer = Unpooled.buffer();
        messageBuffer.writeBytes(sendMessage.getBytes(StandardCharsets.UTF_8));

        StringBuilder builder = new StringBuilder();
        builder.append("전송할 문자열 [");
        builder.append(sendMessage);
        builder.append("]");

//        logger.info("SEND SOCKET MESSAGE: {}", builder.toString());
        System.out.println(builder.toString());

        ctx.writeAndFlush(messageBuffer);
        ctx.writeAndFlush("HELLO!!!!");
        ctx.channel().writeAndFlush("HELLO");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        String readMessage = ((ByteBuf) msg).toString(Charset.defaultCharset());

        StringBuilder builder = new StringBuilder();
        builder.append("수신한 문자열 [");
        builder.append(readMessage);
        builder.append("]");
//        logger.info("RECEIVED SOCKET MESSAGE: {}", builder.toString());
        System.out.println(builder.toString());
        System.out.println(((ByteBuf) msg).toString());
        System.out.println((String)msg);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        ctx.close();
    }
}
