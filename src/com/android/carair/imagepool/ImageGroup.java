package com.android.carair.imagepool;

import java.util.ArrayList;

import android.os.Handler.Callback;

/**
 * ImageGroup接口
 * 定义ImageGroup与ImagePool间交互的接口,ImagePool中是以ImageGroup为单元进行调度的。
 * */


public interface ImageGroup extends  ImageListener ,Callback  
{	
	//优先级
	//group的优先级常量定义
	public static final int PRIORITY_TOP = 0; //分组需要马上被加载，top优先级只有一个分组，设置top会把原top分组降级为普通分组，放入普通分组们的头部
	public static final int PRIORITY_NORMAL = 1; //在加载线程足够的时候，可以并发加载
	public static final int PRIORITY_DORMANT = 2; //休眠状态不会加载，需要被提升优先级
	

	/**
	 * 当前分组的优先级，被暂停的分组，总是处于沉睡级
	 * @return 优先级
	 */
	int getPriority();
	
	/**
	 * 设置分组优先级，如果分组已经被加入到ImagePool,会调整ImagePool的分组队列
	 * @param pri 优先级
	 */
	// 注：除了构造函数priority的改变只在ImagePool的调度程序中进行
	void setPriority(int pri);
				
	
	/**
	 * 设置活动窗口，队列中处于窗口内的图片才会被加载
	 * @param startPosition 起始位置值, 取值范围为0~队列长度-1
	 * @param size 窗口大小， 取值范围0 ~ 队列长度， 0表示窗口大小为空，不会下载
	 * @return true 表示设置成功； false表示设置窗口的值错误
	 */		
	public  boolean setActiveWindow(int startPosition, int size);
												
		

	/** 获得存储类型
	 * @return 存储类型
	 */
	public int getCachePolicy();
	
	/** 设置Image下载结果的Listener回调
	 * @param listener listener回调
	 */
	public void setImageListener(ImageListener listener);
	

	/** 获得Image下载结果的Listener回调
	 * return listener回调
	 */
	public ImageListener getImageListener();
	
	
	/**
	 * 设置列表，如果队列中有内容，会被删除，正在下载的会被停止
	 * 如果设置进一个空列表，则设置之后，队列为空
	 * @param handlers ImageHandler的列表
	 */
	public  void setHandlerList(ArrayList<ImageHandler> handlers);		
	
	/**
	 * 设置一组图片
	 * @param URIs 设置的URIs
	 */
	abstract public void setURIList( ArrayList<String> URIs);
	
	/**
	 * 追加一组图片到队列末尾
	 * @param URIs 追加的URI对应的ImageHandler
	 */	
	//注：为一淘增加appendList函数
	abstract public void appendList(ArrayList<ImageHandler> URIs);
	
	/**
	 * 追加一组图片到队列末尾
	 * @param URIs 追加的URI对应的URI
	 */	
	//注：为一淘增加appendList函数
	abstract public void appendURIList(ArrayList<String> URIs);	

	
	/**
	 * 清空ImageGroup
	 * 
	 */
	public  void clear();
	
	/**
	 * 这个接口只有永久保存的图片用户需要调用，临时图片会自动被删除
	 * @param URL
	 */
	public void deleteImage(String URL);
	
	//+分组操作	
	//和Activity的生命周期相关
	/**
	 * 把分组加入ImagePool的分组队列中
	 * 并遍历列表的加载窗口，尝试从缓存中获取所需图片
	 * 注：由于start会导致对缓存的预读，可能会多次进行文件io，在调用start之前，请确认设置有效的窗口，否则会默认对整个队列进行缓存预读
	 */
	public  void start();
	
	
	//注意pause需要在Activity的OnStop中被调用，而不是OnPause中，因为在OnPause以后，还有一些Draw事件
	/**
	 * 暂停分组的任务请求，当前正在下载的不被放弃
	 */
	public  void pause();

	
	/**
	 * 把处于暂停的中分组激活，分组将会重新被调度
	 */
	public  void resume();
	
	
	
	/**
	 * 释放分组，取消分组中所有请求，放弃正在加载的任务
	 * 被clear的分组如果要重新启动，必须重新调用start
	 * 当分组中的Image Handler处于attach状态，在clear时会被dettach.
	 */
	
	public  void destroy();

		
	
	/** 获得Group的名字
	 * @retrun group name
	 */
	public String getGroupName();

	
	//+调度

	/**
	 * 从分组窗口中调度一个URL进行下载	 
	 * @return  一个ScheduleNext表示成功调度一个， Null 表示没有可下载的
	 */
	ScheduleInfo scheduleNext();
		
	
	//判断是否在UI线程，发Message	
	public void doSendMsg(int res , String URI , int index);
	
	/** 返回Group中正在下载的Item的个数
	 * @return 正在下载的个数 
	 */	
	int getLoadingCount();
		
	
	/** 增加Group中正在下载的Item的个数
	 * 
	 */	
	void addLoadingCount();
	
	
	/** 减少Group中正在下载的Item的个数
	 * 
	 */
	void subLoadingCount();

}
