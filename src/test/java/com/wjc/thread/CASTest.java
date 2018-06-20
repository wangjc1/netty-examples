package com.wjc.thread;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

/**
 * @author: wangjc
 * 2017/6/13
 */
public class CASTest {
    public static void main(String[] args) {
        int count = 10000;
        CyclicBarrier cyclicBarrier= new CyclicBarrier(count);

        MyRunnable runnable = new MyRunnable(0,cyclicBarrier);
        for (int i = 0; i < count; i++) {
            new Thread(runnable).start();
        }

    }
}

class MyRunnable implements Runnable {
    private volatile int i;
    private final AtomicIntegerFieldUpdater<MyRunnable> iUpdater = AtomicIntegerFieldUpdater.newUpdater(MyRunnable.class, "i");

    //所有线程都初始完毕后开始累加
    private final CyclicBarrier cyclicBarrier;

    MyRunnable(int i, CyclicBarrier cyclicBarrier) {
        this.i = i;
        this.cyclicBarrier = cyclicBarrier;
    }

    @Override
    public void run() {
        try {
            //等待其他线程初始化
            cyclicBarrier.await();
        } catch (Exception e) {}

        //以原子方式将输入的数值与实例中的值（AtomicInteger里的value）相加，并返回结果
        //System.out.println(iUpdater.addAndGet(this,1));

        //以原子方式将当前值加1，注意：这里返回的是自增前的值。
        //System.out.println(iUpdater.getAndIncrement(this));

        //如果输入的数值等于预期值，则以原子方式将该值设置为输入的值。
        if(iUpdater.compareAndSet(this,i,i+1)){
           System.out.println(iUpdater.get(this));
        }

    }
}