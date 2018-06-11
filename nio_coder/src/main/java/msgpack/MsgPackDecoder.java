package msgpack;

import java.util.List;

import org.msgpack.MessagePack;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

public class MsgPackDecoder extends MessageToMessageDecoder<ByteBuf>{

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
		//解码
		final byte[] bytes;
		final int length = msg.readableBytes();
		bytes = new byte[length];
		msg.getBytes(msg.readerIndex(), bytes,0,length);
		MessagePack messagePack = new MessagePack();
		//read方法反序列化成object对象
		out.add(messagePack.read(bytes));
	}

	

}
