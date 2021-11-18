import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

public class HttpNettyRequest {
    public static void main(String[] args) {
        EventLoopGroup group = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group);
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel socketChannel) throws Exception {
                ChannelPipeline pipeline = socketChannel.pipeline();
                pipeline.addLast(new HttpClientCodec());
                pipeline.addLast(new HttpNettyResponseHandler());
            }
        });
        String content =
                "{" +
                "  \"body\": {" +
                "    \"trName\": \"t1511\"," +
                "    \"bNext\": false," +
                "    \"query\": {" +
                "      \"upcode\": \"001\"" +
                "    }" +
                "  }," +
                "   \"header\": {" +
                "    \"uuid\": \"7b81c375-d9b9-43c1-8449-77e561a979f2\"" +
                "  }" +
                "}";
        try{
            URI uri = new URI("http://localhost:8888/bee/data");
            FullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, uri.getRawPath());
            request.headers().set(HttpHeaderNames.HOST, uri.getHost());
            request.headers().set(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON);
            request.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);

            ByteBuf byteBuf = Unpooled.copiedBuffer(content, StandardCharsets.UTF_8);
            request.headers().set(HttpHeaderNames.CONTENT_LENGTH, byteBuf.readableBytes());
            request.content().writeBytes(byteBuf);
            Channel ch = bootstrap.connect(uri.getHost(), uri.getPort()).sync().channel();
            ch.writeAndFlush(request);
            ch.closeFuture().sync();
        } catch (URISyntaxException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
        }
    }
}
