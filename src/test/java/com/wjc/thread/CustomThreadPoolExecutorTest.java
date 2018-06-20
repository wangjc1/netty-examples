package com.wjc.thread;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class CustomThreadPoolExecutorTest {
  
      
    private Executor pool = null;
      
      
    /**
     * netty 线程工厂
     */
    static class DefaultThreadFactory implements ThreadFactory {
        private static final AtomicInteger poolNumber = new AtomicInteger(1);
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        DefaultThreadFactory() {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() :
                    Thread.currentThread().getThreadGroup();
            namePrefix = "pool-" +
                    poolNumber.getAndIncrement() +
                    "-thread-";
        }

        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r,
                    namePrefix + threadNumber.getAndIncrement(),
                    0);
            if (t.isDaemon())
                t.setDaemon(false);
            if (t.getPriority() != Thread.NORM_PRIORITY)
                t.setPriority(Thread.NORM_PRIORITY);
            return t;
        }
    }


    /**
     * netty 线程池
     */
    static class ThreadPerTaskExecutor implements Executor {
        private final ThreadFactory threadFactory;

        public ThreadPerTaskExecutor(ThreadFactory threadFactory) {
            if (threadFactory == null) {
                throw new NullPointerException("threadFactory");
            }
            this.threadFactory = threadFactory;
        }

        @Override
        public void execute(Runnable command) {
            threadFactory.newThread(command).start();
        }
    }



    Executor getExecutorPool(){
        Executor pool = //new ThreadPerTaskExecutor(new DefaultThreadFactory());

        /**
         * JDK线程池初始化方法
         *
         * corePoolSize 核心线程池大小----10
         * maximumPoolSize 最大线程池大小----30
         * keepAliveTime 线程池中超过corePoolSize数目的空闲线程最大存活时间----30+单位TimeUnit
         * TimeUnit keepAliveTime时间单位----TimeUnit.MINUTES
         * workQueue 阻塞队列----new ArrayBlockingQueue<Runnable>(10)====10容量的阻塞队列
         * threadFactory 新建线程工厂----new CustomThreadFactory()====定制的线程工厂
         * rejectedExecutionHandler 当提交任务数超过maxmumPoolSize+workQueue之和时,
         *                          即当提交第41个任务时(前面线程都没有执行完,此测试方法中用sleep(100)),
         *                                任务会交给RejectedExecutionHandler来处理
         */
        new ThreadPoolExecutor(
                3,
                10,
                0L,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(10),
                new DefaultThreadFactory(),
                new MyRejectedExecutionHandler()
        );

        /*new ThreadPoolExecutor(
                3,
                10,
                0,
                TimeUnit.MILLISECONDS,
                new SynchronousQueue<Runnable>(),
                new DefaultThreadFactory(),
                new MyRejectedExecutionHandler()
        );*/

        return pool;
    }


    private static class MyRejectedExecutionHandler implements RejectedExecutionHandler {
        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {  
            // 记录异常  
            // 报警处理等  
            System.err.println("线程池消耗殆尽");
        }
    }  
      

    // 测试构造的线程池  
    public static void main(String[] args) throws InterruptedException {
        CustomThreadPoolExecutorTest exec = new CustomThreadPoolExecutorTest();
        Executor pool = exec.getExecutorPool();

        for(int i=1; i<100; i++) {
            System.out.println("提交第" + i + "个任务!");
            //TimeUnit.MILLISECONDS.sleep(100);
            pool.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println(Thread.currentThread().getName() + "  running=====");
                }
            });
        }
          
    }
}  