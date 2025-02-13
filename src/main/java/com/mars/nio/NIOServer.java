package com.mars.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;

/**
 * 1. 当有客户端连接时，会通过ServerSocketChannel得到SocketChannel
 * 2. Selector进行监听select方法，返回有事件发生的通道的个数
 * 3. 将socketChannel注册到Selector上，一个selector上可以注册多个SocketChannel
 * 4. 注册后返回一个SelectionKey，会和该Selector关联
 * 5. 进一步得到各个SelectionKey
 * 6. 通过SelectionKey反向获取SocketChannel
 * 7. 通过得到的channel，进行业务处理
 *
 * Created by geyan on 2025/2/13 21:47
 */
public class NIOServer {

    public static void main(String[] args) throws IOException {

        // 创建ServerSocketChannel
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();

        // 获取Selector对象
        Selector selector = Selector.open();

        // 绑定端口，在服务器里监听，这里拿到socket
        serverSocketChannel.socket().bind(new InetSocketAddress(6666));
        // 设置非阻塞
        serverSocketChannel.configureBlocking(false);

        // 把serverSocketChannel注册到Selector，关心的事件为ACCEPT
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        // 循环等待客户端连接
        while (true) {
            // 等待1秒，如果没有事件发生，就继续
            if (selector.select(1000) == 0) {
                // 没有事件发生
                System.out.println("服务器等待了1秒，无连接");
                continue;
            }
            // 获取到相关的selection集合，有关注事件发生的集合
            // 通过这个，可以反向获取通道
            Set<SelectionKey> selectionKeys = selector.selectedKeys();

            Iterator<SelectionKey> keyIterator = selectionKeys.iterator();
            while (keyIterator.hasNext()) {
                SelectionKey key = keyIterator.next();
                if (key.isAcceptable()) {
                    // 如果发生的是OP_ACCEPT事件，说明有新的客户端来连接
                    // 给该客户端生成一个SocketChannel
                    SocketChannel socketChannel = serverSocketChannel.accept();
                    // 将SocketChannel设置为非阻塞的
                    socketChannel.configureBlocking(false);

                    System.out.println("客户端连接成功，生成看了一个socketChannel： " + socketChannel.hashCode());
                    // 将socketChannel注册到selector，这个关注READ事件，给该channel关联buffer
                    socketChannel.register(selector, SelectionKey.OP_READ, ByteBuffer.allocate(1024));

                }
                if (key.isReadable()) {
                    // 发生了OP_READ事件
                    // 通过key反向获取到对应的channel
                    SocketChannel channel = (SocketChannel)key.channel();
                    // 获取到该channel关联到的buffer
                    ByteBuffer buffer = (ByteBuffer) key.attachment();
                    channel.read(buffer);
                    System.out.println("form client " + new String(buffer.array()));
                }
                // 手动从集合中手动移除当前的selectionKey，防止重复操作，因为是多线程
                keyIterator.remove();
            }
        }
    }
}
