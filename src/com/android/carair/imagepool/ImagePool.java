
package com.android.carair.imagepool;


import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.regex.Pattern;

import android.app.Application;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.TextUtils;

import com.android.carair.imagepool.utility.BitmapHelper;
import com.android.carair.imagepool.utility.BitmapHelperFactory;
import com.android.carair.imagepool.utility.TBDrawable;
import com.android.carair.threadpool2.ThreadPage;
import com.android.carair.utils.MemoryManager;
import com.android.carair.utils.MemoryManager.MemoryManagerListener;
import com.android.carair.utils.Priority;

/**
 * ImagePool包主要是对从服务端下载的图片的进行管理，优化流量和内存：
 * 1.	内存管理
 *  Bitmap对象native层次的内存消耗是造成OOM的重要原因。ImagePool以对应用透明的方式管理这个内存。
 * 2.	带线程池机制的并发下载
 * 3.	Group式的组织和调度
 * 4.	Memory和File的双层Cache机制，以及smart的双向切换
 * 5.	内存的监控
 * 6.	ImageHandler作为Image的代理，自动处理Image从网络、从Cache、从回收状态恢复等逻辑，使用者可以不必关心。
 * 7.	流量机制
 * 		根据设备屏幕大小和网络情况，选取合适图片在缓存中读取或者网络下载。 
 * 8.	集成对webp图片格式的支持
 */




/**
* ImagePool 类是从服务端下载图片的统一管理者 
*/
public class ImagePool implements Runnable  {
	public final static String SCHEME_TYPE_FILE = "file";	
	public final static String SCHEME_TYPE_RESOURCE = "resource";	
	
	public final static float MAX_COMPRESSION_RATIO_WEBP  = 0.4f;
	public final static float MAX_COMPRESSION_RATIO_JPG  = 0.55f;
	public final int LEAK_ALERT_THRESHOLD = 25; //用于调试，当管理图片个数超过LEAK_ALERT_THRESHOLD时，打印当前图片信息
	public final static String PERF_IMAGE_LEAK = "PerfImageLeak"; 
	public Object groupLock = new Object();
	//用于调试
//	public  static void writeFile( String name , byte [] data ) 
//	{
//		int i = name.lastIndexOf('/');
//		String name2 = name.substring(i+1);
//		String filename = "/sdcard/" +name2;
//		File f = new File(filename);
//		if (f.exists())
//		System.out.println("File exits");
//		else
//		try {
//			f.createNewFile();
//		} 
//		catch (IOException e) 
//		{
//			System.out.println(e);
//		}
//		
//		OutputStream os;
//		try {
//			os = new FileOutputStream(filename);
//			os.write(data);
//			os.close();
//		} 
//		catch (FileNotFoundException e) 
//		{
//			e.printStackTrace();
//		} 
//		catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
	///////////
	
	//统计由ImagePool维护的bitmap的生成/释放情况
	class BitmapStatics
	{
		int createNum = 0; //生成的TBDrawable对象的个数
		int destroyNum = 0;	//释放的TBDrawable对象的个数
		int createSize = 0; //生成的TBDrawable对象的内存总大小
		int destroySize = 0; //释放的TBDrawable对象的内存总大小
		
		//输出当前Bitmap的情况
	}
	BitmapStatics stat;
		
	
	//------------------------------------------+分组管理+-----------------------------
	private ImageGroup m_topGroup; //top是优先级最高的分组
	private ArrayList<ImageGroup> m_normalGroups;  //普通优先级的调度分组
	private ArrayList<ImageGroup> m_dormantGroups; //Pause状态下下的分组，处于非调度状态
	
	/*
	 * 检查分组是否存在管理队列中
	 * @param group 待检查的分组
	 * @return true存在，false不存在
	 */
	private final boolean _findGroup(ImageGroup group)
	{
		if(m_topGroup == group)
			return true;
		if(m_normalGroups.contains(group)){
			return true;
		}else{
			return m_dormantGroups.contains(group);
		}
	}
	
	/*
	 * 向ImagePool中加入一个分组
	 * @param group 新增的Group
	 */
	final void addGroup(ImageGroup group)
	{
		synchronized(this.groupLock)
		{
			//check existence first
			if(_findGroup(group))
				return;
			
			switch(group.getPriority())
			{
			case ImageGroup.PRIORITY_TOP:
				//如果top已经存在，则被降级
				if(m_topGroup != null)
				{
					m_topGroup.setPriority(ImageGroup.PRIORITY_NORMAL);//这里会重排队列
				}
				m_topGroup = group;
				reSchedule();
				break;
			case ImageGroup.PRIORITY_NORMAL:
				m_normalGroups.add(group);
				reSchedule();
				break;
			case ImageGroup.PRIORITY_DORMANT:
				m_dormantGroups.add(group);
				break;
			}
		}
	}
	
	/**
	 * 移除一个分组
	 * @param group 移除的Group
	 * @return false表示分组不存在，true表示移除成功
	 */
	public final boolean removeGroup(ImageGroup group)
	{
		 synchronized(this.groupLock)
		 {
			if(m_topGroup == group){
				m_topGroup = null;
				reSchedule();
				return true;
			}else if(m_dormantGroups.remove(group)){//分组在沉睡队列中，移除后无需调度
				return true;
			}else if(m_normalGroups.remove(group)){
				reSchedule();
				return true;
			}else{
				return false;
			}
		 }	
	}
	/**
	 * 通知ImagePool某个分组内容发生改变，需要被调度
	 * @param group 发生改变的Group
	 */
	public  final void groupChanged(ImageGroup group)
	{
	  synchronized(this.groupLock)
	  {
		if(_findGroup(group) && !m_dormantGroups.contains(group))
			reSchedule(); //触发调度线程唤醒
	  }
	}
	
	/**
	 * 优先级调整，对队列进行重排，从原优先级队列移出，放入新优先级队列
	 * @param group 发生改变的Group
	 * @param from 原优先级
	 * @param to 新优先级
	 */
	public  void groupPriorityChanged(ImageGroup group, int from, int to)
	{
		synchronized(groupLock)
		{
			if((from == to) || (null == group))
				return;
			
			
			//check existence first, remove from original queue
			if(m_topGroup == group){
				m_topGroup = null;
			}else if(!m_normalGroups.remove(group) && !m_dormantGroups.remove(group)){
				return;
			}
				
			if(to == ImageGroup.PRIORITY_DORMANT){//从其他到dormant，无需进行调度
				m_dormantGroups.add(group);
			}else{//否则，需要调度
				if(to == ImageGroup.PRIORITY_NORMAL){
					m_normalGroups.add(group);
				}else{
					m_topGroup = group;
				}
				reSchedule();
			}
		}
	}
	//**************************************-分组管理-**********************************
	
	
	
	//--------------------------------------+最大并发+----------------------------------
	private int m_concurrentDownloadCount; //图片下载的最大并发数	
		
	/**
	 * 设置允许并发下载的最大数
	 * @param count
	 */
	public synchronized void setConcurrentDownloadCount(int count)
	{
		m_concurrentDownloadCount = count;	
	}
	//*************************************-最大并发-***********************************
	
	
	
	
	//-------------------------------------+下载队列+-----------------------------------
	/**
	*代表一个图片下载活动，接收数据并通知用户
	*执行下载的http连接由它持有，为了减少反复释放并创建http连接，该对象应该被重用。
	*同时应该考虑重用连接的异常处理，能进行连接重建
	*暂不支持下载暂停和恢复，停止后本次下载只能重新开始，
	*/
		
	/**
	 * 内部类ImageExecutor 负责使用ImageDownloader下载图片
	 * */
	private class ImageExecutor implements IImageDownloader.DownloadNotifier{
		private ImageHandler m_image; //下载的图片所对应的Handler 		
		private ImageGroup m_group; //下载的图片对应的Group
		private int m_indexInGroup;//为了处理group中可能存在多个相同的url的情况
		
		
		private boolean m_b_executing; //标记正在执行
		private IImageDownloader m_idl; //具体用于下载的Downloader
		
