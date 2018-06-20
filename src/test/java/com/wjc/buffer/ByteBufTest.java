package com.wjc.buffer;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufProcessor;
import io.netty.buffer.Unpooled;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author: wangjc
 * 2018/4/17
 */
public class ByteBufTest {

    /**
       占3个字节的：基本等同于GBK，含21000多个汉字
       占4个字节的：中日韩超大字符集里面的汉字，有5万多个
       一个utf8数字占1个字节
       一个utf8英文字母占1个字节
     */
    @Test
    public void chinaWordUTF8Test() throws UnsupportedEncodingException {
        ByteBuf buf = Unpooled.buffer(8);

        buf.writeBytes("中国".getBytes("UTF-8"));
        assertEquals(buf.readableBytes(), 6);

        ByteBuf 中 = Unpooled.buffer(3);
        buf.readBytes(中, 0, 3);


        byte[] wordBuf = 中.array();
        System.out.println(Arrays.toString(wordBuf));//[-28, -72, -83] = 0xe4b8ad

        assertEquals("中", new String(wordBuf, "UTF-8"));
    }

    /**
     GBK编码，汉字占2个字节，utf-8占3~4个字节
     */
    @Test
    public void chinaWordGBKTest() throws UnsupportedEncodingException {
        ByteBuf buf = Unpooled.buffer(8);

        buf.writeBytes("中国".getBytes("gbk"));
        assertEquals(buf.readableBytes(),4);

        ByteBuf 中 = Unpooled.buffer(2);
        buf.readBytes(中,0,2);

        assertEquals("中", new String(中.array(),"gbk"));
    }

    @Test
    public void indexOfTest() {
        ByteBuf buf = Unpooled.buffer(8);
        buf.writeBytes("abc".getBytes());
        //byte('a')=97
        assertEquals(buf.getByte(0), 97);
        //index of b=1
        assertTrue(buf.indexOf(1, 2, (byte) 98) > 0);

        //Arrays.toString(buf.array()) result: [97, 98, 99, 0, 0, 0, 0, 0]
        //From up,three byte is able to read
        assertEquals(buf.writerIndex(), 3);


    }

    @Test
    public void findTest() {
        ByteBuf buf = Unpooled.buffer(8);
        buf.writeBytes("abc\n".getBytes());

        //the index of '\n' is 3
        assertEquals(
                buf.forEachByte(new ByteBufProcessor() {
                    @Override
                    public boolean process(byte value) throws Exception {
                        return ByteBufProcessor.FIND_LF.process(value);
                    }
                }),
        3);
    }


    /**
     * duplicate后的内存和原来的内存共享同一块内存，两个指针指向同一地址.
     */
    @Test
    public void duplicateTest() {
        ByteBuf buf = Unpooled.buffer(8);
        buf.writeBytes("abc\n".getBytes());


        ByteBuf newBuf = buf.duplicate();
        newBuf.setByte(0, 96);

        assertEquals(buf.getByte(0), 96);
    }

    /**
     * copy后的内存和原来是两个独立的内存块，指向不同的地址
     */
    @Test
    public void copyTest() {
        ByteBuf buf = Unpooled.buffer(8);
        buf.writeBytes("abc\n".getBytes());

        assertEquals(buf.capacity(), 8);

        ByteBuf newBuf = buf.copy();
        newBuf.setByte(0,96);

        assertEquals(newBuf.getByte(0), 96);
        assertEquals(buf.getByte(0), 97);
    }


    /**
     * （97,98,99），跳过一个字节，再读应该读到的是98
     */
    @Test
    public void skipBytesTest() {
        ByteBuf buf = Unpooled.buffer(8);
        buf.writeBytes("abc\n".getBytes());

        buf.skipBytes(1);

        byte a = buf.readByte();

        assertEquals(a, 98);
    }



}
