package com.android.carair.threadpool2;

import java.util.Stack;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import com.android.carair.utils.Priority;

public class ThreadPool implements Runnable,ThreadPoolListener{

	private static final int THREADPOOL_RUNNING = 0;
	private static final int THREADPOOL_DESTORYING = THREADPOOL_RUNNING+1;
	private static final int THREADPOOL_DESTORYED = THREADPOOL_DESTORYING+1;
	
	private static final int  LOW_COREPOOLSIZE = 1;		//低优先级
	private static final int LOW_MAXPOOLSIZE = 3;
	private static final int HIGH_COREPOOLSIZE = 5;
	private static final int HIGH_MAXPOOLSIZE = 30;
	private int QUEUEPOOLSIZE = 1;
	
	
	private static ThreadPool single_TP = null;
	
	
	private  ThreadPlExecutor lowPThreadpool;
	private  ThreadPlExecutor highPThreadpool;
	
	private Stack<TaskHolder> highTaskHolderStack;
	private Stack<TaskHolder> lowTaskHolderStack;
	
	
	private ThreadPoolListener listener;
	private boolean running;
	private int state;
	
	//标记有任务进入
	//当已遍历过该threadholder，但该threadholder有任务进入。则notify将失效
	//此标志位将通知调度线程不能wait
	private boolean needWorking;	
	
	private Thread threadPoolMgr;

	protected ThreadPool(){
		this(LOW_COREPOOLSIZE,LOW_MAXPOOLSIZE,HIGH_COREPOOLSIZE,HIGH_MAXPOOLSIZE);
	}
	
	protected ThreadPool(int low_coreSize,int low_maxSize,int high_coreSize,int high_maxSize){

		//低优先级线程池
		lowPThreadpool = new ThreadPlExecutor(low_coreSize,low_maxSize,20*1000,TimeUnit.MICROSECONDS,new ArrayBlockingQueue<Runnable>(QUEUEPOOLSIZE));
		
		//高优先级线程池
		highPThreadpool = new ThreadPlExecutor(high_coreSize,high_maxSize,20*1000,TimeUnit.MICROSECONDS,new ArrayBlockingQueue<Runnable>(QUEUEPOOLSIZE));

		highTaskHolderStack = new Stack<TaskHolder>();
		lowTaskHolderStack = new Stack<TaskHolder>();

		//设置listener
		if(highPThreadpool!=null)
			highPThreadpool.setEventListener(this);
		if(lowPThreadpool!=null)
			lowPThreadpool.setEventListener(this);
		
		//启动调度线程
		threadPoolMgr = new Thread(this, "TBThreadPoolMgr");
		threadPoolMgr.setDaemon(true);
		threadPoolMgr.start();
		state = THREADPOOL_RUNNING;
		running = true;
		needWorking = true;
		
	}

	/*
	 * 获取线程池状态
	 * 	THREADPOOL_RUNNING 			正在运行
	 *	THREADPOOL_DESTORYING 		正在销毁
	 *	THREADPOOL_DESTORYED 		已销毁
	 */
	public int getState(){
		return state;
	}
	
	public static synchronized ThreadPool getInstance(){//单实例  线程安全
		if(single_TP == null)
			single_TP = new ThreadPool();
		return single_TP;
	}
	/*
	 * 注册TaskHolder，加入TaskHolder队列
	 */
	public void regTaskHolder(TaskHolder taskHolder){
		synchronized(highTaskHolderStack){
			if(taskHolder == null || state == THREADPOOL_DESTORYING || state == THREADPOOL_DESTORYED)
				return;
			
			
			switch(taskHolder.getPriority()){
				case Priority.PRIORITY_REALTIME://高优先级放入高优先级page队列
				case Priority.PRIORITY_HIGH:
					
					if(highTaskHolderStack!=null && !highTaskHolderStack.contains(taskHolder)){
							highTaskHolderStack.push(taskHolder);
					
					}
					break;
				case Priority.PRIORITY_NORM://低优先级放入低优先级page队列
				case Priority.PRIORITY_IDLE:
					
					if(lowTaskHolderStack!=null && !lowTaskHolderStack.contains(taskHolder)){
						lowTaskHolderStack.push(taskHolder);
					
					}
					break;
			}
		}
	}
	
