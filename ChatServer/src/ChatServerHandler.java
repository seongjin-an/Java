import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatServerHandler extends ChannelInboundHandlerAdapter {
    private static final ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    private static Map<String, List<Channel>> rooms = new HashMap<>();
    private static int TEST_ROOM = 0;
    private String ROOM_ODD = "odd";
    private String ROOM_EVEN = "even";

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {

        Channel newChannel = ctx.channel();
        channelGroup.add(newChannel);
        TEST_ROOM += 1;
        String nm = TEST_ROOM % 2 == 0 ? "even" : "odd";
        if(rooms.get(nm) == null){
            System.out.println("new room");
            List<Channel> newList = new ArrayList<>();
            newList.add(newChannel);
            rooms.put(nm, newList);

        }else{
            System.out.println("exist room");
            rooms.get(nm).add(newChannel);
        }
        rooms.get(nm).forEach(System.out::println);
        System.out.println("[channelRegistered] :".concat(newChannel.remoteAddress().toString()));

        for(Channel channel: channelGroup){
            channel.writeAndFlush(
                    "[SERVER] ".concat(newChannel.remoteAddress().toString()).concat(" LOGIN").concat("\n")
            );
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Channel newChannel = ctx.channel();
        System.out.println("[NEW CLIENT] remote address - ".concat(newChannel.remoteAddress().toString()));
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        Channel oldChannel = ctx.channel();
        System.out.println("[channelUnregistered]: ".concat(oldChannel.remoteAddress().toString()));
        for(Channel channel: channelGroup){
            channel.writeAndFlush(
                    "[SERVER] ".concat(oldChannel.remoteAddress().toString()).concat(" LOGOUT").concat("\n")
            );
        }
        channelGroup.remove(oldChannel);

        String nm = TEST_ROOM % 2 == 0 ? "even" : "odd";
        if(rooms.get(nm) != null){
            rooms.get(nm).remove(oldChannel);
            rooms.get(nm).forEach(System.out::println);
        }

    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        String message = null;
        message = (String) msg;
        System.out.println("[channelRead]: ".concat(message));
        Channel msgSender = ctx.channel();
//        for(Channel channel: channelGroup){
//            System.out.println("CHANNEL: ".concat(channel.remoteAddress().toString()));
//            channel.writeAndFlush(
//                    "[".concat(msgSender.remoteAddress().toString()).concat("]").concat(message).concat("\n")
//            );
//        }
        rooms.entrySet().forEach(System.out::println);
        String nm = TEST_ROOM % 2 == 0 ? "even" : "odd"; 
        if(rooms.get(nm)!=null){
            List<Channel> channels = rooms.get(ROOM_ODD);
            for(Channel channel: channels){
                System.out.println("CHANNEL: ".concat(channel.remoteAddress().toString()));
                channel.writeAndFlush(
                        "[".concat(msgSender.remoteAddress().toString()).concat("]").concat(message).concat("\n")
                );
            }
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }
}
