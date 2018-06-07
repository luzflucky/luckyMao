package com.maomi.coder.server;

import com.maomi.coder.server.ChannelServer;

/**
 * Created by lucky on 2018/6/6.
 */
public class TimeServer {
    public static void main(String[] args) {
        int port = 8888;
        if(args != null && args.length > 0){
        	try {
				port = Integer.valueOf(args[0]);
			} catch (Exception e) {
				// TODO: handle exception
			}
        }
        ChannelServer server = new ChannelServer(port);
        new Thread(server,"NIO-server").start();
    }
}
