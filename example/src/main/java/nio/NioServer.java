package nio;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.List;

public class NioServer {
    public static void main(String[] args) throws Exception {
        List<SocketChannel> clients = new LinkedList<>();
        ServerSocket serverSocket = new ServerSocket();
        serverSocket.accept();
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        ServerSocket socket = serverSocketChannel.socket();
        socket.bind(new InetSocketAddress(9999));
        serverSocketChannel.bind(new InetSocketAddress(9999));
        serverSocketChannel.configureBlocking(false);
        while (true) {
            Thread.sleep(1000);
            SocketChannel client = serverSocketChannel.accept();
            if (client == null) {
                //.........
            } else {
                client.configureBlocking(false);
                int port = client.socket().getPort();
                System.out.println("client...port = " + port);
                clients.add(client);
            }
            ByteBuffer buffer = ByteBuffer.allocateDirect(4096);
            for (SocketChannel c : clients) {
                int num = c.read(buffer);
                if (num > 0) {
                    buffer.flip();
                    byte[] bytes = new byte[buffer.limit()];
                    buffer.get(bytes);
                    System.out.println(new String(bytes) + " : " + client.socket().getPort());
                    buffer.clear();
                }
            }
        }
    }
}