package msgpack.server;

import java.net.SocketAddress;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPromise;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import msgpack.MsgPackDecoder;
import msgpack.MsgPackEncoder;

public class ChildChannelHandler2 extends ChannelInitializer<SocketChannel> {

	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(65535,0,2,0,2));
    	//编码
		ch.pipeline().addLast("msgpack decoder",new MsgPackDecoder());
    	//加2个空字节分割
    	ch.pipeline().addLast("frameEncoder",new LengthFieldPrepender(2));
    	//解码
    	ch.pipeline().addLast("msgpack encoder",new MsgPackEncoder());
		
		ch.pipeline().addLast(new ServerHanlder2());
		System.out.println("server ok");
		
	}

	

}