		//用于保存同时对一个正在下载的图片感兴趣者		
		class FeedImageListener
		{			
			ImageGroup m_group;
			int m_index;
			
			FeedImageListener( ImageHandler ih , ImageGroup ig , int index)
			{		
				m_group = ig;
				m_index = index;
				
			}
		}
		
		//当相同URL的图片同时下载时，由一个Executor下载。
		//后续的加入到这个Feed queue,在通知结果时通知到所有的ImageGroup
		private ArrayList<FeedImageListener> m_feedQueue;
		
		/* ih与当前下载的图片一样，加入到Feed队列中		 
		 * */
		private void joinExecutor(ImageHandler ih ,ImageGroup ig , int index )
		{
			if(  null == m_feedQueue)
			{
				m_feedQueue = new ArrayList<FeedImageListener>();
			}
			
			synchronized(m_feedQueue)
			{
				m_feedQueue.add(new FeedImageListener( ih ,ig ,index) );
			}
		}
		
		/**
		 * ImageExecutor构造函数
		 * @param ih 待下载的ImageHandler
		 * @group 所属于的ImageGroup
		 * @index 在Group中的索引
		 */
		public ImageExecutor( ImageHandler ih , ImageGroup group, int index) throws Exception
		{
			if(group == null){
				Exception e = new Exception("ImagePool::ImageExecutor new exception: null group param");
				throw e;
			}
			m_group = group;
			m_image = ih;
			m_image.setCachePolicy(m_group.getCachePolicy());
			m_b_executing = false;
			m_indexInGroup = index;
			
		}
		
		//Lock住空闲的Executor
		private synchronized final  void lockExecutor()
		{
			m_b_executing = true;			
		}
		
		//空闲的Executor,通过这个函数设置数据
		private synchronized final void setImageHandler(ImageHandler ih, int index)
		{							
			m_image = ih;
			m_indexInGroup = index;							
		}
		
		/**
		 * 图片下载任务所属的分组
		 * @param group
		 */
		private synchronized void setGroup(ImageGroup group)
		{
			m_group = group;
		}
		
		/**
		 * 开始下载，在此时开始创建连接
		 */
		private synchronized final void start()
		{
	
			//TaoLog.Logd(TaoLog.IMGPOOL_TAG, "ImagePool.ImageExecutor.start()");
			//long start = System.nanoTime();
			
			if(m_idl == null)
				m_idl = new ImageDownloader(this , m_context);

			//询问策略决定的最终实现下载地址
			String mapurl = m_image.URI();
			if(mStragery !=null) 
				mapurl = mStragery.decideUrl(mapurl);
			
			//开始下载
			m_idl.setURL(m_image.URI(),mapurl,m_group.getCachePolicy());
			m_idl.startDownload();
			m_b_executing = true;
			if(m_group!=null)
				m_group.addLoadingCount();
			
			//时间
			//TaoLog.Logd(TaoLog.IMGPOOL_TAG, "ImagePool.ImageExecutor.start() done "+(System.nanoTime() - start)/(1000*1000) +" milliseconds used");
		
		}
		/**
		*停止下载，停止后本次下载只能重新开始。
		*/
		private final void stop()
		{		
			if( m_idl!=null)
				m_idl.stop();			
		}
		
		/**
		 * 释放下载器，释放后需要重新创建
		 */
		private final void releaseIDL()
		{	
			synchronized(this)
			{
				if(m_idl!=null)
				{
					m_idl.destroy();
					m_idl = null;
				}
				m_b_executing = false;
				m_image = null;
				if(m_group!=null)
				{
					m_group.subLoadingCount(); 
					m_group = null;
				}
				m_feedQueue = null;
			}	
		}
		
						
		//图片下载完成后的回调函数		 
		private boolean _handleDownloadFinish(byte[] data, String originurl )
		{			
			String url = originurl;
			//应该是以期望的URL，即实际下来的URL存在缓存中
			if(mStragery!=null)
			{
				url = mStragery.decideUrl(originurl);
			}				
			
			if(m_group !=null && data!=null) 

			if( m_image ==null)
			{
				allFeedImage(ImageListener.OK, originurl);
				return false;
			}
			
			
			if(data==null|| data.length == 0)
			{
				allFeedImage(ImageListener.FAIL, originurl);
				return false;
			}
			
			//先保存图片，在加入到mem cache中，否则有可能在保存时，Bitmap被回收
			boolean saved = false;
			if(m_IC!=null )
			{
				saved = m_IC.saveData(url, data, m_image.getCachePolicy());								
			}
			else{
			}
			
			if(!saved) //保存失败则加入到内存中
			{
				//检查是否在下载过程中，已经在内存中了
				ImageHandler ih = _getImageHandler(originurl,m_image.getCachePolicy(),m_image.getBitmapCreator());
				if(  ih.isRecyceled() || ih.getDrawable() == null)				
				{									
					//保存到文件失败，从网络来的Data生成图片,只加入到内存中
                    //bug fix for jiajing:
                    //用Data数据生成bitmap时，需要看真正的URL来判断
					TBDrawable d = _createTBDrawable(data,url);
					
					if( d != null)
					{
						boolean res = ih.setDrawable(d,false);
						if( m_memCahce.add(ih)  )
						{
							if(res)
							{
								stat.createNum++;		
								stat.createSize += 	d.bitmapSize();
							}
						}								
					}
					else
					{
                        //bug fix for jiajing: 无SDK时多次重复
						allFeedImage(ImageListener.FAIL_NO_REPEAT, originurl );
						return false;
					}
					
				}							
					
				allFeedImage(ImageListener.OK, originurl );				
				return true;
			}
			else
			{
				//ImageHandler已经Ready，通知成功
				allFeedImage(ImageListener.OK, originurl);				
				return true;
			}
		}
		
		/* 用于通知Image下载成功，通知Group的接口和所有注册在Feed Queue中的
		 * 
		 * */
		private void allFeedImage(int res ,String url  )
		{
			if(m_group !=null)
			{				
				m_group.feedImage( res,url, m_indexInGroup);
			}
			
			if(null != m_feedQueue)
			{
				//先得到Queue的数组，减少Sync的范围
				Object [] listeners;
				synchronized(m_feedQueue)
				{
					listeners = m_feedQueue.toArray();
				}
					
				for( Object l : listeners )
				{
					FeedImageListener listener =  (FeedImageListener) l; 
					listener.m_group.feedImage(res, url, listener.m_index);
					
				}
												
			}
		}
		/**
		 *接收下载器回调通知的接口
		 * 注：该接口由外部下载器线程调用，不会先获取ImagePool的对象锁，需要确保在调用的时候获取ImagePool锁
		 */
		@Override
		public void notify(int msg, byte[] data, String result)
		{
			try
			{
				switch(msg)
				{
					case IImageDownloader.MSG_DL_FINISHED:
						_handleDownloadFinish(data,result);						
						break;
					case IImageDownloader.MSG_DL_FAILURE:
					case IImageDownloader.MSG_DL_INVALIDURL:
						//handler the network failure 		
						allFeedImage(ImageListener.FAIL, result );
		//				TaoLog.Logd(TaoLog.IMGPOOL_TAG, "ImagePool.ImageExecutor.notify() msg " + msg + " failed");
						break;
					case IImageDownloader.MSG_DL_FAILURE_NOREPEAT:
						allFeedImage(ImageListener.FAIL_NO_REPEAT, result );
						break;
					case IImageDownloader.MSG_DL_USER_CANCELED:
						break;
					default:
						break;
				}
				
			}
			finally //在发生Exception的情况下，也要保证LoadingCount维护正确
			{
				
				
				synchronized(m_downloaders)
				{
					m_b_executing = false;
					if(m_feedQueue!=null)
					{
						synchronized(m_feedQueue)
						{
							m_feedQueue = null;
						}
					}
					m_image = null; //解与ImageHandler的引用	
					
					//如果下载器个数已经超过并发限制，则释放该下载器
					if(m_downloaders.size() > m_concurrentDownloadCount){
						this.releaseIDL();
						m_downloaders.remove(this);
		//				TaoLog.Logd(TaoLog.IMGPOOL_TAG, "notify() release one downloader, "+ m_downloaders.size() + " remains");
					}
					else
					{
						if(m_group!=null)
						{
							m_group.subLoadingCount();
							m_group =null;
						}
					}
		
				}
				reSchedule(); //在处理完Counting后Reschedule
			}
		}
		@Override
		public void onProgress(String desc, int size, int total) {
			if( m_group != null && m_image != null )
			{
				m_group.onProgress(desc, size, total, m_image.URI(),m_indexInGroup);
			}
			else
			{
			}
		}
		
	}
	
