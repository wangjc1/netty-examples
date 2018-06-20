package com.wjc.buffer;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import org.junit.Test;

/**
 * @描述: .
 * @作者: wangjc.
 * @创建时间: 2017/3/2.
 * @版本: 1.0 .
 */
public class PooledBufTest {

    @Test
    public void bufAllocatorTest(){
        PooledByteBufAllocator.DEFAULT.buffer(8*1024);
    }

    @Test
    public void  bitTest(){
        int id = 2048;

        System.out.println(Integer.toString(id,2));

        id ^= 1;
        System.out.println(Integer.toString(id,2));
    }
}