	public boolean isRegistered(TaskHolder taskHolder){
		synchronized(highTaskHolderStack){
			if(taskHolder == null || state == THREADPOOL_DESTORYING || state == THREADPOOL_DESTORYED)
				return false;
			
			
			switch(taskHolder.getPriority()){
				case Priority.PRIORITY_REALTIME://高优先级放入高优先级page队列
				case Priority.PRIORITY_HIGH:
					
					if(highTaskHolderStack!=null){
						return highTaskHolderStack.contains(taskHolder);
					}else
						return false;
				case Priority.PRIORITY_NORM://低优先级放入低优先级page队列
				case Priority.PRIORITY_IDLE:
					
					if(lowTaskHolderStack!=null){
						return lowTaskHolderStack.contains(taskHolder);
					}else
						return false;
			}
			return false;
		}
	}
	/*
	 * 注销TaskHolder，移除TaskHolder队列
	 */
	public void unregTaskHolder(TaskHolder taskHolder){
		synchronized(highTaskHolderStack){
			if(taskHolder == null)
				return;
			switch(taskHolder.getPriority()){
				case Priority.PRIORITY_REALTIME:
				case Priority.PRIORITY_HIGH:
					if(highTaskHolderStack!=null){
						highTaskHolderStack.remove(taskHolder);
					
					}
					
					break;
				case Priority.PRIORITY_NORM:
				case Priority.PRIORITY_IDLE:
					if(lowTaskHolderStack!=null){
						lowTaskHolderStack.remove(taskHolder);
					
					}
				break;
			}
		}
	}
//	public void remove(AsyncTask task){
//		if(task==null)return;
//		if(highPThreadpool!=null)highPThreadpool.remove(task);
//		if(lowPThreadpool!=null)lowPThreadpool.remove(task);
//	}
	public void setEventListener(ThreadPoolListener listener){
		this.listener = listener;
		
	}
	
	/*
	 * 销毁线程池
	 * 锁住已有的TaskHolder  不能添加新任务
	 * 等待所有任务执行完成后关闭线程池
	 */
	public static void destroy(){
		if(single_TP != null){
			single_TP.state = THREADPOOL_DESTORYING;
			single_TP.wakeup();
			synchronized(single_TP.highTaskHolderStack){//与注册/注销TaskHolder互斥
				int size = single_TP.lowTaskHolderStack.size();
				for(int i=0;i<size;i++){//锁住所有TaskHolder
					TaskHolder taskHolder = single_TP.lowTaskHolderStack.elementAt(size-i-1);
					taskHolder.locked();
					
				}
				
				size = single_TP.highTaskHolderStack.size();
				for(int i=0;i<size;i++){//锁住所有TaskHolder
					TaskHolder taskHolder = single_TP.highTaskHolderStack.elementAt(size-i-1);
					taskHolder.locked();
					
				}
			}
		}
		
	}
	