	private ArrayList<ImageExecutor> m_downloaders; //维护ImageExecutor列表
	
	/**dl
	 * 寻找当前已分配的下载执行器中的空闲线程
	 * @ImageExecutor 
	 */
	private final ImageExecutor findIdleExecutor()
	{
		synchronized(m_downloaders)
		{
			for(ImageExecutor ie : m_downloaders)
			{
				if(!ie.m_b_executing)
					return ie;
			}
		}
		//TaoLog.Logv(TaoLog.IMGPOOL_TAG, "!!!findIdleExecutor return null");
		return null;
	}
	
	/**
	 * 寻找当前正在下载的线程中是否有下载同一张图片的.如果存在，应该合并下载。
	 * @ImageExecutor 
	 */
	private final ImageExecutor findEqualLoadingExecutor( String url)
	{
		synchronized(m_downloaders)
		{
			for(ImageExecutor ie : m_downloaders)
			{
				
				if(ie.m_b_executing)				
				{
					if( ie.m_image != null)
					{
						//TaoLog.Logd(TaoLog.IMGPOOL_TAG, "findEqualLoadingExecutor: url" + url + " , image:"  +ie.m_image.URI( ));
						if( ie.m_image.URI() == url )
						{
							return ie;
						}
					}					
				}
			}
		}
		return null;
	}
	
	

	/*
	 * 表示需要进行一次下载调度,有以下情况会触发该状态有效
	 * 1. 有一个或者多个图片下载结束（缓存中加载成功不会触发调度需求）
	 * 2. 非沉睡队列中的图片分组发生改变
	 */
	private boolean m_b_needSchedule;
	
	//唤醒调度线程
	private  void reSchedule() //this synchronized is for the notify of the schedule thread
	{
		if(m_scheduleThread==null)
			return;
		synchronized(m_scheduleThread) //notify needs synchronized to be a block,can't be the function level
		{
			m_b_needSchedule = true;
			if(!m_scheduleThreadStarted)
			{
				m_scheduleThread.start();
				m_scheduleThreadStarted = true;
			}
			
			try{
				m_scheduleThread.notify();
			}catch(Exception e){
			}
		}
	}
	
	//----------------------------------------------------------------------------------------------
	//以下是一组根据ImageHandler的当前状态，对调度到的Handler进行处理的函数
	
	private static final int PROTOCOL_UNKNOWN = 0;
	private static final int PROTOCOL_HTTP = 1; //HTTP协议
	private static final int PROTOCOL_PACKAGE = 2; //从本地包中获得LOGO图片,协议如：package://xxx.xxx
	private static final int PROTOCOL_CREATOR = 3; //执行使用imageCreator获得图片,协议如: creator://xxx.xxx
	
	//解析协议类型
	private int _parseProtocol(String uri)
	{
		if( uri.startsWith("http"))
			return PROTOCOL_HTTP;
		else if( uri.startsWith("package"))
			return PROTOCOL_PACKAGE;
		else if(uri.startsWith("creator"))
			return PROTOCOL_CREATOR;
		return PROTOCOL_UNKNOWN;
	}
	
	//从网络下载
	private boolean processDownload(ImageHandler ih , ImageGroup imgGroup , int index)
	{
		//TaoLog.Logi(TaoLog.IMGPOOL_TAG, "group:" + m_groupName + " ;" + "ImageGroup::scheduleNext() load image index " +searchStartIndex+ " " + node.URI());
		
		if( ih == null || ih.URI() == null )
		{
			return false;
		}
		
		ih.setState( ImageHandler.LOADING);
		int protocol = _parseProtocol(ih.URI());
		switch( protocol)
		{
		case PROTOCOL_HTTP:
			loadImage( ih , imgGroup, index);
			break;
		case PROTOCOL_PACKAGE:
			loadPackageIcon( ih , imgGroup, index);
			break;
		case PROTOCOL_CREATOR:
			loadImageFromCreator(ih , imgGroup, index);
			break;
		default:
			break;
		}										
		return false;
	}
	

	//从本地加载
	private boolean processLoaded(ImageHandler ih , ImageGroup ig , int index )
	{			

		if(ih != null)
		{
			//TaoLog.Logv(TaoLog.IMGPOOL_TAG, "processLoaded:" + ih.URI() );			
			if( _loadDrawable(ih,ih.URI(),ih.getCachePolicy(),ih.getBitmapCreator()) )
			{												
				try
				{
					//release cpu after read cache operation
					Thread.sleep(2);
				}
				catch(Exception e )
				{
					e.printStackTrace();
				}
				return true;  
			}	
			else
				return processDownload( ih, ig, index); //本地下载未成功，从网上下载			
			           
			//TaoLog.Logi(TaoLog.IMGPOOL_TAG, "group:" + m_groupName + " ;" + "ImageGroup::scheduleNext() "+ node.URI() + " loaded from cache,"+
			//		(System.nanoTime() - start)/(1000*1000)+ "milliseconds used");
		}
		return false;		
	}
	
	//处理已经被回收的ImageHandler
	private boolean processRecycle(ImageHandler ih  )
	{			
		if( null == ih )
			return false;
		
		if( ih.isRecyceled() )
		{
			//recycle的bitmap尝试从File中恢复
			//node.m_ih.printState(); 
			//TaoLog.Logw( TaoLog.IMGPOOL_TAG ,"reload for recycled ih!");
			if( ih._ReloadRecyceledIfNeed() )
			{				                 
				return true;
			}																		                   
		}
		return false;
	}
	
	
	//处理ImageGroup中调度到的Handler，根据所处状态做相应处理			
	private boolean processImageHandler( ImageHandler ih , ImageGroup ig , int index )
	{		
		if( ih == null)
		{
			//触发Schedule继续处理
			groupChanged(ig);
			return false;
		}
		
		//for debug
		//ih.printState();
		//
		
		
		boolean bnotify = false;
		switch( ih.getState() )
		{		
		case ImageHandler.LOADING:
			processDownload(ih,ig,index);
			break;
		case ImageHandler.CONVERTED:
		case ImageHandler.LOADED:	
			if( ih.isRecyceled() )
			{
			}			
			bnotify = true;
			break;
		case ImageHandler.LOAD_RECYCLE:
			bnotify = processRecycle(ih);
			if(!bnotify)
			{
				ih.setState(ImageHandler.NOT_LOADED);
				return processImageHandler(ih,ig,index);
			}				
			break;
		case ImageHandler.LOAD_FAILED:
			break;
		case ImageHandler.NOT_LOADED:		
			bnotify = processLoaded(ih, ig, index);			
			break;
		}
		
		if( bnotify )
		{			
			ig.doSendMsg( ImageListener.OK , ih.URI() ,index);	//异步通知	
		}
		groupChanged(ig); //触发Schedule继续处理		
						
		return true;
	}
	/**
	 * 对分组队列进行调度，下载图片
	 * 进行调度时，不允许其他线程访问ImagePool的数据
	 */
	
