package com.wjc.buffer;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.IllegalReferenceCountException;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.charset.Charset;

/**
 * @author: wangjc
 * 2017/6/2
 */
public class ByteBufferTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testNioByteBuffer() {
        ByteBuffer buffer = ByteBuffer.allocate(88);
        String value = "我的博客";
        buffer.put(value.getBytes());
        buffer.flip();
        byte[] array = new byte[buffer.remaining()];
        buffer.get(array);
        String decodeValue = new String(array);

        Assert.assertEquals(value, decodeValue);
    }

    @Test
    public void testNettyByteBuf() {
        ByteBuf heapBuffer = Unpooled.buffer(88);
        String value = "我的博客";
        heapBuffer.writeBytes(value.getBytes());

        byte[] buf = new byte[heapBuffer.readableBytes()];;
        heapBuffer.readBytes(buf);
        String decodeValue = new String(buf);

        Assert.assertEquals(value, decodeValue);
    }

    @Test
    public void test1() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        IntBuffer intBuffer = IntBuffer.allocate(100);
    }

    @Test
    public void test2() {
        System.out.println("----------Test allocate--------");
        System.out.println("before alocate:"
                + Runtime.getRuntime().freeMemory());

        // 如果分配的内存过小，调用Runtime.getRuntime().freeMemory()大小不会变化？
        // 要超过多少内存大小JVM才能感觉到？
        ByteBuffer buffer = ByteBuffer.allocate(102400);
        System.out.println("buffer = " + buffer);

        System.out.println("after alocate:"
                + Runtime.getRuntime().freeMemory());

        // 这部分直接用的系统内存，所以对JVM的内存没有影响
        ByteBuffer directBuffer = ByteBuffer.allocateDirect(102400);
        System.out.println("directBuffer = " + directBuffer);
        System.out.println("after direct alocate:"
                + Runtime.getRuntime().freeMemory());

        System.out.println("----------Test wrap--------");
        byte[] bytes = new byte[32];
        buffer = ByteBuffer.wrap(bytes);
        System.out.println(buffer);

        buffer = ByteBuffer.wrap(bytes, 10, 10);
        System.out.println(buffer);

    }

    @Test
    public void test3() {
        ByteBuf heapBuffer = Unpooled.buffer(8);
        System.out.println("初始化：" + heapBuffer);
        heapBuffer.writeBytes("测试测试测试".getBytes());
        System.out.println("写入测试测试测试：" + heapBuffer);
    }

    @Test
    public void test4() {
        //1、创建缓冲区
        ByteBuf heapBuffer = Unpooled.buffer(8);

        //2、写入缓冲区内容
        heapBuffer.writeBytes("测试测试测试".getBytes());

        //3、创建字节数组
        byte[] b = new byte[heapBuffer.readableBytes()];

        System.out.println(b[11]);

        //4、复制内容到字节数组b
        heapBuffer.readBytes(b);

        System.out.println(b[11]);

        //5、字节数组转字符串
        String str = new String(b);

        System.out.println(str);

        ByteBuffer bb = heapBuffer.nioBuffer();
        System.out.println(new String(bb.array()));

    }

    @Test
    public void test5() {
        ByteBuf heapBuffer = Unpooled.buffer(10*1024*1024);
        System.out.println(heapBuffer);

        ByteBuf directBuffer = Unpooled.directBuffer();
        System.out.println(directBuffer);

        ByteBuf wrappedBuffer = Unpooled.wrappedBuffer(new byte[128]);
        System.out.println(wrappedBuffer);

        ByteBuf copiedBuffer = Unpooled.copiedBuffer(new byte[128]);
        System.out.println(copiedBuffer);

    }


    /**
     *  测试缓存区扩容
     */
    @Test
    public void test6() {
        //1、创建缓冲区
        ByteBuf heapBuffer = Unpooled.buffer(10 * 1024 * 1024);

        //2、写入缓冲区内容
        byte[] buf = new byte[12*1024*1024];
        heapBuffer.writeBytes(buf);
    }


    @Test
    public void test7() {
        ByteBuf heapBuf = Unpooled.buffer(8,128);
        byte[] b = new byte[64];
        heapBuf.writeBytes(b);
        heapBuf.writeBytes(b);
        heapBuf.writeBytes(b);
    }

    @Test
    public void test8() {
        //刚创建的buf，引用计数为1
        ByteBuf dirBuf = Unpooled.directBuffer();
        Assert.assertEquals(dirBuf.refCnt(), 1);

        //ReferenceCounted接口，自从Netty 4开始，对象的生命周期由它们的引用计数（reference counts）管理，而不是由垃圾收集器（garbage collector）管理了。ByteBuf是最值得注意的，它使用了引用计数来改进分配内存和释放内存的性能
        dirBuf.retain();
        Assert.assertEquals(dirBuf.refCnt(), 2);

        //当释放后，引用计数为0
        dirBuf.release(2);
        Assert.assertEquals(dirBuf.refCnt(), 0);

        //试图访问一个已经被释放的引用计数的对象，将会导致一个 IllegalReferenceCountException。
        thrown.expect(IllegalReferenceCountException.class);
        dirBuf.writeBytes(new byte[256]);

    }

    @Test
    public void testSlice() {
        Charset utf8 = Charset.forName("UTF-8");
        ByteBuf buf = Unpooled.copiedBuffer("Netty in Action rocks!", utf8);
        //用slice不拷贝原来内存只是分隔出一块
        ByteBuf sliced = buf.slice(0, 15);
        System.out.println(sliced.toString(utf8));
        buf.setByte(0, (byte) 'J');
        System.out.println(sliced.toString(utf8));
        assert buf.getByte(0) == sliced.getByte(0);
    }

    @Test
    public void testCopy() {
        Charset utf8 = Charset.forName("UTF-8");
        ByteBuf buf = Unpooled.copiedBuffer("Netty in Action rocks!", utf8);
        //和slice不同，copy方法会再复制一块内存
        ByteBuf copy = buf.copy(0, 15);
        System.out.println(copy.toString(utf8));
        buf.setByte(0, (byte) 'J');
        System.out.println(copy.toString(utf8));
        assert buf.getByte(0) != copy.getByte(0);
    }

    @Test
    public void testToString() {
        Charset utf8 = Charset.forName("UTF-8");
        ByteBuf buf = Unpooled.copiedBuffer("Netty实战!", utf8);
        System.out.println(buf.toString(utf8));
    }
}
