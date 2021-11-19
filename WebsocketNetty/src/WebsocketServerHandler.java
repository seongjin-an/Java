import com.google.gson.Gson;
import dto.Response;
import entity.Client;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.json.simple.parser.ParseException;
import service.MessageService;
import service.RequestService;
import sun.net.www.http.HttpClient;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WebsocketServerHandler extends SimpleChannelInboundHandler<Object> {

    //websocket uri
    private static final String WEBSOCKET_PATH = "/websocket";

    //channelGrouop
    private static Map<Integer, ChannelGroup> channelGroupMap = new ConcurrentHashMap<>();

    //code
    private static final String HTTP_REQUEST_STRING = "request";

    private Client client;

    private WebSocketServerHandshaker handshaker;

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object msg) throws Exception {
        if(msg instanceof FullHttpRequest){
            handleHttpRequest(channelHandlerContext, (FullHttpRequest) msg);
        }else if(msg instanceof WebSocketFrame){
            handleWebSocketFrame(channelHandlerContext, (WebSocketFrame) msg);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    private void handleHttpRequest(ChannelHandlerContext channelHandlerContext, FullHttpRequest req) throws ParseException {
        //Handle bad request
        if(!req.decoderResult().isSuccess()){
            sendHttpResponse(channelHandlerContext, req, new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST));
            return;
        }

        //Allow get method only
        if(req.method() != HttpMethod.GET){
            sendHttpResponse(channelHandlerContext, req, new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.FORBIDDEN));
            return;
        }

        if("/favicon.ico".equals(req.uri()) || "/".equals(req.uri())){
            sendHttpResponse(channelHandlerContext, req, new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND));
            return;
        }

        QueryStringDecoder queryStringDecoder = new QueryStringDecoder(req.uri());
        Map<String, List<String>> parameters = queryStringDecoder.parameters();

        if(parameters.size() == 0 || !parameters.containsKey(HTTP_REQUEST_STRING)){
            System.err.println(HTTP_REQUEST_STRING + " You cannot set parameters to their default values.");
            sendHttpResponse(channelHandlerContext, req, new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND));
            return;
        }

        client = RequestService.registerClient(parameters.get(HTTP_REQUEST_STRING).get(0));
        if(client.getRoomId() == 0){
            System.err.println("Room number cannot be defaulted");
            sendHttpResponse(channelHandlerContext, req, new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND));
            return;
        }

        //If it does not exist in the room list, it is the channel, then add a new channel to channelGroup
        if(!channelGroupMap.containsKey(client.getRoomId())){
            channelGroupMap.put(client.getRoomId(), new DefaultChannelGroup(GlobalEventExecutor.INSTANCE));
        }

        //Make sure there is a room number before adding the client to the channel
        channelGroupMap.get(client.getRoomId()).add(channelHandlerContext.channel());

        //handShake
        WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(getWebSocketLocation(req), null, true);
        handshaker = wsFactory.newHandshaker(req);
        if(handshaker == null){
            WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(channelHandlerContext.channel());
        }else{
            ChannelFuture channelFuture = handshaker.handshake(channelHandlerContext.channel(), req);

            //After the handshake is successful, the business logic
            if(channelFuture.isSuccess()){
                if(client.getId() == 0){
                    System.out.println(channelHandlerContext.channel() + "visit");
                    return;
                }
            }
        }
    }

    private void handleWebSocketFrame(ChannelHandlerContext channelHandlerContext, WebSocketFrame frame) throws ParseException {
        if(frame instanceof CloseWebSocketFrame){
            handshaker.close(channelHandlerContext.channel(), (CloseWebSocketFrame) frame.retain());
            return;
        }
        if(frame instanceof PingWebSocketFrame){
            channelHandlerContext.channel().write(new PingWebSocketFrame(frame.content().retain()));
            return;
        }
        if(!(frame instanceof TextWebSocketFrame)){
            throw new UnsupportedOperationException(String.format("%s frame types not supported", frame.getClass().getName()));
        }

        broadcast(channelHandlerContext, frame);
    }

    private void broadcast(ChannelHandlerContext channelHandlerContext, WebSocketFrame frame) throws ParseException {
        Gson gson = new Gson();
        if(client.getId() == 0) {
            Response response = new Response(1001, "You can’t chat without logging in");

            String msg = gson.toJson(response);
            channelHandlerContext.channel().write(new TextWebSocketFrame(msg));
            return;
        }

        String request = ((TextWebSocketFrame) frame).text();
        System.out.println("RECEIVE ".concat(channelHandlerContext.channel().toString()).concat(request));

        Response response = MessageService.sendMessage(client, request);
        String msg = gson.toJson(response);
        if(channelGroupMap.containsKey(client.getRoomId())){
            channelGroupMap.get(client.getRoomId()).writeAndFlush(new TextWebSocketFrame(msg));
        }
    }

    private String getWebSocketLocation(FullHttpRequest req) {
        String location = req.headers().get(HttpHeaderNames.HOST).concat(WEBSOCKET_PATH);
        return "ws://".concat(location);
    }

    private void sendHttpResponse(ChannelHandlerContext channelHandlerContext, FullHttpRequest req, DefaultFullHttpResponse res) {
        if(res.status().code() != 200){
            ByteBuf buf = Unpooled.copiedBuffer(res.status().toString(), CharsetUtil.UTF_8);
            res.content().writeBytes(buf);
            buf.release();
        }
        ChannelFuture f = channelHandlerContext.channel().writeAndFlush(res);
        if(req.protocolVersion().isKeepAliveDefault() || res.status().code() != 200){
            f.addListener(ChannelFutureListener.CLOSE);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        Channel incoming = ctx.channel();
        System.out.println(incoming.remoteAddress() + " JOIN");
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        if(client != null && channelGroupMap.containsKey(client.getRoomId())){
            channelGroupMap.get(client.getRoomId()).remove(ctx.channel());
        }
    }
}