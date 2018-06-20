package com.wjc.task;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelPromise;
import io.netty.channel.DefaultChannelPromise;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.junit.Test;

/**
 * @author: wangjc
 * 2017/6/22
 */
public class PromiseTest {
    @Test
    public void futureTest() throws InterruptedException {
        ChannelPromise promise = new DefaultChannelPromise(null, GlobalEventExecutor.INSTANCE);
        promise.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                System.out.println("completed");
            }
        });

        promise.sync();
    }
}
