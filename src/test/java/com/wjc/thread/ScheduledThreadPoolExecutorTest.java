package com.wjc.thread;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 schedule方法被用来延迟指定时间来执行某个指定任务。如果你需要周期性重复执行定时任务可以使用scheduleAtFixedRate或者scheduleWithFixedDelay方法，它们不同的是前者以固定频率执行，后者以相对固定频率执行。
 不管任务执行耗时是否大于间隔时间，scheduleAtFixedRate和scheduleWithFixedDelay都不会导致同一个任务并发地被执行。唯一不同的是scheduleWithFixedDelay是当前一个任务结束的时刻，开始结算间隔时间，如0秒开始执行第一次任务，任务耗时5秒，任务间隔时间3秒，那么第二次任务执行的时间是在第8秒开始。
 ScheduledExecutorService的实现类，是ScheduledThreadPoolExecutor。ScheduledThreadPoolExecutor对象包含的线程数量是没有可伸缩性的，只会有固定数量的线程。不过你可以通过其构造函数来设定线程的优先级，来降低定时任务线程的系统占用。
 特别提示：通过ScheduledExecutorService执行的周期任务，如果任务执行过程中抛出了异常，那么过ScheduledExecutorService就会停止执行任务，且也不会再周期地执行该任务了。所以你如果想保住任务都一直被周期执行，那么catch一切可能的异常。

 scheduleWithFixedDelay: 从字面意义上可以理解为就是以固定延迟（时间）来执行线程任务，它实际上是不管线程任务的执行时间的，每次都要把任务执行完成后再延迟固定时间后再执行下一次。
 scheduleFixedRate: 是以固定频率来执行线程任务，固定频率的含义就是可能设定的固定时间不足以完成线程任务，但是它不管，达到设定的延迟时间了就要执行下一次了。

*/
public class ScheduledThreadPoolExecutorTest {
    private static SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static void main(String[] args) {
        //ScheduledExecutorService exec=Executors.newScheduledThreadPool(1);
        ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(1);
        /**
         *每隔一段时间打印系统时间，互不影响的<br/>
         * 创建并执行一个在给定初始延迟后首次启用的定期操作，后续操作具有给定的周期；<br/>
         * 也就是将在 initialDelay 后开始执行，然后在initialDelay+period 后执行，<br/>
         * 接着在 initialDelay + 2 * period 后执行，依此类推。
         */
        exec.scheduleAtFixedRate(new Runnable() {
            public void run() {
                System.out.println(format.format(new Date()));
            }
        }, 1000, 5000, TimeUnit.MILLISECONDS);

        //开始执行后就触发异常,next周期将不会运行
        exec.scheduleAtFixedRate(new Runnable() {
            public void run() {
                System.out.println("RuntimeException no catch,next time can't run");
                throw new RuntimeException();
            }
        }, 1000, 5000, TimeUnit.MILLISECONDS);

        //虽然抛出了运行异常,当被拦截了,next周期继续运行
        exec.scheduleAtFixedRate(new Runnable() {
            public void run() {
                try{
                    throw new RuntimeException();
                }catch (Exception e){
                    System.out.println("RuntimeException catched,can run next");
                }
            }
        }, 1000, 5000, TimeUnit.MILLISECONDS);

        /**
         * 创建并执行一个在给定初始延迟后首次启用的定期操作，<br/>
         * 随后，在每一次执行终止和下一次执行开始之间都存在给定的延迟。
         */
        exec.scheduleWithFixedDelay(new Runnable() {
            public void run() {
                System.out.println("scheduleWithFixedDelay:begin,"+format.format(new Date()));
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("scheduleWithFixedDelay:end,"+format.format(new Date()));
            }
        },1000,5000,TimeUnit.MILLISECONDS);

        /**
         * 创建并执行在给定延迟后启用的一次性操作。
         */
        exec.schedule(new Runnable() {
            public void run() {
                System.out.println("The thread can only run once!");
            }
        },5000,TimeUnit.MILLISECONDS);
    }
}