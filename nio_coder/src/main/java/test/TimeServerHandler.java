package test;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

public class TimeServerHandler extends ChannelHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // TODO Auto-generated method stub
        try {
        //直接输出msg
            System.out.println(msg.toString());
            String remsg = new String("has receive");
        //回复has receive 给客户端
            ctx.write(remsg);
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        // TODO Auto-generated method stub
        ctx.flush();
    }
}