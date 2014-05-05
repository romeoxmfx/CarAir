package com.android.carair.imagepool;
import java.util.ArrayList;

import com.android.carair.utils.SafeHandler;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;


/**
 *这是图片分组的公共基类
 *图片分组是用户加载图片的直接交互类，通过创建和操作图片分组，
 *1 用户可以位置透明的获得所需图片，无需关心位置相关的加载逻辑
 *2 图片分组可以维护请求队列，用户可以一次请求多个图片下载任务，并不断增加或者删除
 *3 通过提供窗口设置，保证在队列中可以有一部分指定请求获得优先执行
 *4 分组队列内的图片下载活动只有一种下载策略
 *5 实际的图片加载动作通过调用ImagePool接口完成
 *6 ImagePool通过图片分组管理所有的图片加载请求
 *7 需要的时候，可以继承并扩展图片分组类，实现更定制化的图片加载队列管理
 *
 *example,一个简单的使用例子
 *ArrayList<String> URIs = new ArrayList<String>();
 *URIs.add("http:://www.taobao.com/img1.png");
 *ImageGroup ig = new ImageGroup(URIs, this, ImageGroup.PRIORITY_NORMAL);
 *ig.start();
*/

public abstract class ImageGroupBase implements ImageGroup
{							
	
	protected int m_priority; //Group的优先级
	protected int m_cachePolicy = ImageCache.CACHE_CATEGORY_NONE; //这个Group所采用的Cache策略
	protected int m_loadingCount = 0; //记录该Group中当前正在下载的图片数量，对于Normal优先级的Group，同时下载的图片数量上限是一个
	protected ImageListener m_il; //用于通知消息的Listener
	protected String m_groupName; 
	protected Thread m_caller;
	private Handler m_imageHandler;
	/** 返回Group中正在下载的Item的个数
	 * @return 正在下载的个数 
	 */		
	public final int getLoadingCount()
	{
		return m_loadingCount;
	}
	
	/** 增加Group中正在下载的Item的个数
	 * 
	 */	
	public final void addLoadingCount()
	{
		m_loadingCount++;
	}
	
	/** 减少Group中正在下载的Item的个数
	 * 
	 */
	public final void subLoadingCount()
	{
		m_loadingCount--;
	}
	
	
	/**
	 * 当前分组的优先级，被暂停的分组，总是处于沉睡级
	 * @return 优先级
	 */
	public int getPriority(){
		return m_priority;
	}
		
	/** 获得Group的名字
	 * @retrun group name
	 */
	public String getGroupName()
	{
		return m_groupName;
	}
	
	/**
	 * 设置分组优先级，如果分组已经被加入到ImagePool,会调整ImagePool的分组队列
	 * @param pri 优先级
	 */
	// 注：除了构造函数priority的改变只在ImagePool的调度程序中进行
	public void setPriority(int pri)
	{	
		if(m_priority == pri)
			return;		
		ImagePool.instance().groupPriorityChanged(this, m_priority, pri);
		m_priority = pri; 
		
	}	
	
	
	
	//抽象方法	
	/**
	 * 设置活动窗口，队列中处于窗口内的图片才会被加载
	 * @param startPosition 起始位置值, 取值范围为0~队列长度-1
	 * @param size 窗口大小， 取值范围0 ~ 队列长度， 0表示窗口大小为空，不会下载
	 * @return true 表示设置成功； false表示设置窗口的值错误
	 */		
	abstract public  boolean setActiveWindow(int startPosition, int size);
												
	
	
	
	
		
	/** 构造函数
	 * @param name group名称，用于调试
	 * @param context context对象
	 * @param priority 优先级
	 * @param cache_type 缓存类型 
	 */	
	public ImageGroupBase( String name , Application context, int priority , int cache_type)
	{
		m_priority = priority;
		m_groupName = name;
		m_cachePolicy = cache_type;	
		m_loadingCount = 0;		
		try
		{			
			m_imageHandler = new SafeHandler(Looper.getMainLooper(),this);
		}
		catch( RuntimeException e)
		{
			//不能在非Looper的线程中create handler
			//否则会有RuntimeException
			
		}
		m_caller = Thread.currentThread();

	}
	

	/** 获得存储类型
	 * @return 存储类型
	 */
	public int getCachePolicy(){
		return m_cachePolicy;
	}
	
	/** 设置Image下载结果的Listener回调
	 * @param listener listener回调
	 */
	public void setImageListener(ImageListener listener){
		m_il = listener;
	}
	

	/** 获得Image下载结果的Listener回调
	 * return listener回调
	 */
	public final ImageListener getImageListener(){
		return m_il;
	}
	
	//-initial
	
	/**
	 * 设置列表，如果队列中有内容，会被删除，正在下载的会被停止
	 * 如果设置进一个空列表，则设置之后，队列为空
	 * @param handlers ImageHandler的列表
	 */
	

	abstract public  void setHandlerList(ArrayList<ImageHandler> handlers);		
	/**
	 * 追加一组图片到队列末尾
	 * @param URIs 追加的URI对应的ImageHandler
	 */	
	//注：为一淘增加appendList函数
	abstract public void appendList(ArrayList<ImageHandler> URIs);	
	abstract public void appendURIList(ArrayList<String> URIs);	

								

	
	//+分组操作	
	
