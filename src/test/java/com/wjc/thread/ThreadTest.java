package com.wjc.thread;

import org.junit.Test;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

/**
 * @author: wangjc
 * 2017/6/12
 */
public class ThreadTest {

    /**
     * 线程的thread.interrupt()方法是中断线程，将会设置该线程的中断状态位，即设置为true，中断的结果线程是死亡、还是等待新的任务或是继续运行至下一步，就取决于这个程序本身。线程会不时地检测这个中断标示位，以判断线程是否应该被中断（中断标示值是否为true）。它并不像stop方法那样会中断一个正在运行的线程。
     */
    @Test
    public void testInterrupted() throws InterruptedException {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                // 每隔一秒检测是否设置了中断标示
                while (!Thread.currentThread().isInterrupted()) {
                    System.out.println("Thread is running...");
                    long time = System.currentTimeMillis();
                    // 使用while循环模拟 sleep
                    while ((System.currentTimeMillis() - time < 1000)) {
                    }
                }
                System.out.println("Thread exiting under request...");
            }
        });
        System.out.println("Starting thread...");
        thread.start();
        Thread.sleep(6000);
        System.out.println("Asking thread to stop...");
        // 发出中断请求
        thread.interrupt();
        Thread.sleep(6000);
        System.out.println("Stopping application...");

    }


    /**
     * 使用thread.interrupt()中断阻塞状态线程
     Thread.interrupt()方法不会中断一个正在运行的线程。这一方法实际上完成的是，设置线程的中断标示位，在线程受到阻塞的地方（如调用sleep、wait、join等地方）抛出一个异常InterruptedException，并且中断状态也将被清除，这样线程就得以退出阻塞的状态。下面是具体实现
     */
    @Test
    public void testInterrupted2() throws InterruptedException {

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!Thread.currentThread().isInterrupted()) {
                    System.out.println("Thread running...");
                    try {
                        /*
                         * 如果线程阻塞，将不会去检查中断信号量stop变量，所 以thread.interrupt()
                         * 会使阻塞线程从阻塞的地方抛出异常，让阻塞线程从阻塞状态逃离出来，并
                         * 进行异常块进行 相应的处理
                         */
                        Thread.sleep(1000);// 线程阻塞，如果线程收到中断操作信号将抛出异常
                    } catch (InterruptedException e) {
                        System.out.println("Thread interrupted...");
                        /*
                         * 如果线程在调用 Object.wait()方法，或者该类的 join() 、sleep()方法
                         * 过程中受阻，则其中断状态将被清除
                         */
                        System.out.println(Thread.currentThread().isInterrupted());// false

                        //中不中断由自己决定，如果需要真真中断线程，则需要重新设置中断位，如果
                        //不需要，则不用调用
                        //Thread.currentThread().interrupt();System.out.println(Thread.currentThread().isInterrupted());// true
                    }
                }
                System.out.println("Thread exiting under request...");
            }
        });

        System.out.println("Starting thread...");
        thread.start();
        Thread.sleep(3000);
        System.out.println("Asking thread to stop...");
        thread.interrupt();// 等中断信号量设置后再调用
        Thread.sleep(3000);
        System.out.println("Stopping application...");
    }

    /**
     * 死锁状态线程无法被中断
     Example4试着去中断处于死锁状态的两个线程，但这两个线都没有收到任何中断信号（抛出异常），所以interrupt()方法是不能中断死锁线程的，因为锁定的位置根本无法抛出异常：
     http://www.cnblogs.com/onlywujun/p/3565082.html
     */
    @Test
    public void testDeadLockInterrupted() throws InterruptedException {
        class DeathLock{
            void swap(Object lock1, Object lock2) {
                try {
                    synchronized (lock1) {
                        Thread.sleep(10);// 不会在这里死掉
                        synchronized (lock2) {// 会锁在这里，虽然阻塞了，但不会抛异常
                            System.out.println(Thread.currentThread());
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    System.exit(1);
                }
            }
        }

        final Object lock1 = new Object();
        final Object lock2 = new Object();
        final DeathLock deathLock = new DeathLock();
        Thread thread1 = new Thread() {
            public void run() {
                deathLock.swap(lock1, lock2);
            }
        };
        Thread thread2 = new Thread() {
            public void run() {
                // 注意，这里在交换了一下位置
                deathLock.swap(lock2, lock1);
            }
        };

        System.out.println("Starting thread...");
        thread1.start();
        thread2.start();
        Thread.sleep(3000);
        System.out.println("Interrupting thread...");
        thread1.interrupt();
        thread2.interrupt();

        Thread.sleep(3000);
        System.out.println("Stopping application...");
    }

    /**
     * 中断I/O操作
     * Java平台为这种情形提供了一项解决方案，即调用阻塞该线程的套接字的close()方法。在这种情形下，如果线程被I/O操作阻塞，当调用该套接字的close方法时，该线程在调用accept地方法将接收到一个SocketException（SocketException为IOException的子异常）异常，这与使用interrupt()方法引起一个InterruptedException异常被抛出非常相似，（注，如果是流因读写阻塞后，调用流的close方法也会被阻塞，根本不能调用，更不会抛IOExcepiton，此种情况下怎样中断？我想可以转换为通道来操作流可以解决，比如文件通道）。下面是具体实现：
     * @throws InterruptedException
     */
    @Test
    public void testIOInterrupted() throws InterruptedException, IOException {
        /*volatile*/ final ServerSocket socket=new ServerSocket(8888);

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!Thread.currentThread().isInterrupted()) {
                    System.out.println("Waiting for connection...");
                    try {
                        socket.accept();
                    } catch (IOException e) {
                        System.out.println("accept() failed or interrupted...");
                        Thread.currentThread().interrupt();//重新设置中断标示位
                    }
                }
                System.out.println("Thread exiting under request...");
            }
        });
        System.out.println("Starting thread...");
        thread.start();
        Thread.sleep(3000);
        System.out.println("Asking thread to stop...");
        Thread.currentThread().interrupt();// 再调用interrupt方法
        socket.close();// 再调用close方法
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
        }
        System.out.println("Stopping application...");

    }

    @Test
    public void testExecutor(){
        final List list = new ArrayList<>();
        list.add("qwe");
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                System.out.println("======================"+list.get(0));
            }
        });

    }
}
