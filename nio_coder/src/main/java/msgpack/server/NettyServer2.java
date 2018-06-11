package msgpack.server;


import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import msgpack.MsgPackDecoder;
import msgpack.MsgPackEncoder;

/**
 * Created by lucky on 2018/6/7.
 */
public class NettyServer2 {

    public void bind(int port){
        //1.创建线程池
        NioEventLoopGroup boos = new NioEventLoopGroup();
        NioEventLoopGroup worker = new NioEventLoopGroup();

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(boos,worker)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG,1024)
                    .childHandler(new ChildChannelHandler2());

            ChannelFuture f = bootstrap.bind(port).sync();
            f.channel().closeFuture().sync();
        }catch (InterruptedException  e){
            
        }finally{
        	boos.shutdownGracefully();
            worker.shutdownGracefully();
        }
        
    }
    
    public static void main(String[] args) {
		NettyServer2 nettyServer = new NettyServer2();
		nettyServer.bind(8089);
	}
    
}
