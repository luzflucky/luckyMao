package netty_file.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpRequestEncoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.stream.ChunkedWriteHandler;
import netty_file.test.HttpFileServerHandler;

public class HttpFileServer {

	private static final String path = "/src/";
	
	public void run(final String host,final int port,final String url) throws InterruptedException{
		
		NioEventLoopGroup boos = new NioEventLoopGroup();
		NioEventLoopGroup worker = new NioEventLoopGroup();
		
		
		try {
			ServerBootstrap b = new ServerBootstrap();
			b.group(boos,worker)
			   .channel(NioServerSocketChannel.class)
			   .childHandler(new ChannelInitializer<SocketChannel>() {
				@Override
				protected void initChannel(SocketChannel ch) throws Exception {
					//HTTP请求解码器
					ch.pipeline().addLast("http-decoder",new HttpRequestDecoder());
					//转为单一的FullHttpRequest 或 FullHttpResponse
					ch.pipeline().addLast("http-aggergator",new HttpObjectAggregator(65536));
					//http 响应编码器
					ch.pipeline().addLast("http-encoder",new HttpResponseEncoder());
					//支持异步发送大的码流
					ch.pipeline().addLast("http-chunked",new ChunkedWriteHandler());
		
					ch.pipeline().addLast(new HttpFileServerHanlder(path));
				}
			});
			System.out.println("HTTP 文件服务器启动, 地址是： " + "http://localhost:" + port + url);
			ChannelFuture f = b.bind(host,port).sync();
			f.channel().closeFuture().sync();
			
		} catch (Exception e) {
			// TODO: handle exception
		}finally{
			boos.shutdownGracefully();
			worker.shutdownGracefully();
		}
	}
	
	public static void main(String[] args) {
		HttpFileServer server = new HttpFileServer();
		try {
			server.run("127.0.0.1", 8081, path);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