	private void doSchedule()
	{
		//时间敏感函数，需要控制执行时间
		long start = System.nanoTime();
		
		
		ScheduleInfo info = null;
		synchronized(this.groupLock)
		{
			//当top分组存在，而且其没有被启动的下载时，启动一个top分组中的任务下载		
			if((null != m_topGroup) && (m_topGroup.getLoadingCount() == 0))
			{	
				info = m_topGroup.scheduleNext();
			}			
		}
		
		if( info != null )
		{
			processImageHandler(info.ih , m_topGroup, info.index);
		}
		
		//仍有可用的下载线程，优先分配给top分组，调度更多top分组的请求进行加载
		while((m_downloaders.size() < m_concurrentDownloadCount) || (findIdleExecutor() != null))
		{
			
			synchronized(this.groupLock)
			{
			
				if(null == m_topGroup ) 
				{
					break;
				}
				//top分组已经没有需要加载的任务
				info = m_topGroup.scheduleNext();
			}
			
			if( info != null )
			{
				if( !processImageHandler(info.ih, m_topGroup, info.index) )
				{
					//继续处理top组
					continue;
				}
			}
			else
			{
				//top组已经处理完毕
				break;
			}
			
		}
		
		
		//从头到尾遍历normal队列，每个normal分组队列最多被启动一个加载任务
		int size = m_normalGroups.size();
		if(size == 0){
			return;
		}		
		int i = 0;
		while( i < size )
		{
			//处理Normal分组
			ImageGroup group = null;
			info = null;
			synchronized(this.groupLock)
			{
			
				
				group = m_normalGroups.get(i);
				
				info  = group.scheduleNext();
				/*
				  if( group.getLoadingCount() == 0  || (m_downloaders.size() < m_concurrentDownloadCount) || (findIdleExecutor() != null)  )
				 
				{			
					
					info  = group.scheduleNext();
		
				}
				else
				{
					//否则跳过这个Group的处理
					//TaoLog.Logi(TaoLog.IMGPOOL_TAG, "!!!! skip schedule ie:" + ie + "  loadingcount: "  + loadingcount + " downsize: " + downsize + " m_concurrentDownloadCount = " + m_concurrentDownloadCount);												
				}
				*/
			}		
			
			if( info != null && group !=null)
			{
				if( processImageHandler(info.ih ,group,info.index) )
				{						
				}
			}
			i++;
		}		
		//时间
	}
	
	private ThreadPage m_threadPage; //运行IconExecutor的ThreadPage
	
	/**
	 * IconExecutor
	 * 从Package包中，获得Logo图片的类
	 * 
	 * */
	private class IconExecutor implements Runnable
	{
		private ImageHandler m_ih;
		private ImageGroup m_ig;
		private int m_index;
		
		/** 构造函数
		 *  ih : ImageHandler
		 *  ig : 所属ImageGroup
		 *  index:在ImageGroup中的位置
		 * */
		public IconExecutor( ImageHandler ih , ImageGroup ig , int index)
		{
			m_ih = ih;
			m_ig = ig;
			m_index = index;
			
		}
		
		/** 启动
		 * */
		public void start()
		{
			
			if( m_threadPage == null)
			{
				m_threadPage = new ThreadPage(Priority.PRIORITY_HIGH);
				m_threadPage.setSimulTask(4);
			}
			m_threadPage.execute( this, Priority.PRIORITY_HIGH);
						
		}
		
		@Override
		public void run() {
			android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_LOWEST);
			if( null ==m_context || m_ih == null || m_ig == null)
			{
				return;
			}
			
		    int res = ImageListener.FAIL_NO_REPEAT;
		    int i = m_ih.URI().indexOf(':');
		    
