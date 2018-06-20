package com.wjc.embedded;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.MessageToMessageEncoder;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

/**
 * Listing 9.4 Testing the AbsIntegerEncoder
 *
 * @author <a href="mailto:norman.maurer@gmail.com">Norman Maurer</a>
 */
public class AbsIntegerEncoderTest {
    @Test
    public void testEncoded() {
        ByteBuf buf = Unpooled.buffer();
        for (int i = 1; i < 10; i++) {
            buf.writeInt(i * -1);
        }

        EmbeddedChannel channel = new EmbeddedChannel(
                new MessageToMessageEncoder<ByteBuf>() {
                    @Override
                    protected void encode(ChannelHandlerContext ctx, ByteBuf msg, List out) throws Exception {
                        while (msg.readableBytes() >= 4) {
                            int value = Math.abs(msg.readInt());
                            out.add(value);
                        }
                    }
                }
        );

        assertTrue(channel.writeOutbound(buf));
        assertTrue(channel.finish());

        // read bytes
        for (int i = 1; i < 10; i++) {
            assertEquals(i, channel.readOutbound());
        }
        assertNull(channel.readOutbound());
    }

    @Test
    public void test1(){
        assertTrue(BigDecimal.ZERO.compareTo(new BigDecimal("0.0000"))==0);
    }
}