	//和Activity的生命周期相关
	//注意pause需要在Activity的OnStop中被调用，而不是OnPause中，因为在OnPause以后，还有一些Draw事件
	/**
	 * 暂停分组的任务请求，当前正在下载的不被放弃
	 */
	public abstract void pause();

	
	/**
	 * 把处于暂停的中分组激活，分组将会重新被调度
	 */
	public abstract void resume();
	
	
	
	/**
	 * 释放分组，取消分组中所有请求，放弃正在加载的任务
	 * 被clear的分组如果要重新启动，必须重新调用start
	 * 当分组中的Image Handler处于attach状态，在clear时会被dettach.
	 */
	
	public abstract void destroy();

	
	/**
	 * 清空ImageGroup
	 * 
	 */
	public abstract void clear();

	

	 
	/**
	 * 把分组加入ImagePool的分组队列中
	 * 并遍历列表的加载窗口，尝试从缓存中获取所需图片
	 * 注：由于start会导致对缓存的预读，可能会多次进行文件io，在调用start之前，请确认设置有效的窗口，否则会默认对整个队列进行缓存预读
	 */
	public final void start()
	{					
		try
		 {				
				//加入到ImagePool中
				ImagePool.instance().addGroup(this);				
				//执行时间
				//TaoLog.Logi(TaoLog.IMGPOOL_TAG, "ImageGroup::start() "+"time " +(System.nanoTime() - start)/(1000*1000)+ "milliseconds used");
		 }
		 catch( Exception e )
		 {
			 e.printStackTrace();
		 }
	}
	
	
	

	/**
	 * 从分组窗口中调度一个URL进行下载
	 * 如果继承了该类，请重载该接口，定制继承类的自己的调度策略
	 * @return true 表示成功调度一个， false 表示没有可下载的
	 */
	 public abstract ScheduleInfo scheduleNext();
	
	
	
	/**
	 * 这个接口只有永久保存的图片用户需要调用，临时图片会自动被删除
	 * @param URL
	 */
	public abstract void deleteImage(String URL);
	
	
	//判断是否在UI线程，发Message
	public void doSendMsg(int res , String URI , int index){
		if(m_caller == Thread.currentThread()){
			
			try{
				m_il.feedImage( res, URI , index );
				
			}catch(Exception e){
			}
		}else{
			//通知者和被通知者不在同一线程，不直接调用回调函数，因为当前执行在下载线程中，不能直接操作ui线程，所以发消息到接受者线程处理
			Message resmsg = Message.obtain(); 
			resmsg.what = ImageDownloader.MSG_DL_FINISHED;
			resmsg.obj = URI;
			resmsg.arg1 = index;
			resmsg.arg2 = res;
			if(m_imageHandler!=null)
				m_imageHandler.sendMessage(resmsg);
		}
	}
	
		class ProgressMsg
		{
			ProgressMsg(String desc, int size, int total,String url,int index)
			{
				m_desc = desc;
				m_size = size;
				m_total = total;
				m_url = url;
				m_index = index;
			}
			String m_desc;
			int m_size;
			int m_total;
			String m_url;
			int m_index;
		}
		//判断是否在UI线程，发Message
		public void doSendProgressMsg(String desc, int size, int total,String url,int index){
			
			{
				//通知者和被通知者不在同一线程，不直接调用回调函数，因为当前执行在下载线程中，不能直接操作ui线程，所以发消息到接受者线程处理
				Message resmsg = Message.obtain(); 
				resmsg.what = ImageDownloader.MSG_DL_PROGRESS;
				resmsg.obj = new ProgressMsg(desc,size,total,url,index);				
				if(m_imageHandler!=null)
					m_imageHandler.sendMessage(resmsg);
			}
		}
	@Override
	public boolean handleMessage(Message msg) {
		// TODO Auto-generated method stub
				
		switch( msg.what)
		{
		case ImageDownloader.MSG_DL_PROGRESS:
			try{
				ProgressMsg pmsg = (ProgressMsg)msg.obj;
				m_il.onProgress( pmsg.m_desc, pmsg.m_size, pmsg.m_total, pmsg.m_url, pmsg.m_index);					
			
			}catch(Exception e){
			}
			break;
		case ImageDownloader.MSG_DL_FINISHED:
			try{
					String URI = (String)msg.obj;
					m_il.feedImage( msg.arg2, URI ,msg.arg1 );					
				
			}catch(Exception e){
			}
			break;
		}
		return false;
	}	
	
/*	public void showProgress(boolean show){
		mShowProgress = show;
	}
	public boolean isShowProgress(){
		return mShowProgress ;
	}
	
	public interface PercentImageMaker{
		 Drawable getPercentImage(int percent);
	}
	private PercentImageMaker mPercentImageMaker; 
	public void setPercentImageMaker(PercentImageMaker p){
		mPercentImageMaker = p;
	}
	public PercentImageMaker getPercentImageMaker(){
		return mPercentImageMaker;
	}*/
}