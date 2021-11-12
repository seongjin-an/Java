import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class EchoServerHandler extends ChannelInboundHandlerAdapter {
    //SimpleChannelInboundHandler
//    @Override
//    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object o) throws Exception {
//        String readMessage = ((ByteBuf) o).toString(Charset.defaultCharset());
//        channelHandlerContext.write(readMessage);
//    }
    public static ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        Channel incoming = ctx.channel();
        for (Channel channel : channels) {
            String response = "[SERVER] -"+incoming. remoteAddress ()+ "Add\n";
            ByteBuf messageBuffer = Unpooled.buffer();
            messageBuffer.writeBytes(response.getBytes(StandardCharsets.UTF_8));
            //ctx.write("SUCCESS");//XXXX
            channel.writeAndFlush(messageBuffer);
//            channel.writeAndFlush ("[SERVER] -"+incoming. remoteAddress ()+ "Add\n");
        }
        channels.add(ctx.channel());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        String readMessage = ((ByteBuf) msg).toString(Charset.defaultCharset());
        System.out.println("수신한 문자열[" + readMessage + "]");
        String responseMessage = "awesome!!";
//        ctx.write(msg);
        ByteBuf messageBuffer = Unpooled.buffer();
        messageBuffer.writeBytes(responseMessage.getBytes(StandardCharsets.UTF_8));
        //ctx.write("SUCCESS");//XXXX
        ctx.write(messageBuffer);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

}
