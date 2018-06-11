package msgpack;

import java.util.Iterator;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelHandler.Skip;

public class EcoChildHanlder extends ChannelHandlerAdapter{
	
	
	private final int senNumber;
	
	public EcoChildHanlder(int senNumber){
		this.senNumber = senNumber;
	}
	
   @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
//       	UserInfo[] users = getUsers();
//       	for (UserInfo userInfo : users) {
//       		ctx.write(userInfo);
//		}
//       	System.out.println("发送成功....clinet");
//       	ctx.flush();
	   ctx.writeAndFlush(new UserInfo(10, "xxxx"));
    }
   
   public UserInfo[] getUsers(){
	   UserInfo[] users = new UserInfo[senNumber];
	   UserInfo user = null;
	   for(int i=0;i<senNumber;i++){
		   user = new UserInfo(i, "this is :"+i);
		   users[i] = user;
	   }
	   return users;
   }
   
   @Override
   public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
       System.out.println("this is server:"+msg);
       ctx.write(msg);
   }

   @Override
   public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
       ctx.flush();
   }
   
   @Override
   public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
	   ctx.close();
   }

}
