package com.android.carair.threadpool2;

import java.util.concurrent.PriorityBlockingQueue;

import com.android.carair.utils.Priority;

public class ThreadPage extends TaskHolder implements Priority{
	
	private int priority;
	private PriorityBlockingQueue<AsyncTask> priorityQueue;
	private boolean isAutoDestory=false;
	private boolean isPause=false;
	private int simulTaskNum=1;
	private int currentTaskNum=0;
	private boolean locked;
	private ThreadPool threadPool;
	
	public ThreadPage(int priority){
//		locked = false;
//		this.priority = priority;
//		priorityQueue = new PriorityBlockingQueue<AsyncTask>(200,new PriorityComparator());
//		ThreadPool.getInstance().regTaskHolder(this);
		this(priority,ThreadPool.getInstance());
	}
	
	public ThreadPage(int priority,ThreadPool threadPool){
		locked = false;
		this.priority = priority;
		priorityQueue = new PriorityBlockingQueue<AsyncTask>(200,new PriorityComparator());
		this.threadPool = threadPool;
		threadPool.regTaskHolder(this);
	}
	/*
	 * 任务放入队列
	 */
	public void execute(Runnable runable, int priority){
       
        if(runable != null && !locked){//如果没有锁定，则可添加任务
        	synchronized(this){
				AsyncTask asyncTask = new AsyncTask(priority,runable,this);
				if(priorityQueue != null && asyncTask != null){
					priorityQueue.add(asyncTask);
					
				}	
        	}
        	if(!threadPool.isRegistered(this)){
        		threadPool = ThreadPool.getInstance();
	        	//如果没有注册，则重新注册
	            threadPool.regTaskHolder(this);
        	}
            //唤醒调度线程
        	threadPool.wakeup();
        }
        
       
	}
	
	
	/*
	 * 销毁ThreadPage
	 * 清除任务
	 * 从ThreadPool中注销
	 */
	public void destroy(){
		
		clearTask();
		threadPool.unregTaskHolder(this);
	}
	
	/*
	 * 任务执行完后自动销毁
	 */
	public void setAutoDestory(boolean flag){
		isAutoDestory = flag;
	}
	/*
	 * 清除任务
	 */
	public synchronized void clearTask(){
		if(priorityQueue!=null){
			priorityQueue.clear();
		}
	}
	
	/*
	 * 暂停任务执行
	 */
	public void pause(){
		isPause = true;
	}
	
	/*
	 * 恢复任务执行
	 */
	public void resume(){
		if(priorityQueue.size()>0)
			threadPool.wakeup();
		isPause = false;		
	}

//    /**
//     * 将任务放入后台运行(降低任务的优先级)
//     * 先清空之前的任务队列,改变优先级别后重新注册
//     */
//    public void toBackground(){
//    	TaoLog.Logd("threadpage","toBackground page size:"+priorityQueue.size());
//        if(this.priority == Priority.PRIORITY_IDLE)
//            return;
//
//        ThreadPool.getInstance().unregTaskHolder(this);
//        this.priority = Priority.PRIORITY_IDLE;
//        ThreadPool.getInstance().regTaskHolder(this);
//    }
//
//    /**
//     * 将任务放入后台运行(提升任务的优先级)
//     *先清空之前的任务队列,改变优先级别后重新注册
//     */
//    public void toFront(){
//    	TaoLog.Logd("threadpage","toFront page size:"+priorityQueue.size());
//        if(this.priority == Priority.PRIORITY_HIGH)
//            return;
//
//        ThreadPool.getInstance().unregTaskHolder(this);
//        this.priority = Priority.PRIORITY_HIGH;
//        ThreadPool.getInstance().regTaskHolder(this);
//    }

	/*
	 * 设置并行任务数
	 */
	public void setSimulTask(int num){
		simulTaskNum = num;
	}
	/*
	 * 获取并行任务数
	 */
	public int getSimulTask(){
		return simulTaskNum;
	}
	
	/*
	 * 获取当前ThreadPage优先级
	 */
	public int getPriority(){
		return priority;
	}
	
	/*
	 * 获取下一个任务
	 * 当无任务或并行任务数满时，则返回空
	 */
	protected synchronized AsyncTask getNextTask(){
//		TaoLog.Logd("threadpage","getNextTask queue size:"+priorityQueue.size()+"currentTaskNum:"
//				+currentTaskNum+"simulTaskNum"+simulTaskNum+"Pause"+isPause);
		if(priorityQueue == null){
			return null;
		}else {
			if(priorityQueue.size()<1){//所有消息已经执行完成
				return null;
			}else if(!isPause && currentTaskNum < simulTaskNum) {//没有暂停，则给出任务					
				return priorityQueue.peek();

			}
			return null;
		}
	}
	
	/*
	 * 任务开始执行
	 */
	protected void taskBegin(AsyncTask task){
	}
	/*
	 * 任务执行结束
	 */
	protected void taskFinsh(AsyncTask task){
		currentTaskNum--;
		if(priorityQueue.size()>0)
			threadPool.wakeup();
	}
	
	
	protected boolean isAutoDestory(){
		return isAutoDestory;
	}
    
    /*
     * 锁定threadpage，拒绝添加任何任务
     */
	protected void locked(){
    	locked = true;
    }
    
	/*
	 * 是否有任务
	 * 但不保证能提供任务，由于并发数的限制
	 */
	protected boolean hasTask(){
		return priorityQueue.size()>0;
//    	if(isPause)
//    		return false;
//    	else
//    		return (priorityQueue.size()>0 && currentTaskNum < simulTaskNum);//有任务并且并发数没超出
    }

	protected void removeTask(AsyncTask task) {
		// TODO Auto-generated method stub
		//不能放在taskBegin，存在互斥问题——引发问题 ：瞬间任务执行数超出限定的并发数  
		currentTaskNum++;
		priorityQueue.remove(task);
	}

}
