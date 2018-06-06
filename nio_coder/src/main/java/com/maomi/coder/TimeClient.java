package com.maomi.coder;

import com.maomi.coder.server.ChannelClient;

public class TimeClient {

	public static void main(String[] args) {
		int port = 8888;
        if(args != null && args.length > 0){
        	try {
				port = Integer.valueOf(args[0]);
			} catch (Exception e) {
				// TODO: handle exception
			}
        }
        ChannelClient client = new ChannelClient("127.0.0.1",port);
        new Thread(client,"time client server").start();
	}

}
