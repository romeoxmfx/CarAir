package com.android.carair.threadpool2;

import com.android.carair.utils.Priority;


public class AsyncTask implements Runnable,Priority {
	private Runnable executor;
	private TaskHolder owner;
	private int priority;
	
	public AsyncTask(int priority,Runnable executor, TaskHolder owner){
		this.priority = priority;
		this.executor = executor;
		this.owner = owner;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		if(owner != null)
			owner.taskBegin(this);
		if(executor != null)
			executor.run();
		if(owner != null)
			owner.taskFinsh(this);
	}
	
	public int getPriority(){
		return priority;
	}
	
}
