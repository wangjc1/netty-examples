package com.wjc.thread;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 * 多个SocketChannel注册Selector。
 *
 *
 */
public class MoreChannelOnOneSelectorTest {
    // 判断已经写过数据的标志
    static boolean s1 = false;
    static boolean s2 = false;

    // 2个连接注册的选择器关键字
    static SelectionKey key1;
    static SelectionKey key2;

    public static void main(String[] args) {
        // 1个选择器，注册2个Socket 通道
        Selector selector = null;
        try {
            // 创建选择器
            selector = Selector.open();
            // 创建2个通道
            SocketChannel sChannel1 = createSocketChannel("localhost", 9055);
            SocketChannel sChannel2 = createSocketChannel("localhost", 9056);
            // 注册选择器，侦听所有的事件
            key1 = sChannel1.register(selector, sChannel1.validOps());
            key2 = sChannel2.register(selector, sChannel1.validOps());
        } catch (IOException e) {
        }
        // 等待事件的循环
        while (true) {
            try {
                // 等待
                selector.select();
            } catch (IOException e) {
                break;
            }
            // 所有事件列表
            Iterator<SelectionKey> it = selector.selectedKeys().iterator();
            // 处理每一个事件
            while (it.hasNext()) {
                // 得到关键字
                SelectionKey selKey = it.next();
                // 删除已经处理的关键字
                it.remove();
                try {
                    // 处理事件
                    processSelectionKey(selKey);
                } catch (IOException e) {
                    // 处理异常
                    selKey.cancel();
                }
            }
        }
    }


    public static SocketChannel createSocketChannel(String hostName, int port)
            throws IOException {
        SocketChannel sChannel = SocketChannel.open();
        sChannel.configureBlocking(false);
        sChannel.connect(new InetSocketAddress(hostName, port));
        return sChannel;
    }

    public static void processSelectionKey(SelectionKey selKey) throws IOException {
        ByteBuffer buf = ByteBuffer.allocateDirect(1024);
        // 确认连接正常
        if (selKey.isValid() && selKey.isConnectable()) {
            // 得到通道
            SocketChannel sChannel = (SocketChannel) selKey.channel();
            // 是否连接完毕？
            boolean success = sChannel.finishConnect();
            if (!success) {
                // 异常
                selKey.cancel();
            }
        }
        // 如果可以读取数据
        if (selKey.isValid() && selKey.isReadable()) {
            // 得到通道
            SocketChannel sChannel = (SocketChannel) selKey.channel();
            if (sChannel.read(buf) > 0) {
                // 转到最开始
                buf.flip();
                while (buf.remaining() > 0) {
                    System.out.print((char) buf.get());
                }
                // 也可以转化为字符串，不过需要借助第三个变量了。
                // buf.get(buff, 0, numBytesRead);
                // System.out.println(new String(buff, 0, numBytesRead, "UTF-8"));
                // 复位，清空
                buf.clear();
            }
        }
        // 如果可以写入数据
        if (selKey.isValid() && selKey.isWritable()) {
            // 得到通道
            SocketChannel sChannel = (SocketChannel) selKey.channel();
            // 区分2个侦听器的关键字
            // 我这里只写一次数据。
            if (!s1 && key1.equals(selKey)) {
                System.out.println("channel1 write data..");
                buf.clear();
                buf.put("HELO localhost/n".getBytes());
                buf.flip();
                sChannel.write(buf);
                s1 = true;
            } else if (!s2 && key2.equals(selKey)) {
                System.out.println("channel2 write data..");
                buf.clear();
                buf.put("HELO localhost/n".getBytes());
                buf.flip();
                sChannel.write(buf);
                s2 = true;
            }
        }
    }

}