		    //获得package名
		    String packageName = "";
		    if( i >=0 )
		    	packageName = m_ih.URI().substring(i+3);
		    if( packageName.length() > 0 )
		    {
		    	List<PackageInfo> packs = null;
		    	PackageManager pm = m_context.getPackageManager();
			    //得到系统安装的所有程序包的PackageInfo对象
		    	if( null == pm )
		    	{
		    		m_ig.feedImage(res, m_ih.URI(), m_index);
		    		return; 
		    	}
		    	try
		    	{
		    		packs = pm.getInstalledPackages(0);
		    	}
		    	catch( Exception e )
		    	{
		    		e.printStackTrace();
		    		m_ig.feedImage(res, m_ih.URI(), m_index);
		    		return;
		    	}
		    	
		        for(PackageInfo pi:packs)  
		        {
		        	if( pi.applicationInfo.packageName.equalsIgnoreCase(packageName) )
		        	{
		        		//加载ICON，应该是bitmap drawable
		        		Drawable d = pi.applicationInfo.loadIcon(pm);
		        		if( !(d instanceof BitmapDrawable))
		        		{
		        			continue;
		        		}
		        		
		        		
		        		//由TBDrawable管理bitmap
		        		BitmapDrawable bd = (BitmapDrawable )d;
		        		TBDrawable td  = new TBDrawable( bd.getBitmap());	        		
						if( td != null)
						{
							boolean suc = m_ih.setDrawable(td,false);
							if( m_memCahce.add(m_ih) )
							{
								if(suc)
								{
									stat.createNum++;		
									stat.createSize += 	td.bitmapSize();
								}
							}	
							res = ImageListener.OK;	
						}
		        		break;
		        	}
		        }
		    }
		    m_ig.feedImage(res, m_ih.URI(), m_index);
		}		
	}
	/**
	 * 异步加载在Package中的Icon
	 * @param ih
	 * @param group
	 * @param index
	 */	
	void loadPackageIcon( ImageHandler ih , ImageGroup group, int index)
	{		
		IconExecutor ie = new IconExecutor(ih,group,index);
		ie.start();				
	}
	
	/**
	 * BitmapCreatorExecutor
	 * 使用BitmapCreator生成图片
	 * 
	 * */
	private class BitmapCreatorExecutor implements Runnable
	{
		private ImageHandler m_ih;
		private ImageGroup m_ig;
		private int m_index;
		
		/** 构造函数
		 *  ih : ImageHandler
		 *  ig : 所属ImageGroup
		 *  index:在ImageGroup中的位置
		 * */
		public BitmapCreatorExecutor( ImageHandler ih , ImageGroup ig , int index)
		{
			m_ih = ih;
			m_ig = ig;
			m_index = index;
			
		}
		
		/** 启动
		 * */
		public void start()
		{
			
			if( m_threadPage == null)//跟iconExecutor共用一个threadPage
			{
				m_threadPage = new ThreadPage(Priority.PRIORITY_HIGH);
				m_threadPage.setSimulTask(4);
			}
			m_threadPage.execute( this, Priority.PRIORITY_HIGH);
						
		}
		
		@Override
		public void run() {
//            Log.i("dick3","run creator executor for url " + m_ih.URI());
			android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_LOWEST);
			if( null ==m_context || m_ih == null || m_ig == null)
			{
//                Log.i("dick3","BitmapCreatorExecutor m_context is null! uri " +  m_ih.URI());
				return;
			}
			
		    int res = ImageListener.FAIL_NO_REPEAT;
		    String uri = m_ih.URI();
		    if(uri == null || uri.length()==0){
//                Log.i("dick3","BitmapCreatorExecutor uri == null || uri.length()==0 ! " + uri);
		    	m_ig.feedImage(res, m_ih.URI(), m_index);
		    	return;
		    }
            TBDrawable td = null;
            if(m_IC!=null )
            {
                td = m_IC.getDrawalbe(uri,m_ih.getCachePolicy());
            }
            if(td == null){
                BitmapCreator creator = m_ih.getBitmapCreator();
                if( creator != null ){
//                Log.i("dick3","BitmapCreatorExecutor creator.createBitmap uri " + uri);
                    Bitmap bitmap = creator.createBitmap(uri);
                    if(bitmap != null){
                        byte[] data = BitmapHelper.Bitmap2BytesPng(bitmap);
                        if(m_IC!=null )
                        {
                            m_IC.saveData(uri, data, m_ih.getCachePolicy());
                        }
                        else{
                        }
                        td  = new TBDrawable(bitmap);
                    }
                }
            }

            if( td != null)
            {
//                        Log.i("dick3","BitmapCreatorExecutor td != null uri " + uri);
                boolean suc = m_ih.setDrawable(td,false);
//                        Log.i("dick3","m_ih.setDrawable return " + suc + " uri " + uri);
                if( m_memCahce.add(m_ih) )
                {
                    if(suc)
                    {
                        stat.createNum++;
                        stat.createSize += 	td.bitmapSize();
                    }
                }
                res = ImageListener.OK;
            }
		    //生成非空TBDrawable认为成功,否则失败,FAIL_NO_REPEAT
		    m_ig.feedImage(res, m_ih.URI(), m_index);
		}		
	}
	
	/**
	 * 异步加载使用bitmapCreator生成的图片
	 * @param ih
	 * @param group
	 * @param index
	 */
	void loadImageFromCreator(ImageHandler ih , ImageGroup group, int index){
		BitmapCreatorExecutor ie = new BitmapCreatorExecutor(ih,group,index);
		ie.start();
	}
	
	/**
	 * 异步下载，通常在缓存加载失败以后调用
	 * @param URI
	 * @param group
	 */	
	 void loadImage( ImageHandler ih , ImageGroup group, int index)
	{
		
		
		
		//查看是否有相同URL的图片正在 下载中
		ImageExecutor ie = findEqualLoadingExecutor(ih.URI());
		if( ie != null )
		{
			ie.joinExecutor( ih, group ,index);
			return;
		}

		//寻找空闲可利用的Executor
		ie = findIdleExecutor();		
		try{
			if(ie == null){
				ie = new ImageExecutor(ih , group, index);
				ie.lockExecutor();
				synchronized(m_downloaders)
				{
					m_downloaders.add(ie);
				}
			}else{
				//在空闲的Executor上设置数据
				ie.lockExecutor();
				ie.setImageHandler(ih,index);
				ie.setGroup(group);
			}
			ie.start();
		}catch(Exception e){
			e.printStackTrace();
		}
		//时间
		
		//TaoLog.Logd(TaoLog.IMGPOOL_TAG, "ImagePool::loadImage() done "+(System.nanoTime() - start)/(1000*1000) +" milliseconds used");
	}
	
	
	/*
	 * 取消一个下载任务，如果下载线程超过并发上限，释放该下载线程
	 * @param URI
	 */
	public void cancelLoad(String URI)
	{
		//找到该下载任务，并停止
		 synchronized(m_downloaders)
		 {
			for(ImageExecutor ie : m_downloaders)
			{
				if( ie.m_image != null)
				{
					if(ie.m_image.URI().equals(URI)){
						ie.stop();
						
						//如果下载器个数已经超过并发限制，则释放该下载器
						if(m_downloaders.size() > m_concurrentDownloadCount){
							ie.releaseIDL();
							m_downloaders.remove(ie);
							//TaoLog.Logd(TaoLog.IMGPOOL_TAG, "ImagePool.cancelLoad() release one downloader, "+ m_downloaders.size() + " remains");
						}
						break;
					}
				}
			}
		 }
	}
	
	
	/**
	 * 调度工作所在的执行线程
	 */
	@Override
	public void run()
	{
		//降低线程优先级
		android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_LOWEST);
		//TaoLog.Logd(TaoLog.IMGPOOL_TAG, "schedule ImagePool::run() begin!");		
		do{
			while(m_b_needSchedule)
			{
				//把设置调度操作放在前面，当调度过程中，发生需要触发再次调度的动作时，不会被后置的设置冲掉
				m_b_needSchedule = false;		
				
				try
				{
					//Thread.sleep(32);
					doSchedule();				
					Thread.sleep(2);
					if(!m_b_needSchedule)
					{
						synchronized(m_scheduleThread)
						{
							//TaoLog.Logd(TaoLog.IMGPOOL_TAG, "@@@ schedule wait!");
							//call m_scheduleThread.wait ,otherwise may not wait.
							m_scheduleThread.wait();
							//TaoLog.Logd(TaoLog.IMGPOOL_TAG, "@@@ schedule wakeup!");
						}
					}	
				}
				catch(Exception e)
				{
				}
			}
		}while(!m_b_exit);
	
	//	TaoLog.Logd(TaoLog.IMGPOOL_TAG, "schedule ImagePool::run() "+ "ends, thread exit!");
				
	}
	//*************************************-下载队列-***********************************
	
	
	
	//-------------------------------------+内存缓存+-----------------------------------	
	
	//存储所有ImageHandler的弱引用，这保证了通过URL到ImagePool获得的ImageHandler在同时只有一个实例
	//初始状态ImageHandler即会在这个Map中，而只有含有BitmapDrawable的才会在Memory Cache中。
	private final ConcurrentHashMap<String, WeakReference<ImageHandler>> m_HandlerMap = new ConcurrentHashMap<String, WeakReference<ImageHandler>>(128);
	
	//ImageMemCache 内部类是图片在内存中的Cache
	private class ImageMemCache implements MemoryManagerListener //ImageMemCache类是线程安全的
	{
		
		public ImageMemCache()
		{
			MemoryManager.getInstance().addListener("ImagePool", this);
		}
		
		private static final int MEM_KEEP_SIZE = 12; //保存的最新可回收状态的个数					
		private final int MAX_CACHE_SIZE = 1024*1024*2; //2M 缺省值
		private  int m_memCacheMaxSize = MAX_CACHE_SIZE; //用作图片Cache的内存上限
	
		private final HashMap<String, ImageHandler> m_imagesHash = new HashMap<String, ImageHandler>(); //内存缓存的图片，临时保存,hashmap为了快速查找指定url的图片
		private final ArrayList<ImageHandler> m_imagesList = new ArrayList<ImageHandler>();      //列表为了快速查找要删除的图片
		
		private  ImagePoolPrinter m_printer;
		public void setDebugPrinter(ImagePoolPrinter printer) {
			m_printer = printer;
		}
		
		public ImagePoolPrinter getDebugPrinter() {
			return m_printer;
		}
		
		//内存Cache的 url应该是原始URL，在有选择策略的时候，和真正的图片对应的URL有差别
		protected boolean add(ImageHandler handler){
			if(handler == null|| handler.m_dr == null) {				
			//	TaoLog.Logw(TaoLog.IMGPOOL_TAG, "null image added!");
				return false;
			}
			String URL =  handler.URI();
			
			//检查是否达到占有内存上限
			LRUBitmapRecycle(handler.m_dr.bitmapSize());			
			String UUIDURL = UUIDUrl(URL);
			synchronized(this){
				if(m_imagesHash.containsKey(UUIDURL))//已经有了,不再增加。这里不考虑图片更新
				{
					//判断是否已经在Memory中存在
					ImageHandler node = m_imagesHash.get(UUIDURL);
					if(  null != node && node != handler)
					{
						node.printState(false, m_printer);											
					}
					return false;					
				}		
				
				
				
				//创建新的节点，放入列表的头部				
				m_imagesList.add(0, handler);
				m_imagesHash.put(UUIDURL, handler);
							
				return true;
			}
		}
		
		/*
		//加入内存Cache的 url应该是原始URL，在有选择策略的时候，和真正的图片对应的URL有差别
		protected ImageHandler get(String URL){
			
			//从hashmap里快速查找图片，如果找到了，把被访问的图片从列表中位置移到最前面
			synchronized(this){
				String uuidUrl = UUIDUrl(URL);
				ImageHandler toGet = m_imagesHash.get(uuidUrl);
				if(toGet != null){				
					{
						if(toGet.isRecyceled())
						{
							m_imagesList.remove(toGet);
							m_imagesHash.remove(uuidUrl);
							m_HandlerMap.remove(uuidUrl);
							TaoLog.Logw(TaoLog.IMGPOOL_TAG, "!!! a recycled image handler in the memory cache! url:" + uuidUrl);
							return null;
						}
						else
						{
							m_imagesList.remove(toGet);
							m_imagesList.add(0, toGet);
							TaoLog.Logd(TaoLog.IMGPOOL_TAG, "!!! ImageMemCache.get() refresh " + URL);
						}	
					}
					return toGet;
				}else return null;
			}
		}
		*/
		
		
		//尝试回收Imageandler中的Bitmap
		protected boolean remove(ImageHandler ih )
		{
			if(ih == null) return false;
			boolean res = false;
			synchronized(this){				
				if( res)
				{
					String uuidURL = UUIDUrl(ih.URI());					
					ImageHandler toGet = m_imagesHash.get(uuidURL);
					if( toGet != null )
					{
						//如果回收成功，从Memory cache中除去
						m_imagesHash.remove(uuidURL); 
						m_imagesList.remove(toGet);		
						m_HandlerMap.remove(uuidURL);
					}
				}				
			}
			return res;
		}
		
		//回收Bitmap
		protected void LRUBitmapRecycle(int newSize )
		{
			//当前内存大小超过界限值
			if( (stat.createSize - stat.destroySize ) >= m_memCacheMaxSize )
			{			
				_LRUBitmapRecycle(newSize);
			}					
		}
		
		
		
		//强制回收Bitmap
		//newSize: 需要腾出的空间，释放到新的空间可以被容纳为止
		protected void _LRUBitmapRecycle(int newSize )
		{
			int initDestroySize = stat.destroySize;
			//long start = System.nanoTime();
			synchronized(this)										
			{									
				int c = m_imagesList.size();
				int unRecyclableCount = 0;
				boolean enough4New = false;
				for( int i = c-1; i >= 0;i-- )
				{
					ImageHandler toDel = m_imagesList.get(i);
					if(toDel != null && toDel.isRecyclable()){
						if(!enough4New && toDel.tryRecycle() )
						{
							String uuidURL = UUIDUrl(toDel.URI());
							//recycled handler should be removed from memory
							m_imagesHash.remove(uuidURL);
							m_imagesList.remove(i);			
							m_HandlerMap.remove(uuidURL);
							if( stat.destroySize - initDestroySize > newSize )
							{
								enough4New = true;
							}
						}
					}else{
						if(toDel != null)
							unRecyclableCount++;
					}
				}
								
				if(  unRecyclableCount > LEAK_ALERT_THRESHOLD )
				{
					dumpMemory(true);
				}
				
				//dumpMemory(false); //TEMP debug...
				
				
			}
	    }

		//强制回收Bitmap
		//keepsize: 保存最近的几个可回收状态的ImageHandler
		protected void ForceBitmapRecycle(int keepsize )
		{			
			//long start = System.nanoTime();
			synchronized(this)										
			{									
				int c = m_imagesList.size();
				int recycleCount = 0;
				for( int i = c-1; i >= 0;i-- )
				{
					ImageHandler toDel = m_imagesList.get(i);
					if( toDel != null && toDel.tryRecycle() )
					{
						String uuidURL = UUIDUrl(toDel.URI());
						//recycled handler should be removed from memory
						m_imagesHash.remove(uuidURL);
						m_imagesList.remove(i);	
						
						//在Handler中占用了字符串内存
						m_HandlerMap.remove(uuidURL); 
						//保留最近分配的 MEM_KEEP_SIZE个处于可回收状态的Handler
						recycleCount++;												
						if( keepsize + recycleCount >= c) 
						{							
							break;
						}
					}					
				}
								
				if(  c - recycleCount > LEAK_ALERT_THRESHOLD )
				{
					dumpMemory(true);
				}
				
				//dumpMemory(false); //TEMP debug...								
			}
	    }
	
		//强制回收所有可回收状态Bitmap
	    protected void ForceBitmapRecycleAll()
		{
	    	ForceBitmapRecycle(0);
		}
	    

	    private void printerDebugInfo(String info){
	    	if(m_printer != null) {
	    		m_printer.printState(info);
	    	}
	    }
	    
	    /*打印当前ImageCache中所有的ImageHandler，用于调试内存占用状态
	     * */
	    public void dumpMemory(boolean bOnlyRef)
		{
			String temp = "start dump image in image pool memory";
			printerDebugInfo(temp);
			int totalsize = 0;
			synchronized(this)
			{
				int c = m_imagesList.size();				
				for( int i = c-1; i >= 0;i-- )
				{
					ImageHandler toDel = m_imagesList.get(i);
					if( toDel != null )
					{						
							if(!toDel.isRecyceled())
							{
								toDel.printState(bOnlyRef, m_printer);
								int size = toDel.bitmapSize();
								totalsize += size;
								
							}
							else
							{
								//toDel.printState();
							}												
					}
					
				}
				temp = "handler size in map:" + m_HandlerMap.size();
				printerDebugInfo(temp);
			}
			temp = "end dump image in image pool memory";
			printerDebugInfo(temp);
									
		}

	    //以下实现 MemoryManagerListener接口
		@Override
		public void onSetMaxMemory(int size) {
			m_memCacheMaxSize = size;
		}

		@Override
		public int onGetMemory() {
			return  stat.createSize - stat.destroySize ;
		}

		@Override
		public void onLowMemory() {
			this.ForceBitmapRecycle(MEM_KEEP_SIZE);
		}
	
	}
	
	private ImageMemCache m_memCahce = new ImageMemCache();
	//*************************************-内存缓存-***********************************
	
	/**
	 * 尝试释放ImageHandler中维护的Bitmap的Native内存
	 * @return 是否释放了native层的内存
	 */
	public boolean remove(ImageHandler ih )
	{	
		return m_memCahce.remove(ih);		
	}
	
			
	//-------------------------------------+单例模型+-----------------------------------
	private static class SingletonHolder{
		private static ImagePool instance = new ImagePool();
	}
    
	private static boolean m_b_exit; //调度线程的退出标示
	private static Thread m_scheduleThread = null; //调度线程
	private static boolean m_scheduleThreadStarted = false; //调度线程是否启动的标志
	//private static final int MAX_DOWNLOAD_COUNT = 2;//同时下载的Image个数
	
	private IImageQualityStrategy mStragery; //图片下载策略的实现
	private ImagePool()
	{
		try{
			stat = new BitmapStatics(); 			
			m_b_exit = false;			
			int count = Runtime.getRuntime().availableProcessors();
			if(count  > 2 &&  count <= 8)
				m_concurrentDownloadCount = count;	
			else if(count > 8)
				m_concurrentDownloadCount = 8;
			else 
				m_concurrentDownloadCount = 2;
			
			//m_concurrentDownloadCount = MAX_DOWNLOAD_COUNT; //最大同时下载格式			
			m_b_needSchedule = false;
			m_topGroup = null;
			m_normalGroups = new ArrayList<ImageGroup>();
			m_dormantGroups = new ArrayList<ImageGroup>();
			m_downloaders = new ArrayList<ImageExecutor>();
			
			m_scheduleThread = new Thread(this,"image_pool_thread");			
			m_scheduleThread.setPriority(Thread.MIN_PRIORITY);
			m_scheduleThreadStarted = false;
											
		}catch(Exception e){
		//	TaoLog.Logd(TaoLog.IMGPOOL_TAG, "ImagePool.ImagePool() exception" + e.getMessage());
			e.printStackTrace();
		}

	}
	
	/**
	 * 单例获取接口
	 * @return ImagePool的单例
	 */
	public static ImagePool instance()
	{
		return SingletonHolder.instance;
	}
	
	//从bs数据生成Drawable对象
	static TBDrawable _createTBDrawable(byte[] bs , String url)
    {
    	if ((bs != null) && (bs.length != 0)) {
            //TaoLog.Logd(TaoLog.IMGPOOL_TAG, "---ImageCache.getFile success " + url);        
        	TBDrawable dr = null;
        	try
        	{     		
        		
	        	//处理webp图片
        		Bitmap b = BitmapHelperFactory.Bytes2Bimap(bs,url);
        		if(b !=null)
        		{
        			//筛选字节宽高比大于MAX_COMPRESSION_RATIO的图片
        			ImagePoolPrinter printer = ImagePool.instance().getDebugPrinter();
    				if(printer != null){
    					int h=b.getHeight();
            			int w=b.getWidth();
            			float compressionRatio = 0;
            			if(h*w!=0)
        				compressionRatio=(float)bs.length/(float)(h*w);
            			float max = MAX_COMPRESSION_RATIO_JPG;
            			if(url.contains(".webp"))
            				max = MAX_COMPRESSION_RATIO_WEBP;
        				if(compressionRatio > max){
        					String[] temp = {ImagePoolPrinter.IMAGE_COMPRESSION, "图片压缩比过低(解压前字节/(长*宽))", url, "压缩率:"+compressionRatio};
        					printer.printExt(temp);
        					
        					String info =String.format("压缩比(%.2f)", compressionRatio);
        					if(w<=150)
        						info =String.format("(%.2f)", compressionRatio);
        					Bitmap grayb = BitmapHelper.toGrayscaleAndMark(ImagePool.instance().m_context,b,info);
        					if(b != null && !b.isRecycled())
        						b.recycle();
        					b = grayb;
        					
        				}
    				}
    				dr = new TBDrawable(b);
        		}
        		else
        		{
        			return null;
        		}
        			
	        	
	        	
        	}
        	catch(OutOfMemoryError e )
        	{
        		e.printStackTrace();        		
        		ImagePool.instance().dumpMemory();
        	}   
        	catch( Exception e)
        	{
        		e.printStackTrace();        		
        	}
            return dr;
        } else {
            return null;
        }    	    				
    }
	   
	/**
	 * 结束调度线程,释放文件缓存句柄
	 * 注：一旦结束，调度工作不会再进行，将无法下载图片。
	 */
