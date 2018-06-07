package netty.server;


import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;


/**
 * Created by lucky on 2018/6/7.
 */
public class ChildChannelHandler extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel channel) throws Exception {
        channel.pipeline().addLast(new HandlerServer());
    }
}
