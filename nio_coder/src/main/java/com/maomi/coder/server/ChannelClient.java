package com.maomi.coder.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.rmi.server.SocketSecurityException;
import java.util.Iterator;
import java.util.Set;

import com.sun.corba.se.impl.ior.ByteBuffer;

public class ChannelClient implements Runnable{

	private String host;
	
	private int port;
	
	private Selector selector;
	
	private SocketChannel socketChannel;
	
	private volatile boolean stop;
	
	public ChannelClient(String host,int port){
		this.host = host == null ? "127.0.0.1" :host;
		this.port = port;
		try {
			selector = Selector.open();
			socketChannel = SocketChannel.open();
			socketChannel.configureBlocking(false);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	@Override
	public void run() {
		try {
			//连接 socket
			doConnect();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		System.out.println("client start...");
		while(!stop){
			try {
				selector.select(1000);
				Set<SelectionKey> selectedKeys = selector.selectedKeys();
				Iterator<SelectionKey> it = selectedKeys.iterator();
				SelectionKey key = null;
				while(it.hasNext()){
					key = it.next();
					it.remove();
					try {
						handleInput(key);
					} catch (Exception e) {
						// TODO: handle exception
						if(key != null){
							if(key.channel() != null){
								key.channel().close();
							}
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
		if(selector != null){
			try {
				selector.close();
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
	}
	
	private void handleInput(SelectionKey key) throws IOException{
		//1.判断是否有效
		if(key.isValid()){
			SocketChannel sc = (SocketChannel) key.channel();
			//2.是否连接成功
			if(sc.finishConnect()){
				sc.register(selector, SelectionKey.OP_READ);
				//写入
				doWrite(sc);
			}else{
				System.exit(1);
			}
			//3.是否是读
			if(key.isReadable()){
				java.nio.ByteBuffer buffer = java.nio.ByteBuffer.allocate(1024);
				//将管道的数据读到缓冲里
				int readByte = sc.read(buffer);
				
				if(readByte > 0){
					buffer.flip();
					byte[] bytes = new byte[buffer.remaining()];
					buffer.get(bytes);
					String body = new String(bytes,"UTF-8");
					body = "this is body{hahahah}"+body;
					System.out.println(body);
					
//					this.stop = true;
				}else if(readByte < 0){
					//关闭链路
					key.cancel();
					sc.close();
				}
				
			}
		}
	}
	
	private void doConnect() throws IOException{
		if(socketChannel.connect(new InetSocketAddress(host, port))){
			socketChannel.register(selector, SelectionKey.OP_READ);
			doWrite(socketChannel);
		}else{
			socketChannel.register(selector, SelectionKey.OP_CONNECT);
		}
	}
	
	private void doWrite(SocketChannel sc) throws IOException{
		byte[] req = "ni hao a".getBytes();
		//创建缓冲
		java.nio.ByteBuffer buffer = java.nio.ByteBuffer.allocate(req.length);
		//为缓冲添加内容
		buffer.put(req);
		buffer.flip();
		
		sc.write(buffer);
		if(!buffer.hasRemaining()){
			System.out.println("send order server success");
		}
		
	}

}
