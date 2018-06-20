package com.wjc.reactor;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Acceptor线程： 从主线程池中随机选择一个Reactor线程作为Acceptor线程，用于绑定监听端口，接收客户端连接
 * Main Reactor线程：Acceptor线程接收客户端连接请求之后创建新的SocketChannel，将其注册到 Main Reactor线程上，由其负责接入认证、IP黑白名单过滤、握手等操作；
 * Sub Reactor线程：步骤2完成之后，业务层的链路正式建立，将SocketChannel从主线程池的Reactor线程的多路复用器上摘除，重新注册到Sub线程池的线程上，用于处理I/O的读写操作
 * @author: wangjc
 * 2017/7/20
 */
public class ReactorThreadBTest {

    private static final int DEFAULT_PORT = 9090;

    public static void main(String[] args) {

        new Thread(new Acceptor()).start();

    }


    private static class Acceptor implements Runnable {

        // main Reactor 线程池，用于处理客户端的连接请求
        private static ExecutorService mainReactor = Executors.newSingleThreadExecutor();

        public void run() {
            // TODO Auto-generated method stub
            ServerSocketChannel ssc = null;

            try {
                ssc = ServerSocketChannel.open();
                ssc.configureBlocking(false);
                ssc.bind(new InetSocketAddress(DEFAULT_PORT));
                //转发到 MainReactor反应堆
                dispatch(ssc);

                System.out.println("服务端成功启动。。。。。。");

            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        private void dispatch(ServerSocketChannel ssc) {
            mainReactor.submit(new MainReactor(ssc));
        }

    }

    /**
     * 主Reactor,主要用来处理连接请求的反应堆
     *
     * @author Administrator
     */
    public static class MainReactor implements Runnable {

        private Selector selector;
        private SubReactorThreadGroup subReactorThreadGroup;

        public MainReactor(SelectableChannel channel) {
            try {
                selector = Selector.open();
                channel.register(selector, SelectionKey.OP_ACCEPT);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            subReactorThreadGroup = new SubReactorThreadGroup(4);
        }

        public void run() {

            System.out.println("MainReactor is running");
            // TODO Auto-generated method stub
            while (!Thread.interrupted()) {

                Set<SelectionKey> ops = null;
                try {
                    selector.select(1000);
                    ops = selector.selectedKeys();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                // 处理相关事件
                for (Iterator<SelectionKey> it = ops.iterator(); it.hasNext(); ) {
                    SelectionKey key = it.next();
                    it.remove();
                    try {
                        if (key.isAcceptable()) { // 客户端建立连接
                            System.out.println("收到客户端的连接请求。。。");
                            ServerSocketChannel serverSc = (ServerSocketChannel) key.channel();// 这里其实，可以直接使用ssl这个变量
                            //通过 ServerSocketChannel.accept() 方法监听新进来的连接。
                            //当 accept()方法返回的时候,它返回一个包含新进来的连接的 SocketChannel。
                            //因此, accept()方法会一直阻塞到有新连接到达。
                            SocketChannel clientChannel = serverSc.accept();
                            clientChannel.configureBlocking(false);
                            subReactorThreadGroup.dispatch(clientChannel); // 转发该请求
                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
                        System.out.println("客户端主动断开连接。。。。。。。");
                    }

                }


            }

        }
    }

    /**
     * nio 线程组;简易的NIO线程组
     * @author dingwei2
     *
     */
    public static class SubReactorThreadGroup {

        private static final AtomicInteger requestCounter = new AtomicInteger();  //请求计数器

        private final int nioThreadCount;  // 线程池IO线程的数量
        private static final int DEFAULT_NIO_THREAD_COUNT;
        private SubReactorThread[] nioThreads;
        private ExecutorService businessExecutePool; //业务线程池

        static {
//      DEFAULT_NIO_THREAD_COUNT = Runtime.getRuntime().availableProcessors() > 1
//              ? 2 * (Runtime.getRuntime().availableProcessors() - 1 ) : 2;

            DEFAULT_NIO_THREAD_COUNT = 4;
        }

        public SubReactorThreadGroup() {
            this(DEFAULT_NIO_THREAD_COUNT);
        }

        public SubReactorThreadGroup(int threadCount) {

            if(threadCount < 1) {
                threadCount = DEFAULT_NIO_THREAD_COUNT;
            }

            businessExecutePool = Executors.newFixedThreadPool(threadCount);

            this.nioThreadCount = threadCount;
            this.nioThreads = new SubReactorThread[threadCount];
            for(int i = 0; i < threadCount; i ++ ) {
                this.nioThreads[i] = new SubReactorThread(businessExecutePool);
                this.nioThreads[i].start(); //构造方法中启动线程，由于nioThreads不会对外暴露，故不会引起线程逃逸
            }

            System.out.println("Nio 线程数量：" + threadCount);
        }

        public void dispatch(SocketChannel socketChannel) {
            if(socketChannel != null ) {
                next().register(new NioTask(socketChannel, SelectionKey.OP_READ));
            }
        }

        protected SubReactorThread next() {
            return this.nioThreads[ requestCounter.getAndIncrement() %  nioThreadCount ];
        }


    }

    /**
     * Nio 线程，专门负责nio read,write
     * 本类是实例行代码，不会对nio,断线重连，写半包等场景进行处理,旨在理解 Reactor模型（多线程版本）
     * @author dingwei2
     *
     */
    public static class SubReactorThread extends Thread {

        private Selector selector;
        private ExecutorService businessExecutorPool;
        private List<NioTask> taskList = new ArrayList<NioTask>(512);
        private ReentrantLock taskMainLock = new ReentrantLock();

        /**
         * 业务线程池
         * @param businessExecutorPool
         */
        public SubReactorThread(ExecutorService businessExecutorPool) {
            try {
                this.businessExecutorPool = businessExecutorPool;
                this.selector = Selector.open();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        /**
         * socket channel
         *
         */
        public void register(NioTask task) {
            if (task != null) {
                try {
                    taskMainLock.lock();
                    taskList.add(task);
                } finally {
                    taskMainLock.unlock();
                }
            }
        }

        // private

        public void run() {
            while (!Thread.interrupted()) {
                Set<SelectionKey> ops = null;
                try {
                    selector.select(1000);
                    ops = selector.selectedKeys();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    continue;
                }

                // 处理相关事件
                for (Iterator<SelectionKey> it = ops.iterator(); it.hasNext();) {
                    SelectionKey key = it.next();
                    it.remove();

                    try {
                        if (key.isWritable()) { // 向客户端发送请求
                            SocketChannel clientChannel = (SocketChannel) key
                                    .channel();
                            ByteBuffer buf = (ByteBuffer) key.attachment();
                            buf.flip();
                            clientChannel.write(buf);
                            System.out.println("服务端向客户端发送数据。。。");
                            // 重新注册读事件
                            clientChannel.register(selector, SelectionKey.OP_READ);
                        } else if (key.isReadable()) { // 接受客户端请求
                            System.out.println("服务端接收客户端连接请求。。。");
                            SocketChannel clientChannel = (SocketChannel) key
                                    .channel();
                            ByteBuffer buf = ByteBuffer.allocate(1024);
                            System.out.println(buf.capacity());
                            clientChannel.read(buf);//解析请求完毕

                            //转发请求到具体的业务线程；当然，这里其实可以向dubbo那样，支持转发策略，如果执行时间短，
                            //，比如没有数据库操作等，可以在io线程中执行。本实例，转发到业务线程池
                            dispatch(clientChannel, buf);

                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
                        System.out.println("客户端主动断开连接。。。。。。。");
                    }

                }

                // 注册事件
                if (!taskList.isEmpty()) {
                    try {
                        taskMainLock.lock();
                        for (Iterator<NioTask> it = taskList
                                .iterator(); it.hasNext();) {
                            NioTask task = it.next();
                            try {
                                SocketChannel sc = task.getSc();
                                if(task.getData() != null) {
                                    sc.register(selector, task.getOp(), task.getData());
                                } else {
                                    sc.register(selector, task.getOp());
                                }

                            } catch (Throwable e) {
                                e.printStackTrace();// ignore
                            }
                            it.remove();
                        }

                    } finally {
                        taskMainLock.unlock();
                    }
                }

            }
        }

        /**
         * 此处的reqBuffer处于可写状态
         * @param sc
         * @param reqBuffer
         */
        private void dispatch(SocketChannel sc, ByteBuffer reqBuffer) {
            businessExecutorPool.submit( new Handler(sc, reqBuffer, this)  );
        }
    }

    /**
     * Nio task
     * @author Administrator
     *
     */
    public static class NioTask implements Serializable {

        private SocketChannel sc;
        private int op;
        private Object data;

        public NioTask(SocketChannel sc, int op) {
            this.sc = sc;
            this.op = op;
        }

        public NioTask(SocketChannel sc, int op, Object data) {
            this(sc, op);
            this.data = data;
        }
        public SocketChannel getSc() {
            return sc;
        }
        public void setSc(SocketChannel sc) {
            this.sc = sc;
        }
        public int getOp() {
            return op;
        }
        public void setOp(int op) {
            this.op = op;
        }
        public Object getData() {
            return data;
        }
        public void setData(Object data) {
            this.data = data;
        }



    }

    /**
     * 业务线程
     * 该handler的功能就是在收到的请求信息，后面加上 hello,服务器收到了你的信息，然后返回给客户端
     * @author Administrator
     *
     */
    public static class Handler implements Runnable {

        private static final byte[] b = "hello,服务器收到了你的信息。".getBytes(); // 服务端给客户端的响应

        private SocketChannel sc;
        private ByteBuffer reqBuffer;
        private SubReactorThread parent;

        public Handler(SocketChannel sc, ByteBuffer reqBuffer,
                       SubReactorThread parent) {
            super();
            this.sc = sc;
            this.reqBuffer = reqBuffer;
            this.parent = parent;
        }

        public void run() {
            System.out.println("业务在handler中开始执行。。。");
            // TODO Auto-generated method stub
            //业务处理
            reqBuffer.put(b);
            parent.register(new NioTask(sc, SelectionKey.OP_WRITE, reqBuffer));
            System.out.println("业务在handler中执行结束。。。");
        }

    }

}