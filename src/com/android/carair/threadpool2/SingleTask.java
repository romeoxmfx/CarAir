package com.android.carair.threadpool2;

import com.android.carair.utils.Priority;



/*
 * 单任务管理器
 * 当任务执行完后，自动销毁
 * 目的：任务被动调度的模式，有线程资源则执行
 */
public class SingleTask extends TaskHolder{

	private AsyncTask task;
	private int priority;

	public SingleTask(Runnable runable,int priority){
		this.priority = priority;
		if(runable != null){
			task = new AsyncTask(Priority.PRIORITY_IDLE,runable,this);
		}
	}
	
	/*
	 * 执行任务
	 * 将该TaskHolder加入线程池调度队列
	 */
	public void start(){
		ThreadPool.getInstance().regTaskHolder(this);
		ThreadPool.getInstance().wakeup();
	}
	
	@Override
	protected synchronized AsyncTask getNextTask() {
		// TODO Auto-generated method stub
		return task;
	}

	@Override
	protected void removeTask(AsyncTask task) {
		// TODO Auto-generated method stub
		this.task = null;
	}
	@Override
	protected boolean hasTask() {
		// TODO Auto-generated method stub
		return task != null;
	}

	@Override
	protected boolean isAutoDestory() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public int getPriority() {
		// TODO Auto-generated method stub
		return priority;
	}

	@Override
	protected void taskBegin(AsyncTask task) {
		// TODO Auto-generated method stub
	}

	@Override
	protected void taskFinsh(AsyncTask task) {
		// TODO Auto-generated method stub
	}

	

}
