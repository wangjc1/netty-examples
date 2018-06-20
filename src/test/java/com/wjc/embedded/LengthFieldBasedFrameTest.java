package com.wjc.embedded;

import com.phei.netty.frame.customer.CustomDecoder;
import com.phei.netty.frame.customer.CustomEncoder;
import com.phei.netty.frame.customer.CustomMsg;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import static org.junit.Assert.assertEquals;

/**
 * @author: wangjc
 * 2017/8/30
 */
public class LengthFieldBasedFrameTest {

    @Test
    public void testDecode() throws UnsupportedEncodingException {
        //读数据，调用解码Handler
        //一开始就是长度字段，所以lengthFieldOffset=0
        //长度类型是short，2个字节，所以lengthFieldLength=2
        EmbeddedChannel channel = new EmbeddedChannel(new LengthFieldBasedFrameDecoder(1024,0,2));
        ByteBuf buf = Unpooled.buffer(1024);
        String msg = "Hello,Netty";
        byte[] msgBytes = msg.getBytes(Charset.forName("utf-8"));
        buf.writeShort(msgBytes.length);
        buf.writeBytes(msgBytes);
        boolean canRead = channel.writeInbound(buf);
        channel.finish();

        ByteBuf result = channel.readInbound();
        assertEquals(result.readShort(), msgBytes.length);
        byte[] msgByte = new byte[msg.length()];
        result.readBytes(msgByte);
        assertEquals(new String(msgByte, "utf-8"), msg);
    }

    @Test
    public void testCustomerEncode() {
        //写数据，调用编码Handler
        EmbeddedChannel channel = new EmbeddedChannel(new CustomEncoder());
        CustomMsg msg = new CustomMsg((byte) 0xAB, (byte) 0xCD, "Hello,Netty".length(), "Hello,Netty");
        boolean canRead = channel.writeOutbound(msg);
        channel.finish();

        //读数据，调用解码Handler
        ByteBuf data = (ByteBuf) channel.readOutbound();
        channel = new EmbeddedChannel(new CustomDecoder(1024 * 1024, 4, 2, 0, 0, true)/*, new CustomServerHandler()*/);
        //调用 CustomDecoder->decode(),返回true表示能读取数据
        //如果消息在CustomServerHandler中被读取了，channel.writeInbound()则返回false，否则返回true
        if (channel.writeInbound(data)) {
            msg = channel.readInbound();
            System.out.println("Client->Server:" + channel.remoteAddress() + " send " + msg.getBody());
        }
    }


}
