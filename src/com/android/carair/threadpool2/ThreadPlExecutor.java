package com.android.carair.threadpool2;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadPlExecutor extends ThreadPoolExecutor {

	
	

	private ThreadPoolListener listener;

	public ThreadPlExecutor(int corePoolSize, int maximumPoolSize,
			long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
		// TODO Auto-generated constructor stub
	}

	public void setEventListener(ThreadPoolListener listener){
		this.listener = listener;
	}
	
	@Override
	protected void terminated() {
		// TODO Auto-generated method stub
		if(listener != null)
			listener.onTPShutDown();
		super.terminated();
	}

}
