package com.android.carair.imagepool;
import java.util.ArrayList;

import android.app.Application;


/**
 * 这是ImageGroup的一个内置实现。DataLogic包中有和List配合使用的ImageGroup实现。 
 * ImageGroupImp与ImageGroupAdvImp适合于非List的场合。ImageGroupAdvImp适合于比较简单的场景：
 * URI和View一一对应的绑定。ImageGroupAdvImp使用简单，而ImageGroupImp提供较大的灵活性。
 * @deprecated
 */

public class ImageGroupImp extends ImageGroupBase 
{							
		
	//存储元素的数组
	private ArrayList<ImageHandler> m_queue; //线程不安全
	//-block 3
	
	//+block 4
	//这个会带来同步的麻烦，由每个ImageHandler维护State信息，这个只维护当前下载位置
	private int m_dlIndex=0; //当前待下载的元素在Group中的下标
	public static final int WINDOW_TO_TAIL = -1;
	private int m_activeWindowStart; //加载窗口
	private int m_activeWindowSize; //WINDOW_TO_TAIL 表示默认窗口大小为到队列最后
	
	
	/**
	 * 设置活动窗口，队列中处于窗口内的图片才会被加载
	 * @param startPosition 起始位置值, 取值范围为0~队列长度-1
	 * @param size 窗口大小， 取值范围0 ~ 队列长度， 0表示窗口大小为空，不会下载
	 * @return true 表示设置成功； false表示设置窗口的值错误
	 */		
	public  boolean setActiveWindow(int startPosition, int size)
	{
		if(null == m_queue){
			return false;
		}
		int total = m_queue.size();
		if(startPosition < 0 || startPosition >= total){
			return false;
		}else if(total < size){
			return false;
		}
		
		m_activeWindowStart = startPosition;
		m_activeWindowSize = Math.min(size, total - startPosition);
		
		
		m_dlIndex = m_activeWindowStart;				
		
//		//FIXME: temp
//		for( int i =  m_activeWindowStart ; i < m_activeWindowStart + m_activeWindowSize ; i++ )
//		{
//			GroupNode gn = m_queue.get(i);
//			ImageHandler ih = gn.getImageHandler();
//			if( ih != null)
//			{
//				ih.printState();
//			}
//			else
//			{
//				TaoLog.Loge(TaoLog.IMGPOOL_TAG, "no ih!!!");
//			}
//		}
		
		ImagePool.instance().groupChanged(this);
		return true;
	}
							
	/**
	 * 当前分组的优先级，被暂停的分组，总是处于沉睡级
	 * @return 优先级
	 */
	public int getPriority(){
		if(m_b_paused)
			return PRIORITY_DORMANT;
		return m_priority;
	}
	//Group name for debug 
	
	//+initial	
	
		
	/** 构造函数
	 * @param name group名称，用于调试
	 * @param context context对象
	 * @param priority 优先级
	 * @param cache_type 缓存类型 
	 */	
	public ImageGroupImp( String name , Application context, int priority , int cache_type)
	{
		super(name,context,priority,cache_type);
		defaultInit();				
	

	}
	
	/** 构造函数
	 * @param name group名称，用于调试
	 * @param URIs 初始的URI列表
	 * @param context context对象
	 * @param priority 优先级
	 * @param cachePolicy 缓存类型 
	 */	
	public ImageGroupImp(String name , ArrayList<String> URIs,  Application context, ImageListener listener, int priority, int cachePolicy)	
	{
		super(name,context,priority,cachePolicy);
		
		m_il = listener; 			
		defaultInit();				
		setURIList(URIs); 		
	}	
	
	
	/* defaultInit
	 * 追加一组图片到队列末尾
	 * @param URIs 追加的URI对应的ImageHandler
	 */	
	protected void defaultInit()
	{
			
		m_activeWindowStart = 0;
		m_dlIndex= 0; 
		m_activeWindowSize = WINDOW_TO_TAIL;
		m_queue = new ArrayList<ImageHandler>();
		m_b_paused = false;					
	}
	//-initial
	
