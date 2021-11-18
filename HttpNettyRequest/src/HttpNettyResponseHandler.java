import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.util.CharsetUtil;

public class HttpNettyResponseHandler extends SimpleChannelInboundHandler<HttpObject> {

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, HttpObject httpObject) throws Exception {
        if(httpObject instanceof HttpResponse){
            HttpResponse response = (HttpResponse) httpObject;
            System.out.println("STATUS: " + response.status());
            System.out.println("VERSION: " + response.protocolVersion());
            System.out.println();

            if(!response.headers().isEmpty()){
                for(CharSequence name: response.headers().names()){
                    for(CharSequence value: response.headers().getAll(name)){
                        System.out.println("HEADEr: " + name + " = " + value);
                    }
                }
                System.out.println();
            }
            System.out.println("CONTENT: [");
        }
        if(httpObject instanceof HttpContent){
            HttpContent content = (HttpContent) httpObject;

            System.out.print(content.content().toString(CharsetUtil.UTF_8));
            System.out.flush();

            if(content instanceof LastHttpContent){
                System.out.println();
                System.out.println("] END OF CONTENT");
                channelHandlerContext.close();
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