	/*
	 * 立即结束线程池
	 * 清除未处理任务
	 * 关闭线程池
	 */
	public static void destoryNow(){
		if(single_TP != null){
			single_TP.state = THREADPOOL_DESTORYED;
			synchronized(single_TP.highTaskHolderStack){
				single_TP.lowTaskHolderStack.clear();
				single_TP.highTaskHolderStack.clear();
			}
			
			single_TP.running = false;
			single_TP.wakeup();
			single_TP.highPThreadpool.shutdownNow();
			single_TP.lowPThreadpool.shutdownNow();
			single_TP = null;
		}
	}

	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		while(running){
			
			try {
				boolean hasTask = false;
				synchronized(highTaskHolderStack){//与注册/注销TaskHolder互斥
					
					hasTask = excuteTask(highPThreadpool,highTaskHolderStack);
					hasTask = hasTask || excuteTask(lowPThreadpool,lowTaskHolderStack);
				}
				
				if(!hasTask){//无任务可执行
					if(state == THREADPOOL_DESTORYING){//已经进入关闭状态
						//关闭线程池
						highPThreadpool.shutdown();
						lowPThreadpool.shutdown();
						running = false;
						single_TP = null;
						state = THREADPOOL_DESTORYED;
						
						//TaoLog.Logi("Threadpool", "Threadpool_destroyed_at_mgr");
						
					}else{
						//TaoLog.Logi("Threadpool", "sleep");
						if(needWorking){//已有新任务进入
							Thread.sleep(1);
							
						}
						synchronized(threadPoolMgr){
							if(!needWorking){//无任务可执行 则wait
								threadPoolMgr.wait();
								
							}							
						}
						
						
						needWorking = false;
					}
				}else{
					//由于线程池会出现并发任务高峰，所以不能完全调度完任务再wait。
					//尤其在初始状态，需要创建线程阶段。
					needWorking = false;
					Thread.sleep(1);//执行一个任务挂起调度线程，以防止过度抢占CPU时间片
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	/*
	 * 返回值，是否有任务需要执行
	 */
	private boolean excuteTask(ThreadPlExecutor executor,Stack<TaskHolder> taskPool){//执行任务
		boolean hasTask = false;
		if(taskPool != null){
			//TaoLog.Logd("threadpool", "total runned task:"+executor.getCompletedTaskCount()+"running task:"+executor.getActiveCount()+"max task capacity:"+executor.getMaximumPoolSize());

			AsyncTask asyncTask = null;
			
			int size = taskPool.size();
			for(int i=0;i<size;i++){//遍历任务集
				
				TaskHolder taskHolder = taskPool.elementAt(size-i-1);
				
				if(taskHolder.hasTask()){
					
					asyncTask = taskHolder.getNextTask();
					if(asyncTask != null){//获取到任务
						
						hasTask = true;	//有任务执行
						try{
							if(executor != null && executor.getActiveCount() < executor.getMaximumPoolSize()){
								executor.execute(asyncTask);
								taskHolder.removeTask(asyncTask);
	
								if(executor.getActiveCount() >= executor.getCorePoolSize()){//线程数到达核心线程时，让其立即执行
									Runnable task = new EmptyTask();
									
									executor.execute(task);
									executor.remove(task);
									
								}
							}
						}catch(RejectedExecutionException e){
							e.printStackTrace();
						}
						break;//有任务，但不管执行与否，都将退出
					}
				}else if(taskHolder.isAutoDestory()){
					taskPool.remove(taskHolder);					
				}
			}
			
			
				
		}
		
		return hasTask;
		
		
	}
	class EmptyTask implements Runnable{

		@Override
		public void run() {
			// TODO Auto-generated method stub
			
		}
		
	}


	@Override
	public void onTPShutDown() {
		// TODO Auto-generated method stub
		if(lowPThreadpool.isTerminated() && highPThreadpool.isTerminated() && listener != null)
			listener.onTPShutDown();
	}
	
	public void wakeup(){
		synchronized(threadPoolMgr){
			//标记有任务进入
			//当已遍历过该threadholder，但该threadholder有任务进入。则notify将失效
			//此标志位将通知调度线程不能wait
			needWorking = true;
			threadPoolMgr.notify();
		}
		synchronized(this){
			
			//the thread pool runnable may be exit by vm abort 
			if(!threadPoolMgr.isAlive() && running){
				threadPoolMgr = new Thread(this, "TBThreadPoolMgr_back");
				threadPoolMgr.setDaemon(true);
				threadPoolMgr.start();
				state = THREADPOOL_RUNNING;
				running = true;
			}
		}
	}
}
