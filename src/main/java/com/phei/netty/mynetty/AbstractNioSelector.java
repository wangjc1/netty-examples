package com.phei.netty.mynetty;

import com.phei.netty.mynetty.pool.NioSelectorRunnablePool;

import java.io.IOException;
import java.nio.channels.Selector;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * 抽象selector线程类：服务线程和消费线程公共功能抽象
 * 
 * @author -琴兽-
 * 
 */
public abstract class AbstractNioSelector implements Runnable {

	/**
	 * 线程池
	 */
	private final Executor executor;

	/**
	 * 选择器
	 */
	protected Selector selector;

	/**
	 * 选择器wakenUp状态标记
	 */
	protected final AtomicBoolean wakenUp = new AtomicBoolean();

	/**
	 * 任务队列
	 */
	private final Queue<Runnable> taskQueue = new ConcurrentLinkedQueue<Runnable>();

	/**
	 * 线程名称
	 */
	private String threadName;
	
	/**
	 * 线程管理对象
	 */
	protected NioSelectorRunnablePool selectorRunnablePool;

	AbstractNioSelector(Executor executor, String threadName, NioSelectorRunnablePool selectorRunnablePool) {
		this.executor = executor;
		this.threadName = threadName;
		this.selectorRunnablePool = selectorRunnablePool;
		openSelector();
	}

	/**
	 * 获取selector并启动线程
	 */
	private void openSelector() {
		try {
			this.selector = Selector.open();
		} catch (IOException e) {
			throw new RuntimeException("Failed to create a selector.");
		}
		//启动线程，执行实现Runnable接口的run方法
		executor.execute(this);
	}

	@Override
	public void run() {
		
		Thread.currentThread().setName(this.threadName);
		System.out.println(Thread.currentThread().getName());

		//一直轮询处理请求，netty里面会做优化，不会一直轮询
		while (true) {
			System.out.println("==");
			try {
				wakenUp.set(false);

				select(selector);

				processTaskQueue();

				process(selector);
			} catch (Exception e) {
				// ignore
			}

		}

	}

	/**
	 * 注册一个任务并激活selector
	 * 
	 * @param task
	 */
	protected final void registerTask(Runnable task) {
		taskQueue.add(task);

		Selector selector = this.selector;

		if (selector != null) {
			//有任务了，唤醒selector
			if (wakenUp.compareAndSet(false, true)) {
				/**
				 某个线程调用select()方法后阻塞了，即使没有通道已经就绪，也有办法让其从select()方法返回。
				 只要让其它线程在第一个线程调用select()方法的那个对象上调用Selector.wakeup()方法即可。
				 阻塞在select()方法上的线程会立马返回。如果有其它线程调用了wakeup()方法，但当前没有线程
				 阻塞在select()方法上，下个调用select()方法的线程会立即“醒来（wake up）”。

				 如果是在一个线程中，那么我们不用关心，以最后的一个register为准，后续请求select能够被激活。
				 但是如果是在多线程下，如果不显式进行wakeup。那么就麻烦了，主线程中的selelct是不会被激活的，因此
				 这里必须要在子线程中wakeup激活一下主线程的select。让nio重新计算interest(事件集合)

				 这四种事件用SelectionKey的四个常量来表示：
				 SelectionKey.OP_CONNECT
				 SelectionKey.OP_ACCEPT
				 SelectionKey.OP_READ
				 SelectionKey.OP_WRITE
				 如果你对不止一种事件感兴趣，那么可以用“位或”操作符将常量连接起来，如下：
				 int interestSet = SelectionKey.OP_READ | SelectionKey.OP_WRITE;
				 */
				selector.wakeup();
			}
		} else {
			taskQueue.remove(task);
		}
	}

	/**
	 * 执行队列里的任务
	 */
	private void processTaskQueue() {
		for (;;) {
			final Runnable task = taskQueue.poll();
			if (task == null) {
				break;
			}
			task.run();
		}
	}
	
	/**
	 * 获取线程管理对象
	 * @return
	 */
	public NioSelectorRunnablePool getSelectorRunnablePool() {
		return selectorRunnablePool;
	}

	/**
	 * select抽象方法
	 * 
	 * @param selector
	 * @return
	 * @throws IOException
	 */
	protected abstract int select(Selector selector) throws IOException;

	/**
	 * selector的业务处理
	 * 
	 * @param selector
	 * @throws IOException
	 */
	protected abstract void process(Selector selector) throws IOException;

}
