package msgpack.server;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

public class ServerHanlder2 extends ChannelHandlerAdapter{
	
	   @Override
	   public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
	       System.out.println("this is client:"+msg);
	       ctx.writeAndFlush(msg);
	       System.out.println("发送成功....server");
	   }


	   
	   @Override
	   public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		   ctx.close();
	   }
}
