package com.android.carair.threadpool2;



/*
 * 请重写该类里的所有方法
 */
public abstract class TaskHolder {
	
	//获取下一个任务
	protected AsyncTask getNextTask(){
		return null;
	}	
	
	//移除一个任务
	protected void removeTask(AsyncTask task){
		
	}
	//是否存在未执行完的任务
	protected boolean hasTask(){
		return false;
	}
	
	//是否需要自动销毁
	protected boolean isAutoDestory(){
		return true;
	}
	
	//获取优先级
	public abstract int getPriority();				
	
	//锁定任务队列，不能再执行任务
	protected void locked(){
		
	}
	
	//任务开始执行
	protected void taskBegin(AsyncTask task){
		
	}
	
	//任务执行结束
	protected void taskFinsh(AsyncTask task){
		
	}
}
