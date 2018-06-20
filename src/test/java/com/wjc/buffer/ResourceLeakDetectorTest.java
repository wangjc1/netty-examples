package com.wjc.buffer;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.ResourceLeakDetector;
import org.junit.Assume;
import org.junit.Test;

import java.net.InetSocketAddress;

/**
 * @描述: 内存泄露检测工具类测试.
 * @作者: wangjc.
 * @创建时间: 2017/2/27.
 * @版本: 1.0 .
 */
public class ResourceLeakDetectorTest {

    @Test
    public void testDirectBuffer() throws Exception {
        ByteBuf buf = Unpooled.directBuffer(512);
        System.out.println(buf);
        // SimpleLeakAwareByteBuf(UnpooledUnsafeDirectByteBuf(ridx: 0, widx: 0, cap: 512))
        // SimpleLeakAwareByteBuf是包装类，使用了装饰模式，内部维护一个UnpooledUnsafeDirectByteBuf,
        // 该类与UnpooledDirectByteBuf类似。首先在创建SimpleLeakAwareByteBuf的时候，会将该引用加入到内存泄漏检测
        // 的引用链中。
        try {
            //使用业务

        } finally {
             /*
                public boolean release() {
                    boolean deallocated =  super.release();
                    if (deallocated) {
                        leak.close();
                    }
                    return deallocated;
                }
             */
            //该方法首先调用直接内存UnpooledDirectByteBuf方法，释放所占用的堆外内存，
            //然后调用leak.close方法，通知内存泄漏检测程序，该引用所指向的堆外内存已经释放，没有泄漏。
            //如果 release没有调用，则当UnpooledDirectBytebuf被垃圾回收器收集号，该ByteBuf
            //申请的堆外内存将再也不受应用程序所掌控了，会引起内存泄漏。
            buf.release();
        }
    }
}
