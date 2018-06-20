package com.phei.netty.mynetty;

import com.phei.netty.mynetty.pool.NioSelectorRunnablePool;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
/**
 * 启动函数
 * @author -琴兽-
 *
 */
public class StartServer {

	public static void main(String[] args) {
		int port = 8082;

		//初始化线程
		NioSelectorRunnablePool nioSelectorRunnablePool = new NioSelectorRunnablePool(Executors.newFixedThreadPool(3), Executors.newCachedThreadPool());
		
		//获取服务类
		ServerBootstrap bootstrap = new ServerBootstrap(nioSelectorRunnablePool);
		
		//绑定端口
		bootstrap.bind(new InetSocketAddress(port));

		//bootstrap.bind(new InetSocketAddress(8083));
		//bootstrap.bind(new InetSocketAddress(8084));
		
		System.out.println("The Server Listen in "+port);
	}

}
