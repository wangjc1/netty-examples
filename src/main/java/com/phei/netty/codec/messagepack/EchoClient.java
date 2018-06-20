package com.phei.netty.codec.messagepack;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;


/**
 *
 当EchoClient.java中的sendNumber为1时，服务端和客户端都是正常工作的，此时，服务端和客户端的输出分别如下：

 服务端：

 Server receive the msgpack message : ["ABCDEFG --->0",0]
 客户端：

 Client receive the msgpack message : ["ABCDEFG --->0",0]
 但是当sendNumber数字很大时，就不能正常工作了，比如可以设置为1000，此时输出结果如下：

 服务端：

 Server receive the msgpack message : ["ABCDEFG --->0",0]
 Server receive the msgpack message : ["ABCDEFG --->1",1]
 Server receive the msgpack message : ["ABCDEFG --->3",3]
 ...省略输出...
 Server receive the msgpack message : ["ABCDEFG --->146",146]
 Server receive the msgpack message : 70
 Server receive the msgpack message : ["ABCDEFG --->156",156]
 Server receive the msgpack message : ["ABCDEFG --->157",157]
 ...省略输出...
 客户端：

 Client receive the msgpack message : ["ABCDEFG --->0",0]
 Client receive the msgpack message : 62
 Client receive the msgpack message : 68
 显然运行结果跟预期的不太一样，这是因为出现了TCP粘包问题。


 */
public class EchoClient {
    private final String host;
    private final int port;
    private final int sendNumber;

    public EchoClient(int port, String host, int sendNumber) {
        this.host = host;
        this.port = port;
        this.sendNumber = sendNumber;
    }

    public void run() throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group).channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            //LengthFieldBasedFrameDecoder用于处理半包消息
                            //这样后面的MsgpackDecoder接收的永远是整包消息
                            ch.pipeline().addLast("frameDecoder", new LengthFieldBasedFrameDecoder(65535, 0, 2, 0, 2));
                            ch.pipeline().addLast("msgpack decoder", new MsgPackDecoder());
                            //在ByteBuf之前增加2个字节的消息长度字段
                            ch.pipeline().addLast("frameEncoder", new LengthFieldPrepender(2));
                            ch.pipeline().addLast("msgpack encoder", new MsgpackEncoder());
                            ch.pipeline().addLast(new EchoClientHandler(sendNumber));
                        }

                    });
            ChannelFuture f = b.connect(host, port).sync();
            f.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception {
        int port = 8085;
        new EchoClient(port, "127.0.0.1", 1000).run();
    }
}