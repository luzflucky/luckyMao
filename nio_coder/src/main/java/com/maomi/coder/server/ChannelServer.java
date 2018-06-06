package com.maomi.coder.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by lucky on 2018/6/6.
 */
public class ChannelServer implements Runnable {

    private Selector selector;

    private ServerSocketChannel serverSocketChannel;

    private volatile boolean stop = false;

    public ChannelServer(int port){
        try {
            //1.打开多路复用器
            selector = Selector.open();
            //2.打开管道
            serverSocketChannel = ServerSocketChannel.open();
            //3.配置非阻塞
            serverSocketChannel.configureBlocking(false);
            //3.绑定ip backlog 请求队列最大长度 不传默认是50
            serverSocketChannel.socket().bind(new InetSocketAddress(port),1024);
            //4.注册多路复用器
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stop(){
        if(!stop){
            stop = true;
        }
    }

    public void run() {
        System.out.println("...");
        while(!stop){
            try {
                //1.阻塞超时
                selector.select(1000);
                //2.递归keys
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> it = selectionKeys.iterator();
                SelectionKey key = null;
                while(it.hasNext()){
                    key = it.next();
                    it.remove();
                    try {
                        //3.处理key的内容
                        handelInput(key);
                    }catch (Exception e){
                        e.printStackTrace();
                        if(key != null){
                            key.cancel();
                            if(key.channel() != null){
                                key.channel().close();
                            }
                        }
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            if(selector != null){
                try {
                    selector.close();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }

    private void handelInput(SelectionKey key) throws IOException {
        //1.判断key是否有效
        if(key.isValid()){
            if(key.isAcceptable()){
                //2.接收新的连接
                ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
                SocketChannel ac = ssc.accept();
                ac.configureBlocking(false);
                ac.register(selector,SelectionKey.OP_READ);
            }else{
                if(key.isReadable()){
                    SocketChannel sc = (SocketChannel) key.channel();
                    ByteBuffer buffer = ByteBuffer.allocate(1024);
                    int read = sc.read(buffer);
                    if(read > 0){
                        buffer.flip();
                        byte[] bytes = new byte[buffer.remaining()];
                        buffer.get(bytes);
                        String body = new String(bytes,"UTF-8");
                        body = "this is body{}"+body;
                        System.out.println(body);

                        doWrite(sc,body);
                    }
                }
            }
        }
    }

    private void doWrite(SocketChannel sc,String body) throws IOException {
        byte[] bytes = body.getBytes();
        ByteBuffer buffer = ByteBuffer.allocate(bytes.length);
        buffer.put(bytes);
        buffer.flip();
        sc.write(buffer);
    }
}
