package netty.server;

/**
 * Created by lucky on 2018/6/7.
 */
public class ServerTest {

    public static void main(String[] args) {
        int port = 8889;
        new NettyServer().bind(port);
    }
}
