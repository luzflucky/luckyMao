package msgpack;

import org.msgpack.MessagePack;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class MsgPackEncoder extends MessageToByteEncoder<Object>{

	@Override
	protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
		//编码
		MessagePack messagePack = new MessagePack();
		//使用 messaePack转成字节码
		byte[] raw = messagePack.write(msg);
		out.writeBytes(raw);
	}

}