//	public synchronized final void release()
//	{
//		//关闭线程
//		m_b_exit = true;
//		m_scheduleThread.interrupt();
//		
//		if( null !=m_threadPage)
//		{
//			m_threadPage.destroy();
//			m_threadPage = null;
//		}
//		//释放缓存句柄
//		if( m_IC !=null )
//			m_IC.release();
//	}
	
	/**
	 * 结束调度线程,释放文件缓存句柄
	 * 注：一旦结束，调度工作不会再进行，将无法下载图片。
	 */
//	public synchronized final void ReleaseCache()
//	{
//	
//		TaoLog.Logv(TaoLog.IMGPOOL_TAG, "!!!ImagePool ReleaseCache");
//		//释放缓存句柄
//		if( m_IC !=null )
//		{
//			m_IC.release();
//			m_IC = null;
//		}
//	}
	
	
	//************************************-图片选择策略-************************************
	/** 设置图片选择策略的实现
	 * 
	 * */
	public void setImageQualityStrategy( IImageQualityStrategy s)	
	{
		mStragery = s;
		if(m_IC!=null)
			m_IC.setImageQualityStrategy(s);
	}
	
	/** 获得当前的图片策略
	 * 
	 * */
	public IImageQualityStrategy getImageQualityStrategy()
	{
		return mStragery;
	}
		
	
	//ImageCache提供多种策略的图片本地文件cache缓存
	ImageCache m_IC;  //线程安全
	Application m_context;

	static String m_userAgent; //图片下载时使用的Agent
	static Pattern m_picPattern; //图片的Pattern，图片地址必须含有Pattern中的字符串才认为是有效图片
	
	/**
	 * 初始化，需要保证在使用功能前被调用 
	 * @param context 设置context
	 * @param userAgent 图片下载时使用的Agent
	 * @param picPattern 图片的Pattern，图片地址必须含有Pattern中的字符串才认为是有效图片
	 */
	public synchronized void Init(Application context, String userAgent , String picPattern)
	{
		m_context = context;
		m_userAgent = userAgent;
		m_picPattern = Pattern.compile(picPattern);
		if(m_IC == null)
		{
			m_IC = new ImageCache(context);
			m_IC.setImageQualityStrategy(this.mStragery);
		}
	}
	
	
	/**
	 * 从对应存储中删除图片
	 * 2013-2-6 改为public，应用需要手动删除
	 * */
	public void delImage(String URI, int cache_Type){
		
		if(m_IC!=null)
			m_IC.deleteFile(URI, cache_Type);
	}
	
	/*
	 * 根据url加载Drawable到ImageHandler中
	 * @param  ih 待加载的ImageHandler对象
	 * @param ourl 图片url
	 * @param cachePolicy 在哪种Cache中
	 * @return 是否加载成功
	 * */		
    boolean _loadDrawable( ImageHandler ih , String ourl , int cachePolicy,BitmapCreator creator)
    {
        ImageHandler handler = _getImageHandler(ourl,cachePolicy,creator);
        if( handler != null)
        {
            if( ih != handler )
            {
                if( ih.isRecyclable() )
                {
                    //useless code
                    //ih = handler;
                }
            }

            if( handler.getState() ==  ImageHandler.LOADED)
            {
                return true;
            }
        }
        return false;

    }
	
    
    /** 根据URL在内存和指定的缓存中寻找对应的ImageHandler，如果还不存在则 返回一个新创建的
	 *  这个函数需要和releaseImageHandler配对使用，调用者在使用完以后需调用releaseImageHandler归还给image pool。
	 *  否则会导致ImageHandler对象的图像内存泄露。		
	 *  @param URI 
	 *  @param  cache_Type 指定缓存种类
	 *  @return URI对应的ImageHandler，注意如果有选择策略，则返回的是选择策略下的ImageHandler，其URI和参数oURI可能不同。
	 *  注：返回的Handler是含有
	 */
	public ImageHandler getImageHandler(String URL,int cache_Type)
	{
		return getImageHandler(URL,cache_Type,null);
	}
	/** 根据URL在内存和指定的缓存中寻找对应的ImageHandler，如果还不存在则 返回一个新创建的
	 *  这个函数需要和releaseImageHandler配对使用，调用者在使用完以后需调用releaseImageHandler归还给image pool。
	 *  否则会导致ImageHandler对象的图像内存泄露。		
	 *  @param URI 
	 *  @param  cache_Type 指定缓存种类
	 *  @return URI对应的ImageHandler，注意如果有选择策略，则返回的是选择策略下的ImageHandler，其URI和参数oURI可能不同。
	 *  注：返回的Handler是含有
	 */
	public ImageHandler getImageHandler(String URL,int cache_Type,BitmapCreator creator)
	{
		ImageHandler ih = _getImageHandler(URL,cache_Type,creator);
		if( ih != null)
		{	
			if( !ih.isValideDrawable() )
			{
				return null;
			}									
		}
		return ih;
	}
	
	/** releaseImageHandler 回收ImageHandler	  getImageHandler函数需要和releaseImageHandler配对使用
	 *  @param  ih  
	 */
	public void releaseImageHandler(ImageHandler ih )
	{	
		if( ih != null)
		{			
			ih.subRef();
		}
	}
	
	
	private ImageHandler _getImageHandler(String URL,int cache_Type, BitmapCreator creator)
	{
				
		//TaoLog.Logv(TaoLog.IMGPOOL_TAG, "getImageHandler from image pool url:" + URL);
		ImageHandler res = _getImageHandlerInMemory(URL);
				
		if( res == null)
		 {
             if(creator != null){
                 res = new ImageHandler( URL, cache_Type, creator);
                 m_HandlerMap.put(UUIDUrl(URL),new  WeakReference<ImageHandler>(res));
             } else {
                 //初始状态ImageHandler，加入到Handler Map中
                 res = new ImageHandler( URL, cache_Type );
                 m_HandlerMap.put(UUIDUrl(URL),new  WeakReference<ImageHandler>(res));
             }
		 }		
							
		if( res.isRecyceled() || res.m_dr == null  )			
		{
			//尝试从cache恢复恢复
			_createTBDrawable(  res ,URL ,cache_Type);				 				
		}
		return res;				
	}
	
	public ImageHandler _createImageHandler(String URL,int cache_Type){
		return _createImageHandler(URL, cache_Type, null);
	}
	
	/** 
	 *  只是查看内存里是否有这个对象，没有的话create一个初始状态下的ImageHandler.调用该函数不增加引用计数。
	 *  这个函数时为ImageGroup的实现者，初始化一个ImageHandler用的。
	 *  一般SDK使用者不应调用该函数，
	 */
	public ImageHandler _createImageHandler(String URL,int cache_Type,BitmapCreator bc)
	{
		//检查是否在内存中，已经存在
		ImageHandler res = _getImageHandlerInMemory(URL);
		if( res == null)
		 {
			 //初始状态ImageHandler，加入到Handler Map中
			if (bc == null) {
				res = new ImageHandler(URL, cache_Type);
			} else {
				res = new ImageHandler(URL, cache_Type, bc);
			}
			m_HandlerMap.put( UUIDUrl(URL),new  WeakReference<ImageHandler>(res));					
		 }				
		return res;		
	}
	
	/** 根据URL在内存读取对应ImageHandler	 
	 *  @param URI 	 *  
	 *  @return URI对应的ImageHandler，null如果不存在
	 */
	public ImageHandler getImageHandlerInMemory(String URL)
	{
		ImageHandler res = _getImageHandlerInMemory(URL);
		if( res != null)
		{
			if( !res.isValideDrawable() )
			{
				res.setState(ImageHandler.NOT_LOADED);
				return null;
			}
			
		}
		return res;
		
	}
	
	
	//URL中HOST部分做为Key是冗余的，以倒数第一个/之后的窜作为Key
	//TODO：和CDN的格式绑定的太紧	
	private static String UUIDUrl( String url)
	{
		
		if( url == null || url.length() == 0 )
		{
			return "";
		}
		/*
		int i = url.lastIndexOf('/');
		if( i < 0 )
		{
			TaoLog.Logw(TaoLog.IMGPOOL_TAG,"bad format url:" + url);
			return "";
		}
		return new String(url.substring(i));
		*/
		return url;	
	}
	
	//只在内存中查看ImageHandler，返回null如果不存在或已经被回收
	private ImageHandler _getImageHandlerInMemory(String URL)
	{	
		//TaoLog.Logv(TaoLog.IMGPOOL_TAG, "getImageHandlerInMemory from image pool url:" + URL);
		ImageHandler res = null;
				
		//从Handler的弱引用池中查找
		String uuidUrl = UUIDUrl(URL);
		WeakReference<ImageHandler> wih = m_HandlerMap.get(uuidUrl);
		if( null == wih)
			return null;

		res = wih.get();
		if( res != null )
		{				
			if(res.isRecyceled() )
			{
				res =null;
			}
		}

		if( res == null )
		{
			m_HandlerMap.remove(uuidUrl); //ImageHandler已经释放
		}
		return res;
	}
	
	//为ImageHandler生成Drawable
	private void _createTBDrawable(ImageHandler ih, String url , int cachePolicy)
	{
		if( m_IC == null )
			return ;
	    if( ih == null )
	    	return; //在Activity切换时，ih可能为空？导致Null Pointer
		TBDrawable d = null;
		Uri uri = Uri.parse(url);	
		if(uri != null && uri.getScheme() != null && TextUtils.equals(uri.getScheme().toLowerCase(),SCHEME_TYPE_FILE)){
			d = new TBDrawable(BitmapHelper.URI2Bimap(uri.getPath()));		   
		} else if (uri != null
				&& uri.getScheme() != null
				&& TextUtils.equals(uri.getScheme().toLowerCase(),
						SCHEME_TYPE_RESOURCE)) {
			
			int index = url.lastIndexOf('\\');
			if (index != -1 && index + 1 < url.length()) {
				try {
					int id = Integer.parseInt(url.substring(index + 1));
					BitmapDrawable bd = (BitmapDrawable) m_context
							.getResources().getDrawable(id);
					d = new TBDrawable(bd.getBitmap());
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}
			}

		}
		else{
			if( mStragery!= null )
			{
				url = mStragery.decideUrl(url); 
			}		
			d = m_IC.getDrawalbe(url, cachePolicy);
		}
		
		if(d != null)
		{
				boolean res = ih.setDrawable(d,false);			
				if( m_memCahce.add(ih) )
				{
					if(res)
					{
						stat.createNum++;		
						stat.createSize += 	d.bitmapSize();
					}
				}
			
			return ;
		}
		else
			ih.setState(ImageHandler.NOT_LOADED);
		return ;
	}
		
	/** 根据URL和缓存类型，返回该图片在缓存中的全路径	 
	 *  @param URL 图片的URL
	 *  @param  cache_type 指定缓存种类
	 *  @return 缓存中的路径
	 */
	/* a utility function, get persist full path from the url
	 * return "", if not available 	 			
	 * */
	//test code 
	//String path = ImagePool.instance().URLtoPersistPath(m_ih.URI() ,cache_Type);
	public String URLtoPersistPath( String URL, int cache_type)
	{
		if(mStragery!=null)
		{
			URL = mStragery.decideUrl(URL);
		}		
		
		if(m_IC !=null)
		{
			return m_IC.URLtoPersistPath(URL, cache_type);
		}
			
		return ""; 
	}
	
	/** 建议Bitmap进行回收，先会判断是否到底内存的上限	 
	 */
	public void BitmapRecycle()
	{
		if( null != m_memCahce) 
			m_memCahce.ForceBitmapRecycle(ImageMemCache.MEM_KEEP_SIZE);
	}

	/** 强制回收内存中管理的Bitmap	 
	 */
    public void ForceBitmapRecycleAll()
    {
       if( null != m_memCahce)
			m_memCahce.ForceBitmapRecycleAll();
    }


    
	/** 清空自定类型的文件缓存	 
	 */
    public void clearCache(int policy)
    {
    	if( m_IC != null)
    		m_IC.clearCache(policy);
    }
    
    public void setDebugPrinter(ImagePoolPrinter printer){
    	 if( null != m_memCahce)
  			m_memCahce.setDebugPrinter(printer);
    }
    
    public ImagePoolPrinter getDebugPrinter(){
   	 if( null != m_memCahce)
   		 return m_memCahce.getDebugPrinter();
   	 else
   		 return null;
   }
    
    //调试用
    //打印出内存Cache中的所有对象
    public void dumpMemory()
    {
    	 if( null != m_memCahce)
 			m_memCahce.dumpMemory(false);
    }
    
	/** 设置管理图片的占用内存上限,设置是立即生效的
	 * @param size 内存上限,单位是字节
	 */
    public void setMaxMemory(int size)
    {
    	 if( null != m_memCahce)
  			m_memCahce.onSetMaxMemory(size);
    }
	
    /*
    public void dumpInGroup()
    {

    	
    	//----------
    	//dump image in group
    	TaoLog.Logi(TaoLog.IMGPOOL_TAG,"----- TOP");
    	if( m_topGroup != null )
    	{
    		m_topGroup.dumpMemory();
    	}
    	
    	TaoLog.Logi(TaoLog.IMGPOOL_TAG,"----- NORMAL");
    	for( int i = 0 ; i < m_normalGroups.size() ;i++)
    	{
    		ImageGroup g =  m_normalGroups.get(i);
    		g.dumpMemory();
    	}
    	TaoLog.Logi(TaoLog.IMGPOOL_TAG,"----- DORM");
    	for( int i = 0 ; i < m_dormantGroups.size() ;i++)
    	{
    		ImageGroup g =  m_dormantGroups.get(i);
    		g.dumpMemory();
    	}
    }
    */
    
    /**
     * @param bitmap 纳入管理的Bitmap对象
     * @param url	图片的url
     * @param cacheType 缓存类型
     * @param save  是否立刻保存到本地
     * @return Future<String> 如果需要立刻保存到本地，这个返回值是该图片的绝对路径
     */
    public Future<String> addBitmap(final Bitmap bitmap,final String url,final int cacheType,final boolean save){    	
    	ExecutorService executor = Executors.newSingleThreadExecutor();  	
		FutureTask<String> future =   
		       new FutureTask<String>(new Callable<String>() {
		         public String call() {   		         
		        	 ImageHandler handler = _createImageHandler(url, cacheType);
		         	//由TBDrawable管理bitmap
		     		TBDrawable td  = new TBDrawable( bitmap);	        		
		     		if( td != null)
		     		{
		     			boolean res = handler.setDrawable(td,false);
		     			if( m_memCahce.add(handler) )
		     			{
		     				if( res)
		     				{
		     					stat.createNum++;		
		     					stat.createSize += 	td.bitmapSize();
		     				}
		     			}	
		     		}
		     		//加入缓存
		     		if(m_IC != null && save){
		     			byte[] data = BitmapHelper.Bitmap2BytesJpeg(bitmap);
		     			m_IC.saveFile(url, cacheType, data);
		     			return URLtoPersistPath(url, cacheType);
		     		}
		     		
		     		return null;
		         }});
		
		executor.execute(future);
	
		return future;
    	
    }
}
