package com.wjc.thread;

import org.junit.Test;

import java.util.concurrent.*;

public class WangBa implements Runnable {
  
    private DelayQueue<Wangming> queue = new DelayQueue<Wangming>();
    public volatile boolean yinye =true;
      
    public void shangji(String name,String id,int money){  
        Wangming man = new Wangming(name,id,1000*60*money+System.currentTimeMillis());        
        System.out.println("网名" + man.getName() + " 身份证" + man.getId() + "交钱" + money + "块,开始上机...");
        this.queue.add(man);
    }
      
    public void xiaji(Wangming man){  
        System.out.println("网名" + man.getName() + " 身份证" + man.getId() + "时间到下机...");
    }  
  
    @Override  
    public void run() {  
        // TODO Auto-generated method stub  
        while(yinye){  
            try {  
                System.out.println("检查ing");  
                Wangming man = queue.take();
                xiaji(man);  
            } catch (InterruptedException e) {  
                // TODO Auto-generated catch block  
                //e.printStackTrace();
                //Thread.interrupted();
                yinye=false;
                System.out.println("run interrupted!");
            }
        }  
    }

    @Test
    public void testThreadPool(){
        ExecutorService executorService = Executors.newFixedThreadPool(5);
        final CountDownLatch latch=new CountDownLatch(5);//两个工人的协作
        for(int i=0;i<5;i++)
        executorService.execute(new Runnable() {
            public void run() {
                System.out.println(Thread.currentThread().getName()+":Asynchronous task");
                latch.countDown();
            }
        });


        try {
            TimeUnit.MILLISECONDS.sleep(0);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        try {
            executorService.awaitTermination(0,TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        executorService.shutdown();

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void testThreadPool2(){
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future future = executorService.submit(new Runnable() {
            public void run() {
                System.out.println("Asynchronous task");
                try {
                    TimeUnit.SECONDS.sleep(20);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        //如果任务结束执行则返回 null
        try {
            System.out.println("future.get()=" + future.get());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        try {
            executorService.awaitTermination(2, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        executorService.shutdown();
    }

    @Test
    public void testThreadPool3(){
        ExecutorService service = Executors.newCachedThreadPool();
        Future<?> task = null;
        try{
            WangBa siyu = new WangBa();
            siyu.shangji("路人甲", "123", 1);

            task = service.submit(siyu);
            service.shutdown();

            //service.execute(siyu);
            task.get(50, TimeUnit.SECONDS);
        }
        catch(Exception ex){
            //Thread.interrupted();
            System.out.println("main interrupted!");
        }finally {
            task.cancel(true);
        }
    }


    @Test
    public void testThreadPool4(){
        final BlockingQueue<Wangming> queue2 = new ArrayBlockingQueue(2);
        final CountDownLatch latch=new CountDownLatch(1);//两个工人的协作
        ExecutorService service = Executors.newCachedThreadPool();
        Future<?> task = null;
        try{
            service.execute(new Runnable() {
                @Override
                public void run() {
                    Wangming man = new Wangming("wjc", "33", 1000 * 60 * 2 + System.currentTimeMillis());
                    // queue2.add(man);
                }
            });

            task = service.submit(new Runnable() {
                @Override
                public void run() {
                    Wangming man = null;
                    try {
                        man = queue2.take();
                        xiaji(man);
                        latch.countDown();
                    } catch (InterruptedException e) {
                        System.out.println("submit interrupted!");
                        latch.countDown();
                    }
                }
            });

            task.get(5, TimeUnit.SECONDS);

            //TimeUnit.MILLISECONDS.sleep(200);
            service.shutdown();
            latch.await();

        }
        catch(Exception e){
            System.out.println("main interrupted!");
        }finally {
            task.cancel(true);
        }
    }

    @Test
    public void testThreadPool5(){
        ExecutorService service = Executors.newCachedThreadPool();
        CountDownLatch latch = new CountDownLatch(1);
        try{

            service.execute(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        System.out.print("| ");
                    }
                }
            });
            service.shutdown();
            //service.awaitTermination(1,TimeUnit.SECONDS);

            latch.await();
        }
        catch(Exception ex){
            System.out.println("main interrupted!");
        }finally {
            latch.countDown();
        }
    }

    @Test
    public void testThreadPool6(){
        ExecutorService service = Executors.newSingleThreadExecutor();
        CountDownLatch latch = new CountDownLatch(1);
        try{
            Future<?> task = service.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        System.out.println("执行任务。。。。");
                        TimeUnit.SECONDS.sleep(5);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });

            task.get(10, TimeUnit.SECONDS);//阻塞
            service.shutdown();
            System.out.println("任务执行完毕。。。。");

            latch.await();
        }
        catch(Exception ex){
            System.out.println("main interrupted!");
        }finally {
            latch.countDown();
        }
    }


    @Test
    public void testThreadPool7(){
        ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
        CountDownLatch latch = new CountDownLatch(1);
        try{
            service.schedule(new Runnable() {
                @Override
                public void run() {
                    try {
                        System.out.println("执行任务。。。。");
                        TimeUnit.SECONDS.sleep(5);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }, 5, TimeUnit.SECONDS);
            //service.shutdown();
            //service = Executors.newSingleThreadScheduledExecutor();
            service.schedule(new Runnable() {
                @Override
                public void run() {
                    try {
                        System.out.println("执行任务2。。。。");
                        TimeUnit.SECONDS.sleep(5);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }, 5, TimeUnit.SECONDS);

            latch.await();
        }
        catch(Exception ex){
            ex.printStackTrace();
        }finally {
            latch.countDown();
        }
    }

    public static void main2(String args[]){
        try{  
            System.out.println("网吧开始营业");  
            WangBa siyu = new WangBa();  
            Thread shangwang = new Thread(siyu);  
            shangwang.start();  
              
            siyu.shangji("路人甲", "123", 1);  
            siyu.shangji("路人乙", "234", 2);  
            siyu.shangji("路人丙", "345", 100);
        }  
        catch(Exception ex){  
              
        }  
    }


    private static ExecutorService service = Executors.newCachedThreadPool();

    public static void timedRun(Runnable r, long timeout, TimeUnit unit) {

    }
}

class Wangming implements Delayed {

    private String name;
    //身份证
    private String id;
    //截止时间
    private long endTime;

    public Wangming(String name,String id,long endTime){
        this.name=name;
        this.id=id;
        this.endTime=endTime;
    }

    public String getName(){
        return this.name;
    }

    public String getId(){
        return this.id;
    }

    /**
     * 用来判断是否到了截止时间
     */
    @Override
    public long getDelay(TimeUnit unit) {
        // TODO Auto-generated method stub
        return endTime-System.currentTimeMillis();
    }

    /**
     * 相互批较排序用
     */
    @Override
    public int compareTo(Delayed o) {
        // TODO Auto-generated method stub
        Wangming jia = (Wangming)o;
        return endTime-jia.endTime>0?1:0;
    }

}