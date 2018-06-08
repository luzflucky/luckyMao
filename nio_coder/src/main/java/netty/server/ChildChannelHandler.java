package netty.server;


import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;


/**
 * Created by lucky on 2018/6/7.
 */
public class ChildChannelHandler extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel channel) throws Exception {
        //换行符分隔 line.setpatator
//        channel.pipeline().addLast(new LineBasedFrameDecoder(1024));
        //使用特殊字符分隔
        ByteBuf buf = Unpooled.copiedBuffer("$_".getBytes());
        channel.pipeline().addLast(new DelimiterBasedFrameDecoder(1024,buf));
        channel.pipeline().addLast(new StringDecoder());
        channel.pipeline().addLast(new HandlerServer());
    }
}
