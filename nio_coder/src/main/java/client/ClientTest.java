package client;

/**
 * Created by lucky on 2018/6/7.
 */
public class ClientTest {
    public static void main(String[] args) {
        String host = "127.0.0.1";
        int port = 8889;
        new NettyClient().connect(host,port);
    }
}