	//在Handler List中是否有相同URI的
	private boolean _isInList(ArrayList<ImageHandler> handlers,String url)
	{
		for( ImageHandler ih : handlers)
		{
			if( ih.URI() == url )
				return true;
		}
		return false;
	}
	/**
	 * 设置列表，如果队列中有内容，会被删除，正在下载的会被停止
	 * 如果设置进一个空列表，则设置之后，队列为空
	 * @param handlers ImageHandler的列表
	 */
	
	public final void setHandlerList(ArrayList<ImageHandler> handlers)
	{
		//TaoLog.Logd(TaoLog.IMGPOOL_TAG, "ImageGroup.setList(), size " + ((handlers != null)?handlers.size():0));
		if(ImageCache.CACHE_CATEGORY_PERSIST_AUTOREPLACE == m_cachePolicy)
		{
			//对于CACHE_CATEGORY_PERSIST_AUTOREPLACE策略，这里先删除老的List中对应的Cache数据
			for( ImageHandler ih : m_queue)
			{
				//判断
				if( !_isInList( handlers, ih.URI()) )
				{
					this.deleteImage(ih.URI());
				}
			}
		}
		
		cancelList();
		appendList(handlers);			
	}
	
	
	public final void appendURIList(ArrayList<String> URIs)	
	{	
		
			ArrayList<ImageHandler> hds = new ArrayList<ImageHandler>();
			for( String str : URIs)
			{
				ImageHandler ih = ImagePool.instance()._createImageHandler(str, this.getCachePolicy());
				hds.add(ih);
			}
			appendList(hds);		
			
	}
	
	/**
	 * 追加一组图片到队列末尾
	 * @param URIs 追加的URI对应的ImageHandler
	 */	
	//注：为一淘增加appendList函数
	public final void appendList(ArrayList<ImageHandler> URIs)	
	{	
		
			synchronized(this)
			{				
				if(URIs != null)
				{
					for(ImageHandler URI : URIs)
					{
						if(m_queue != null)
							m_queue.add(URI);				
					}
				}		
			}
			
			ImagePool.instance().groupChanged(this);	
			
	}
	
	/**
	 * 设置一组URIs
	 * @param URIs 需要下载的一组图片
	 */	
	public final void setURIList( ArrayList<String> URIs)
	{
		ArrayList<ImageHandler> handlers = new ArrayList<ImageHandler>();		
		for(String uri : URIs)
		{				
			//TaoLog.Logd(TaoLog.IMGPOOL_TAG, "origin url: " + uri);
			handlers.add( ImagePool.instance()._createImageHandler(uri, m_cachePolicy));
		}						
		setHandlerList(handlers);
	}
	
	//取消这组图片中的下载					
	private final void cancelList()
	{
			
		if(m_queue.size() > 0){			
				synchronized(this)
				{
					
					//停止正在下载的
					for( ImageHandler ih : m_queue)
					{
						if( null != ih )
						{
							if( ih.getState()  == ImageHandler.LOADING )
							{
								ImagePool.instance().cancelLoad(ih.URI());
							}
							//在从Group中去除前回收已分配的内存 
							//TODO:
						}						
					}
					//清空队列
					m_queue.clear();
				}
			
		}
		m_loadingCount = 0;
		m_dlIndex= 0; //List已经改变，需要重置下载起始位置
	  
	}
	
		
	//+分组操作
	private boolean m_b_paused;
	
	//注意pause需要在Activity的OnStop中被调用，而不是OnPause中，因为在OnPause以后，还有一些Draw事件
	/**
	 * 暂停分组的任务请求，当前正在下载的不被放弃
	 */
	public  void pause()
	{				
		if(m_b_paused)
		{
		}else
		{								
			m_b_paused = true;						
			ImagePool.instance().groupPriorityChanged(this, m_priority, PRIORITY_DORMANT);
			
			///TODO: temp 
			//ImagePool.instance().dumpMemory(); //调试
			///////////////
			
		}	
	}
	
