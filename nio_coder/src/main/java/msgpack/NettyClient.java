package msgpack;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;

/**
 * Created by lucky on 2018/6/7.
 */
public class NettyClient {
	
	
    
    public void connect(String host,int port,int sendNumber){
        NioEventLoopGroup worker = new NioEventLoopGroup();

        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(worker)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY,true)
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        protected void initChannel(SocketChannel chan) throws Exception {
                        	
                        	chan.pipeline().addLast(new LengthFieldBasedFrameDecoder(65535,0,2,0,2));
                        	//编码
                        	chan.pipeline().addLast("msgpack decoder",new MsgPackDecoder());
                        	//加2个空字节分割
                        	chan.pipeline().addLast("frameEncoder",new LengthFieldPrepender(2));
                        	//解码
                        	chan.pipeline().addLast("msgpack encoder",new MsgPackEncoder());
                        	chan.pipeline().addLast(new EcoChildHanlder(sendNumber));
                        	System.out.println("client ok");
                        }
                    });
            ChannelFuture f = bootstrap.connect(host,port).sync();
            f.channel().closeFuture().sync();
        }catch (Exception e){
           
        }finally {
        	 worker.shutdownGracefully();
		}

    }
    
    public static void main(String[] args) {
		NettyClient nettyClient = new NettyClient();
		nettyClient.connect("127.0.0.1", 8089, 30);
	}

}
