package com.mars.nio;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * Created by geyan on 2025/2/16 19:24
 */
public class NIOClient {

    public static void main(String[] args) throws Exception {
        // 得到一个网络通道
        SocketChannel socketChannel = SocketChannel.open();
        // 设置非阻塞
        socketChannel.configureBlocking(false);
        // 提供服务器端的ip和端口
        InetSocketAddress address = new InetSocketAddress("127.0.0.1", 6666);

        // 连接服务器
        if (!socketChannel.connect(address)) {
            while (!socketChannel.finishConnect()) {
                System.out.println("因为连接需要时间，客户端不会阻塞，可以做其他工作");
            }
        }
        // 如果连接成功，就会发送数据
        String str = "HELLO, NETTY";
        ByteBuffer buffer = ByteBuffer.wrap(str.getBytes());

        // 发送数据，将buffer数据写入channels
        socketChannel.write(buffer);

        // 阻塞一下
        System.in.read();
    }
}