	/**
	 * 把处于暂停的中分组激活，分组将会重新被调度
	 */
	public final void resume()
	{		
		if(!m_b_paused)
		{
		}else{
						
			m_b_paused = false;	
			
			m_dlIndex = m_activeWindowStart;					
			ImagePool.instance().groupPriorityChanged(this, PRIORITY_DORMANT, m_priority);		
		}
	}

	/**
	 * 释放分组，取消分组中所有请求，放弃正在加载的任务
	 * 被clear的分组如果要重新启动，必须重新调用start	 
	 */	
	public  void destroy()
	{
		m_il = null; //解耦 ImageGroup和Listener				
		cancelList();
		ImagePool.instance().removeGroup(this);				
		//call image pool force recycle to make sure. recycle immediately
		ImagePool.instance().ForceBitmapRecycleAll();
	}
	
	/**
	 * 清空ImageGroup
	 * 
	 */
	public final void clear()
	{												
		cancelList();
		ImagePool.instance().removeGroup(this);														
	}
	


	/**
	 * 从分组窗口中调度一个URL进行下载
	 * 如果继承了该类，请重载该接口，定制继承类的自己的调度策略
	 * @return  非null表示成功调度一个， null 表示没有可下载的
	 */
	public synchronized ScheduleInfo scheduleNext()
	{		
		if(m_activeWindowSize != WINDOW_TO_TAIL )
		{
			if(m_dlIndex < m_activeWindowStart)
			{
				m_dlIndex = m_activeWindowStart;
			}
		}				
		int total = m_queue.size();
		if( m_dlIndex < total )
		{
			//Active window末尾的控制
			if(m_activeWindowSize != WINDOW_TO_TAIL)
			{
				if(m_dlIndex >= m_activeWindowStart + m_activeWindowSize)
					return null;
            }	
			ScheduleInfo info = new ScheduleInfo();
			info.index = m_dlIndex;
			info.ih = m_queue.get(m_dlIndex++);
			if(ImagePool.instance().getImageHandlerInMemory(info.ih.URI()) == null){
				info.ih = ImagePool.instance()._createImageHandler(info.ih.URI(), m_cachePolicy);
				m_queue.set(m_dlIndex - 1, info.ih);
			}
			//TaoLog.Loge(TaoLog.IMGPOOL_TAG, "scheduleNext url:" + info.ih.URI());
			return info;
		}
		return null;
	}
	
	

	
	/**
	 * 这个回调在下载结束被调用，如果是缓存加载的，不会调用这个回调
	 *注：该方法不访问ImagePool,可以不获取ImagePool单例对象锁
	 */
	@Override
	public void feedImage( int res , String URI, int index) {
		//if( ih != null)
		//	TaoLog.Loge(TaoLog.IMGPOOL_TAG, "dowloaded view tag:" + ih.getViewTag());
		
		if(index < 0 || index >= m_queue.size())
			return;
		
		
		if(res == ImageListener.OK)
		{
			m_queue.get(index).setState(ImageHandler.LOADED);
		}
		else 
		{
			//set to init state,want to try again
			m_queue.get(index).setState( ImageHandler.NOT_LOADED);
		}
		
		//下载完成，消息通知接收者
		doSendMsg( res , URI , index);		
	}
	
	
	
	/**
	 * 这个接口只有永久保存的图片用户需要调用，临时图片会自动被删除
	 * @param URL
	 */
	public void deleteImage(String URL)
	{		
		ImagePool.instance().delImage(URL, m_cachePolicy);		
	}

	
	@Override
	public void onProgress(String desc, int size, int total,String url,int index) {
		// TODO Auto-generated method stub
		
	}
	
	/*
	public void dumpMemory()
	{
		TaoLog.Logv(TaoLog.IMGPOOL_TAG, "---- dump image  group" + this.m_groupName );
		for( int i = 0 ; i < m_queue.size() ; i++)
		{
			ImageHandler ih = m_queue.get(i);
			if( ih != null)
			{
				ih.printState();				
			}
			else
				TaoLog.Logd(TaoLog.IMGPOOL_TAG, "ih is null ");
		}
		TaoLog.Logv(TaoLog.IMGPOOL_TAG, "---- dump image group end" + this.m_groupName );
	}
	*/
	
}
